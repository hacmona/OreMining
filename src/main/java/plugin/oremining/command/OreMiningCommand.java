package plugin.oremining.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import org.bukkit.command.Command;

import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
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
 * 鉱石採掘ゲームを管理するクラスです。このゲームでは、プレイヤーは制限時間内に可能な限り多くの鉱石を採掘し、
 * 鉱石の種類ごとに異なる点数を獲得します。ゲームの終了時には、採掘した鉱石の合計点数に基づいてスコアが計算され、
 * プレイヤー名、得点、およびゲームの日時がデータベースに保存されます。
 */

public class OreMiningCommand extends BaseCommand implements Listener {

    public static final int GAME_TIME = 300;
    private final List<ExecutingPlayer> executingPlayerList = new ArrayList<>();
    public static final String LIST = "list";
    private final Main main;
    private final PlayerScoreData playerScoreData = new PlayerScoreData();


    public OreMiningCommand(Main main)  {
        this.main = main;
    }

    @Override
    public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals(LIST)) {
            sendPlayerList(player);
            return true;
        }

        if (args.length == 0 || "oreMining".equalsIgnoreCase(args[0])) {
            ExecutingPlayer nowExecutingPlayer = getPlayerScore(player);
            initStatus(player);

            int gameDuration = GAME_TIME / 60;
            player.sendTitle("鉱石採掘ゲームスタート！", "制限時間は" + gameDuration +
                    "分です。たくさん採掘しよう！", 10, 70, 20);
            gamePlay(player, nowExecutingPlayer);
            return true;
        }
        return false;
    }

    /**
     * 登録されている全プレイヤーのスコアの一覧をプレイヤーに送信します。
     * 送信される情報には、プレイヤーID、名前、スコア、記録日時が含まれます。
     * 日時は 'yyyy-MM-dd HH:mm:ss' の形式でフォーマットされます。
     *
     * @param player スコアリストを受け取るプレイヤー
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
     * ゲームのメインロジックを実行します。このメソッドは、ゲームのスタート時に呼び出され、
     *  定期的なタイマーでゲームの残り時間を更新し、終了条件をチェックします。
     *  ゲームの進行状況（残り時間）に応じてプレイヤーに通知を行い、
     *  ゲーム終了時にはスコアを記録し、プレイヤーの状態をリセットします。
     * @param player コマンドを実行したプレイヤー
     * @param nowExecutingPlayer プレイヤースコア情報
     */

    private void gamePlay(Player player, ExecutingPlayer nowExecutingPlayer) {
        nowExecutingPlayer.setGameActive(true);
        nowExecutingPlayer.setGameTime(GAME_TIME);

        Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
            int remainingTime = nowExecutingPlayer.getGameTime();
            if (nowExecutingPlayer.getGameTime() <= 0) {
                Runnable.cancel();

                nowExecutingPlayer.setGameActive(false);
                player.sendTitle("ゲーム終了！",
                        nowExecutingPlayer.getPlayerName() + " 合計"
                                + nowExecutingPlayer.getScore() + "点！お疲れ様でした！",
                        0, 60, 5);

                removePotionEffect(player);

                playerScoreData.insert(
                        new PlayerScore(nowExecutingPlayer.getPlayerName()
                                , nowExecutingPlayer.getScore()));

                nowExecutingPlayer.setScore(0);
                nowExecutingPlayer.setConsecutiveOreCount(0);

                return;
            }
            if (remainingTime == GAME_TIME / 2) {
                player.sendTitle("残り時間はあと半分！","",0,45,5);
            }

            if (remainingTime == 60) {
                player.sendTitle("残り時間はあと1分！","",0,45,5);
            }

            nowExecutingPlayer.setGameTime(nowExecutingPlayer.getGameTime() - 5);
        }, 0L, 100L);
    }

    @Override
    public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    /**
     * エンティティがダメージを受けた際のイベントを処理します。
     * このメソッドは、ゲーム中のプレイヤーが特定のダメージ源（エンティティの攻撃またはスイープ攻撃）から
     * ダメージを受けた場合に、そのダメージをキャンセルします。
     * ゲームがアクティブでない場合、またはプレイヤーが見つからない場合はダメージキャンセルは行いません。
     *
     * @param event ダメージイベント情報
     */


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            ExecutingPlayer executingPlayer = findExecutingPlayer(player.getName());

            if (executingPlayer != null && executingPlayer.isGameActive() && (
                    event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                    event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * プレイヤーの食料レベルが変更された際のイベントを処理します。
     * このメソッドは、ゲーム中のプレイヤーの食料レベルが変更された際に、その変更をキャンセルします。
     * ゲームがアクティブなプレイヤーである場合のみ変更をキャンセルし、それ以外の場合は変更を許可します。
     *
     * @param event 食料レベル変更イベント情報
     */


    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            ExecutingPlayer executingPlayer = findExecutingPlayer(player.getName());
            if (executingPlayer == null || !executingPlayer.isGameActive()) {
                return;
            }
            event.setCancelled(true);
        }
    }


    /**
     * 指定されたプレイヤー名を持つ実行中のプレイヤーを探します。
     * 実行リストからプレイヤー名に一致する最初のプレイヤーを返し、
     * 一致するプレイヤーがいない場合はnullを返します。
     *
     * @param playerName 検索するプレイヤーの名前
     * @return 対応するExecutingPlayerオブジェクトまたはnull
     */

    private ExecutingPlayer findExecutingPlayer(String playerName) {
        return executingPlayerList.stream()
                .filter(p -> p.getPlayerName().equals(playerName))
                .findFirst()
                .orElse(null);
    }

    /**
     * ブロック破壊イベントが発生したときに呼び出されるメソッドです。プレイヤーが鉱石を破壊すると、
     * その鉱石の種類に基づいてスコアが計算され、プレイヤーの現在のスコアに加算されます。
     * また、連続して同じ種類の鉱石を破壊した場合はボーナスポイントが与えられます。
     *
     * @param e ブロック破壊イベントの詳細情報を持つオブジェクト
     */

    @EventHandler
        public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        ExecutingPlayer executingPlayer = findExecutingPlayer(player.getName());
        if (executingPlayer == null || !executingPlayer.isGameActive())  {
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
                    String oreName;
                    int basePoint = switch (blockType) {
                        case COPPER_ORE, DEEPSLATE_COPPER_ORE -> { oreName = "銅鉱石"; yield 5; }
                        case COAL_ORE, DEEPSLATE_COAL_ORE -> { oreName = "石炭"; yield 10; }
                        case IRON_ORE, DEEPSLATE_IRON_ORE -> { oreName = "鉄鉱石"; yield 15; }
                        case GOLD_ORE, DEEPSLATE_GOLD_ORE -> { oreName = "金鉱石"; yield 20; }
                        case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> { oreName = "ラピスラズリ鉱石"; yield 25; }
                        case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> { oreName = "レッドストーン鉱石"; yield 30; }
                        case AMETHYST_BLOCK, AMETHYST_CLUSTER -> { oreName = "アメジスト"; yield 30; }
                        case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> { oreName = "エメラルド鉱石"; yield 50; }
                        case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> { oreName = "ダイヤモンド鉱石"; yield 500; }
                        default -> { oreName = "その他"; yield -1; }
                    };

                    if (basePoint == -1) {
                        return;
                    }

                    if (blockType == p.getLastOreType()) {
                        p.setConsecutiveOreCount(p.getConsecutiveOreCount() + 1);
                    } else {
                        p.setConsecutiveOreCount(1);
                        p.setLastOreType(blockType);
                    }

                    int bonus = 0;
                    switch (p.getConsecutiveOreCount()) {
                        case 5 -> {
                            bonus = 20;
                            player.sendMessage("ボーナスポイント！" + oreName + "を5回連続で破壊しました。+20点！");
                        }
                        case 10 -> {
                            bonus = 50;
                            player.sendMessage("大ボーナスポイント！" + oreName + "を10回連続で破壊しました。+50点！");
                        }
                        case 15 -> {
                            bonus = 100;
                            player.sendMessage("特大ボーナスポイント！" + oreName + "を15回連続で破壊しました。+100点！");
                        }
                    }

                    int totalPoints = basePoint + bonus;
                    p.setScore(p.getScore() + totalPoints);
                    player.sendMessage(oreName + "を採掘した！" + oreName + "は" + basePoint + "点！" +
                            "現在のスコアは" + p.getScore() + "点です！");
                });
    }

    /**
     * 指定されたプレイヤー名に基づいて実行中のプレイヤーを取得します。
     * プレイヤーリストに既存のプレイヤーが存在する場合はそのプレイヤーを返し、
     * 存在しない場合は新しいプレイヤーを作成してリストに追加後、そのプレイヤーを返します。
     * 新規プレイヤーは指定されたゲーム時間とスコア0で初期化されます。
     *
     * @param player コマンドを実行したプレイヤー
     * @return 対応するExecutingPlayerオブジェクト
     */

    private ExecutingPlayer getPlayerScore(Player player) {
        return executingPlayerList.stream()
                .filter(p -> p.getPlayerName().equals(player.getName()))
                .findFirst()
                .orElseGet(() -> {
                    ExecutingPlayer newPlayer = new ExecutingPlayer(player.getName());
                    newPlayer.setGameTime(GAME_TIME);
                    newPlayer.setScore(0);
                    executingPlayerList.add(newPlayer);
                    return newPlayer;
                });
    }


    /**
     * ゲーム開始前にプレイヤーの初期状態を設定します。
     * このメソッドでは、プレイヤーの体力と空腹度を最大値に設定し、
     * メインハンドにネザライト製のツルハシを、オフハンドに松明を64個装備させます。
     *
     * @param player ゲームに参加するプレイヤー
     */

    private static void initStatus(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);

        PlayerInventory inventory = player.getInventory();
        inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_PICKAXE));
        inventory.setItemInOffHand(new ItemStack(Material.TORCH,64));
    }

    /**
     * 指定されたプレイヤーからすべてのポーション効果を削除します。
     * これはゲームの開始時または終了時にプレイヤーの状態をリセットするために使用されます。
     *
     * @param player ポーション効果を削除するプレイヤー
     */

    private static void removePotionEffect(Player player) {
        player.getActivePotionEffects()
                .stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);
    }

}


