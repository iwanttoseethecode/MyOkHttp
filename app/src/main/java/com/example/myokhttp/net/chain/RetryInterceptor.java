package com.example.myokhttp.net.chain;

import android.util.Log;

import com.example.myokhttp.net.Call;
import com.example.myokhttp.net.Response;

import java.io.IOException;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class RetryInterceptor implements Interceptor {
    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        Log.e("interceptor","重试拦截器……");
        Call call = chain.call;
        IOException exception = null;
        for (int i = 0; i < chain.call.getClient().getRetrys();i++){
            if (call.isCanceled()){
                throw new IOException("Canceled");
            }
            try{
                Response response = chain.proceed();
                return response;
            }catch(IOException e){
                exception = e;
            }
        }
        throw exception;
    }
}
