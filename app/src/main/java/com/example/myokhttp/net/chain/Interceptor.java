package com.example.myokhttp.net.chain;

import com.example.myokhttp.net.Response;

import java.io.IOException;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public interface Interceptor {

    Response intercept(InterceptorChain chain) throws IOException;

}
