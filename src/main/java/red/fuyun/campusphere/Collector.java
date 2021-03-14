package red.fuyun.campusphere;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import red.fuyun.pojo.Do.TempletDo;
import red.fuyun.util.OkHttpUtil;
import red.fuyun.util.Result;
import red.fuyun.util.ResultCode;
import red.fuyun.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/24 12:05
 */
@Component("collector")
@Scope("prototype")
public class Collector {

    @Value("${collector.queryCollectorHistoryList}")
    private String queryCollectorHistoryList;

    @Value("${collector.detailCollector}")
    private String detailCollector;

    @Value("${collector.getFormFields}")
    private String getFormFields;

    @Value("${collector.queryCollectorProcessingList}")
    private String queryCollectorProcessingList;

    @Value("${collector.submitForm}")
    private String submitForm;


    private  String ua;
    private OkHttpClient client = OkHttpUtil.getClient();
    private String cookie;
    private String origin ;
    private Headers headers;

    public Collector(String cookie,String ua,String origin){
        this.cookie = cookie;
        this.ua = ua;
        this.origin = origin;
        this.headers = new Headers.Builder()
                .add("user-agent",this.ua)
                .add("cookie",this.cookie)
                .build();
    }


    public Result collect(TempletDo templet) throws IOException {
        JSONObject queryCollectorProcessingList = queryCollectorProcessingList();
        if (!OkHttpUtil.CheckRequestSuccess(queryCollectorProcessingList, "queryCollectorProcessingList")){
            String message = queryCollectorProcessingList.getString("message");
            return Result.of(ResultCode.FAIL,message);
        }
        JSONObject datas = queryCollectorProcessingList.getJSONObject("datas");
        JSONArray rows = datas.getJSONArray("rows");
        ArrayList<JSONObject> handleds = new ArrayList<>();
        for (int i = 0; i <rows.size() ; i++) {
            JSONObject row = rows.getJSONObject(i);
            Integer isHandled = row.getInteger("isHandled");
            if (isHandled.equals(0)){
                handleds.add(row);
            }
        }
        if (handleds.size()<=0){
            return Result.of(ResultCode.FAIL,"没有进行中的任务");
        }

        for (JSONObject handled : handleds) {
            String wid = handled.getString("wid");
            String formWid = handled.getString("formWid");
            JSONObject formFields = getFormFields(formWid, wid);

            if (!OkHttpUtil.CheckRequestSuccess(formFields, "formFields")){
                String message = queryCollectorProcessingList.getString("message");
                return Result.of(ResultCode.FAIL,message);
            }

            JSONObject datas1 = formFields.getJSONObject("datas");
            JSONArray rows1 = datas1.getJSONArray("rows");
            String data = templet.getData();
            JSONArray templ = JSONArray.parseArray(data);
            JSONArray form = Util.buildForm(rows1, templ,templet.getCollectorPhotoUrl());
            if (Objects.isNull(form)){
                continue;
            }
            JSONObject submit = new JSONObject();
            submit.put("address",templet.getSignAddress());
            submit.put("collectWid",wid);
            submit.put("form",form);
            submit.put("formWid",formWid);
            submit.put("schoolTaskWid",null);
            submit.put("uaIsCpadaily",true);
            JSONObject jsonObject = submitForm(submit, templet.getCpdailyExtension());
            if (!OkHttpUtil.CheckRequestSuccess(jsonObject, "submitForm")){
                String message = queryCollectorProcessingList.getString("message");
                return Result.of(ResultCode.FAIL,message);
            }else {
                break;
            }

        }

        return Result.of(ResultCode.SUCCESS,"提交成功");
    }


    /**
     * 获取日常采集任务
     * @return
     */

    public JSONObject queryCollectorProcessingList() throws IOException {

        String url = origin+queryCollectorProcessingList;
        JSONObject body = new JSONObject();
        body.put("pageNumber",1);
        body.put("pageSize",20);
        Response response = OkHttpUtil.post(url, headers, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }
    /**
     * 获取历史采集任务
     * @return
     */

    public JSONObject queryCollectorHistoryList() throws IOException {

        String url = origin+queryCollectorHistoryList;
        JSONObject body = new JSONObject();

        body.put("pageNumber",1);
        body.put("pageSize",20);
        Response response = OkHttpUtil.post(url, headers, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }


    /**
     * 获取采集任务详情
     * @return
     */

    public JSONObject getDetailCollector(String collectorWid) throws IOException {

        String url = origin+detailCollector;
        JSONObject body = new JSONObject();

        body.put("collectorWid",collectorWid);
        Response response = OkHttpUtil.post(url, headers, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }

    /**
     * 获取采集任务字段数据
     * @return
     */

    public JSONObject getFormFields(String formWid,String collectorWid) throws IOException {

        String url = origin+getFormFields;
        JSONObject body = new JSONObject();

        body.put("pageNumber",1);
        body.put("pageSize",50);
        body.put("formWid",formWid);
        body.put("collectorWid",collectorWid);
        Response response = OkHttpUtil.post(url, headers, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }

    public JSONObject submitForm(JSONObject body,String cpdaily) throws IOException {

        String url = origin+submitForm;
         Headers header = new Headers.Builder()
                 .addAll(headers)
                 .add("Cpdaily-Extension",cpdaily)
                 .build();
        Response response = OkHttpUtil.post(url, header, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return  jsonObject;
    }

}
