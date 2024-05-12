package com.xuecheng.base.model;

import java.io.Serializable;

public class Result implements Serializable {

    private Integer code;
    private String msg;


    public Result(String msg) {
        this.msg = msg;
    }

    public Result(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public int getErrCode() {
        return code;
    }

    public void setErrCode(int code) {
        this.code = code;
    }

    public String getErrMessage() {
        return msg;
    }

    public void setErrMessage(String msg) {
        this.msg = msg;}


    }
