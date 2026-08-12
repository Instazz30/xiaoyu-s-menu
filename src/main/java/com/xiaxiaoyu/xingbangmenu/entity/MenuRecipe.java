package com.xiaxiaoyu.xingbangmenu.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MenuRecipe {

    private Long id;
    private String title;
    private LocalDate recipeDate;
    private Integer issue;
    private String canteenName;
    private Long groupId;
    private Boolean isCurrent;
    private Long creatorId;
    private String originalText;
    private String status;
    private String templateId;
    private Boolean displayPrice;
    private Boolean displayDate;
    private Boolean displayCanteen;
    private Long currentPosterId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getRecipeDate() { return recipeDate; }
    public void setRecipeDate(LocalDate recipeDate) { this.recipeDate = recipeDate; }

    public Integer getIssue() { return issue; }
    public void setIssue(Integer issue) { this.issue = issue; }

    public String getCanteenName() { return canteenName; }
    public void setCanteenName(String canteenName) { this.canteenName = canteenName; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public Boolean getDisplayPrice() { return displayPrice; }
    public void setDisplayPrice(Boolean displayPrice) { this.displayPrice = displayPrice; }

    public Boolean getDisplayDate() { return displayDate; }
    public void setDisplayDate(Boolean displayDate) { this.displayDate = displayDate; }

    public Boolean getDisplayCanteen() { return displayCanteen; }
    public void setDisplayCanteen(Boolean displayCanteen) { this.displayCanteen = displayCanteen; }

    public Long getCurrentPosterId() { return currentPosterId; }
    public void setCurrentPosterId(Long currentPosterId) { this.currentPosterId = currentPosterId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}
