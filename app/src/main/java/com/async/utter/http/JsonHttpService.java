package com.async.utter.http;

import android.text.TextUtils;

import com.async.utter.http.interfaces.IHttpListener;
import com.async.utter.http.interfaces.IHttpService;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/31.
 */

public class JsonHttpService implements IHttpService {

    private String url;
    private IHttpListener httpListener;
    private byte[] reBytes = null;

    private HttpClient httpClient = new DefaultHttpClient();
    private HttpPost httpPost;

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void post() {
        if (TextUtils.isEmpty(this.url)){
            throw new IllegalArgumentException("UriRequest must not be null");
        }
        httpPost = new HttpPost(this.url);
        if (reBytes != null){
            httpPost.setEntity(new ByteArrayEntity(reBytes));
        }
        try {
            httpClient.execute(httpPost , new HttpResponseHandler());
        } catch (IOException e) {
            e.printStackTrace();
            httpListener.onFailure(e.getMessage());
        }
    }

    @Override
    public void get() {
        if (TextUtils.isEmpty(this.url)){
            throw new NullPointerException("Current URL is Null");
        }
        HttpGet httpGet = new HttpGet(url);
        try {
            httpClient.execute(httpGet , new HttpResponseHandler());
        } catch (IOException e) {
            e.printStackTrace();
            httpListener.onFailure(e.getMessage());
        }
    }

    @Override
    public void setHttpListener(IHttpListener httpListener) {
        this.httpListener = httpListener;
    }

    @Override
    public void setRequstData(byte[] requstData) {
        this.reBytes = requstData;
    }

    @Override
    public Map<String, String> getHttpHeadMap() {
        return null;
    }

    @Override
    public boolean cancle() {
        return false;
    }

    @Override
    public boolean isCancle() {
        return false;
    }

    @Override
    public boolean isPause() {
        return false;
    }

    private class HttpResponseHandler extends BasicResponseHandler{
        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException {
            int code = response.getStatusLine().getStatusCode();
            switch (code){
                case 200:
                    httpListener.onSuccess(response.getEntity());
                    break;
                case 504:
                    httpListener.onFailure("网络不给力");
                    break;
                case 502:
                case 404:
                    httpListener.onFailure("服务器异常，请稍后再试");
                    break;
                default:
                    httpListener.onFailure(code+"");
                    break;
            }
            return null;
        }
    }
}
