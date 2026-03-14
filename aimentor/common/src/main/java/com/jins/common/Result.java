package com.jins.common;

import com.jins.constants.Status;
import lombok.Data;

/**
 * 接口返回对象封装 - Result
 */
@Data
public class Result<T> {

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

    public Result() {
    }

    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Result success() {
        return new Result(Status.CODE_200, null, null);
    }

    public static Result success(Object data) {
        return new Result(Status.CODE_200, null, data);
    }

    public static Result error() {
        return new Result(Status.CODE_500, null, null);
    }

    public static Result error(String msg) {
        return new Result(Status.CODE_500, msg, null);
    }

    public static Result error(String code, String msg) {
        return new Result(code, msg, null);
    }

    public Result code(String code) {
        this.code = code;
        return this;
    }

}
