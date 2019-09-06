package com.example.myokhttp.net.chain;

import com.example.myokhttp.net.Call;
import com.example.myokhttp.net.HttpCodec;
import com.example.myokhttp.net.HttpConnection;
import com.example.myokhttp.net.Response;

import java.io.IOException;
import java.util.List;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class InterceptorChain {

    private final List<Interceptor> interceptorList;
    public final Call call;
    public final int index;
    public final HttpConnection connection;

    public final HttpCodec httpCodec = new HttpCodec();

    public InterceptorChain(List<Interceptor> interceptors, int index, Call call, HttpConnection connection){
        this.interceptorList = interceptors;
        this.call = call;
        this.connection = connection;
        this.index = index;
    }

    public Response proceed() throws IOException{
        return proceed(connection);
    }

    public Response proceed(HttpConnection connection)throws IOException{
        Interceptor interceptor = interceptorList.get(index);
        InterceptorChain next = new InterceptorChain(interceptorList,index+1,call,connection);
        Response response = interceptor.intercept(next);
        return response;
    }

}
