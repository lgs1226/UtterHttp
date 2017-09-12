package com.async.utter.http.download;

import com.async.utter.http.UtterHttpTask;
import com.async.utter.http.db.annotation.DbFiled;

/**
 * Created by Administrator on 2017/9/1.
 */

public class DownloadWrapper extends BaseEntity<DownloadWrapper>{

    private String url;
    private String filePath;



    public DownloadWrapper(String url, String filePath) {
        this.url = url;
        this.filePath = filePath;
    }

    public DownloadWrapper() {

    }

    /**
     * 当前下载进度
     */
    private long currentLength;

    /**
     * 下载文件大小
     */
    private long totalLength;


    /**
     * 下载id
     */
    @DbFiled("_id")
    public Integer id;

    /**
     * 下载文件显示名
     */
    public String displayName;

    /**
     * 下载开始时间
     */
    public String startTime;

    /**
     * 下载结束时间
     */
    public String finishTime;

    /**
     * 用户id
     */
    public String userId;

    /**
     * 下载任务类型
     */
    public String httpTaskType;

    /**
     * 下载优先级
     */
    public Integer priority;

    /**
     * 下载停止模式
     */
    public Integer stopMode;


    //下载的状态
    public Integer status;

    private transient UtterHttpTask utterHttpTask;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public UtterHttpTask getUtterHttpTask() {
        return utterHttpTask;
    }

    public void setUtterHttpTask(UtterHttpTask utterHttpTask) {
        this.utterHttpTask = utterHttpTask;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHttpTaskType() {
        return httpTaskType;
    }

    public void setHttpTaskType(String httpTaskType) {
        this.httpTaskType = httpTaskType;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getStopMode() {
        return stopMode;
    }

    public void setStopMode(Integer stopMode) {
        this.stopMode = stopMode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
