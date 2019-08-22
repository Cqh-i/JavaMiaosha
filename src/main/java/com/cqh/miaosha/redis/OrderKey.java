package com.cqh.miaosha.redis;

/**
 * 暂时不设置过期时间
 */
public class OrderKey extends BasePrefix {

    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getMiaoshaOrderByUidAndGid = new OrderKey("ms_uidgid");

}
