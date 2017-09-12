package com.async.utter.http.interfaces;

/**
 * Created by Administrator on 2017/8/31.
 */

public interface IDataListener<M> {

    void onSuccess(M entity);

    void onFailure(String e);

}
