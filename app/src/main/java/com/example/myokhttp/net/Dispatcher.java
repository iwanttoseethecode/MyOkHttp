package com.example.myokhttp.net;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by luoling on 2019/9/5.
 * description:
 */
public class Dispatcher {

    //最多同时请求
    private int maxRequests;

    //同一个host同时最多请求
    private int maxRequestPerHost;

    //线程池，发送异步请求
    private ExecutorService executorService;

    /*
    * 等待执行
    * */
    private final Deque<Call.AsyncCall> readyAsyncCalls = new ArrayDeque<>();

    /*
    * 正在执行的队列
    * */
    private final Deque<Call.AsyncCall> runningAsyncCalls = new ArrayDeque<>();

    public Dispatcher(){
        this(64,2);
    }

    public Dispatcher(int maxRequests,int maxRequestPerHost){
        this.maxRequests = maxRequests;
        this.maxRequestPerHost = maxRequestPerHost;
    }

    public synchronized ExecutorService executorService(){
        if (executorService == null){
            ThreadFactory threadFactory = new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread result = new Thread(r,"OkHttp Dispatcher");
                    return result;
                }
            };

            executorService = new ThreadPoolExecutor(0,Integer.MAX_VALUE,60, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),threadFactory);
        }
        return executorService;
    }

    //异步
    public void enqueue(Call.AsyncCall call){
        //不能超过最大请求数，同时执行相同的host请求不能超过最大host数
        if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call)<maxRequestPerHost){
            Log.e("Dispatcher","提交执行");
            runningAsyncCalls.add(call);
            executorService().execute(call);
        }else{
            Log.e("Dispatcher","等待执行");
            readyAsyncCalls.add(call);
        }
    }

    /**
     * 同一host 的 同时请求数
     *
     * @param call
     * @return
     */
    private int runningCallsForHost(Call.AsyncCall call){
        int result = 0;
        //如果执行这个请求，则相同的host数量是result
        for (Call.AsyncCall c : runningAsyncCalls){
            if (c.host().equals(call.host())){
                result++;
            }
        }
        return result;
    }

    /*
     *请求结束 移出正在运行队列
     *并判断是否执行等待队列中的请求
     */
    public void finished(Call.AsyncCall asyncCall){
        synchronized (this){
            runningAsyncCalls.remove(asyncCall);
            promoteCalls();
        }
    }

    /*
    * 判断是否执行等待队列的请求
    * */
    private void promoteCalls(){
        //同时请求达到上限
        if (runningAsyncCalls.size() >= maxRequests){
            return;
        }

        //没有等待执行请求
        if (readyAsyncCalls.isEmpty()){
            return;
        }
        for (Iterator<Call.AsyncCall> iterator = readyAsyncCalls.iterator();iterator.hasNext();){
            Call.AsyncCall call = iterator.next();
            if (runningCallsForHost(call)<maxRequestPerHost){
                iterator.remove();
                runningAsyncCalls.add(call);
                executorService().execute(call);
            }
            //到达同时请求上限
            if (runningAsyncCalls.size() >= maxRequests){
                return;
            }
        }
    }

}
