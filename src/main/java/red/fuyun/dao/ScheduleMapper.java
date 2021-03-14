package red.fuyun.dao;

import org.apache.ibatis.annotations.Param;
import red.fuyun.pojo.Do.ScheduleDo;

import java.sql.Time;
import java.util.List;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/4 13:20
 */
public interface ScheduleMapper {

    Integer insert(ScheduleDo scheduleDo);

    List<ScheduleDo> queryByUid(Integer uid);

    List<ScheduleDo> queryByTimeAndState(@Param("nowtime") Time nowtime,@Param("state") Integer state);

    Integer update(ScheduleDo scheduleDo);

    Integer delScheduleByUid(Integer uid);

    Integer updateScheduleReset(@Param("state") Integer state,@Param("advise")Integer advise,@Param("info") String info);

}
