package red.fuyun.campusphere;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import red.fuyun.util.OkHttpUtil;

import java.io.IOException;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/25 11:45
 */
@Data
public class UploadImage {

    private  String ua;
    private OkHttpClient client = OkHttpUtil.getClient();
    private String cookie;
    private String origin ;
    private Headers headers;


    public UploadImage(String cookie,String ua,String origin){
        this.cookie = cookie;
        this.ua = ua;
        this.origin = origin;
        this.headers = new Headers.Builder()
                .add("user-agent",this.ua)
                .add("cookie",this.cookie)
                .build();
    }


    public JSONObject getUploadPolicy() throws IOException {
        String url = origin+"/wec-counselor-sign-apps/stu/oss/getUploadPolicy";

        JSONObject body = new JSONObject();
        body.put("fileType",1);
        Response response = OkHttpUtil.post(url, headers, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }


    public JSONObject previewAttachment(String ossKey) throws IOException {
        String url = origin+"/wec-counselor-sign-apps/stu/sign/previewAttachment";

        JSONObject body = new JSONObject();
        body.put("ossKey",ossKey);
        Response response = OkHttpUtil.post(url, headers, body,client);
        String resBody = response.body().string();
        response.close();
        JSONObject jsonObject = JSONObject.parseObject(resBody);
        return jsonObject;
    }


}
