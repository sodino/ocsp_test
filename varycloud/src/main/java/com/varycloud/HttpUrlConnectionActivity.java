package com.varycloud;

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
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class HttpUrlConnectionActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback {

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
//        Request request = new Request.Builder().url(Constant.URL).addHeader("Connection", "Close").build();
//        Response response = null;
//        try {
//            Call call = client.newCall(request);
//
//            long timeStart = System.currentTimeMillis();
//            response = call.execute();
//            long consume = System.currentTimeMillis() - timeStart;
//            if (response.code() == Constant.URL_RESP_CODE) {
//                //String str = response.body().string();
//
//                //Log.d("Test", "consume=" + consume);
//                calcTime(consume);
//                return true;
//            } else {
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            if (response != null){
//                response.close();
//            }
//        }


        URL url = null;
        HttpsURLConnection httpConnection = null;
        try {
            url = new URL(Constant.URL);
            httpConnection = (HttpsURLConnection) url.openConnection();
            httpConnection.addRequestProperty("Connection", "Close");

            long timeStart = System.currentTimeMillis();
            httpConnection.connect();
            int rspCode = httpConnection.getResponseCode();
            long consume = System.currentTimeMillis() - timeStart;
            if (rspCode == Constant.URL_RESP_CODE) {
                calcTime(consume);
                return true;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }

        return false;
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
}
