package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.MenuSection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuSectionMapper {

    int insert(MenuSection section);

    List<MenuSection> selectByRecipeId(@Param("recipeId") Long recipeId);

    MenuSection selectById(@Param("id") Long id);

    int update(MenuSection section);

    int deleteById(@Param("id") Long id);

    int deleteByRecipeId(@Param("recipeId") Long recipeId);

    int insertBatch(@Param("list") List<MenuSection> sections);
}
