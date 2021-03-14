package red.fuyun.campusphere;



import lombok.AllArgsConstructor;
import lombok.Data;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import red.fuyun.util.OkHttpUtil;

import java.util.Objects;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/2 18:31
 */
@Data
@AllArgsConstructor
public class LoginCat {

    private String origin;
    private String ua;
    private String outhCookie;
    public String login() throws Exception {
        String url = origin+"/wec-counselor-sign-apps/stu/mobile/index.html";
        OkHttpClient client = OkHttpUtil.getClient();

        Headers ua = new Headers.Builder()
                .add("user-agent", this.ua)
                .build();


        Response response = OkHttpUtil.get(url, ua, client);
        String location = response.header("location");
        System.out.println(location);


        Headers cookieAndUa = new Headers.Builder().addAll(ua)
                .add("cookie", outhCookie)
                .add("user-agent", this.ua)
                .build();

        Response authRes = OkHttpUtil.get(location, cookieAndUa, client);
        String authResLocation = authRes.header("location");
        if (Objects.isNull(authResLocation)){
            throw new Exception("登录失败！");
        }
        System.out.println("继续执行");
        Response modRes = OkHttpUtil.get(authResLocation,ua, client);
        Headers headers = modRes.headers();
        return headers.get("set-cookie");
    }


}
