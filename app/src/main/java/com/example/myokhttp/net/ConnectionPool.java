package com.example.myokhttp.net;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by luoling on 2019/9/5.
 * description:keep-alive 就是浏览器和服务端之间保持长连接，这个连接是可以复用的。在HTTP1.1中是默认开启的。
 *
 * 使用连接池，减少握手的次数，大幅提高效率。
 */
public class ConnectionPool {

    /**
     * 每个连接的最大存活时间
     */
    private final long keepAliveDuration;

    //复用队列
    private final Deque<HttpConnection> connections = new ArrayDeque<>();

    private boolean cleanupRunning;

    public ConnectionPool(){
        this(1, TimeUnit.MINUTES);
    }

    public ConnectionPool(long keepAliveDuration,TimeUnit timeUnit){
        //毫秒
        this.keepAliveDuration = timeUnit.toMillis(keepAliveDuration);
    }

    /**
     * 垃圾回收线程
     * 线程池，用来检测闲置socket并对其进行清理
     */
    private static ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread result = new Thread(r,"DNHttpClient ConnectionPool");
            result.setDaemon(true);
            return result;
        }
    };

    private static final Executor executor = new ThreadPoolExecutor(0,Integer.MAX_VALUE,60,TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),threadFactory);

    private final Runnable cleanupRunable = new Runnable() {
        @Override
        public void run() {
            while (true){
                long waitTimes = cleanUp(System.currentTimeMillis());
                if (waitTimes == -1){
                    return;
                }
                if (waitTimes > 0){
                    synchronized (ConnectionPool.this){
                        try{
                            //调用某个对象的wait()方法能让当前线程阻塞，
                            //并且当前线程必须拥有此对象的monitor(锁)
                            ConnectionPool.this.wait(waitTimes);
                        }catch(InterruptedException ignored){
                        }
                    }
                }
            }
        }
    };

    public HttpConnection get(String host,int port){
        Iterator<HttpConnection> iterator = connections.iterator();
        while(iterator.hasNext()){
            HttpConnection connection = iterator.next();
            //检查连接是否复用（同样的host）
            if (connection.isSameAddress(host,port)){
                //正在使用的移出连接池
                iterator.remove();
                return connection;
            }
        }
        return null;
    }

    public void put(HttpConnection connection){
        //执行检测清理
        if (!cleanupRunning){
            cleanupRunning = true;
            executor.execute(cleanupRunable);
        }
        connections.add(connection);
    }

    /**
     * 检查需要移除的连接返回下次检查时间
     */
    private long cleanUp(long now){
        long longestIdleDuration = -1;
        synchronized (this){
            for (Iterator<HttpConnection> it = connections.iterator(); it.hasNext();){
                HttpConnection connection = it.next();
                //获得闲置时间
                long idleDuration = now - connection.lastUsetime;
                if (idleDuration > keepAliveDuration){
                    connection.closeQuietly();
                    it.remove();
                    Log.e("Pool","移出连接池");
                    continue;
                }
                //获得最大闲置时间
                if (longestIdleDuration < idleDuration){
                    longestIdleDuration = idleDuration;
                }
            }
            //下次检查时间
            if (longestIdleDuration >= 0){
                return keepAliveDuration - longestIdleDuration;
            }else{
                //连接池没有连接 可以退出
                cleanupRunning = false;
                return longestIdleDuration;
            }
        }
    }

}
