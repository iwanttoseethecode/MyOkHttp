package com.example.myokhttp.net.chain;

import android.util.Log;

import com.example.myokhttp.net.HttpClient;
import com.example.myokhttp.net.HttpConnection;
import com.example.myokhttp.net.HttpUrl;
import com.example.myokhttp.net.Request;
import com.example.myokhttp.net.Response;

import java.io.IOException;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class ConnectionInterceptor implements Interceptor {

    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        Log.e("interceptor","连接拦截器……");
        Request request = chain.call.request();
        HttpClient client = chain.call.getClient();
        HttpUrl url = request.getUrl();
        String host = url.getHost();
        int port = url.getPort();
        HttpConnection httpConnection = client.connectionPool().get(host,port);
        if (httpConnection == null){
            httpConnection = new HttpConnection();
        }else{
            Log.e("call","使用连接池……");
        }
        httpConnection.setRequest(request);
        Response response = chain.proceed(httpConnection);
        if (response.isKeepAlive()){
            client.connectionPool().put(httpConnection);
        }
        return response;
    }
}
