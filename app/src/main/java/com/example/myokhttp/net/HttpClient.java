package com.example.myokhttp.net;

import com.example.myokhttp.net.chain.Interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class HttpClient {

    //分发器
    private Dispatcher dispatcher;
    //连接池
    private ConnectionPool connectionPool;
    //重试连接次数
    private int retrys;
    //客户端拦截器集合
    private List<Interceptor> interceptors;

    public HttpClient(){
        this(new Builder());
    }

    public HttpClient(Builder builder){
        this.dispatcher = builder.dispatcher;
        this.connectionPool = builder.connectionPool;
        this.retrys = builder.retrys;
        this.interceptors = builder.interceptors;
    }

    public static class Builder{

        Dispatcher dispatcher = new Dispatcher();

        ConnectionPool connectionPool = new ConnectionPool();

        int retrys = 3;

        List<Interceptor> interceptors = new ArrayList<>();

        public Builder retrys(int retrys){
            this.retrys = retrys;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor){
            interceptors.add(interceptor);
            return this;
        }

    }

    public Call newCall(Request request){
        return new Call(request,this);
    }

    public int getRetrys(){
        return retrys;
    }

    public Dispatcher dispatcher(){
        return dispatcher;
    }

    public ConnectionPool connectionPool(){
        return connectionPool;
    }

    public List<Interceptor> interceptors(){
        return interceptors;
    }

}
