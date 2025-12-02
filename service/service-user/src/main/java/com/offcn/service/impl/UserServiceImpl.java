package com.offcn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.mapper.UserInfoMapper;
import com.offcn.model.user.UserInfo;
import com.offcn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@SuppressWarnings({"all"})
public class UserServiceImpl implements UserService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Override
    public UserInfo login(UserInfo userInfo) {
        QueryWrapper<UserInfo> qw = new QueryWrapper<>();
        qw.eq("login_name",userInfo.getLoginName());
        // 注意密码是加密：
        String passwd = userInfo.getPasswd(); //123
        // 将passwd 进行加密
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        qw.eq("passwd",newPasswd);
        UserInfo userInfo1 = userInfoMapper.selectOne(qw);
        return userInfo1;
    }
}
