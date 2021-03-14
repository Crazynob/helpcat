package red.fuyun.campusphere.impl;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;


import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.springframework.stereotype.Component;
import red.fuyun.dao.ScheduleMapper;
import red.fuyun.dao.TempletMapper;
import red.fuyun.dao.UserInfoMapper;
import red.fuyun.pojo.Do.ScheduleDo;
import red.fuyun.pojo.Do.TempletDo;
import red.fuyun.pojo.Do.UserInfoDo;
import red.fuyun.util.BaseHolder;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;





/**
 * @author Crazynob
 * @Title: 签到任务队列
 * @Package
 * @Description: 从数据库读取符合条件的用户加入任务队列
 * @date 2020/9/20 13:42
 */

@Component
@EnableScheduling
public class WaitSigneQueue{

//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(cron = "5/20 * * * * ? ")
    public void executeInternal(){
        SqlSessionFactory sqlSessionFactory =  BaseHolder.getApplicationContext().getBean(SqlSessionFactory.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        ScheduleMapper scheduleMapper = sqlSession.getMapper(ScheduleMapper.class);
        ScheduleDo scheduleDo = new ScheduleDo();
        ScheduleDo currentSchedule = null;
        try {
            System.out.println("WaitSigneQueue........................");
            //获取线程池
            ThreadPoolTaskExecutor threadPoolExecutor = BaseHolder.getApplicationContext().getBean(ThreadPoolTaskExecutor.class);

            List<ScheduleDo> schedules = scheduleMapper.queryByTimeAndState(new Time(System.currentTimeMillis()),0);

            if (schedules.size()<=0){
                sqlSession.close();
                System.out.println("没有任务,当前时间:"+ LocalDateTime.now().toString());
                return;
            }
            TempletMapper templetMapper = sqlSession.getMapper(TempletMapper.class);
            UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
            HashMap<Integer,TempletDo> templetCache = new HashMap<>();
            HashMap<Integer,UserInfoDo> userInfoCache = new HashMap<>();

            for (ScheduleDo schedule : schedules) {
                currentSchedule =schedule;
                Integer tid = schedule.getTid();
                Integer uid = schedule.getUid();
                TempletDo templetDo = templetCache.get(tid);
                UserInfoDo userInfoDo = userInfoCache.get(uid);
                if (templetDo == null) {
                    templetDo = templetMapper.queryTempletById(tid);
                    templetCache.put(tid, templetDo);
                }

                if (userInfoDo == null) {
                    userInfoDo = userInfoMapper.queryById(uid);
                    userInfoCache.put(uid, userInfoDo);
                }


                scheduleDo.setId(schedule.getId());
                scheduleDo.setState(1);
                scheduleMapper.update(scheduleDo);

                ExecuteTask executeTask =  BaseHolder.getApplicationContext().getBean(ExecuteTask.class,schedule, userInfoDo, templetDo);
//                ExecuteTask executeTask = new ExecuteTask(schedule, userInfoDo, templetDo);
                threadPoolExecutor.execute(executeTask);
            }

        }catch (Exception e){

            try {
                scheduleDo.setId(currentSchedule.getId());
                scheduleDo.setState(3);
                scheduleDo.setInfo("获取定时任务出异常了"+e.getMessage());
                scheduleMapper.update(scheduleDo);
            }catch (Exception ex){
                System.out.println("这会出错,一个currentSchedule为null or setInfo信息太长超过数据库字段长度限制");
            }

            e.printStackTrace();
            System.out.println("获取定时任务出异常了");
        }finally {
            sqlSession.close();
        }

    }
}
