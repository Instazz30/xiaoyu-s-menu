package com.xiaxiaoyu.xingbangmenu.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class RecipeUpdateRequest {

    @Size(max = 100, message = "标题不超过100字")
    private String title;

    private LocalDate recipeDate;

    /** 期数（1-4期） */
    private Integer issue;

    @Size(max = 100, message = "食堂名称不超过100字")
    private String canteenName;

    private String templateId;
    private Boolean displayPrice;
    private Boolean displayDate;
    private Boolean displayCanteen;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getRecipeDate() { return recipeDate; }
    public void setRecipeDate(LocalDate recipeDate) { this.recipeDate = recipeDate; }

    public Integer getIssue() { return issue; }
    public void setIssue(Integer issue) { this.issue = issue; }

    public String getCanteenName() { return canteenName; }
    public void setCanteenName(String canteenName) { this.canteenName = canteenName; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public Boolean getDisplayPrice() { return displayPrice; }
    public void setDisplayPrice(Boolean displayPrice) { this.displayPrice = displayPrice; }

    public Boolean getDisplayDate() { return displayDate; }
    public void setDisplayDate(Boolean displayDate) { this.displayDate = displayDate; }

    public Boolean getDisplayCanteen() { return displayCanteen; }
    public void setDisplayCanteen(Boolean displayCanteen) { this.displayCanteen = displayCanteen; }
}
