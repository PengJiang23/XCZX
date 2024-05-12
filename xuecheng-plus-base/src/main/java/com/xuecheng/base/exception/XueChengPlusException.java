package com.xuecheng.base.exception;

import java.util.Collections;

/**
 * @author Mr.M
 * @version 1.0
 * @description 本项目自定义异常类型
 * @date 2023/2/12 16:56
 */
public class XueChengPlusException extends RuntimeException {

    private String errMessage;
    private Integer errCode = 0;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;

    }

    public XueChengPlusException(String message, Integer errCode) {
        super(message);
        this.errMessage = message;
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }
    public Integer getErrorCode() {
        return errCode;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
    public void setErrorCode(Integer errCode) {
        this.errCode = errCode;
    }

    public static void cast(String message){
        throw new XueChengPlusException(message);
    }
    public static void cast(String message, Integer errCode){
        throw new XueChengPlusException(message, errCode);
    }
    public static void cast(CommonError error){
        throw new XueChengPlusException(error.getErrMessage());
    }

}
