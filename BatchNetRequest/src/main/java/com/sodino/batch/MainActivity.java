package com.sodino.batch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Dns {

    private static final int COUNT = 1000;

    private EditText editName, editUrl, editIP;
    private Button btnAdd, btnRun, btnStop;
    private TextView txtInfo;

    private LinkedList<ReqBean> list = new LinkedList<>();

    private ReqBean reqCurrentBean = null;

    private boolean running = false;

    private OkHttpClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editName = (EditText) findViewById(R.id.editName);
        editUrl = (EditText) findViewById(R.id.editUrl);
        editUrl.setText("https://api.meipai.com/hot/feed_timeline.json?page=7&guid=acb36d064aaad40284e786cd13e7ec08&language=zh-Hans%20&client_id=1089857302&device_id=869540023395705&version=6300%20&channel=QQ&model=m2+note&os=5.1&origin_channel=QQ&locale=1%20&imei=869540023395705&mac=68%3A3e%3A34%3Aba%3Abe%3A0a%20&android_id=49f7033a2b5a6219&ab_codes=%5B0%5D%20&sig=e720de11552b94ca8100ed85e02692be&sigVersion=1.3&sigTime=1503478865115");
        editIP = (EditText) findViewById(R.id.editIP);
        editIP.setText("27.155.71.172");

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        btnRun = (Button) findViewById(R.id.btnRun);
        btnRun.setOnClickListener(this);
        btnRun.setText("Run(" + COUNT + ")");

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);

        txtInfo = (TextView) findViewById(R.id.txtInfo);

        initOKHttpClient();
    }

    private void initOKHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.dns(this);

        client = builder.build();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnAdd:{
                String name = editName.getText().toString();
                String url = editUrl.getText().toString();
                String ip = editIP.getText().toString();

                boolean bool = addTask(name, url, ip);

                if (bool) {
                    showReqTask();
                }
            }break;
            case R.id.btnRun:{
                editName.setEnabled(false);
                editUrl.setEnabled(false);
                editIP.setEnabled(false);

                btnAdd.setEnabled(false);
                btnRun.setEnabled(false);
                btnStop.setEnabled(true);

                new Thread() {
                    public void run() {
                        running = true;
                        startLoopTask();
                    }
                } .start();
            }break;
            case R.id.btnStop:{
                editName.setEnabled(true);
                editUrl.setEnabled(true);
                editIP.setEnabled(true);
                running = false;

                btnAdd.setEnabled(true);
                btnRun.setEnabled(true);
                btnStop.setEnabled(false);
            }break;
        }
    }

    private void startLoopTask() {
        while(running) {
            reqCurrentBean = findNextTask();
            if (reqCurrentBean == null) {
                break;
            }

            requestUrl(reqCurrentBean);
            reqCurrentBean = null;
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        running = false;

        for (ReqBean bean : list) {
            bean.printInfo();

            bean.clear();
        }


        txtInfo.post(new Runnable() {
            @Override
            public void run() {
                onClick(btnStop);
            }
        });
    }

    private void requestUrl(ReqBean bean) {
        Request request = new Request.Builder().url(bean.url)
                .addHeader("Connection", "Close")
                .build();
        Response response = null;
        try {
            Call call = client.newCall(request);

            long timeStart = System.currentTimeMillis();
            response = call.execute();
            int code = response.code();
            long timeRespCode = System.currentTimeMillis() - timeStart;
            long timeAll = 0;
            String httpProtocol = String.valueOf(response.protocol());
            String str = null;
            if (code == HttpURLConnection.HTTP_OK) {
                str = response.body().string();
                timeAll = System.currentTimeMillis() - timeStart;

                bean.addLogTime(timeRespCode, timeAll);
                bean.countSuccess ++;
            } else {
                bean.countFailed ++;
            }
            Log.d("NetTest", "Task : " + reqCurrentBean.name);
            Log.d("NetTest", "code=" + code + " httpProtocol=" + httpProtocol);
            Log.d("NetTest", "time respCode=" + timeRespCode  + " all=" + timeAll + " str=" + str);

            bean.countTry ++;
        } catch (Exception e) {
            e.printStackTrace();
            bean.countFailed ++;
            bean.countTry ++;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private ReqBean findNextTask() {
        ReqBean beanMinTry = null;

        for (ReqBean bean : list){
            if (bean.countSuccess == COUNT) {
                continue;
            }
            if (bean.countFailed >= COUNT) {
                continue;
            }

            if (beanMinTry == null) {
                beanMinTry = bean;
            } else if (beanMinTry.countTry > bean.countTry) {
                beanMinTry = bean;
            }
        }


        return beanMinTry;
    }

    private void showReqTask() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0;i < list.size();i ++) {
            ReqBean bean = list.get(i);
            sb.append(i).append(" ").append(bean.name). append(" ");
            sb.append(bean.url).append("\n");
            if (TextUtils.isEmpty(bean.ip) == false) {
                sb.append("dns:").append(bean.ip);
            }
            sb.append("\n");
        }


        txtInfo.setText(sb.toString());
    }

    private boolean addTask(String name, String url, String ip) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        if (URLUtil.isNetworkUrl(url) == false) {
            return false;
        }

        for (ReqBean bean : list) {
            if (bean.url.equals(url) && bean.ip.equals(ip)) {
                return false;
            }
        }

        ReqBean bean = new ReqBean(name, url, ip);
        list.add(bean);

        if (list.size() > 0) {
            btnRun.setEnabled(true);
        }

        return true;
    }


    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if (reqCurrentBean != null
                && hostname.equals(reqCurrentBean.host)) {
            String ip = reqCurrentBean.ip;


//            Log.d("NetTest", "hostname=" + hostname + " ip=" + ip);

            List<InetAddress> inetAddresses =
                    Arrays.asList(InetAddress.getAllByName(ip));
            return inetAddresses;
        } else {
            return SYSTEM.lookup(hostname);
        }
    }
}
