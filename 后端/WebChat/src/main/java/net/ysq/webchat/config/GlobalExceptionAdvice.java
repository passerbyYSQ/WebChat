package net.ysq.webchat.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import net.ysq.webchat.common.ResultModel;
import net.ysq.webchat.common.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 全局异常捕获
 *
 * @author passerbyYSQ
 * @create 2020-11-02 23:27
 */
@ControllerAdvice
@ResponseBody
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class GlobalExceptionAdvice {

    // 参数校验不通过的异常统一捕获处理
    @ExceptionHandler(BindException.class)
    public ResultModel handleBindException(BindException e) {
        List<FieldError> errors = e.getFieldErrors();
        return ResultModel.failed(StatusCode.PARAM_IS_INVALID.getCode(),
                joinErrorMsg(errors));
    }

    // 处理 json 请求体调用接口校验失败抛出的异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultModel methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        return ResultModel.failed(StatusCode.PARAM_IS_INVALID.getCode(),
                joinErrorMsg(errors));
    }

    private String joinErrorMsg(List<FieldError> errors) {
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError error : errors) {
            errorMsg.append(error.getField())
                    .append(error.getDefaultMessage())
                    .append(";");
        }
        return errorMsg.toString();
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResultModel handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        Iterator<ConstraintViolation<?>> iterator = constraintViolations.iterator();
        StringBuilder errorMsg = new StringBuilder();
        while (iterator.hasNext()) {
            ConstraintViolation<?> cvl = iterator.next();
            errorMsg.append(cvl.getPropertyPath().toString().split("\\.")[1]) // .需要转义
                    .append(cvl.getMessage());
        }
        return ResultModel.failed(StatusCode.PARAM_IS_INVALID.getCode(),
                errorMsg.toString());
    }


    @ExceptionHandler(JWTVerificationException.class)
    public ResultModel handlerJwtVerificationException(JWTVerificationException e) {
        // 主要分为两类错误
        if (e instanceof TokenExpiredException) {
            // token过期，登录状态过期
            return ResultModel.failed(StatusCode.TOKEN_IS_EXPIRED);
        }
        // 无效token（有可能被修改了等原因导致验证失败）
        return ResultModel.failed(StatusCode.TOKEN_IS_INVALID);
    }


    // 前后端联调时和正式上线后开启
    // 后端编码时，为了方便测试，先注释掉
//    @ExceptionHandler({Exception.class})
//    public ResultModel handleOtherException(Exception e) {
//        // 判断异常中是否有错误信息，如果存在就使用异常中的消息，否则使用默认消息
//        if (!StringUtils.isEmpty(e.getMessage())) {
//            return ResultModel.error(5000, e.getMessage());
//        }
//        return ResultModel.error(StatusCode.UNKNOWN_ERROR);
//    }

}
