package com.varycloud;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BatchTaskActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback {

    private OkHttpClient client;

    private TextView txtInfo;
    private Button btnStart, btnStop;
    private boolean going;

    private Task taskNormal = new Task();

    private Handler handler = new Handler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);

        setContentView(R.layout.activity_mix);


        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);

        btnStart.setText("Start(" + Constant.LOOP_COUNT + ")");

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);

        txtInfo = (TextView) findViewById(R.id.txtInfo);

        initOkHttpClient();

    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void initOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        client = builder.build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart: {
                btnStart.setVisibility(View.GONE);
                btnStop.setVisibility(View.VISIBLE);
                going = true;
                taskNormal.clear();
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
                    try {
                        Thread.sleep(100*3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                handler.sendEmptyMessage(Constant.MSG_DONE);
                Log.d("Test", "end looping...");
            }
        }.start();
    }

    private boolean runReq() {
        Request request = new Request.Builder().url(Constant.URL).addHeader("Connection", "Close").build();
        Response response = null;
        try {
            Call call = client.newCall(request);

            long timeStart = System.currentTimeMillis();
            response = call.execute();
            long consume = System.currentTimeMillis() - timeStart;
            if (response.code() == Constant.URL_RESP_CODE){
                //String str = response.body().string();

                StringBuilder sb = new StringBuilder();
                //Log.d("Test", "consume=" + consume);
                String logName = Constant.LOG_NORMAL_HOST;
                taskNormal.addBean(new ReqBean(false, timeStart, consume));
                sb.append(taskNormal.list.size()).append("   ")
                        .append(taskNormal.average).append("   ");

                sb.append(Constant.DATE_FORMAT.format(new Date(timeStart))).append("   ")
                        .append(consume);

                String line = sb.toString();
                FileUtil.write(line, Constant.LOG_FOLDER, logName);
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


    @Override
    public boolean handleMessage(Message msg) {
        switch(msg.what){
            case Constant.MSG_DONE:{
                going = false;
                btnStop.setVisibility(View.GONE);
                btnStart.setVisibility(View.VISIBLE);

                StringBuilder sb = new StringBuilder();

                sb.append("normal host").append("\t").append(taskNormal.list.size()).append("\n")
                        .append(" sum:").append(taskNormal.sum)
                        .append(" avg:").append(taskNormal.average)
                        .append(" min:").append(taskNormal.getMinConsume())
                        .append(" max:").append(taskNormal.getMaxConsume());
                sb.append("\n");
                sb.append("\n");


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

                sb.append("going....\n");
                sb.append("normal  \n")
                        //.append(" sum:").append(taskNormal.sum)
                        .append("idx:" + taskNormal.list.size())
                        .append(" avg:").append(taskNormal.average)
                        .append(" min:").append(taskNormal.getMinConsume())
                        .append(" max:").append(taskNormal.getMaxConsume());
                sb.append("\n");
                sb.append("\n").append(Constant.LINE_DIVIDE).append("\n");

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

                handler.sendEmptyMessageDelayed(Constant.MSG_REFRESH, 1000);
            }break;
        }
        return true;
    }
}
