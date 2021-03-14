package red.fuyun.ProxyServer;


import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullRequestIntercept;
import io.netty.handler.codec.http.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import red.fuyun.dao.UserInfoMapper;
import red.fuyun.util.BaseHolder;
import red.fuyun.util.Util;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/1 22:28
 */
public class MeFullRequestIntercept extends FullRequestIntercept {

    private SqlSessionFactory sqlSessionFactory  = BaseHolder.getApplicationContext().getBean(SqlSessionFactory.class);



    @Override
    public boolean match(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {

        return true;
    }


    @Override
    public void handleRequest(FullHttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
        final String uri = httpRequest.uri();
        HttpHeaders headers = httpRequest.headers();
        String host = headers.get("host");
        if (!Util.matchUri(uri) && !Util.matchHost(host)){
            httpRequest.release();
        }

        if (uri.contains("user/new/myMainPage")){
            System.out.println("---------------myMainPage---------------------");
            httpRequest.setMethod(HttpMethod.POST);
            System.out.println("---------------myMainPage------Success---------------------");
        }
///wec-portal-mobile/client/userStoreAppList?oick=bd3b61d4
        if(uri.contains("client/userStoreAppList")){
            Util.userStoreAppList(httpRequest,sqlSessionFactory);
        }

    }

}
