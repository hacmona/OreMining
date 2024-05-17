package plugin.oremining.mapper.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;

import java.time.LocalDateTime;

/**
 * プレイヤーのスコアと採掘情報を管理するクラスです。
 * このクラスのインスタンスは、データベースに存在する「player_score」テーブルと連動し、
 * プレイヤーごとの採掘スコア、最後に採掘した鉱石の種類、連続採掘数などの情報を保持します。
 * また、新しいプレイヤーのスコアを初期化する際にも使用されます。
 */
@Getter
@Setter
@NoArgsConstructor

public class PlayerScore {
    private int id;
    private String playerName;
    private int score;
    private LocalDateTime registeredAt;
    private Material lastOreType;
    private int consecutiveOreCount;

    public PlayerScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
        this.consecutiveOreCount = 0;
        this.lastOreType = null;
    }

}
