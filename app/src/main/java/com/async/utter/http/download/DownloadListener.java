package com.async.utter.http.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.async.utter.http.download.interfaces.IDownListener;
import com.async.utter.http.download.interfaces.IDownloadServiceCallable;
import com.async.utter.http.interfaces.IHttpService;

import org.apache.http.HttpEntity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/9/4.
 */

public class DownloadListener implements IDownListener {

    private IHttpService httpService;

    private DownloadWrapper downloadWrapper;

    private File file;

    private String url;

    private long breakPoint;

    private IDownloadServiceCallable downloadServiceCallable;


    private final static int DOWNLOAD_TOTAL_LEN_RECEIVE = 1;

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {

        }
    };

    public DownloadListener(IHttpService httpService, DownloadWrapper downloadWrapper, IDownloadServiceCallable downloadServiceCallable) {
        this.httpService = httpService;
        this.downloadWrapper = downloadWrapper;
        this.downloadServiceCallable = downloadServiceCallable;
        this.file = new File(downloadWrapper.getFilePath());
        this.breakPoint = file.length();
    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        Log.e("---------" , "进入该方法");
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long startTime = System.currentTimeMillis();
        //用于计算每秒多少k
        long speed = 0L;
        //花费时间
        long userTime = 0L;
        //下载长度
        long getLen = 0L;
        //接收的长度
        long receiveLen = 0L;
        boolean bufferLen = false;
        //得到下载的长度
        long dataLength = httpEntity.getContentLength();
        //单位时间下载的字节数
        long calcSpeedLen = 0L;
        //总数
        long totalLenth = this.breakPoint + dataLength;
        //更新数量
        this.receiveTotalLenth(totalLenth);
        //更新状态
        this.downloadStatusChange(DownloadStatus.downloading);
        byte[] bytes = new byte[1024];
        int count = 0;
        long currentTime = System.currentTimeMillis();
        BufferedOutputStream boStream = null;
        FileOutputStream foStream = null;
        if (!makeDir(this.getFile().getParentFile())){
            downloadServiceCallable.onDownloadError(downloadWrapper , 1 , "创建文件夹失败！");
        }else {
            try {
                foStream = new FileOutputStream(this.getFile() , true);
                boStream = new BufferedOutputStream(foStream);
                int length = 1;
                while ((length = inputStream.read(bytes)) != -1){
                    if (this.getHttpService().isCancle()){
                        downloadServiceCallable.onDownloadError(downloadWrapper , 1 , "被取消了");
                        return;
                    }
                    if (this.getHttpService().isPause()){
                        downloadServiceCallable.onDownloadError(downloadWrapper , 1 , "暂停");
                    }
                    boStream.write(bytes , 0 , length);
                    getLen += length;
                    receiveLen += length;
                    calcSpeedLen += length;
                    ++count;
                    if (receiveLen * 10 / totalLenth >= 1L || count >= 5000){
                        currentTime = System.currentTimeMillis();
                        userTime = currentTime - startTime;
                        startTime = currentTime;
                        speed = 1000L * calcSpeedLen / userTime;
                        count = 0;
                        calcSpeedLen = 0L;
                        receiveLen = 0L;
                        this.downloadLengthChange(this.breakPoint+getLen , totalLenth , speed);
                    }
                }
                boStream.close();
                foStream.close();
                if (dataLength != getLen){
                    downloadServiceCallable.onDownloadError(downloadWrapper , 3 , "下载长度不相等");
                }else {
                    this.downloadLengthChange(this.breakPoint + getLen , totalLenth , speed);
                    downloadServiceCallable.onDownLoadSuccess(downloadWrapper);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    boStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    foStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     *
     */
    private void downloadLengthChange(final long downLength , final long totalLength , final long speed){
        Log.e("---------------" , "下载长度:"+downLength+",总长度:"+totalLength+",下载速度:"+speed);
        downloadWrapper.setCurrentLength(downLength);
        if (downloadServiceCallable != null){
            final DownloadWrapper downloadWrapper1 = downloadWrapper.copy();
            synchronized (this.downloadServiceCallable){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadServiceCallable.onCurrentSizeChanged(downloadWrapper1 , downLength / totalLength, speed);
                    }
                });
            }
        }
    }

    private boolean makeDir(File parentFile){
        return parentFile.exists() && !parentFile.isFile() ? parentFile.exists() && parentFile.isDirectory() : parentFile.mkdirs();
    }

    /*
     * 更新下载时的状态
     */
    private void downloadStatusChange(DownloadStatus downloadStatus){
        downloadWrapper.setStatus(downloadStatus.getValue());
        final DownloadWrapper copyDownloadWrapper = downloadWrapper.copy();
        if (downloadServiceCallable != null){
            synchronized (this.downloadServiceCallable){
                synchronized (this.downloadServiceCallable){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadServiceCallable.onDownloadStatusChanged(copyDownloadWrapper);
                        }
                    });
                }
            }
        }
    }

    /*
     * 回调长度的变化
     */
    private void receiveTotalLenth(long totalLength){
        downloadWrapper.setTotalLength(totalLength);
        final DownloadWrapper downloadWrapper = this.downloadWrapper.copy();
        if (downloadServiceCallable != null){
            synchronized (this.downloadServiceCallable){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadServiceCallable.onTotalLenthReceived(downloadWrapper);
                    }
                });
            }
        }
    }

    @Override
    public void onFailure(String e) {

    }

    @Override
    public void setHttpService(IHttpService httpService) {
        this.httpService = httpService;
    }

    @Override
    public void setCancleCall() {

    }

    @Override
    public void setPauseCall() {

    }

    public IHttpService getHttpService() {
        return httpService;
    }

    public File getFile() {
        return file;
    }
}
