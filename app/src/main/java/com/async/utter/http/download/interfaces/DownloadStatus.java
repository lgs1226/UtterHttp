package com.async.utter.http.download.interfaces;

/**
 * Created by Administrator on 2017/9/4.
 */

public enum DownloadStatus {

    waitting(0),

    starting(1),

    downloading(2),

    pause(3),

    finish(4),

    failed(5);

    private int value;
    DownloadStatus(int status) {
        this.value = status;
    }

    public int getValue() {
        return value;
    }
}
