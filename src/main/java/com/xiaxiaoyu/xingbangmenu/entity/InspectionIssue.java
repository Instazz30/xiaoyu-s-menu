package com.xiaxiaoyu.xingbangmenu.entity;

import java.time.LocalDateTime;

public class InspectionIssue {

    private Long id;
    private Long userId;
    private String location;
    private String reason;
    private String measure;
    private String issueImageUrl;
    private String issueThumbnailUrl;
    private String resultImageUrl;
    private String resultThumbnailUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getMeasure() { return measure; }
    public void setMeasure(String measure) { this.measure = measure; }

    public String getIssueImageUrl() { return issueImageUrl; }
    public void setIssueImageUrl(String issueImageUrl) { this.issueImageUrl = issueImageUrl; }

    public String getIssueThumbnailUrl() { return issueThumbnailUrl; }
    public void setIssueThumbnailUrl(String issueThumbnailUrl) { this.issueThumbnailUrl = issueThumbnailUrl; }

    public String getResultImageUrl() { return resultImageUrl; }
    public void setResultImageUrl(String resultImageUrl) { this.resultImageUrl = resultImageUrl; }

    public String getResultThumbnailUrl() { return resultThumbnailUrl; }
    public void setResultThumbnailUrl(String resultThumbnailUrl) { this.resultThumbnailUrl = resultThumbnailUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
