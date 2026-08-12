package com.xiaxiaoyu.xingbangmenu.entity;

import java.time.LocalDateTime;

public class GroupPosterTemplate {

    private Long id;
    private Long groupId;
    private Long creatorId;
    private String name;
    private String baseTemplateId;
    private String backgroundUrl;
    private String logoUrl;
    private String logoSlot;
    private String qrCodeUrl;
    private String qrCodeSlot;
    private Boolean displayPrice;
    private Boolean displayDate;
    private Boolean displayCanteen;
    private String status;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseTemplateId() { return baseTemplateId; }
    public void setBaseTemplateId(String baseTemplateId) { this.baseTemplateId = baseTemplateId; }
    public String getBackgroundUrl() { return backgroundUrl; }
    public void setBackgroundUrl(String backgroundUrl) { this.backgroundUrl = backgroundUrl; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getLogoSlot() { return logoSlot; }
    public void setLogoSlot(String logoSlot) { this.logoSlot = logoSlot; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public String getQrCodeSlot() { return qrCodeSlot; }
    public void setQrCodeSlot(String qrCodeSlot) { this.qrCodeSlot = qrCodeSlot; }
    public Boolean getDisplayPrice() { return displayPrice; }
    public void setDisplayPrice(Boolean displayPrice) { this.displayPrice = displayPrice; }
    public Boolean getDisplayDate() { return displayDate; }
    public void setDisplayDate(Boolean displayDate) { this.displayDate = displayDate; }
    public Boolean getDisplayCanteen() { return displayCanteen; }
    public void setDisplayCanteen(Boolean displayCanteen) { this.displayCanteen = displayCanteen; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}
