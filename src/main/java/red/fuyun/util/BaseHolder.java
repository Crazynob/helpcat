package red.fuyun.util;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import red.fuyun.pojo.Do.ScheduleDo;
import red.fuyun.pojo.Do.TempletDo;
import red.fuyun.pojo.Do.UserInfoDo;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description: 用于获取Application实例
 * @date 2020/9/26 7:56
 */


@Component
public class BaseHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 服务器启动，Spring容器初始化时，当加载了当前类为bean组件后，
     * 将会调用下面方法注入ApplicationContext实例
     */
    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        System.out.println("初始化了");
        BaseHolder.applicationContext = arg0;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

}