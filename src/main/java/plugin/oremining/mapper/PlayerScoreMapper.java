package plugin.oremining.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import plugin.oremining.mapper.data.PlayerScore;

import java.util.List;

public interface PlayerScoreMapper {

    @Select("select * from ore_player_score")
    List<PlayerScore> selectList();

    @Insert("insert into ore_player_score (player_name, score, registered_at) values (#{playerName}, #{score}, now());")
    int insert(PlayerScore playerScore);

}
