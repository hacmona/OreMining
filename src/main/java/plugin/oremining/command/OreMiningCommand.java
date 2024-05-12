package plugin.oremining.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import org.bukkit.command.Command;

import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import plugin.oremining.Main;
import plugin.oremining.PlayerScoreData;
import plugin.oremining.data.ExecutingPlayer;
import plugin.oremining.mapper.data.PlayerScore;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;



/**
 * 制限時間内に鉱石を採掘し、スコアを獲得するゲームを起動するコマンドです。
 * スコアは鉱石の種類によって変わり、採掘できた鉱石の合計によってスコアが変動します
 * 結果はプレイヤー名、点数、日時などで保存されます。
 */

public class OreMiningCommand extends BaseCommand implements Listener {

    public static final int GAME_TIME = 30;
    private final List<ExecutingPlayer> executingPlayerList = new ArrayList<>();
    public static final String LIST = "list";
    private final Main main;
    private final PlayerScoreData playerScoreData = new PlayerScoreData();


    public OreMiningCommand(Main main)  {
        this.main = main;
    }

    @Override
    public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
        if(args.length == 1 && (LIST.equals(args[0]))){
            sendPlayerList(player);
            return false;
        }

        ExecutingPlayer nowExecutingPlayer = getPlayerScore(player);

        InitStatus(player);

        gamePlay(player, nowExecutingPlayer);

        return true;
    }

    /**
     * 現在登録されているスコアの一覧をメッセージで送る。
     *
     * @param player プレイヤー
     */

    private void sendPlayerList(Player player) {
        List<PlayerScore> playerScoreList = playerScoreData.selectList();
        for(PlayerScore playerScore:playerScoreList){
            player.sendMessage(playerScore.getId() + " | "
                    + playerScore.getPlayerName()+ " | "
                    + playerScore.getScore() + " | "
                    + playerScore.getRegisteredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }

    /**
     * ゲームを実行します。規定の時間内に鉱石を採掘すると、スコアが加算されます。合計スコアを時間経過後に表示します。
     *
     * @param player コマンドを実行したプレイヤー
     * @param nowExecutingPlayer プレイヤースコア情報
     */

    private void gamePlay(Player player, ExecutingPlayer nowExecutingPlayer) {
        nowExecutingPlayer.setGameActive(true);
        Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
            if (nowExecutingPlayer.getGameTime() <= 0) {
                Runnable.cancel();

                nowExecutingPlayer.setGameActive(false);
                player.sendTitle("ゲーム終了！",
                        nowExecutingPlayer.getPlayerName() + " 合計" + nowExecutingPlayer.getScore() + "点！お疲れ様でした！",
                        0, 60, 5);

                removePotionEffect(player);

                playerScoreData.insert(
                        new PlayerScore(nowExecutingPlayer.getPlayerName()
                                , nowExecutingPlayer.getScore()));

                return;
            }
            nowExecutingPlayer.setGameTime(nowExecutingPlayer.getGameTime() - 5);
        }, 0, 5 * 30);
    }

    @Override
    public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }


    @EventHandler
        public void onBlockBreak(BlockBreakEvent e) {
            // ここにブロックが壊された時の処理を追加
            Player player = e.getPlayer();
        if (executingPlayerList.isEmpty()) {
            return;
        }

        executingPlayerList.stream()
                .filter(p -> p.getPlayerName().equals(player.getName()))
                .findFirst()
                .ifPresent(p -> {
                    if (!p.isGameActive()) {
                        return;
                    }
                    Material blockType = e.getBlock().getType();
                    int point = switch (blockType) {
                        case COPPER_ORE -> 5;
                        case COAL_ORE -> 10;
                        case IRON_ORE -> 15;
                        case GOLD_ORE -> 20;
                        case REDSTONE_ORE -> 50;
                        case EMERALD_ORE -> 80;
                        case DIAMOND_ORE -> 200;
                        default -> -1;
                    };

                    if (point != -1) {
                        p.setScore(p.getScore() + point);
                        player.sendMessage("ブロックを壊した！現在のスコアは" + p.getScore() + "点です！");
                    }
                });
    }

    /**
     * 現在実行しているプレイヤーのスコア情報を取得する。
     *
     * @param player コマンドを実行したプレイヤー
     * @return 現在実行しているプレイヤーのスコア情報
     */


    private ExecutingPlayer getPlayerScore(Player player) {
        ExecutingPlayer executingPlayer = new ExecutingPlayer(player.getName());

        if (executingPlayerList.isEmpty()) {
            executingPlayer = addNewPlayer(player);
        } else {
            executingPlayer = executingPlayerList.stream()
                    .findFirst()
                    .map(ps -> ps.getPlayerName().equals(player.getName())
                    ? ps
                    : addNewPlayer(player)).orElse(executingPlayer);
        }

        executingPlayer.setGameTime(GAME_TIME);
        executingPlayer.setScore(0);
        removePotionEffect(player);
        return executingPlayer;
    }



    /**
     * 新規のプレイヤー情報をリストに追加します。
     *
     * @param player コマンドを実行したプレイヤー
     * @return 新規プレイヤー
     */

    private ExecutingPlayer addNewPlayer(Player player) {
        ExecutingPlayer newPlayer = new ExecutingPlayer(player.getName());
        executingPlayerList.add(newPlayer);
        return newPlayer;
    }

    /**
     * ゲームを始める前にプレイヤーの状態を設定する。
     * 体力と空腹度を最大にして、メインハンドにネザライトのツルハシ、オフハンドに松明を64個持たせる。
     *
     * @param player　コマンドを実行したプレイヤー
     */

    private static void InitStatus(Player player) {
        //プレイヤーの状態を初期化する
        player.setHealth(20);
        player.setFoodLevel(20);

        PlayerInventory inventory = player.getInventory();
        inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_PICKAXE));
        inventory.setItemInOffHand(new ItemStack(Material.TORCH,64));
    }

    /**
     * プレイヤーに設定されている特殊効果を除外します。
     *
     * @param player コマンドを実行したプレイヤー
     */


    private static void removePotionEffect(Player player) {
        player.getActivePotionEffects()
                .stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);
    }

}


