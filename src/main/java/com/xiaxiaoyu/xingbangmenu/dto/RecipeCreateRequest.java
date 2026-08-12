package com.xiaxiaoyu.xingbangmenu.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class RecipeCreateRequest {

    @NotNull(message = "请指定所属小组")
    private Long groupId;

    @Size(max = 100, message = "标题不超过100字")
    private String title;

    private LocalDate recipeDate;

    /** 期数（1-4期），默认 1 */
    private Integer issue;

    @Size(max = 100, message = "食堂名称不超过100字")
    private String canteenName;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getRecipeDate() { return recipeDate; }
    public void setRecipeDate(LocalDate recipeDate) { this.recipeDate = recipeDate; }

    public Integer getIssue() { return issue; }
    public void setIssue(Integer issue) { this.issue = issue; }

    public String getCanteenName() { return canteenName; }
    public void setCanteenName(String canteenName) { this.canteenName = canteenName; }
}
