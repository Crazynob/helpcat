package red.fuyun.campusphere.impl;

import lombok.NoArgsConstructor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import red.fuyun.dao.ScheduleMapper;
import red.fuyun.campusphere.Attendance;
import red.fuyun.campusphere.Collector;
import red.fuyun.campusphere.LoginCat;
import red.fuyun.campusphere.Sign;
import red.fuyun.pojo.Do.ScheduleDo;
import red.fuyun.pojo.Do.TempletDo;
import red.fuyun.pojo.Do.UserInfoDo;
import red.fuyun.util.BaseHolder;
import red.fuyun.util.Result;
import red.fuyun.util.ResultCode;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/10/6 14:18
 */
@Component("executeTask")
@Scope("prototype")
@NoArgsConstructor
public class ExecuteTask implements Runnable {
//    private SqlSessionFactory sqlSessionFactory = BaseHolder.getBean(SqlSessionFactory.class);
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    private ScheduleDo schedule;
    private UserInfoDo userInfoDo;
    private TempletDo templetDo;

    public ExecuteTask(ScheduleDo schedule, UserInfoDo userInfoDo, TempletDo templetDo) {
        this.schedule =schedule;
        this.userInfoDo = userInfoDo;
        this.templetDo = templetDo;
    }

    @Override
    public void run() {
        Integer taskType = schedule.getTaskType();
        String origin = userInfoDo.getOrigin();
        String ua = userInfoDo.getUa();
        String cookie = userInfoDo.getCookie();
        LoginCat loginCat = new LoginCat(origin, ua, cookie);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        ScheduleMapper scheduleMapper = sqlSession.getMapper(ScheduleMapper.class);
        ScheduleDo scheduleDo = new ScheduleDo();
        try {
            String modAuthCas = loginCat.login();
            if (taskType.equals(1)){
//                    Sign sign = new Sign(modAuthCas,ua,origin);
                    Sign sign =  BaseHolder.getApplicationContext().getBean(Sign.class,modAuthCas,ua,origin);
                    Result siginResult = sign.sigin(templetDo);
                    String code = siginResult.getCode();
                    scheduleDo.setId(schedule.getId());
                    if (code.equals(ResultCode.SUCCESS.getCode())){
                        scheduleDo.setState(2);
                        scheduleMapper.update(scheduleDo);
                    }
                    if (code.equals(ResultCode.FAIL.getCode())){
                        scheduleDo.setState(3);
                        scheduleDo.setInfo(siginResult.getMsg());
                        scheduleMapper.update(scheduleDo);
                    }
            }

            if (taskType.equals(2)){
//                Collector collector = new Collector(modAuthCas,ua,origin);
                Collector collector = BaseHolder.getApplicationContext().getBean(Collector.class,modAuthCas,ua,origin);
                Result collect = collector.collect(templetDo);
                String code = collect.getCode();
                scheduleDo.setId(schedule.getId());
                if (code.equals(ResultCode.SUCCESS.getCode())){
                    scheduleDo.setState(2);
                    scheduleMapper.update(scheduleDo);
                }
                if (code.equals(ResultCode.FAIL.getCode())){
                    scheduleDo.setState(3);
                    scheduleDo.setInfo(collect.getMsg());
                    scheduleMapper.update(scheduleDo);
                }
            }
            if (taskType.equals(3)){
//                Attendance attendance = new Attendance(modAuthCas,ua,origin);
                Attendance attendance = BaseHolder.getApplicationContext().getBean(Attendance.class,modAuthCas,ua,origin);
                Result sign = attendance.sign(templetDo);
                String code = sign.getCode();
                scheduleDo.setId(schedule.getId());
                if (code.equals(ResultCode.SUCCESS.getCode())){
                    scheduleDo.setState(2);
                    scheduleMapper.update(scheduleDo);
                }
                if (code.equals(ResultCode.FAIL.getCode())){
                    scheduleDo.setState(3);
                    scheduleDo.setInfo(sign.getMsg());
                    scheduleMapper.update(scheduleDo);
                }

            }
        } catch (Exception e) {
            scheduleDo.setId(schedule.getId());
            scheduleDo.setState(3);
            scheduleDo.setInfo(e.getMessage());
            scheduleMapper.update(scheduleDo);
            e.printStackTrace();

        }finally {
            sqlSession.close();
        }

    }

}
