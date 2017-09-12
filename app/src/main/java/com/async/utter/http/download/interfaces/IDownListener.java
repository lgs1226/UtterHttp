package com.async.utter.http.download.interfaces;

import com.async.utter.http.interfaces.IHttpListener;
import com.async.utter.http.interfaces.IHttpService;

/**
 * Created by Administrator on 2017/9/1.
 */

public interface IDownListener extends IHttpListener {

    void setHttpService(IHttpService httpService);

    void setCancleCall();

    void setPauseCall();
}
