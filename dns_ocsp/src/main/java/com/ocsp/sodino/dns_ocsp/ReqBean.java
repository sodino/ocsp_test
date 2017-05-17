package com.ocsp.sodino.dns_ocsp;

import java.util.Date;

/**
 * Created by sodino on 2017/5/11.
 */

public class ReqBean {
    long start;
    long consume;

    boolean reqOCSPHost;

    public ReqBean(boolean isOcsp, long start, long consume) {
        this.reqOCSPHost = isOcsp;
        this.start = start;
        this.consume = consume;
    }

    public String toString() {
        return (reqOCSPHost ? "normal" : "OCSP_Host") + "start " + Constant.DATE_FORMAT.format(new Date(start)) + " consume=" + consume;
    }
}
