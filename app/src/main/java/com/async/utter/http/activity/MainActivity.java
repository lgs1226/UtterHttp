package com.async.utter.http.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.async.utter.http.R;
import com.async.utter.http.db.dao.BaseDaoFactory;
import com.async.utter.http.download.DownFileManager;
import com.async.utter.http.download.DownloadWrapper;
import com.async.utter.http.download.dao.DownloadDao;
import com.async.utter.http.download.enums.DownloadStatus;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String url = "http://fanyi.youdao.com/openapi.do?keyfrom=Yanzhikai&key=2032414398&type=data&doctype=json&version=1.1&q=car";
    private TextView tv_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_test = (TextView) findViewById(R.id.tv_test);
    }

    public void post(View view){
//        UtterHttpClient.post(url, null, TestBean.class, new IDataListener<TestBean>() {
//            @Override
//            public void onSuccess(TestBean entity) {
//                tv_test.setText(entity.toString());
//            }
//
//            @Override
//            public void onFailure(String e) {
//                Log.e("=============" , e);
//            }
//        });
        DownFileManager manager = new DownFileManager();
        manager.download("http://gdown.baidu.com/data/wisegame/8be18d2c0dc8a9c9/WPSOffice_177.apk" , null);
    }
}
