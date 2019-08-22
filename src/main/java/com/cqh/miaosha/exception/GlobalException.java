package com.cqh.miaosha.exception;

import com.cqh.miaosha.result.CodeMsg;

/**
 * 全局异常处理
 *
 */
public class GlobalException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private CodeMsg cm;

    public GlobalException(CodeMsg cm) {
        super(cm.toString());
        this.cm = cm;

    }

    public CodeMsg getCm() {
        return cm;
    }

    public void setCm(CodeMsg cm) {
        this.cm = cm;
    }

}
