package com.async.utter.http.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.async.utter.http.RequestHolder;
import com.async.utter.http.ThreadPoolManager;
import com.async.utter.http.UtterHttpTask;
import com.async.utter.http.db.dao.BaseDaoFactory;
import com.async.utter.http.download.dao.DownloadDao;
import com.async.utter.http.download.enums.DownloadStopMode;
import com.async.utter.http.download.enums.Priority;
import com.async.utter.http.download.interfaces.IDownloadCallable;
import com.async.utter.http.download.interfaces.IDownloadServiceCallable;
import com.async.utter.http.interfaces.IHttpListener;
import com.async.utter.http.interfaces.IHttpService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;

/**
 * Created by Administrator on 2017/9/5.
 */

public class DownFileManager implements IDownloadServiceCallable {

    /**
     * 观察者模式
     */
    private final List<IDownloadCallable> applisteners = new CopyOnWriteArrayList<IDownloadCallable>();

    /**
     * 正在下载的所有任务
     */
    private static List<DownloadWrapper> downloadFileTaskList = new CopyOnWriteArrayList();

    private DownloadDao downloadDao = BaseDaoFactory.getInstance("download.db").getDataHelper(DownloadDao.class , DownloadWrapper.class);

    private final static String TAG = "DownFileManager";

    private Handler handler = new Handler(Looper.getMainLooper());
    public void down(String url){
        synchronized (DownFileManager.class){
            String[] preFixs=url.split("/");
            String afterFix=preFixs[preFixs.length-1];
            File file=new File(Environment.getExternalStorageDirectory(),afterFix);
            DownloadWrapper downloadWrapper = new DownloadWrapper(url , file.getAbsolutePath());
            RequestHolder requestHolder = new RequestHolder();
            IHttpService httpService = new FileDownloadServices();
            Map<String, String> httpHeadMap = httpService.getHttpHeadMap();
            IHttpListener httpListener=new DownloadListener(httpService ,downloadWrapper , this);
            requestHolder.setHttpService(httpService);
            requestHolder.setHttpListener(httpListener);
            requestHolder.setUrl(url);
            UtterHttpTask utterHttpTask = new UtterHttpTask(requestHolder);
            try {
                ThreadPoolManager.getInstance().execute(new FutureTask(utterHttpTask , null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public int download(String url){
        String[] preFix=url.split("/");
        return this.download(url , Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+preFix[preFix.length - 1]);
    }

    public int download(String url , String filePath){
        String[] preFix=url.split("/");
        String fileName=preFix[preFix.length-1];
        return this.download(url , filePath , fileName);
    }

    public int download(String url , String filePath , String fileName){
        return this.download(url , filePath , fileName , Priority.middle);
    }

    public int download(String url , String filePath , String fileName , Priority priority){
        if (priority == null){
            priority = Priority.low;
        }
        File file = new File(fileName);
        DownloadWrapper downloadWrapper = downloadDao.findRecord(url , filePath);
        //没有下载记录
        if (downloadWrapper == null){
            ArrayList<DownloadWrapper> record = downloadDao.findRecord(filePath);
            if (record.size() > 0){
                DownloadWrapper downloadWrapper1 = record.get(0);
                if (downloadWrapper.getCurrentLength() == downloadWrapper.getTotalLength()){
                    synchronized (applisteners){
                        for (IDownloadCallable iDownloadCallable:applisteners) {
                            iDownloadCallable.onDownloadError(downloadWrapper1.getId() , 2 , "文件已下载");
                        }
                    }
                }
            }
            downloadWrapper = downloadDao.addRecord(url, filePath, fileName, priority.getValue());
            if (downloadWrapper != null){
                synchronized (applisteners){
                    for (IDownloadCallable iDownloadCallable:applisteners) {
                        iDownloadCallable.onDownloadInfoAdd(1);
                    }
                }
            }
            downloadWrapper = downloadDao.findRecord(url , filePath);
            if (isDowning(file.getAbsolutePath())){
                synchronized (applisteners){
                    for (IDownloadCallable iDownloadCallable:applisteners) {
                        iDownloadCallable.onDownloadError(downloadWrapper.getId() , 4 , "正在下载中...");
                    }
                }
                return downloadWrapper.getId();
            }

            if (downloadWrapper != null){
                downloadWrapper.setPriority(priority.getValue());
                if (downloadWrapper.getStatus() != DownloadStatus.finish.getValue()){
                    if (downloadWrapper.getTotalLength() == 0L || file.length() == 0){
                        Log.e(TAG, "download: 还未开始下载");
                        downloadWrapper.setStatus(DownloadStatus.failed.getValue());
                    }
                    /**
                     * 判断数据库中长度是否等于文件长度
                     */
                    if (downloadWrapper.getTotalLength() == file.length() && downloadWrapper.getTotalLength() != 0){
                        downloadWrapper.setStatus(DownloadStatus.finish.getValue());
                        synchronized (applisteners){
                            for (IDownloadCallable iDownloadCallable:applisteners) {
                                iDownloadCallable.onDownloadError(downloadWrapper.getId() , 4 , "文件已下载");
                            }
                        }
                    }
                }
                downloadDao.upadateRecord(downloadWrapper);
            }
            /**
             * 判断是否已经下载完毕
             */
            if (downloadWrapper.getStatus() == DownloadStatus.finish.getValue()){
                Log.i(TAG, "download: 下载完成，准备回调应用层");
                final int downId = downloadWrapper.getId();
                synchronized (applisteners){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (IDownloadCallable iDownloadCallable : applisteners) {
                                iDownloadCallable.onDownloadSuccess(downId);
                            }
                        }
                    });
                }
                downloadDao.removeRecorFromMemory(downloadWrapper.getId());
                return downloadWrapper.getId();
            }
            //之前的下载，状态设置为暂停状态
            List<DownloadWrapper> allDown = downloadFileTaskList;
            if (priority != Priority.high){
                for (DownloadWrapper downing:allDown) {
                    //从下载表中  获取到全部正在下载的任务
                    downing = downloadDao.findSigleRecord(downing.getFilePath());
                    if (downloadWrapper != null && downloadWrapper.getPriority() == Priority.high.getValue()){
                        if (downloadWrapper.getFilePath().equals(downing.getFilePath())){
                            return downloadWrapper.getId();
                        }
                    }
                }
            }
            DownloadWrapper downloadWrapper1 = startDown(downloadWrapper);
            if (priority == Priority.high || priority == Priority.middle){
                synchronized (allDown){
                    for (DownloadWrapper down:allDown) {
                        if (!downloadWrapper.getFilePath().equals(down.getFilePath())){
                            DownloadWrapper sigleRecord = downloadDao.findSigleRecord(down.getFilePath());
                            if (sigleRecord != null){
                                pause(sigleRecord.getId() , DownloadStopMode.auto);
                            }
                        }
                    }
                }
                return downloadWrapper.getId();
            }
            return -1;
        }
        return -1;
    }

    public void pause(int downId , DownloadStopMode downloadStopMode){
        if (downloadStopMode == null){
            downloadStopMode = DownloadStopMode.auto;
        }
        DownloadWrapper download = downloadDao.findRecordById(downId);
        if (download != null){
            download.setStopMode(downloadStopMode.getValue());
            download.setStatus(DownloadStatus.pause.getValue());
            downloadDao.upadateRecord(download);

            for (DownloadWrapper downWrapper : downloadFileTaskList){
                if (downId == downWrapper.getId()){
                    downWrapper.getUtterHttpTask().pause();
                }
            }
        }
    }

    public DownloadWrapper startDown(DownloadWrapper downloadWrapper){
        Log.e("---------------" , downloadWrapper.getUrl());
        synchronized (DownFileManager.class){
            RequestHolder requestHolder = new RequestHolder();
            //请求下载
            IHttpService httpService = new FileDownloadServices();
            Map<String, String> map = httpService.getHttpHeadMap();
            IHttpListener httpListener = new DownloadListener(httpService , downloadWrapper , this);
            requestHolder.setHttpService(httpService);
            requestHolder.setHttpListener(httpListener);
            requestHolder.setUrl(downloadWrapper.getUrl());
            UtterHttpTask utterHttpTask = new UtterHttpTask(requestHolder);
            downloadWrapper.setUtterHttpTask(utterHttpTask);
            downloadFileTaskList.add(downloadWrapper);
            utterHttpTask.start();
        }
        return downloadWrapper;
    }

    private boolean isDowning(String filePath){
        for (DownloadWrapper downloadWrapper:downloadFileTaskList) {
            if (downloadWrapper.getFilePath().equals(filePath)){
                return true;
            }
        }
        return false;
    }
    @Override
    public void onDownloadStatusChanged(DownloadWrapper downloadWrapper) {

    }

    @Override
    public void onTotalLenthReceived(DownloadWrapper downloadWrapper) {

    }

    @Override
    public void onCurrentSizeChanged(DownloadWrapper downloadWrapper, double downLen, long speed) {
        Log.i(TAG,"下载速度："+ speed/1000 +"k/s");
        Log.i(TAG,"-----路径  "+ downloadWrapper.getFilePath()+"  下载长度  "+downLen+"   速度  "+speed);
    }

    @Override
    public void onDownLoadSuccess(DownloadWrapper downloadWrapper) {
        Log.i(TAG,"下载成功    路劲  "+ downloadWrapper.getFilePath()+"  url "+ downloadWrapper.getUrl());
    }

    @Override
    public void onDownloadPause(DownloadWrapper downloadWrapper) {

    }

    @Override
    public void onDownloadError(DownloadWrapper downloadWrapper, int i, String s) {

    }
}
