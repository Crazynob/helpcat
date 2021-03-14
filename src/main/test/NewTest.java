import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import red.fuyun.dao.TempletMapper;
import red.fuyun.dao.UserInfoMapper;

import red.fuyun.campusphere.LoginCat;
import red.fuyun.campusphere.Sign;
import red.fuyun.pojo.Do.TempletDo;
import red.fuyun.pojo.Do.UserInfoDo;
import red.fuyun.campusphere.UploadImage;
import red.fuyun.campusphere.impl.WaitSigneQueue;
import red.fuyun.util.BaseHolder;
import red.fuyun.util.Util;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/2 18:17
 */


//@WebAppConfiguration
//@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
//@RunWith(SpringJUnit4ClassRunner.class)
public class NewTest {


    @Test
    public void test1() throws Exception {

        String origin = "https://whit.campusphere.net";
        String ua = "Mozilla/5.0 (Linux; Android 11; Mi 10 Build/RKQ1.200826.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/86.0.4240.185 Mobile Safari/537.36  cpdaily/8.2.20 wisedu/8.2.20";
        String outhCookie = "acw_tc=2f624a0216154587250823015e3b5a64160b35105564e3ef7e418a532cac0d; clientType=cpdaily_student; tenantId=whit; sessionToken=8e68c712-6625-4b14-98f2-f325581f56a8";

        LoginCat loginCat = new LoginCat(origin,ua,outhCookie);
        String cookie = loginCat.login();
        UploadImage uploadImage = new UploadImage(cookie,ua,origin);

        JSONObject uploadPolicy = uploadImage.getUploadPolicy();
        JSONObject datas = uploadPolicy.getJSONObject("datas");
        String filePath = "C:\\Users\\Administrator\\Desktop\\bg.jpg";
        Util.uploadImage(datas,filePath);
        String fileName = datas.getString("fileName")+".png";
        JSONObject jsonObject = uploadImage.previewAttachment(fileName);
        System.out.println(jsonObject);

    }
    @Test
    public void test3() throws InterruptedException {
        WaitSigneQueue waitSigneQueue = new WaitSigneQueue();
        waitSigneQueue.executeInternal();
        Thread.sleep(100000000);
    }
    @Test
    public void test2() throws Exception {
        SqlSessionFactory sqlSessionFactory = BaseHolder.getApplicationContext().getBean(SqlSessionFactory.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
        TempletMapper templetMapper = sqlSession.getMapper(TempletMapper.class);
        UserInfoDo userInfoDo = userInfoMapper.queryById(5);
        String origin = userInfoDo.getOrigin();
        String cookie = userInfoDo.getCookie();
        String ua = userInfoDo.getUa();
        LoginCat loginCat = new LoginCat(origin, ua, cookie);
        String modCas = loginCat.login();
        Sign sign = new Sign(modCas, ua, origin);
        TempletDo templetDo = templetMapper.queryTempletById(1);
        sign.sigin(templetDo);
        JSONObject stuSignInfosByWeekMonth = sign.getStuSignInfosByWeekMonth();
        JSONObject datas = stuSignInfosByWeekMonth.getJSONObject("datas");
        JSONArray rows = datas.getJSONArray("rows");
//        AutoGenTempletAndTask autoGenTempletAndTask = new AutoGenTempletAndTask(sign,userInfoDo);
//        autoGenTempletAndTask.findMeetConditionsignedTasks(rows);
    }

    @Test
    public void test4(){
        long lon =123;
        float f = 2.3f;
        
        System.out.println(lon);
    }
}
