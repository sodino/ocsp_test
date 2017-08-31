package com.ocsp;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseLongArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ocsp.sodino.dns_ocsp.R;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SingleTaskActivity extends AppCompatActivity implements Dns, View.OnClickListener, Handler.Callback {

    private OkHttpClient client;

    private SparseLongArray arr = new SparseLongArray();
    private long max, min, sum;
    private float average;
    private TextView txtInfo;
    private Button btnStart, btnStop;
    private boolean going;
    private LinkedList<String> listResult = new LinkedList<>();

    private Handler handler = new Handler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);

        btnStart.setText("Start(" + Constant.LOOP_COUNT + ")");

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);

        txtInfo = (TextView) findViewById(R.id.txtInfo);

        initOkHttpClient();
    }

    private void initOkHttpClient() {

        final X509TrustManager trustManager = new X509TrustManager() {
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
        };

        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.dns(this);
        builder.sslSocketFactory(sslSocketFactory, trustManager);

        client = builder.build();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart: {
                btnStart.setVisibility(View.GONE);
                btnStop.setVisibility(View.VISIBLE);
                going = true;
                sum = max = min = 0;
                average = 0.0f;
                arr.clear();
                reqGetUrl();

                handler.sendEmptyMessageDelayed(Constant.MSG_REFRESH, 1000);
            }
            break;
            case R.id.btnStop: {
                going = false;
                btnStop.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);
            }
            break;
        }
    }

    private void reqGetUrl() {
        new Thread() {
            @Override
            public void run() {
                int i = 0;
                Log.d("Test", "start looping...");
                while (i < Constant.LOOP_COUNT && going) {
                    i++;
                    boolean result = runReq();
                    if (!result) {
                        i--;
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(Constant.MSG_DONE);
                Log.d("Test", "end looping...");
            }
        }.start();
    }

    private boolean runReq() {
//        Request request = new Request.Builder().url(Constant.URL_normal)
        Request request = new Request.Builder().url("https://g.alicdn.com/s.gif")
                .addHeader("Connection", "Close")
                .build();
        Response response = null;
        try {
            Call call = client.newCall(request);

            long timeStart = System.currentTimeMillis();
            response = call.execute();
            long consume = System.currentTimeMillis() - timeStart;
            int code = response.code();

            String httpProtocol = String.valueOf(response.protocol());
            Log.d("Test", "code=" + code + " httpProtocol=" + httpProtocol);
            if (code == Constant.URL_RESP_CODE) {
                //String str = response.body().string();

                //Log.d("Test", "consume=" + consume);
                calcTime(consume);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private void calcTime(long consume) {
        arr.append(arr.size(), consume);
        if (consume > max) {
            max = consume;
        }

        if (consume < min || min == 0) {
            min = consume;
        }


        sum += consume;

        average = sum * 1.0f / arr.size();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.MSG_DONE: {
                going = false;
                btnStop.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);

                StringBuilder sb = new StringBuilder();

                sb.append("\nsum:" + sum);
                sb.append(" average:" + average);
                sb.append(" min:" + min + " max:" + max);
                sb.append("\n\n");


                String strOld = txtInfo.getText().toString();

                int index = strOld.indexOf(Constant.LINE_DIVIDE);
                if (index > 0) {
                    strOld = strOld.substring(index + Constant.LINE_DIVIDE.length() + 1);
                }

                if (strOld.length() > 2000) {
                    strOld = strOld.substring(0, 2000);
                }

                txtInfo.setText(sb.toString());
                txtInfo.append(strOld);
            }
            break;
            case Constant.MSG_REFRESH: {
                handler.removeMessages(Constant.MSG_REFRESH);
                if (!going) {
                    // 任务结束就不再显示进度了
                    return true;
                }
                StringBuilder sb = new StringBuilder();

                sb.append("going ... \n");
                sb.append("\nidx:" + (arr.size() + 1) + " count:" + Constant.LOOP_COUNT);
                sb.append("\nsum:" + sum);
                sb.append("\naverage:" + average);
                sb.append("\nmin:" + min + " max:" + max);
                sb.append("\n" + Constant.LINE_DIVIDE + "\n");

                String strOld = txtInfo.getText().toString();


                int index = strOld.indexOf(Constant.LINE_DIVIDE);
                if (index > 0) {
                    strOld = strOld.substring(index + Constant.LINE_DIVIDE.length() + 1);
                }

                if (strOld.length() > 2000) {
                    strOld = strOld.substring(0, 2000);
                }

                txtInfo.setText(sb.toString());
                txtInfo.setText(sb.toString());

                txtInfo.append(strOld);


                handler.sendEmptyMessageDelayed(Constant.MSG_REFRESH, 1000);
            }
            break;
        }
        return true;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if ("beizimu.com".equals(hostname)) {
            String ip = "120.24.48.119";

            List<InetAddress> inetAddresses =
                    Arrays.asList(InetAddress.getAllByName(ip));
            return inetAddresses;
        } else {
            return SYSTEM.lookup(hostname);
        }
    }

}
