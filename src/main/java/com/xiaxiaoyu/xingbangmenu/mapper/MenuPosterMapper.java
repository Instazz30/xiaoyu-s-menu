package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.MenuPoster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuPosterMapper {

    int insert(MenuPoster poster);

    MenuPoster selectById(@Param("id") Long id);

    List<MenuPoster> selectByRecipeId(@Param("recipeId") Long recipeId);

    int update(MenuPoster poster);

    int deleteById(@Param("id") Long id);
}
