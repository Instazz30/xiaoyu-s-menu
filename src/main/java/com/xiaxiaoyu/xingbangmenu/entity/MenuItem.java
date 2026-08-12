package com.xiaxiaoyu.xingbangmenu.entity;

import java.time.LocalDateTime;

public class MenuItem {

    private Long id;
    private Long sectionId;
    private Long recipeId;
    private String name;
    private String description;
    private Integer sortOrder;
    private Long imageId;
    private String imageStatus;
    private Boolean needsConfirmation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Long getImageId() { return imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }

    public String getImageStatus() { return imageStatus; }
    public void setImageStatus(String imageStatus) { this.imageStatus = imageStatus; }

    public Boolean getNeedsConfirmation() { return needsConfirmation; }
    public void setNeedsConfirmation(Boolean needsConfirmation) { this.needsConfirmation = needsConfirmation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}
