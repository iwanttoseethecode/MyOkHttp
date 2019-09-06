package com.example.myokhttp.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class RequestBody {

    /**
     * 表单提交 使用url encoded编码
     */
    private final static String CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final static String CHARSET = "utf-8";

    Map<String,String> encodedBodys = new HashMap<>();

    public String getContentType(){
        return CONTENT_TYPE;
    }

    public String body(){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> entry:encodedBodys.entrySet()){
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (sb.length() != 0){
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public long contentLength(){
        return body().getBytes().length;
    }

    public RequestBody add(String name,String value){
        try {
            encodedBodys.put(URLEncoder.encode(name,CHARSET),URLEncoder.encode(value,CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }
}
