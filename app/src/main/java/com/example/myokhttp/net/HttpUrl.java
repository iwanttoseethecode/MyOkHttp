package com.example.myokhttp.net;


import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class HttpUrl {
    //协议http https
    String protocol;
    //192.6.2.3
    String host;
    //文件路径
    String file;
    //端口
    int port;

    String url;

    public HttpUrl(String url){
        this.url = url;
        try {
            URL uri = new URL(url);
            host = uri.getHost();
            file = uri.getFile();
            file = TextUtils.isEmpty(file) ? "/" : file;
            protocol = uri.getProtocol();
            port = uri.getPort();
            port = (port == -1 ? uri.getDefaultPort() : port);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getProtocol(){
        return protocol;
    }

    public String getHost(){
        return host;
    }

    public String getFile(){
        return file;
    }

    public int getPort(){
        return port;
    }

    @Override
    public String toString() {
        return url;
    }
}
