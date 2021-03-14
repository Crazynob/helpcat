package red.fuyun.log;

import okhttp3.logging.HttpLoggingInterceptor;


/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/7 13:29
 */
public class HttpLogger implements HttpLoggingInterceptor.Logger {
    @Override
    public void log(String message) {
        System.out.println("HttpLogInfo:"+message);
    }
}