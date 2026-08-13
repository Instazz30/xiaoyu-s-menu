package com.xiaxiaoyu.xingbangmenu.dto;

import com.xiaxiaoyu.xingbangmenu.entity.SysUser;

public class LoginResult {

    private String token;
    private SysUser user;

    public LoginResult(String token, SysUser user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public SysUser getUser() { return user; }
}
