package com.jins.aimentor.common;

import com.jins.aimentor.constants.Status;
import lombok.Data;

/**
 * 接口返回对象封装 - Result
 */
@Data
public class Result<T> {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private String code;

    /**
     * 状态消息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    /**
     * 私有无参构造方法
     */
    public Result() {}

    /**
     * 私有全参构造方法
     */
    private Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return Result对象，code=200，msg=success
     */
    public static <T> Result success() {
        return new Result<>(Status.CODE_200, "success", null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Result对象，code=200，msg=success，包含data
     */
    public static <T> Result<T> success(T data) {
        return new Result(Status.CODE_200, "success", data);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param msg 自定义消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(Status.CODE_200, msg, data);
    }

    /**
     * 错误响应
     *
     * @return
     * @param <T>
     */
    public static <T> Result<T> error() {
        return new Result<>(Status.CODE_500, null, null);
    }

    /**
     * 错误响应
     *
     * @param msg
     * @return
     */
    public static <T> Result<T> error(String msg) {
        return new Result(Status.CODE_500, msg, null);
    }

    /**
     * 错误响应
     *
     * @param code 错误码
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(String code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 错误响应（带数据）
     *
     * @param code 错误码
     * @param msg 错误消息
     * @param data 错误相关数据
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(String code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public Result<T> code(String code) {
        this.code = code;
        return this;
    }

    public Result<T> msg(String msg) {
        this.msg = msg;
        return this;
    }
}
