package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.InspectionIssue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InspectionIssueMapper {

    int insert(InspectionIssue issue);

    InspectionIssue selectById(@Param("id") Long id);

    List<InspectionIssue> selectByUserId(@Param("userId") Long userId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime,
                                         @Param("location") String location);

    List<InspectionIssue> selectByIds(@Param("userId") Long userId,
                                      @Param("ids") List<Long> ids);

    int updateText(InspectionIssue issue);

    int updateResultImage(@Param("id") Long id,
                          @Param("userId") Long userId,
                          @Param("resultImageUrl") String resultImageUrl,
                          @Param("resultThumbnailUrl") String resultThumbnailUrl,
                          @Param("status") String status);

    int deleteByIds(@Param("userId") Long userId,
                    @Param("ids") List<Long> ids);
}
