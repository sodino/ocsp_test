package com.varycloud;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;

/**
 * Created by sodino on 2017/5/11.
 */

public class Constant {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd_HH:mm:ss.SSSS");
    public static final String LINE_DIVIDE = "---------";
    public static final int MSG_DONE = 1;
    public static final int MSG_REFRESH = 2;

    public static final String HOST_IP = "106.122.254.22";
    public static final String HOST_ocsp_IP = "27.148.145.195";

    public static int LOOP_COUNT = 2000;
//    public static final String URL = "https://api.meipai.com/";
//    public static final int URL_RESP_CODE = HttpURLConnection.HTTP_BAD_REQUEST;
    public static final String URL = "https://varycloud.com/";
    public static final int URL_RESP_CODE = HttpURLConnection.HTTP_OK;

    public static final String LOG_FOLDER = "Test/varycloud";

    public static final String LOG_NORMAL_HOST = "varycloud.log";
}
