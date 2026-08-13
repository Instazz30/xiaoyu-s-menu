package com.xiaxiaoyu.xingbangmenu.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MenuSection {

    private Long id;
    private Long recipeId;
    private String name;
    private BigDecimal price;
    private String priceText;
    private Integer sortOrder;
    private Boolean needsConfirmation;
    private Boolean isXiaowan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}
