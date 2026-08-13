package com.xiaxiaoyu.xingbangmenu.template;

import java.util.List;

/**
 * 海报渲染上下文，包含所有模板共用的数据。
 */
public class PosterContext {

    private String title;
    private String dateText;
    private String canteenName;
    private Integer issue;
    private boolean showPrice;
    private boolean showDate;
    private boolean showCanteen;
    private List<SectionData> sections;
    private List<ItemData> xiaowanImages;
    private String customBackgroundPath;
    private String logoPath;
    private String logoSlot;
    private String qrCodePath;
    private String qrCodeSlot;

    public static class SectionData {
        private String name;
        private String priceText;
        private boolean isXiaowan;
        private List<ItemData> items;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPriceText() { return priceText; }
        public void setPriceText(String priceText) { this.priceText = priceText; }
        public boolean isXiaowan() { return isXiaowan; }
        public void setXiaowan(boolean xiaowan) { isXiaowan = xiaowan; }
        public List<ItemData> getItems() { return items; }
        public void setItems(List<ItemData> items) { this.items = items; }
    }

    public static class ItemData {
        private String name;
        private String imagePath;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDateText() { return dateText; }
    public void setDateText(String dateText) { this.dateText = dateText; }
    public String getCanteenName() { return canteenName; }
    public void setCanteenName(String canteenName) { this.canteenName = canteenName; }
    public Integer getIssue() { return issue; }
    public void setIssue(Integer issue) { this.issue = issue; }
    public boolean isShowPrice() { return showPrice; }
    public void setShowPrice(boolean showPrice) { this.showPrice = showPrice; }
    public boolean isShowDate() { return showDate; }
    public void setShowDate(boolean showDate) { this.showDate = showDate; }
    public boolean isShowCanteen() { return showCanteen; }
    public void setShowCanteen(boolean showCanteen) { this.showCanteen = showCanteen; }
    public List<SectionData> getSections() { return sections; }
    public void setSections(List<SectionData> sections) { this.sections = sections; }
    public List<ItemData> getXiaowanImages() { return xiaowanImages; }
    public void setXiaowanImages(List<ItemData> xiaowanImages) { this.xiaowanImages = xiaowanImages; }
    public String getCustomBackgroundPath() { return customBackgroundPath; }
    public void setCustomBackgroundPath(String customBackgroundPath) { this.customBackgroundPath = customBackgroundPath; }
    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
    public String getLogoSlot() { return logoSlot; }
    public void setLogoSlot(String logoSlot) { this.logoSlot = logoSlot; }
    public String getQrCodePath() { return qrCodePath; }
    public void setQrCodePath(String qrCodePath) { this.qrCodePath = qrCodePath; }
    public String getQrCodeSlot() { return qrCodeSlot; }
    public void setQrCodeSlot(String qrCodeSlot) { this.qrCodeSlot = qrCodeSlot; }
}
