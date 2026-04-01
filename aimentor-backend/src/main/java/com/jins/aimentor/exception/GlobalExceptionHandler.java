package com.jins.aimentor.exception;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.constants.Status;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理：将异常转为统一 {@link Result}，HTTP 状态码仍为 200，
 * 便于前端 axios 走成功回调，用 body 里的 code/msg 展示具体原因。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 校验失败（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        if (msg.isEmpty()) {
            msg = "请求参数校验失败";
        }
        log.warn("[GlobalException] 参数校验失败: {}", msg);
        return Result.error(Status.CODE_400, msg);
    }

    /**
     * 表单绑定校验失败
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.warn("[GlobalException] BindException: {}", msg);
        return Result.error(Status.CODE_400, msg.isEmpty() ? "参数绑定失败" : msg);
    }

    /**
     * 方法参数上的校验（如 @RequestParam @Min）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
        log.warn("[GlobalException] ConstraintViolation: {}", msg);
        return Result.error(Status.CODE_400, msg);
    }

    /**
     * 错用 @NotBlank 等在非字符串字段上时 Hibernate Validator 抛出的类型异常
     */
    @ExceptionHandler(jakarta.validation.UnexpectedTypeException.class)
    public Result<Void> handleUnexpectedType(jakarta.validation.UnexpectedTypeException e) {
        log.error("[GlobalException] UnexpectedTypeException: {}", e.getMessage());
        return Result.error(Status.CODE_400,
                "参数校验配置异常，请联系管理员。详情：" + e.getMessage());
    }

    /**
     * JSON 体无法反序列化、格式错误、类型不匹配等
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("[GlobalException] HttpMessageNotReadable: {}", e.getMessage());
        return Result.error(Status.CODE_400, "请求体格式错误，请检查 JSON 字段类型与必填项");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = "缺少必填参数：" + e.getParameterName();
        log.warn("[GlobalException] {}", msg);
        return Result.error(Status.CODE_400, msg);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = "参数「" + e.getName() + "」类型不正确";
        log.warn("[GlobalException] {}", msg);
        return Result.error(Status.CODE_400, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[GlobalException] IllegalArgument: {}", e.getMessage());
        return Result.error(Status.CODE_400, e.getMessage());
    }

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        log.warn("[GlobalException] BizException code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 兜底：避免只返回空白 500，尽量带上异常信息（生产环境可再收敛）
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleAny(Exception e) {
        log.error("[GlobalException] 未捕获异常", e);
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "服务异常，请稍后重试";
        }
        return Result.error(Status.CODE_500, msg);
    }
}
