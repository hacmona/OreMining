package plugin.oremining.mapper.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;

import java.time.LocalDateTime;

/**
 * プレイヤーのスコア情報を扱うオブジェクト。
 * DBに存在するテーブルと連動する。
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

    public PlayerScore(String playerName, int score){
        this.playerName = playerName;
        this.score = score;
        this.consecutiveOreCount = 0;
        this.lastOreType = null;
    }

}
