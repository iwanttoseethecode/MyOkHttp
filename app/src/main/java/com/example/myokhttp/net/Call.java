package com.example.myokhttp.net;

import android.os.Handler;
import android.os.Looper;

import com.example.myokhttp.net.chain.CallServiceInterceptor;
import com.example.myokhttp.net.chain.ConnectionInterceptor;
import com.example.myokhttp.net.chain.HeadersInterceptor;
import com.example.myokhttp.net.chain.Interceptor;
import com.example.myokhttp.net.chain.InterceptorChain;
import com.example.myokhttp.net.chain.RetryInterceptor;
import com.example.myokhttp.net.interfaces.Callback;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class Call {
    Request request;
    HttpClient client;

    /*
    * 是否执行过
    * */
    boolean executed;

    /*
    * 取消
    * */
    boolean canceled;

    Handler handler = new Handler(Looper.getMainLooper());

    public Request request(){
        return request;
    }

    public void setPostHandler(Looper looper){
        handler = new Handler(looper);
    }

    public void setPostHandler(Handler handler){
        this.handler = handler;
    }

    public Call(Request request,HttpClient client){
        this.request = request;
        this.client = client;
    }

    public Call enqueue(Callback callback){
        synchronized (this){
            if (executed){
                throw new IllegalStateException("Already Execute");
            }
            executed = true;
        }
        client.dispatcher().enqueue(new AsyncCall(callback));
        return this;
    }

    public void cancel(){
        canceled = true;
    }

    public boolean isCanceled(){
        return canceled;
    }

    public HttpClient getClient(){
        return client;
    }

    final class AsyncCall implements Runnable{

        private final Callback callback;

        public AsyncCall(Callback callback){
            this.callback = callback;
        }

        @Override
        public void run() {
            //是否已经通知过callback
            boolean signalledCallback = false;
            try{
                final Response response = getResponse();
                if (canceled){
                    signalledCallback = true;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(Call.this,new IOException("Canceled"));
                        }
                    });
                }else{
                    signalledCallback = true;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(Call.this,response);
                        }
                    });
                }
            }catch(final IOException e){
                if (!signalledCallback){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(Call.this,e);
                        }
                    });
                }
            }finally {
                client.dispatcher().finished(this);
            }
        }

        public String host(){
            return request.getUrl().getHost();
        }
    }

    private Response getResponse() throws IOException{

        //添加拦截器
        ArrayList<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        interceptors.add(new RetryInterceptor());
        interceptors.add(new HeadersInterceptor());
        interceptors.add(new ConnectionInterceptor());
        interceptors.add(new CallServiceInterceptor());

        InterceptorChain interceptorChain = new InterceptorChain(interceptors,0,this,null);
        return interceptorChain.proceed();
    }

}
