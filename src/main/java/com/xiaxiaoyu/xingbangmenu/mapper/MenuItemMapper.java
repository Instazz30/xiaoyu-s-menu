package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.MenuItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuItemMapper {

    int insert(MenuItem item);

    List<MenuItem> selectByRecipeId(@Param("recipeId") Long recipeId);

    List<MenuItem> selectBySectionId(@Param("sectionId") Long sectionId);

    MenuItem selectById(@Param("id") Long id);

    int update(MenuItem item);

    int deleteById(@Param("id") Long id);

    int deleteBySectionId(@Param("sectionId") Long sectionId);

    int deleteByRecipeId(@Param("recipeId") Long recipeId);

    int insertBatch(@Param("list") List<MenuItem> items);

    List<java.util.Map<String, Object>> countByDishName(@Param("groupId") Long groupId,
                                                        @Param("limit") int limit);
}
