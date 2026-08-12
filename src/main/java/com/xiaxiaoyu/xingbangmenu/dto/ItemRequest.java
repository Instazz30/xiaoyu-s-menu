package com.xiaxiaoyu.xingbangmenu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ItemRequest {

    @NotBlank(message = "菜品名称不能为空")
    @Size(max = 200, message = "菜品名称不超过200字")
    private String name;

    @Size(max = 500, message = "菜品描述不超过500字")
    private String description;

    private Long sectionId;
    private Integer sortOrder;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
