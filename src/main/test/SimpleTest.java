import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import red.fuyun.dao.TempletMapper;
import red.fuyun.dao.UserInfoMapper;
import red.fuyun.pojo.Do.TempletDo;
import red.fuyun.pojo.Do.UserInfoDo;
import red.fuyun.campusphere.Collector;
import red.fuyun.campusphere.LoginCat;
import red.fuyun.util.BaseHolder;
import red.fuyun.util.ParticipleUtils;

import java.io.IOException;
import java.util.Vector;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/13 18:16
 */

//@WebAppConfiguration
//@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
//@RunWith(SpringJUnit4ClassRunner.class)
public class SimpleTest {
    @Test
    public void test(){
        String content = "36.7°C及以下";
        String content1 = "36.7℃以下";
        double similarity = ParticipleUtils.findSimilarity(content, content1);
        System.out.println(similarity);
        Vector<String> str1 = new Vector<>();
        str1.add(content);

        Vector<String> str2 = new Vector<>();
        str2.add(content1);
        try {
            double bfb = ParticipleUtils.getSimilarity(str1, str2);
            System.out.println(bfb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        SqlSessionFactory sqlSessionFactory = BaseHolder.getApplicationContext().getBean(SqlSessionFactory.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
        TempletMapper templetMapper = sqlSession.getMapper(TempletMapper.class);

        UserInfoDo userInfoDo = userInfoMapper.queryById(35);
        LoginCat loginCat = new LoginCat(userInfoDo.getOrigin(),userInfoDo.getUa(),userInfoDo.getCookie());
        String login = loginCat.login();
        Collector collector = new Collector(login,userInfoDo.getUa(),userInfoDo.getOrigin());

        TempletDo templetDo = templetMapper.queryTempletById(54);
        collector.collect(templetDo);


    }



    @Test
    public void test3() throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        SqlSessionFactory sqlSessionFactory = BaseHolder.getApplicationContext().getBean(SqlSessionFactory.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserInfoMapper userInfoMapper = sqlSession.getMapper(UserInfoMapper.class);
        TempletMapper templetMapper = sqlSession.getMapper(TempletMapper.class);
        UserInfoDo userInfoDo = userInfoMapper.queryById(39);
        TempletDo templetDo = templetMapper.queryTempletById(54);


    }

}
