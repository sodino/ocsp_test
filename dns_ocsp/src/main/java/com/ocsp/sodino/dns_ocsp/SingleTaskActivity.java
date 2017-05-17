package com.ocsp.sodino.dns_ocsp;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseLongArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SingleTaskActivity extends AppCompatActivity implements Dns ,View.OnClickListener, Handler.Callback {

    private OkHttpClient client;

    private SparseLongArray arr = new SparseLongArray();
    private long max, min, sum;
    private float average;
    private TextView txtInfo;
    private Button btnStart, btnStop;
    private CheckBox checkBox;
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

        checkBox = (CheckBox) findViewById(R.id.checkBox);

        txtInfo = (TextView) findViewById(R.id.txtInfo);


        initOkHttpClient();
    }

    private void initOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.dns(this);
        client = builder.build();
    }

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
                    i ++;
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
        Request request = new Request.Builder().url(Constant.URL).build();
        Response response = null;
        try {
            Call call = client.newCall(request);

            long timeStart = System.currentTimeMillis();
            response = call.execute();
            long consume = System.currentTimeMillis() - timeStart;
            if (response.code() == HttpURLConnection.HTTP_BAD_REQUEST){
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
            if (response != null){
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
        switch(msg.what){
            case Constant.MSG_DONE:{
                going = false;
                btnStop.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);

                StringBuilder sb = new StringBuilder();

                sb.append(checkBox.isChecked() ? "OCSP " + Constant.HOST_ocsp_IP : Constant.HOST_IP);
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
            case Constant.MSG_REFRESH:{
                handler.removeMessages(Constant.MSG_REFRESH);
                if (!going) {
                    // 任务结束就不再显示进度了
                    return true;
                }
                StringBuilder sb = new StringBuilder();

                sb.append("going ... \n");
                sb.append(checkBox.isChecked() ? "OCSP " + Constant.HOST_ocsp_IP : Constant.HOST_IP);
                sb.append("\nidx:" + (arr.size() + 1) + " count:"+Constant.LOOP_COUNT);
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
            }break;
        }
        return true;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if ("api.meipai.com".equals(hostname)) {
            String ip = checkBox.isChecked() ? Constant.HOST_ocsp_IP : Constant.HOST_IP;

            List<InetAddress> inetAddresses =
                    Arrays.asList(InetAddress.getAllByName(ip));
            return inetAddresses;
        } else {
            return SYSTEM.lookup(hostname);
        }
    }
}
