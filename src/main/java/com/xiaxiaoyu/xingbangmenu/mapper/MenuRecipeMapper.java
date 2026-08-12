package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.MenuRecipe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MenuRecipeMapper {

    int insert(MenuRecipe recipe);

    MenuRecipe selectById(@Param("id") Long id);

    List<MenuRecipe> selectList(@Param("groupId") Long groupId,
                                @Param("status") String status,
                                @Param("dateFrom") LocalDate dateFrom,
                                @Param("dateTo") LocalDate dateTo,
                                @Param("keyword") String keyword,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    long countList(@Param("groupId") Long groupId,
                   @Param("status") String status,
                   @Param("dateFrom") LocalDate dateFrom,
                   @Param("dateTo") LocalDate dateTo,
                   @Param("keyword") String keyword);

    int update(MenuRecipe recipe);

    int deleteById(@Param("id") Long id);

    MenuRecipe selectCurrentByGroup(@Param("groupId") Long groupId);

    int clearCurrentByGroup(@Param("groupId") Long groupId);

    MenuRecipe selectTodayDraft(@Param("date") LocalDate date);

    List<java.util.Map<String, Object>> countByStatus(@Param("groupId") Long groupId);

    List<java.util.Map<String, Object>> countByMonth(@Param("groupId") Long groupId,
                                                     @Param("months") int months);
}
