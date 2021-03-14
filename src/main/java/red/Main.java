package red;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import red.fuyun.ProxyServer.InterceptFullHttpProxyServer;

import java.util.Scanner;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/8 21:10
 */

public class Main {
    public static void main(String[] args) throws InterruptedException {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InterceptFullHttpProxyServer.start();
            }
        });
        System.out.println("帮助猫启动成功");
        thread.start();


//        Scanner scanner = new Scanner(System.in);
//        while(scanner.hasNext()){
//            String s = scanner.nextLine();
//        }


    }
}
