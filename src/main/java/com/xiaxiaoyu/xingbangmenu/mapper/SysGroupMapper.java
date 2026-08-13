package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.SysGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysGroupMapper {

    int insert(SysGroup group);

    SysGroup selectById(@Param("id") Long id);

    SysGroup selectByCode(@Param("code") String code);

    List<SysGroup> selectByUserId(@Param("userId") Long userId);

    int update(SysGroup group);

    int deleteById(@Param("id") Long id);

    int updateOwner(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
