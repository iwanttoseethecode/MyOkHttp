package com.example.myokhttp.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luoling on 2019/9/5.
 * description:拼接头部的工具类
 */
public class HttpCodec {

    //回车和换行
    static final String CRLF = "\r\n";
    static final int CR = 13;
    static final int LF = 10;
    static final String SPACE = " ";
    static final String VERSION = "HTTP/1.1";
    static final String COLON = ":";

    public static final String HEAD_HOST = "Host";
    public static final String HEAD_CONNECTION = "Connection";
    public static final String HEAD_CONTENT_TYPE = "Content-Type";
    public static final String HEAD_CONTENT_LENGTH = "Content-Length";
    public static final String HEAD_TRANSFER_ENCODEING = "Transfer-Encoding";
    public static final String HEAD_VALUE_KEEP_ALIVE = "Keep-Alive";
    public static final String HEAD_VALUE_CHUNKED = "chunked";

    ByteBuffer byteBuffer;

    public HttpCodec(){
        byteBuffer = ByteBuffer.allocate(10*1024);
    }

    public void writeRequest(OutputStream os,Request request) throws IOException{
        StringBuffer protocol = new StringBuffer();

        //请求行
        protocol.append(request.getMethod());
        protocol.append(SPACE);
        protocol.append(request.getUrl().file);
        protocol.append(SPACE);
        protocol.append(VERSION);
        protocol.append(CRLF);

        //http请求头
        Map<String,String> headers = request.getHeaders();
        for (Map.Entry<String,String> entry:headers.entrySet()){
            protocol.append(entry.getKey());
            protocol.append(COLON);
            protocol.append(SPACE);
            protocol.append(entry.getValue());
            protocol.append(CRLF);
        }
        protocol.append(CRLF);

        //http请求体 如果存在
        RequestBody body = request.getBody();
        if (null != body){
            protocol.append(body.body());
        }

        //写出
        os.write(protocol.toString().getBytes());
        os.flush();
    }

    public Map<String,String> readHeaders(InputStream is) throws IOException{
        HashMap<String,String> headers = new HashMap<>();
        while(true){
            String line = readLine(is);
            //读取到空行 则下面的为body
            if (isEmptyLine(line)){
                break;
            }
            int index = line.indexOf(":");
            if (index > 0){
                String name = line.substring(0,index);
                // ": "移动两位到 总长度减去两个("\r\n")
                String value = line.substring(index + 2,line.length() - 2);
                headers.put(name,value);
            }
        }
        return headers;
    }

    public String readLine(InputStream inputStream) throws IOException{
        try{
            byte b;
            boolean isMabeyEofLine = false;
            //标记
            byteBuffer.clear();
            byteBuffer.mark();
            while((b = (byte) inputStream.read()) != -1){
                byteBuffer.put(b);
                // 读取到/r则记录，判断下一个字节是否为/n
                if (b == CR){
                    isMabeyEofLine = true;
                }else if (isMabeyEofLine){
                    //上一个字节是/r 并且本次读取到/n
                    if (b == LF){
                        //获得目前的所有字节
                        byte[] lineBytes = new byte[byteBuffer.position()];
                        //返回标记位置
                        byteBuffer.reset();
                        byteBuffer.get(lineBytes);
                        //清空所有index并重新标记
                        byteBuffer.clear();
                        byteBuffer.mark();
                        String line = new String(lineBytes);
                        return line;
                    }
                    isMabeyEofLine = false;
                }
            }
        }catch(Exception e){
            throw new IOException(e);
        }
        throw new IOException("Response Read Line.");
    }

    boolean isEmptyLine(String line){
        return line.equals("\r\n");
    }

    public byte[] readBytes(InputStream is,int len) throws IOException{
        byte[] bytes = new byte[len];
        int readNum = 0;
        while(true){
            readNum += is.read(bytes,readNum,len-readNum);
            if (readNum == len){
                return bytes;
            }
        }

    }

    public String readChunked(InputStream is) throws IOException{
        int len = -1;
        boolean isEmptyData = false;
        StringBuffer chunked = new StringBuffer();
        while(true){
            if (len < 0){
                String line = readLine(is);
                line = line.substring(0,line.length() - 2);
                len = Integer.valueOf(line, 16);
                //chunk编码的数据最后一段为 0\r\n\r\n
                isEmptyData = len == 0;
            }else{
                //块长度不包括\r\n  所以+2将 \r\n 读走
                byte[] bytes = readBytes(is, len + 2);
                chunked.append(new String(bytes));
                len = -1;
                if (isEmptyData) {
                    return chunked.toString();
                }
            }
        }
    }

}
