package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.GroupPosterTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupPosterTemplateMapper {
    int insert(GroupPosterTemplate template);
    GroupPosterTemplate selectById(@Param("id") Long id);
    List<GroupPosterTemplate> selectByGroupId(@Param("groupId") Long groupId);
    int countByGroupId(@Param("groupId") Long groupId);
    int update(GroupPosterTemplate template);
    int clearDefault(@Param("groupId") Long groupId);
    int deleteById(@Param("id") Long id);
}
