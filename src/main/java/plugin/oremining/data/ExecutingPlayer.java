package plugin.oremining.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

/**
 * OreMiningのゲームを実行する際のスコア情報を扱うオブジェクト。
 * ゲーム中のプレイヤーのスコアとゲーム時間を管理し、ゲームがアクティブな間はスコアが更新される
 * プレイヤー名、合計点数、日時などの情報を持つ。
 */

@Getter
@Setter

public class ExecutingPlayer {

    private String playerName;
    private int score;
    private int gameTime;
    private boolean gameActive;
    private Material lastOreType;
    private int consecutiveOreCount;

    public ExecutingPlayer(String playerName){

        this.playerName = playerName;
        this.lastOreType = null;
        this.consecutiveOreCount = 0;
    }


}
