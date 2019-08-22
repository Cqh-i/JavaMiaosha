package com.cqh.miaosha.result;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    /**   
     * @Description: 成功时候调用 
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>(data);
    }

    /**   
     * @Description: 失败时候调用 
     */
    public static <T> Result<T> error(CodeMsg cm) {
        return new Result<T>(cm);
    }

    private Result(T data) {
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    private Result(CodeMsg cm) {
        if (cm == null) {
            return;
        }
        this.code = cm.getCode();
        this.msg = cm.getMsg();
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

}
