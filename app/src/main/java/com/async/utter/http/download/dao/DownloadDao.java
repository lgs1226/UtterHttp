package com.async.utter.http.download.dao;

import com.async.utter.http.db.dao.BaseDao;
import com.async.utter.http.download.enums.DownloadStatus;
import com.async.utter.http.download.DownloadWrapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/9/11.
 */

public class DownloadDao extends BaseDao<DownloadWrapper>{

    /**
     * 保存应该下载的集合
     * 不包括已经下载成功的
     */
    private List<DownloadWrapper> downloadItemInfoList=
            Collections.synchronizedList(new ArrayList<DownloadWrapper>());


    /**
     *根据下载地址和下载路径查询下载记录
     */
    public DownloadWrapper findRecord(String url , String filePath){
        synchronized (DownloadDao.class){
            for (DownloadWrapper downloadWrapper:downloadItemInfoList) {
                if (downloadWrapper.getUrl().equals(url) && downloadWrapper.getFilePath().equals(filePath)){
                    return downloadWrapper;
                }
            }
        }
        DownloadWrapper downloadWrapper = new DownloadWrapper(url , filePath);
        ArrayList<DownloadWrapper> query = this.query(downloadWrapper);
        if (query.size() > 0){
            return query.get(0);
        }
        return null;
    }

    /**
     *根据下载路径查找下载记录
     */
    public ArrayList<DownloadWrapper> findRecord(String filePath){
        synchronized (DownloadDao.class){
            DownloadWrapper downloadWrapper = new DownloadWrapper();
            downloadWrapper.setFilePath(filePath);
            ArrayList<DownloadWrapper> query = this.query(downloadWrapper);
            return query;
        }
    }

    /**
     * 添加下载记录
     */
    public DownloadWrapper addRecord(String url , String filePath , String fileName , int priority){
        synchronized (DownloadDao.class){
            DownloadWrapper downloadWrapper = findRecord(url , filePath);
            if (downloadWrapper == null){
                downloadWrapper = new DownloadWrapper();
                downloadWrapper.setUrl(url);
                downloadWrapper.setFilePath(filePath);
                downloadWrapper.setDisplayName(fileName);
                downloadWrapper.setPriority(priority);
                downloadWrapper.setCurrentLength(0L);
                downloadWrapper.setTotalLength(0L);
                downloadWrapper.setStatus(DownloadStatus.waitting.getValue());
                downloadWrapper.setStartTime(new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").format(new Date()));
                downloadWrapper.setFinishTime("0");
                downloadWrapper.setPriority(priority);
                super.insert(downloadWrapper);
                //需要把下载ID查询来
                downloadWrapper = super.query(downloadWrapper).get(0);
                downloadItemInfoList.add(downloadWrapper);
                return downloadWrapper;
            }
            return null;
        }
    }

    public int upadateRecord(DownloadWrapper downloadWrapper){
        DownloadWrapper downloadWrapper1 = new DownloadWrapper();
        downloadWrapper1.setId(downloadWrapper.getId());
        int result = 0;
        synchronized (DownloadDao.class){
            result = super.update(downloadWrapper , downloadWrapper1);
            if (result > 0){
                for (int i = 0; i < downloadItemInfoList.size(); i++) {
                    if (downloadItemInfoList.get(i).getId() == downloadWrapper.getId()){
                        downloadItemInfoList.set(i , downloadWrapper);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public boolean removeRecorFromMemory(int downId){
        synchronized (DownloadDao.class){
            for (int i = 0; i < downloadItemInfoList.size(); i++) {
                if (downloadItemInfoList.get(i).getId() == downId){
                    downloadItemInfoList.remove(downloadItemInfoList);
                    break;
                }
            }
            return true;
        }
    }

    public DownloadWrapper findSigleRecord(String filePath){
        ArrayList<DownloadWrapper> record = findRecord(filePath);
        if (record.isEmpty()){
            return null;
        }
        return record.get(0);
    }

    public DownloadWrapper findRecordById(int downId){
        synchronized (DownloadDao.class){
            for (DownloadWrapper down:downloadItemInfoList) {
                if (down.getId() == downId){
                    return down;
                }
            }
            DownloadWrapper downloadWrapper = new DownloadWrapper();
            downloadWrapper.setId(downId);
            ArrayList<DownloadWrapper> query = super.query(downloadWrapper);
            if (query.size() > 0){
                return query.get(0);
            }
            return null;
        }

    }
}
