package com.async.utter.http;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.async.utter.http.interfaces.IDataListener;
import com.async.utter.http.interfaces.IHttpListener;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by Administrator on 2017/8/31.
 */

public class JsonResponseHandler<M> implements IHttpListener {

    private Class<M> responseEntity;

    private IDataListener<M> dataListener;

    private Handler handler = new Handler(Looper.getMainLooper());
    public JsonResponseHandler(Class<M> responseEntity , IDataListener<M> dataListener) {
        this.responseEntity = responseEntity;
        this.dataListener = dataListener;
    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        try {
            String s = EntityUtils.toString(httpEntity, "utf-8");
            final M entity = JSON.parseObject(s, responseEntity);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dataListener.onSuccess(entity);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            dataListener.onFailure(e.getMessage());
        }
    }

    @Override
    public void onFailure(String e) {
        dataListener.onFailure(e);
    }
}
