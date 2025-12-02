package com.offcn.controller;

import com.alibaba.fastjson.JSONObject;
import com.offcn.common.constant.RedisConst;
import com.offcn.common.result.Result;
import com.offcn.common.util.IpUtil;
import com.offcn.model.user.UserInfo;
import com.offcn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/passport")
public class UserInfoController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    //登录
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request, HttpServletResponse response){
        UserInfo login = userService.login(userInfo);
        //判断用户不空，用户的状态放入redis key uuid value用户状态
        if (login != null){
            String token = UUID.randomUUID().toString().replace("-","");
            //返回前台的数据封装，前台需要这俩数据
            Map<String,Object> map = new HashMap<>();
            map.put("token",token);
            map.put("nickName",login.getNickName());//nickName用户昵称
            //放入redis中   用户状态id+ip
            JSONObject user = new JSONObject();
            user.put("userId",login.getId().toString());
            user.put("ip", IpUtil.getIpAddress(request));
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX+token,user.toJSONString(),
                    RedisConst.USERKEY_TIMEOUT,
                    TimeUnit.SECONDS);
            return Result.ok(map);
        }else {
            return Result.fail().message("用户名或者密码错误");
        }
    }
    //登出
    /**
     * 退出登录   删除redis中的key
     * @param request
     * @return
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request,HttpServletResponse response){
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX+request.getHeader("token"));
        return Result.ok();
    }
}
