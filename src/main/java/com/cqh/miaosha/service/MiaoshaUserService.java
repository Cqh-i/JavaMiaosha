package com.cqh.miaosha.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cqh.miaosha.dao.MiaoshaUserDao;
import com.cqh.miaosha.domain.MiaoshaUser;
import com.cqh.miaosha.exception.GlobalException;
import com.cqh.miaosha.redis.MiaoshaUserKey;
import com.cqh.miaosha.redis.RedisService;
import com.cqh.miaosha.result.CodeMsg;
import com.cqh.miaosha.util.MD5Util;
import com.cqh.miaosha.util.UUIDUtil;
import com.cqh.miaosha.vo.LoginVo;

@Service
public class MiaoshaUserService {
    public static final String COOKIE1_NAME_TOKEN = "token";

    @Autowired
    MiaoshaUserDao miaoshaUserDao;
    @Autowired
    RedisService redisService;

    /**
     * 根据id取得对象，先去缓存中取
     * @param id
     * @return
     */
    public MiaoshaUser getById(long id) {
        // 1.取缓存 ---先根据id来取得缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
        // 能再缓存中拿到
        if (user != null) {
            return user;
        }
        // 2.缓存中拿不到，那么就去取数据库
        user = miaoshaUserDao.getById(id);
        // 3.设置缓存
        if (user != null) {
            redisService.set(MiaoshaUserKey.getById, "" + id, user);
        }
        return user;
    }

    public String login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        // 判断手机号是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if (user == null)
            throw new GlobalException(CodeMsg.MOBILE_NOTEXIST);
        // 验证密码
        String dbPass = user.getPwd();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if (!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        // 生成cookie
        String token = UUIDUtil.uuid();
        addCookie(user, token, response);
        return token;
    }

    /**   
     * @Description: 从缓存里面取得值，取得value
     */
    public MiaoshaUser getByToken(String token, HttpServletResponse response) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        // return redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        // 再次请求时候，延长有效期
        // 重新设置缓存里面的值，使用之前cookie里面的token
        if (user != null) {
            addCookie(user, token, response);
        }
        // System.out.println("@MiaoshaUserService-getByToken user:" + user);
        return user;
    }

    /*    *//**
            * 注意数据修改时候，保持缓存与数据库的一致性
            * 需要传入token
            * @param id
            * @return
            *//*
                                                    public boolean updatePassword(String token, long id, String passNew) {
                                                     // 1.取user对象，查看是否存在
                                                     MiaoshaUser user = getById(id);
                                                     if (user == null) {
                                                         throw new GlobalException(CodeMsg.MOBILE_NOTEXIST);
                                                     }
                                                     // 2.更新密码
                                                     MiaoshaUser toupdateuser = new MiaoshaUser();
                                                     toupdateuser.setId(id);
                                                     toupdateuser.setPwd(MD5Util.inputPassToDbPass(passNew, user.getSalt()));
                                                     miaoshaUserDao.update(toupdateuser);
                                                     // 3.更新数据库与缓存，一定保证数据一致性，修改token关联的对象以及id关联的对象
                                                     redisService.delete(MiaoshaUserKey.getById, "" + id);
                                                     // 不能直接删除token，删除之后就不能登录了，所以只能是修改
                                                     user.setPwd(toupdateuser.getPwd());
                                                     redisService.set(MiaoshaUserKey.token, token, user);
                                                     return true;
                                                    }*/

    /* * 
    * 添加或者叫做更新cookie
    */
    public void addCookie(MiaoshaUser user, String token, HttpServletResponse response) {
        // 可以用老的token，不用每次都生成cookie，可以用之前的
        System.out.println("uuid:" + token);
        // 将token写到cookie当中，然后传递给客户端
        // 此token对应的是哪一个用户,将我们的私人信息存放到一个第三方的缓存中
        // prefix:MiaoshaUserKey.token key:token value:用户的信息 -->以后拿到了token就知道对应的用户信息。
        // MiaoshaUserKey.token-->
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE1_NAME_TOKEN, token);
        // 设置cookie的有效期，与session有效期一致
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        // 设置网站的根目录
        // 可在同一应用服务器内共享方法
        cookie.setPath("/");
        // 需要写到response中
        response.addCookie(cookie);
    }

    /**
     * 注意数据修改时候，保持缓存与数据库的一致性
     * 需要传入token
     */
    public boolean updatePassword(String token, long id, String passNew) {
        // 1.取user对象，查看是否存在
        MiaoshaUser user = getById(id);
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOTEXIST);
        }
        // 2.更新密码
        MiaoshaUser toupdateuser = new MiaoshaUser();
        toupdateuser.setId(id);
        toupdateuser.setPwd(MD5Util.inputPassToDbPass(passNew, user.getSalt()));
        miaoshaUserDao.update(toupdateuser);
        // 3.更新数据库与缓存，一定保证数据一致性，修改token关联的对象以及id关联的对象
        redisService.delete(MiaoshaUserKey.getById, "" + id);
        // 不能直接删除token，删除之后就不能登录了，所以只能是修改
        user.setPwd(toupdateuser.getPwd());
        redisService.set(MiaoshaUserKey.token, token, user);
        return true;
    }
}
