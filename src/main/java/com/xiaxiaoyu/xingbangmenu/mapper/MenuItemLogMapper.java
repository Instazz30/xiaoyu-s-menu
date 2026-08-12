package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.MenuItemLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MenuItemLogMapper {

    int insert(MenuItemLog log);

    List<MenuItemLog> selectByGroupId(@Param("groupId") Long groupId,
                                      @Param("menuDate") LocalDate menuDate,
                                      @Param("operatorId") Long operatorId,
                                      @Param("keyword") String keyword,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    long countByGroupId(@Param("groupId") Long groupId,
                        @Param("menuDate") LocalDate menuDate,
                        @Param("operatorId") Long operatorId,
                        @Param("keyword") String keyword);

    /** 导出用：日期区间内的全量修改记录（不分页） */
    List<MenuItemLog> selectAllForExport(@Param("groupId") Long groupId,
                                         @Param("dateFrom") LocalDate dateFrom,
                                         @Param("dateTo") LocalDate dateTo);
}
