package red.fuyun.util;

/**
 * @author Crazynob
 * @Title:
 * @Package
 * @Description:
 * @date 2020/9/4 22:06
 */
public enum ResultCode {
    /**
     * 成功
     */
    SUCCESS("0", "success"),

    /**
     * 未知错误
     */
    UNKNOWN_ERROR("0x10001", "未知错误"),

    /**
     * 用户名错误或不存在
     */
    USERNAME_ERROR("0x10002", "用户名错误或不存在"),

    CAPTCHA_ERROR("0x10008","图形验证码错误"),
    EMAIL_CAPTCHA_ERROR("0x10015","邮箱验证码错误"),

    USERNAME_REPEAT("0x10009","用户名已存在!"),

    PASSWORD_REX_ERROR("0x10010","密码不合法,长度在6-18之间,只能包含字符、数字、下划线、点、@"),

    USERNAME_REX_ERROR("0x10011","用户名不合法,字母开头,允许5-10字符,允许字母数字下划线"),

    EMAIL_REX_ERROR("0x10013","邮箱不正确！"),

    REGISTER_FAIL("0x10012","注册失败!"),
    /**
     * 密码错误
     */
    PASSWORD_ERROR("0x10003", "password error"),

    /**
     * 用户名不能为空
     */
    USERNAME_EMPTY("0x10004", "username can not be empty"),
    FAIL("0x1005","失败"),

    Templet_EXISTS_SCHEDULE("0x1006","模板存在关联任务,请取消关联!"),

    Templet_CREATE_SUCCESS("0x1007","模板创建成功"),
    EMAIL_TIME_BEFORE("0x10014","间隔时间不足"),
    EMAIL_EXISTS("0x10015","邮箱已存在!");





    /**
     * 结果码
     */
    private String code;

    /**
     * 结果码描述
     */
    private String msg;


    ResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
