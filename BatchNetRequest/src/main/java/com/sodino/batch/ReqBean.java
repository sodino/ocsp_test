package com.sodino.batch;

import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import java.util.LinkedList;

/**
 * Created by sodino on 2017/8/30.
 */

public class ReqBean {
    public String name;
    public String url;
    public String ip;

    public String host;

    public int countSuccess;
    public int countFailed;
    public int countTry;
//    public long timeAll;
//    public long timeRespCode;

    public long sumRespCode = 0;
    public long sumTimeAll = 0;
    public long maxRespCode = 0;
    public long maxTimeAll = 0;
    public long minRespCode = Long.MAX_VALUE;
    public long minTimeAll = Long.MAX_VALUE;
    public LinkedList<Pair<Long, Long>> linkedList = new LinkedList<>();


    public ReqBean(String name, String url, String ip) {
        this.name = name;
        this.url = url;

        Uri uri = Uri.parse(url);
        host = uri.getHost();

        this.ip = ip;
    }

    public void addLogTime(long timeRespCode, long timeAll) {
        linkedList.add(new Pair<Long, Long>(timeRespCode, timeAll));
        sumRespCode += timeRespCode;
        sumTimeAll += timeAll;

        if (maxRespCode < timeRespCode) {
            maxRespCode = timeRespCode;
        }
        if (maxTimeAll < timeAll) {
            maxTimeAll = timeAll;
        }

        if (minRespCode > timeRespCode) {
            minRespCode = timeRespCode;
        }
        if (minTimeAll > timeAll) {
            minTimeAll = timeAll;
        }
    }

    public void printInfo() {
        int size = linkedList.size();
        Log.d("NetTest", "name:" + name + " try:" + countTry + " success:" + countSuccess + " fail:" + countFailed + " host:" + host + " ip:" + ip + " url:" + url);
        Log.d("NetTest", "time RespCode avg:" + (sumRespCode *1.0d/ size) + " max=" + maxRespCode + " min=" + minRespCode);
        Log.d("NetTest", "time All avg:" + (sumTimeAll * 1.0d / size) + " max=" + maxTimeAll + " min=" + minTimeAll);
        Log.d("NetTest", "idx \t rsp \t read \t all");
        for (int i = 0; i < size; i ++) {
            Pair<Long, Long> pair = linkedList.get(i);
            Log.d("NetTest", i + " \t" + pair.first + " \t" + (pair.second - pair.first) + " \t" + pair.second);
        }
    }

    public void clear() {
        sumTimeAll = sumRespCode = maxRespCode = maxTimeAll = 0;
        minRespCode = minTimeAll = Long.MAX_VALUE;

        countFailed = countSuccess = countTry = 0;

        linkedList.clear();
    }
}
