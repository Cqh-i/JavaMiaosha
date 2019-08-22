package com.cqh.miaosha.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cqh.miaosha.redis.RedisService;
import com.cqh.miaosha.result.Result;
import com.cqh.miaosha.service.MiaoshaUserService;
import com.cqh.miaosha.service.UserService;
import com.cqh.miaosha.vo.LoginVo;

import ch.qos.logback.classic.Logger;

@RequestMapping("/login")
@Controller
public class LoginController {

    @Autowired
    UserService userService;
    @Autowired
    RedisService redisService;
    @Autowired
    MiaoshaUserService miaoshaUserService;

    // slf4j
    private static Logger log = (Logger) LoggerFactory.getLogger(Logger.class);

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";// 返回页面login
    }

    @RequestMapping("/do_login") // 作为异步操作
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
        /*        // 参数校验 使用了注解参数校验
        String passInput = loginVo.getPassword();
        String mobile = loginVo.getMobile();
        // import org.apache.commons.lang3.StringUtils;
        if (StringUtils.isEmpty(passInput)) {
            return Result.error(CodeMsg.PASSWORD_EMPTY);
        }
        if (StringUtils.isEmpty(mobile)) {
            return Result.error(CodeMsg.MOBILE_EMPTY);
        }
        System.out.println("mobile：" + mobile);
        if (!ValidatorUtil.isMobile(mobile)) {// 手机号验证不通过 false
            return Result.error(CodeMsg.MOBILE_ERROR);
        }*/
        // 登录
        String token = miaoshaUserService.login(response, loginVo);
        return Result.success(token);
    }
}
