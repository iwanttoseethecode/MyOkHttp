package com.example.myokhttp.net.chain;

import android.util.Log;

import com.example.myokhttp.net.HttpCodec;
import com.example.myokhttp.net.Request;
import com.example.myokhttp.net.Response;

import java.io.IOException;
import java.util.Map;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class HeadersInterceptor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        Log.e("interceptor","请求头拦截器……");
        Request request = chain.call.request();
        Map<String,String> headers = request.getHeaders();
        headers.put(HttpCodec.HEAD_HOST,request.getUrl().getHost());
        headers.put(HttpCodec.HEAD_CONNECTION,HttpCodec.HEAD_VALUE_KEEP_ALIVE);
        if (null != request.getBody()){
            String contentType = request.getBody().getContentType();
            if (contentType != null){
                headers.put(HttpCodec.HEAD_CONTENT_TYPE,contentType);
            }
            long contentLength = request.getBody().contentLength();
            if (contentLength != -1){
                headers.put(HttpCodec.HEAD_CONTENT_TYPE,Long.toString(contentLength));
            }
        }
        return chain.proceed();
    }
}
