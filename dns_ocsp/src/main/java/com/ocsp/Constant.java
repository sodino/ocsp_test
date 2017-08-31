package com.ocsp;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by sodino on 2017/5/11.
 */

public class Constant {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd_HH:mm:ss.SSSS");
    public static final String LINE_DIVIDE = "---------";
    public static final int MSG_DONE = 1;
    public static final int MSG_REFRESH = 2;

    public static final String IP_normal = "42.62.69.168";
    public static final String IP_ocsp = "42.62.69.169";

    public static int LOOP_COUNT = 2;
    public static final String URL_normal = "https://beizimu.com/";
    public static final String URL_ocsp = "https://varycloud.com/";
    public static final String HOST_normal = "beizimu.com";
    public static final String HOST_ocsp = "varycloud.com";

    public static final String URL_meipai = "https://api.meipai.com/";
    public static final String IP_meipai_normal = "27.148.145.195";
    public static final String IP_meipai_ocsp = "106.122.254.22";

   public static final int URL_RESP_CODE_meipai = HttpURLConnection.HTTP_BAD_REQUEST;
    public static final int URL_RESP_CODE = HttpURLConnection.HTTP_OK;
//
    public static final String LOG_FOLDER = "Test";

    public static final String LOG_NORMAL_HOST = "normal.log";
    public static final String LOG_OCSP_HOST = "ocsp.log";
}
