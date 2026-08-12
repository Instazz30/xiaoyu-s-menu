package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface GroupMemberMapper {

    int insert(GroupMember member);

    List<Map<String, Object>> selectByGroupId(@Param("groupId") Long groupId);

    GroupMember selectByGroupAndUser(@Param("groupId") Long groupId, @Param("userId") Long userId);

    int update(GroupMember member);

    int leave(@Param("id") Long id);

    int deleteByGroupId(@Param("groupId") Long groupId);

    long countByGroupId(@Param("groupId") Long groupId);
}
