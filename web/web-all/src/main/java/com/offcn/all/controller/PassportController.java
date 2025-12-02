package com.offcn.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户认证接口
 * </p>
 *
 */
@Controller
public class PassportController {

    /**
     * 你登陆成功之后，从哪个页面来的，跳转到哪个页面去,如果没有就跳转到首页
     * @return
     */
    @GetMapping("login.html")
    public String login(HttpServletRequest request) {
        String originUrl = request.getParameter("originUrl");
        //originUrl表示从哪个页面跳转过来的，然后跳转到哪个页面去
        request.setAttribute("originUrl",originUrl);
        return "login";
    }

}
