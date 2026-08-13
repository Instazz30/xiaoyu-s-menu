package com.xiaxiaoyu.xingbangmenu.entity;

import java.time.LocalDateTime;

public class GroupMember {

    private Long id;
    private Long groupId;
    private Long userId;
    private String role;
    private Integer albumPermission;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Boolean isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getAlbumPermission() { return albumPermission; }
    public void setAlbumPermission(Integer albumPermission) { this.albumPermission = albumPermission; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getLeftAt() { return leftAt; }
    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}
