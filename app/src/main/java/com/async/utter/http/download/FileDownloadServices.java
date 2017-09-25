package com.async.utter.http.download;

import android.util.Log;

import com.async.utter.http.interfaces.IHttpListener;
import com.async.utter.http.interfaces.IHttpService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2017/9/5.
 */

public class FileDownloadServices implements IHttpService {

    private final static String TAG = "FileDownloadServices";

    private String url;
    private IHttpListener iHttpListener;
    private byte[] requestDate;
    private HttpGet httpGet;

    /**
     * 即将添加到请求头的信息
     */
    private Map<String ,String> headerMap= Collections.synchronizedMap(new HashMap<String ,String>());
    private HttpClient httpClient;

    private AtomicBoolean pause=new AtomicBoolean(false);

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void post() {
        httpGet = new HttpGet(url);
        constructHeader();
        httpClient = new DefaultHttpClient();
        try {
            httpClient.execute(httpGet , new HttpResponseHandler());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private void constructHeader() {
        Iterator<String> iterator = headerMap.keySet().iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            String value = headerMap.get(key);
            Log.e(TAG, "constructHeader: 请求头信息:"+key+","+value);
            httpGet.addHeader(key , value);
        }
    }

    @Override
    public void get() {

    }

    @Override
    public void setHttpListener(IHttpListener httpListener) {
        this.iHttpListener = httpListener;
    }

    @Override
    public void setRequstData(byte[] requstData) {
        this.requestDate=requstData;
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
        return pause.get();
    }

    @Override
    public void pause() {
        pause.compareAndSet(false , true);
    }

    private class HttpResponseHandler extends BasicResponseHandler{
        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            int code = response.getStatusLine().getStatusCode();
            Log.e("===============" , code+"");
            if (code == 200){
                iHttpListener.onSuccess(response.getEntity());
            }else {
                iHttpListener.onFailure("StatusCode:"+code);
            }
            return null;
        }
    }
}
