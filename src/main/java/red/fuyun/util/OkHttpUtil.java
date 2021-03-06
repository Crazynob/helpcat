package red.fuyun.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import red.fuyun.log.HttpLogger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/1 20:35
 */
public class OkHttpUtil {
    private static  OkHttpClient okHttpClient;
    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLogger());

    static {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5,TimeUnit.SECONDS)
                .writeTimeout(5,TimeUnit.SECONDS)
                .addNetworkInterceptor(loggingInterceptor)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                })
                .followRedirects(false)
                .build();
    }

    public static OkHttpClient getClient(){
        return okHttpClient;
    }



    public static Response get(String url, Headers headers, OkHttpClient client) throws IOException {
        Request request = new Request.Builder()
                .headers(headers)
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        response.close();
        return response;
    }
    public static Response post(String url, Headers headers,JSONObject jsonObject,OkHttpClient client) throws IOException {
        String body = jsonObject.toString();
        RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .headers(headers)
                .url(url)
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    public static boolean CheckRequestSuccess(JSONObject json,String msgSufix){
        if (json == null){
            System.out.println(msgSufix+":???null");
            return false;
        }

        boolean message = json.getString("message").equals("SUCCESS");
        boolean code = json.getInteger("code").equals(0);

        if (message && code){
            return true;
        }
        System.out.println(msgSufix+":"+json.getString("message"));
        return false;
    }


    /**
     * ??????????????????????????? ???????????? ????????????????????????
     * @param unSignedTasks ???????????????????????????
     * @return ???????????????null
     */
    public static JSONObject getunSignedTask(JSONArray unSignedTasks){
        for (int i = 0; i <unSignedTasks.size() ; i++) {
            JSONObject unSignedTask = unSignedTasks.getJSONObject(i);
            boolean meetConditions = checkunSignedTask(unSignedTask);
            if (meetConditions){
                return  unSignedTask;
            }
        }
        return null;
    }


    /**
     * ?????????????????????????????????????????????
     * @param unSignedTask ???????????????????????????
     * @return
     */
    private static boolean checkunSignedTask(JSONObject unSignedTask){
        Integer signStatus = unSignedTask.getInteger("signStatus");
        Integer isLeave = unSignedTask.getInteger("isLeave");
        
        //??????????????????????????????
        boolean unSign = signStatus.equals(2);
        //???????????????  1????????? 2????????? 5???????????????
        boolean leaveSign = signStatus.equals(5);

        if (!unSign && !leaveSign){
            return false;
        }
        String rateTaskBeginTime = unSignedTask.getString("rateTaskBeginTime");
        String rateTaskEndTime = unSignedTask.getString("rateTaskEndTime");
        String rateSignDate = unSignedTask.getString("rateSignDate");

        if(Objects.isNull(rateTaskBeginTime) || Objects.isNull(rateTaskEndTime) || Objects.isNull(rateSignDate)){
            return false;
        }
        String date = rateSignDate.substring(0, 10);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate signDate = LocalDate.parse(date, dateTimeFormatter);
        //?????????????????????????????? ?????????????????? ???????????????
        boolean equal = signDate.isEqual(LocalDate.now());
        if (!equal){
           return false;
        }
        LocalTime beginTime = LocalTime.parse(rateTaskBeginTime);
        LocalTime endTime = LocalTime.parse(rateTaskEndTime);
        boolean after = LocalTime.now().isAfter(beginTime);
        boolean before = LocalTime.now().isBefore(endTime);
        return after && before;

    }





}
