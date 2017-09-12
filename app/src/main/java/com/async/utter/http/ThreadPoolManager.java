package com.async.utter.http;

import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/8/31.
 */

public class ThreadPoolManager {

    private final static int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_SECONDS = 20;
    private static final ArrayBlockingQueue<Runnable> sPoolWorkQueue =
            new ArrayBlockingQueue<Runnable>(10);
    private static String TAG = "ThreadPoolManager";

    private static ThreadPoolManager instance = new ThreadPoolManager();

    private LinkedBlockingQueue<Future<?>> taskQuene=new LinkedBlockingQueue<>();

    private ThreadPoolExecutor threadPoolExecutor;
    private ThreadPoolManager() {
        threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE , MAXIMUM_POOL_SIZE , KEEP_ALIVE_SECONDS , TimeUnit.SECONDS , sPoolWorkQueue , handler);
        threadPoolExecutor.execute(runnable);
    }

    public static ThreadPoolManager getInstance(){
        return instance;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                FutureTask futureTask = null;
                try {
                    futureTask = (FutureTask) taskQuene.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (futureTask != null){
                    threadPoolExecutor.execute(futureTask);
                }
                Log.e(TAG , "线程池大小:"+threadPoolExecutor.getPoolSize());
            }
        }
    };

    public void execute(FutureTask futureTask) throws InterruptedException {
        taskQuene.put(futureTask);
    }

    public boolean removeTask(FutureTask futureTask){
        boolean result = false;
        /*
         *阻塞式队列是否含有线程
         */
        if (taskQuene.contains(futureTask)){
            taskQuene.remove(futureTask);
        }else {
            result = threadPoolExecutor.remove(futureTask);
        }
        return result;
    }

    private RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                taskQuene.put(new FutureTask<Object>(r , null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
