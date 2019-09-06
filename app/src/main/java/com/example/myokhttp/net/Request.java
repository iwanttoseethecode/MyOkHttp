package com.example.myokhttp.net;

import android.text.TextUtils;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class Request {

    //请求头
    private Map<String,String> headers;
    //请求体
    private RequestBody body;
    //解析url 成HttpUrl对象
    private HttpUrl url;
    //请求方法
    private String method;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public RequestBody getBody() {
        return body;
    }

    public HttpUrl getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public Request(Builder builder){
        this.headers = builder.headers;
        this.body = builder.body;
        this.url = builder.url;
        this.method = builder.method;
    }

    public static class Builder {

        private Map<String,String> headers = new HashMap<String,String>();

        private RequestBody body;

        private HttpUrl url;

        private String method = "GET";

        public Builder addHeaders(String name,String values) {
            this.headers.put(name, values);
            return this;
        }

        public Builder removeHeader(String name){
            headers.remove(name);
            return this;
        }

        public Builder get(){
            method = "GET";
            return this;
        }

        public Builder post(RequestBody body) {
            this.body = body;
            method = "POST";
            return this;
        }

        public Builder setUrl(String url) {
            try{
                this.url = new HttpUrl(url);
                return this;
            }catch(Exception e){
                throw new IllegalStateException("Failed Http Url",e);
            }
        }

        public Request build(){
            if (TextUtils.isEmpty(url.toString())){
                throw new IllegalStateException("请填写正确的url");
            }
            Request request = new Request(this);
            return request;
        }

    }

}
