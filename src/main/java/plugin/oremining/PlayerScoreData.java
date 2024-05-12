package plugin.oremining;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import plugin.oremining.mapper.PlayerScoreMapper;
import plugin.oremining.mapper.data.PlayerScore;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * DB接続やそれに不随する登録や更新処理を行うクラスです。
 */
public class PlayerScoreData {

    private final SqlSessionFactory sqlSessionFactory;
    private final PlayerScoreMapper mapper;

    public PlayerScoreData(){
        try {
            InputStream inputStream= Resources.getResourceAsStream("mybatis-config.xml");
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession session = sqlSessionFactory.openSession(true);
            this.mapper = session.getMapper(PlayerScoreMapper.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * プレイヤースコアテーブルから一覧でスコア情報を取得する。
     *
     * @return スコア引数
     */
    public List<PlayerScore> selectList() {
        return mapper.selectList();
    }

    /**
     * プレイヤースコアテーブルにスコア情報を登録する。
     * @param playerScore プレイヤースコア
     */
    public void insert(PlayerScore playerScore){
        mapper.insert(playerScore);
    }
}
