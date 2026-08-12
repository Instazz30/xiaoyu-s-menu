package com.xiaxiaoyu.xingbangmenu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParseRequest {

    @NotBlank(message = "菜谱文字不能为空")
    @Size(max = 5000, message = "菜谱文字不超过5000字")
    private String originalText;

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }
}
