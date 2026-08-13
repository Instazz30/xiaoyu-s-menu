package com.xiaxiaoyu.xingbangmenu.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.xiaxiaoyu.xingbangmenu.entity.ImageAsset;

public class RecipeDetailResponse {

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
    private List<ImageAsset> xiaowanImages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SectionDto> sections;

    public static class SectionDto {
        private Long id;
        private Long recipeId;
        private String name;
        private BigDecimal price;
        private String priceText;
        private Integer sortOrder;
        private Boolean needsConfirmation;
        private Boolean isXiaowan;
        private List<ItemDto> items;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getRecipeId() { return recipeId; }
        public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getPriceText() { return priceText; }
        public void setPriceText(String priceText) { this.priceText = priceText; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public Boolean getNeedsConfirmation() { return needsConfirmation; }
        public void setNeedsConfirmation(Boolean needsConfirmation) { this.needsConfirmation = needsConfirmation; }
        public Boolean getIsXiaowan() { return isXiaowan; }
        public void setIsXiaowan(Boolean isXiaowan) { this.isXiaowan = isXiaowan; }
        public List<ItemDto> getItems() { return items; }
        public void setItems(List<ItemDto> items) { this.items = items; }
    }

    public static class ItemDto {
        private Long id;
        private Long sectionId;
        private Long recipeId;
        private String name;
        private String description;
        private Integer sortOrder;
        private Long imageId;
        private String imageStatus;
        private Boolean needsConfirmation;
        private String imageUrl;
        private String thumbnailUrl;
        private List<ImageAsset> images;

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
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public List<ImageAsset> getImages() { return images; }
        public void setImages(List<ImageAsset> images) { this.images = images; }
    }

    // --- getters/setters ---

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
    public List<ImageAsset> getXiaowanImages() { return xiaowanImages; }
    public void setXiaowanImages(List<ImageAsset> xiaowanImages) { this.xiaowanImages = xiaowanImages; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<SectionDto> getSections() { return sections; }
    public void setSections(List<SectionDto> sections) { this.sections = sections; }
}
