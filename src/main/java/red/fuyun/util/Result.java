package red.fuyun.util;

/**
 * @author Crazynob
 * @Title: 结果类
 * @Package
 * @Description: 返回给前端的统一的返回结果类
 * @date 2020/9/4 22:05
 */
public class Result {
    /**
     * 结果码
     */
    private String code;

    /**
     * 结果码描述
     */
    private String msg;


    /**
     *存放返回数据
     */
    private Object data;
    public Result() {

    }

    public Result(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    public Result(ResultCode resultCode, Object data) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.data = data;
    }

    public Result(ResultCode resultCode, String msg) {
        this.code = resultCode.getCode();
        this.msg = msg;
    }


    /**
     * 生成一个Result对象, 并返回
     *
     * @param resultCode
     * @return
     */
    public static Result of(ResultCode resultCode) {
        return new Result(resultCode);
    }
    public static Result of(ResultCode resultCode,Object data) {
        return new Result(resultCode,data);
    }

    public static Result of(ResultCode resultCode,String msg) {
        return new Result(resultCode,msg);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
