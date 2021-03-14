package red.fuyun.ProxyServer;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullResponseIntercept;
import io.netty.handler.codec.http.*;
import org.apache.ibatis.session.SqlSessionFactory;
import red.fuyun.util.BaseHolder;
import red.fuyun.util.Util;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/11/1 22:29
 */
public class MeFullResponseIntercept extends FullResponseIntercept {

    private SqlSessionFactory sqlSessionFactory  = BaseHolder.getApplicationContext().getBean(SqlSessionFactory.class);
    @Override
    public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
        final String uri = httpRequest.uri();
        if (Util.matchUri(uri)){
            return true;
        }
        return false;
    }

    @Override
    public void handleResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
        final String uri = httpRequest.uri();
        if (Util.matchUri(uri)){
            Util.Kq(sqlSessionFactory,httpRequest,httpResponse,uri);
        }
    }


}
