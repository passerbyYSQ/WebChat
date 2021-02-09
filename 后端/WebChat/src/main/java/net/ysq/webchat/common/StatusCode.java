package net.ysq.webchat.common;

/**
 * 2000 - 成功处理请求
 * 3*** - 重定向，需要进一步的操作已完成请求
 * 4*** - 客户端错误，请求参数错误，语法错误等等
 * 5*** - 服务器内部错误
 * ...
 *
 * @author passerbyYSQ
 * @create 2020-11-02 16:26
 */
// 不加上此注解，Jackson将对象序列化为json时，直接将枚举类转成它的名字
//@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusCode {
    SUCCESS(2000, "成功"),

    // 服务器内部错误
    UNKNOWN_ERROR(5000, "未知错误"),
    NO_PERM(5001, "无权限操作"),

    // 参数相关
    PARAM_NOT_COMPLETED(6001, "参数缺失"),
    PARAM_IS_INVALID(6002, "参数无效"),
    FILE_TYPE_INVALID(6003, "非法文件类型"),
    FILE_SIZE_EXCEEDED(6004, "文件大小超出限制"),

    // 用户相关
    USERNAME_IS_EXIST(6101, "用户名已存在"),
    PASSWORD_INCORRECT(6102, "密码错误"),
    USER_NOT_EXIST(6103, "用户不存在"), // 可能是userId错误

    // 账户相关
    TOKEN_IS_MISSING(6200, "token缺失"),
    FORCED_OFFLINE(6201, "异地登录，当前账户被迫下线，请重新登录"),
    TOKEN_IS_EXPIRED(6202, "token已过期"),
    TOKEN_IS_INVALID(6203, "无效token"),

    // 好友相关的
    FRIEND_REQUEST_FREQUENT(6300, "距离上一次好友申请不足半个小时，请勿频繁申请"),
    CAN_NOT_ADD_SELF(6301, "不能添加自己为好友"),
    HAVE_BEEN_FRIEND(6302, "你们已经是好友，请勿重复处理"),
    FRIEND_REQUEST_NOT_EXIST(6303, "好友申请不存在"), // 传参错误
    REPEAT_PROCESS(6304, "请勿重复处理")
    ;


    // 状态码数值
    private Integer code;
    // 状态码描述信息
    private String msg;

    StatusCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 根据业务状态码获取对应的描述信息
     * @param code      业务状态码
     * @return
     */
    public static String getMsgByCode(Integer code) {
        for (StatusCode status : StatusCode.values()) {
            if (status.code.equals(code)) {
                return status.msg;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
