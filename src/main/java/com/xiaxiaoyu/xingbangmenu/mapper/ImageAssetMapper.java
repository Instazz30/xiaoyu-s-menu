package com.xiaxiaoyu.xingbangmenu.mapper;

import com.xiaxiaoyu.xingbangmenu.entity.ImageAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImageAssetMapper {

    int insert(ImageAsset image);

    ImageAsset selectById(@Param("id") Long id);

    List<ImageAsset> selectByRecipeId(@Param("recipeId") Long recipeId);

    int update(ImageAsset image);

    int deleteById(@Param("id") Long id);

    List<ImageAsset> selectByItemId(@Param("itemId") Long itemId);

    List<java.util.Map<String, Object>> selectPendingByGroup(@Param("groupId") Long groupId);
}
