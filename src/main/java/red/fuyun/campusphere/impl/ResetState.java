package red.fuyun.campusphere.impl;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import red.fuyun.dao.ScheduleMapper;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description: 重置任务状态的定时任务类
 * @date 2020/10/16 21:52
 */
@Component
@EnableScheduling
public class ResetState{

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Scheduled(cron = "0 0 0 1/1 * ?")
    public void executeInternal(){
        System.out.println("ResetStateResetStateResetState");
//        SqlSessionFactory sqlSessionFactory = BaseHolder.getBean(SqlSessionFactory.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        ScheduleMapper scheduleMapper = sqlSession.getMapper(ScheduleMapper.class);
        try{
            Integer integer = scheduleMapper.updateScheduleReset(0,0,"");
        }catch (Exception e){

        }finally {
            sqlSession.close();
        }

    }
}
