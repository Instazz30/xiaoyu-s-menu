package com.xiaxiaoyu.xingbangmenu.dto;

import java.util.List;

public class ParseResult {

    private String title;
    private List<SectionResult> sections;
    private List<String> unrecognizedLines;
    private String warning;

    public static class SectionResult {
        private String name;
        private String priceText;
        private java.math.BigDecimal price;
        private List<String> items;
        private boolean needsConfirmation;
        private boolean isXiaowan;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPriceText() { return priceText; }
        public void setPriceText(String priceText) { this.priceText = priceText; }
        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }
        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
        public boolean isNeedsConfirmation() { return needsConfirmation; }
        public void setNeedsConfirmation(boolean needsConfirmation) { this.needsConfirmation = needsConfirmation; }
        public boolean isXiaowan() { return isXiaowan; }
        public void setXiaowan(boolean xiaowan) { isXiaowan = xiaowan; }
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<SectionResult> getSections() { return sections; }
    public void setSections(List<SectionResult> sections) { this.sections = sections; }
    public List<String> getUnrecognizedLines() { return unrecognizedLines; }
    public void setUnrecognizedLines(List<String> unrecognizedLines) { this.unrecognizedLines = unrecognizedLines; }
    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }
}
