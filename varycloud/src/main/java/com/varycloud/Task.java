package com.varycloud;

import java.util.LinkedList;

/**
 * Created by sodino on 2017/5/11.
 */

public class Task {
    LinkedList<ReqBean> list = new LinkedList<>();

    float average;
    long sum;
    ReqBean beanMin;
    ReqBean beanMax;

    public void addBean(ReqBean bean){
        list.add(bean);
        sum += bean.consume;

        average = sum * 1.0f / list.size();
        if (beanMax == null) {
            beanMax = bean;
        } else if (beanMax.consume < bean.consume) {
            beanMax = bean;
        }

        if (beanMin == null) {
            beanMin = bean;
        } else if (beanMin.consume > bean.consume) {
            beanMin = bean;
        }
    }

    public void clear() {
        list.clear();
        average = 0.0f;
        sum = 0;
        beanMin = beanMax = null;
    }

    public long getMinConsume() {
        return beanMin == null ? 0 : beanMin.consume;
    }

    public long getMaxConsume() {
        return beanMax == null ? 0 : beanMax.consume;
    }
}
