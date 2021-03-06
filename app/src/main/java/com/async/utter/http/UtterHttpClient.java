package com.async.utter.http;

import com.async.utter.http.interfaces.IDataListener;
import com.async.utter.http.interfaces.IHttpListener;
import com.async.utter.http.interfaces.IHttpService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

/**
 * Created by Administrator on 2017/8/31.
 */

public class UtterHttpClient {

    private Map<String , String> headMap = new HashMap<>();

    public static<T , M> void post(String url , T requstInfo , Class<M> responseModel , IDataListener<M> dataListener){
        RequestHolder<T> requestHolder = new RequestHolder<>();
        requestHolder.setUrl(url);
        requestHolder.setRequestInfo(requstInfo);
        IHttpService iHttpService = new JsonHttpService();
        IHttpListener iHttpListener = new JsonResponseHandler<M>(responseModel , dataListener);
        requestHolder.setHttpListener(iHttpListener);
        requestHolder.setHttpService(iHttpService);
        UtterHttpTask<T> httpTask = new UtterHttpTask<>(requestHolder);
        try {
            ThreadPoolManager.getInstance().execute(new FutureTask(httpTask , null));
        } catch (InterruptedException e) {
            e.printStackTrace();
            dataListener.onFailure(e.getMessage());
        }
    }

    public void addHeader(String key , String value){
        headMap.put(key , value);
    }

    // TODO:GET请求
    public void get(){

    }

    //TODO:POST请求
    public void post(){

    }

    //TODO:Download
    public void download(){

    }


}
