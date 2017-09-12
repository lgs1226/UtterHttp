package com.async.utter.http.interfaces;

import org.apache.http.HttpEntity;

/**
 * Created by Administrator on 2017/8/30.
 */

public interface IHttpListener {

    void onSuccess(HttpEntity httpEntity);

    void onFailure(String e);

}
