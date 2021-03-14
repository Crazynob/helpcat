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
import red.fuyun.util.Util;


import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/2 18:28
 */

@Component("sign")
@Scope("prototype")
public class Sign {

    @Value("${signe.StuSignInfosInOneDay}")
    private String StuSignInfosInOneDay;

    @Value("${signe.detailSignInstance}")
    private String detailSignInstance;

    @Value("${signe.StuSignInfosByWeekMonth}")
    private String StuSignInfosByWeekMonth;

    @Value("${signe.submitSign}")
    private String submitSign;




    private  String ua;
    private OkHttpClient client = OkHttpUtil.getClient();
    private String cookie;
    private String origin ;
    private  Headers headers;
    public Sign(String cookie,String ua,String origin){
        this.cookie = cookie;
        this.ua = ua;
        this.origin = origin;
        this.headers = new Headers.Builder()
                .add("user-agent",this.ua)
                .add("cookie",this.cookie)
                .build();
    }

    public Result sigin(TempletDo templet) throws IOException {
        //获取日常签到任务
        JSONObject getStuSignInfosInOneDayRes = this.getStuSignInfosInOneDay();
        //验证结果是正确 访问是够成功
        if (!OkHttpUtil.CheckRequestSuccess(getStuSignInfosInOneDayRes,"queryDailySginTasksRes")){
            String message = getStuSignInfosInOneDayRes.getString("message");
            return Result.of(ResultCode.FAIL,message);
        }else {
            System.out.println("queryDailySginTasksRes:成功");
        }


        //获取未签到任务
        JSONObject datas = getStuSignInfosInOneDayRes.getJSONObject("datas");
        // codeRcvdTasks unSignedTasks leaveTasks signedTasks

        JSONArray unSignedTasks = datas.getJSONArray("unSignedTasks");

        if(unSignedTasks.size()<=0){
            unSignedTasks = datas.getJSONArray("leaveTasks");
        }

        if (unSignedTasks == null || unSignedTasks.size()<=0){
            System.out.println("constomMessage:unSignedTasks为空");
            return Result.of(ResultCode.FAIL,"unSignedTasks为空");
        }
        JSONObject unSignedTask = OkHttpUtil.getunSignedTask(unSignedTasks);
        if (unSignedTask == null){
            System.out.println("constomMessage:没有满足条件的签到任务");
            return Result.of(ResultCode.FAIL,"没有满足条件的签到任务");
        }

        JSONObject detailSignInstanceRes = this.detailSignInstance(unSignedTask);
        //验证结果是正确 访问是够成功
        if (!OkHttpUtil.CheckRequestSuccess(detailSignInstanceRes,"detailSignTaskInstRes")){
            String message = detailSignInstanceRes.getString("message");
            return Result.of(ResultCode.FAIL,message);
        }else {
            System.out.println("detailSignTaskInst:成功");
        }
        JSONObject datasOne = detailSignInstanceRes.getJSONObject("datas");
        String signInstanceWid = datasOne.getString("signInstanceWid");
        Integer isNeedExtra = datasOne.getInteger("isNeedExtra");
        Integer isPhoto = datasOne.getInteger("isPhoto");
        Integer isMalposition = datasOne.getInteger("isMalposition");
        JSONArray extraField = datasOne.getJSONArray("extraField");
        JSONArray temple = JSONArray.parseArray(templet.getData());
        JSONArray extraFieldItems = null;
        String longitude = templet.getLongitude();
        String latitude = templet.getLatitude();
        String position = templet.getSignAddress();
        String signPhotoUrl = "";

        if (isNeedExtra.equals(1)){
            extraFieldItems = Util.buildExtraFieldItems(temple,extraField);
        }

        if (isPhoto.equals(1)){
            signPhotoUrl = templet.getSignPhotoUrl();
        }

        JSONObject submitBody = Util.buildSubmitBody(isMalposition, isNeedExtra, latitude, longitude, position, signInstanceWid,extraFieldItems,signPhotoUrl);
        JSONObject submitRes = this.submitSign(submitBody,templet);
        //验证结果是正确 访问是够成功
        if (!OkHttpUtil.CheckRequestSuccess(submitRes,"submitRes")){
            String message = submitRes.getString("message");
            return Result.of(ResultCode.FAIL,message);
        }else {
            System.out.println("submitRes:成功");
        }

        return Result.of(ResultCode.SUCCESS,"签到成功");
    }

    /**
     * 获取日常签到任务
     * @return
     */
//    https://cmoc.campusphere.net/wec-counselor-sign-apps/stu/sign/getStuSignInfosInOneDay
    public JSONObject getStuSignInfosInOneDay() throws IOException {
//        String url = origin+"/wec-counselor-sign-apps/stu/sign/getStuSignInfosInOneDay";
        String url = origin+StuSignInfosInOneDay;
        Response response = OkHttpUtil.post(url, headers, new JSONObject(),client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }


//    https://cmoc.campusphere.net/wec-counselor-sign-apps/stu/sign/detailSignTaskInst
//    https://cmoc.campusphere.net/wec-counselor-sign-apps/stu/sign/detailSignTaskInst

    /**
     * 获取签到任务的详细信息
     * @return
     */
//    https://cmoc.campusphere.net/wec-counselor-sign-apps/stu/sign/detailSignInstance
//    https://cmoc.campusphere.net/wec-counselor-sign-apps/stu/sign/detailSignInstance
    public JSONObject detailSignInstance(JSONObject unSignedTask) throws IOException {
//        String url = origin+"/wec-counselor-sign-apps/stu/sign/detailSignInstance";
        String url = origin+detailSignInstance;
        String signInstanceWid = unSignedTask.getString("signInstanceWid");
        String signWid = unSignedTask.getString("signWid");
        JSONObject reqBody = new JSONObject();
        reqBody.put("signInstanceWid",signInstanceWid);
        reqBody.put("signWid",signWid);
        Response response = OkHttpUtil.post(url, headers,reqBody,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }

//


    /**
     * 提交签到信息
     * @param submitBody
     * @param templet
     * @return
     * @throws IOException
     */

    public JSONObject submitSign(JSONObject submitBody, TempletDo templet) throws IOException {

        String url =origin+submitSign;
        Headers headers = new Headers.Builder()
                .addAll(this.headers)

                .add("CpdailyStandAlone", "0")
                .add("extension", "1")
                .add("Cpdaily-Extension", templet.getCpdailyExtension())
                .build();
        Response response = OkHttpUtil.post(url, headers, submitBody, client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }


    /**
     * 获取这个月的所有签到任务
     * @return
     * @throws IOException
     */
    public  JSONObject getStuSignInfosByWeekMonth() throws IOException {

        String url = origin+StuSignInfosByWeekMonth;
        JSONObject reqBody = new JSONObject();
        LocalDate now = LocalDate.now().minusDays(3);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String statisticYearMonth = now.format(dateTimeFormatter);
        reqBody.put("statisticYearMonth",statisticYearMonth);
        Response response = OkHttpUtil.post(url, headers,reqBody,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }

}
