package com.xiaxiaoyu.xingbangmenu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class SectionRequest {

    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100, message = "区域名称不超过100字")
    private String name;

    @Size(max = 50, message = "价格文字不超过50字")
    private String priceText;

    private BigDecimal price;
    private Integer sortOrder;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPriceText() { return priceText; }
    public void setPriceText(String priceText) { this.priceText = priceText; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
