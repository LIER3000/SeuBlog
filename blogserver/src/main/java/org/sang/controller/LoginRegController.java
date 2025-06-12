package org.sang.controller;

import org.sang.bean.RespBean;
import org.sang.bean.User;
import org.sang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


@RestController
public class LoginRegController {
    @Autowired
    UserService userService;

    @RequestMapping("/login_error")
    public RespBean loginError() {
        return new RespBean("error", "登录失败!");
    }

    @RequestMapping("/login_success")
    public RespBean loginSuccess() {
        return new RespBean("success", "登录成功!");
    }

    /**
     * 如果自动跳转到这个页面，说明用户未登录，返回相应的提示即可
     * <p>
     * 如果要支持表单登录，可以在这个方法中判断请求的类型，进而决定返回JSON还是HTML页面
     *
     * @return
     */
    @RequestMapping("/login_page")
    public RespBean loginPage() {
        return new RespBean("error", "尚未登录，请登录!");
    }

    @PostMapping("/reg")
    public Map<String, Object> reg(User user) {
        Map<String, Object> result = new HashMap<>();

        int code = userService.reg(user);
        result.put("code", code);

        switch (code) {
            case 0:
                result.put("message", "注册成功");
                result.put("userId", user.getId());
                break;
            case 1:
                result.put("message", "用户名已存在");
                break;
            case 2:
                result.put("message", "系统错误，请联系管理员");
                break;
            default:
                result.put("message", "未知错误");
        }

        final Logger logger = LoggerFactory.getLogger(LoginRegController.class);
        logger.info("收到注册请求: {}", user.getUsername());
        logger.info("返回响应: code={}, message={}", code, result.get("message"));

        return result;
    }
}
