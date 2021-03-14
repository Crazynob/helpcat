package red.fuyun.campusphere;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import red.fuyun.pojo.Do.TempletDo;
import red.fuyun.util.OkHttpUtil;
import red.fuyun.util.Result;
import red.fuyun.util.ResultCode;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
public class Attendance {


    @Value("${attendance.getSignStatisticsInfoByMonth}")
    private String getSignStatisticsInfoByMonth;

    @Value("${attendance.detailSignInstance}")
    private String detailSignInstance;

    @Value("${attendance.getStuAttendacesInOneDay}")
    private String getStuAttendacesInOneDay;

    @Value("${attendance.submitSign}")
    private String submitSign;


    private  String ua;
    private OkHttpClient client = OkHttpUtil.getClient();
    private String cookie;
    private String origin ;
    private Headers headers;

    public Attendance(String cookie,String ua,String origin){
        this.cookie = cookie;
        this.ua = ua;
        this.origin = origin;
        this.headers = new Headers.Builder()
                .add("user-agent",this.ua)
                .add("cookie",this.cookie)
                .build();
    }


    public Result sign(TempletDo templet) throws IOException {
        JSONObject stuAttendacesInOneDay = getStuAttendacesInOneDay();

//        stuAttendacesInOneDay
        boolean getStuAttendacesInOneDay = OkHttpUtil.CheckRequestSuccess(stuAttendacesInOneDay, "getStuAttendacesInOneDay");
        if (!getStuAttendacesInOneDay){
            String message = stuAttendacesInOneDay.getString("message");
            return Result.of(ResultCode.FAIL,message);
        }
        JSONObject data = stuAttendacesInOneDay.getJSONObject("datas");
        JSONArray unSignedTasks = data.getJSONArray("unSignedTasks");
        if (unSignedTasks.size()<=0){
            return Result.of(ResultCode.FAIL,"无任务");
        }

        JSONObject submitBody = new JSONObject();
        submitBody.put("longitude",templet.getLongitude());
        submitBody.put("latitude",templet.getLatitude());
        submitBody.put("isMalposition",0);
        submitBody.put("abnormalReason","");
        submitBody.put("signPhotoUrl",templet.getAttendancePhotoUrl());
        submitBody.put("position",templet.getSignAddress());
        submitBody.put("uaIsCpadaily","True");
        LocalTime now = LocalTime.now();
        for (int i = 0; i < unSignedTasks.size(); i++) {
            JSONObject unSignedTask = unSignedTasks.getJSONObject(i);
            Integer signStatus = unSignedTask.getInteger("signStatus");
            if (!signStatus.equals(0)){
                continue;
            }
            String rateTaskBeginTime = unSignedTask.getString("rateTaskBeginTime");
            String rateTaskEndTime = unSignedTask.getString("rateTaskEndTime");
            LocalTime begin = LocalTime.parse(rateTaskBeginTime);
            LocalTime end = LocalTime.parse(rateTaskEndTime);
            boolean isBetween = now.isAfter(begin) && now.isBefore(end);
            if (!isBetween){
                continue;
            }
            String signInstanceWid = unSignedTask.getString("signInstanceWid");
            submitBody.put("signInstanceWid",signInstanceWid);
            JSONObject sumitResult= submitSign(submitBody, templet.getCpdailyExtension());
            
            boolean isSumitAccess = OkHttpUtil.CheckRequestSuccess(sumitResult, "submitSign");
            if (isSumitAccess){
               break;
            }
        }

        return Result.of(ResultCode.SUCCESS,"提交成功");
    }


    public JSONObject getSignStatisticsInfoByMonth() throws IOException {
        String url = origin+getSignStatisticsInfoByMonth;
        JSONObject body = new JSONObject();
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String format = now.format(dateTimeFormatter);
        body.put("statisticYearMonth",format);
        Response response = OkHttpUtil.post(url, headers, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }

    public JSONObject detailSignInstance(String signInstanceWid,String signWid) throws IOException {
        String url = origin+detailSignInstance;
        JSONObject body = new JSONObject();
        body.put("signInstanceWid",signInstanceWid);
        body.put("signWid",signWid);
        Response response = OkHttpUtil.post(url, headers, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }


    public JSONObject getStuAttendacesInOneDay() throws IOException {

        String url = origin+getStuAttendacesInOneDay;
        JSONObject body = new JSONObject();
        Response response = OkHttpUtil.post(url, headers, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }

    public JSONObject submitSign(JSONObject submitBody,String cpdaily) throws IOException {
        String url = origin+submitSign;

        Headers header = new Headers.Builder()
                .addAll(headers)
                .add("Cpdaily-Extension",cpdaily)
                .build();
        Response response = OkHttpUtil.post(url, header, submitBody,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }




}



