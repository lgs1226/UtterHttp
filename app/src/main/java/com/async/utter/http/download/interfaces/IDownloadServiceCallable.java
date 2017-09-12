package com.async.utter.http.download.interfaces;

import com.async.utter.http.download.DownloadWrapper;

/**
 * Created by Administrator on 2017/9/1.
 */

public interface IDownloadServiceCallable {

    void onDownloadStatusChanged(DownloadWrapper downloadWrapper);

    void onTotalLenthReceived(DownloadWrapper downloadWrapper);

    void onCurrentSizeChanged(DownloadWrapper downloadWrapper , double downLen , long speed);

    void onDownLoadSuccess(DownloadWrapper downloadWrapper);

    void onDownloadPause(DownloadWrapper downloadWrapper);

    void onDownloadError(DownloadWrapper downloadWrapper, int i, String s);
}
