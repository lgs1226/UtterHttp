package com.async.utter.http;

import com.alibaba.fastjson.JSON;
import com.async.utter.http.interfaces.IHttpService;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.FutureTask;

/**
 * Created by Administrator on 2017/8/31.
 */

public class UtterHttpTask<T> implements Runnable {
    private IHttpService httpService;
    private FutureTask futureTask;

    public UtterHttpTask(RequestHolder<T> tRequestHolder) {
        httpService = tRequestHolder.getHttpService();
        httpService.setUrl(tRequestHolder.getUrl());
        httpService.setHttpListener(tRequestHolder.getHttpListener());
        T t = tRequestHolder.getRequestInfo();
        String requestInfo = JSON.toJSONString(t);
        try {
            httpService.setRequstData(requestInfo.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        httpService.post();
    }

    /**
     * 新增方法
     */
    public void start()
    {
        futureTask = new FutureTask(this, null);
        try {
            ThreadPoolManager.getInstance().execute(futureTask);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 新增方法
     */
    public  void pause()
    {
        httpService.pause();
        if(futureTask!=null)
        {
            ThreadPoolManager.getInstance().removeTask(futureTask);
        }

    }
}
