package com.async.utter.http.interfaces;

import java.util.Map;

/**
 * Created by Administrator on 2017/8/30.
 */

public interface IHttpService {

    void setUrl(String url);

    void post();

    void get();

    void setHttpListener(IHttpListener httpListener);

    void setRequstData(byte[] requstData);

    /**
     * 获取请求头的map
     * @return
     */
    Map<String,String> getHttpHeadMap();

    boolean cancle();

    boolean isCancle();

    boolean isPause();

    void pause();
}
