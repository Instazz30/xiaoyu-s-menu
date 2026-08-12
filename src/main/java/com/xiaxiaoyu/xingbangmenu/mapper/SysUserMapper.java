package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {

    int insert(SysUser user);

    SysUser selectById(@Param("id") Long id);

    SysUser selectByOpenid(@Param("openid") String openid);

    int update(SysUser user);
}
