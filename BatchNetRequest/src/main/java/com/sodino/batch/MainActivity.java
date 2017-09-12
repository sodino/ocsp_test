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
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Dns {

    private static final int COUNT = 10;

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
        editIP = (EditText) findViewById(R.id.editIP);

//        editName.setText("http2");
//        editUrl.setText("https://www.beizimu.com/index.html");
//        editIP.setText("120.24.48.119");

//        editName.setText("https");
//        editUrl.setText("https://www.beizimu.com:444/index.html");
//        editIP.setText("120.24.48.119");

//        editName.setText("http.set.Connection.Close");
//        editUrl.setText("http://120.24.48.119/index.html");
//        editIP.setText("120.24.48.119");

//        editName.setText("http.NO.Connection.Close");
//        editUrl.setText("http://120.24.48.119/index.html");
//        editIP.setText("120.24.48.119");

//        editName.setText("HTTPS.set.Connection.Close.and.NO.Session.Resumption");
//        editUrl.setText("https://www.beizimu.com:440/index.html");
//        editIP.setText("120.24.48.119");

//        editName.setText("HTTPS.NO.Connection.Close.and.Session.Resumption");
//        editUrl.setText("https://www.beizimu.com:441/index.html");
//        editIP.setText("120.24.48.119");

//        editName.setText("HTTP2.set.Connection.Close.and.NO.Session.Resumption");
//        editUrl.setText("https://www.beizimu.com:442/index.html");
//        editIP.setText("120.24.48.119");

        editName.setText("HTTP2.NO.Connection.Close.and.Session.Resumption");
        editUrl.setText("https://www.beizimu.com:443/index.html");
        editIP.setText("120.24.48.119");


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

    ////////////////////////////////////////////////////////////////
    private MyTrustManager mMyTrustManager;

    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            mMyTrustManager = new MyTrustManager();
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{mMyTrustManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return ssfFactory;
    }

    //实现X509TrustManager接口
    public class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    //实现HostnameVerifier接口
    private class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    ////////////////////////////////////////////////////////////////




    private void initOKHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.dns(this);

        builder.sslSocketFactory(createSSLSocketFactory(), mMyTrustManager)
                .hostnameVerifier(new TrustAllHostnameVerifier());

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

                showReqTask();
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
                if (running == false) {
                    return;
                }
                running = false;
                printTaskInfo();

                editName.setEnabled(true);
                editUrl.setEnabled(true);
                editIP.setEnabled(true);

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
//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }


        if (running) {
            printTaskInfo();
        }

        running = false;
    }

    private void printTaskInfo() {
        final StringBuilder sb = new StringBuilder();
        for (ReqBean bean : list) {
            String str = bean.printInfo();
            sb.append(str);

            bean.clear();
        }

        txtInfo.post(new Runnable() {
            @Override
            public void run() {
                txtInfo.setText(sb.toString());
                editName.setEnabled(true);
                editUrl.setEnabled(true);
                editIP.setEnabled(true);

                btnAdd.setEnabled(true);
                btnRun.setEnabled(true);
                btnStop.setEnabled(false);

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
            Log.d("NetTest", "Task : " + reqCurrentBean.name + " success=" + reqCurrentBean.countSuccess + " failed=" + reqCurrentBean.countFailed + " all=" + reqCurrentBean.countTry);
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

        if (beanMinTry != null) {
            Log.d("NetTest", "find " + beanMinTry.name + " try=" + beanMinTry.countTry
                    + " success=" + beanMinTry.countSuccess + " failed=" + beanMinTry.countFailed);
        } else {
            Log.d("NetTest", "find null");
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
