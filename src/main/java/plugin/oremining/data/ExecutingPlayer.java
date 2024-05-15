package plugin.oremining.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

/**
 * ゲーム中のプレイヤーに関する情報を管理するクラスです。
 * プレイヤー名、現在のスコア、ゲーム時間、アクティブ状態、最後に採掘された鉱石の種類、連続採掘回数を保持します。
 * このクラスはゲームの進行状況を追跡し、ゲームがアクティブな間はプレイヤーのスコアとゲーム時間を更新します。
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class ExecutingPlayer {
    @EqualsAndHashCode.Include
    private String playerName;
    private int score;
    private int gameTime;
    private boolean gameActive;
    private Material lastOreType;
    private int consecutiveOreCount;

    public ExecutingPlayer(String playerName) {
        this.playerName = playerName;
        this.lastOreType = null;
        this.consecutiveOreCount = 0;
    }
}
