package com.offcn.service;

import com.offcn.model.user.UserInfo;

public interface UserService {
    //验证登录
    UserInfo login(UserInfo userInfo);
}
