package red.fuyun.dao;

import red.fuyun.pojo.Do.UserInfoDo;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/4 13:20
 */
public interface UserInfoMapper {

    Integer insert(UserInfoDo userInfoDo);

    Integer update(UserInfoDo userInfoDo);

    UserInfoDo queryById(Integer id);

    UserInfoDo queryByStudentNo(String studentNo);

}
