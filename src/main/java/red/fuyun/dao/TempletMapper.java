package red.fuyun.dao;

import org.apache.ibatis.annotations.Param;
import red.fuyun.pojo.Do.TempletDo;
import java.util.List;

import java.awt.*;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/4 13:20
 */
public interface TempletMapper {

    Integer insert(TempletDo templetDo);

    Integer update(TempletDo templetDo);

    TempletDo queryTempletById(Integer id);

    List<TempletDo> queryTempletByCpdailyInfo(String cpdailyInfo);
    List<TempletDo> queryTempletByUserId(String userid);

    Integer delTempletByUid(Integer uid);

    Integer updateCpdailyInfoById(@Param("id") Integer id, @Param("cpdailyInfo") String cpdailyInfo,
                                  @Param("lon") String lon,@Param("lat") String lat,@Param("address") String address);

}
