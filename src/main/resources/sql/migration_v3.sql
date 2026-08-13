-- 智慧食堂菜谱生成系统 V3 迁移脚本（幂等，可重复执行）
-- 新增：个人隐患检查与 Excel 导出模块
USE xingbang_menu;

CREATE TABLE IF NOT EXISTS inspection_issue (
    id                   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '隐患记录唯一编号',
    user_id              BIGINT        NOT NULL COMMENT '所属用户ID',
    location             VARCHAR(255)  DEFAULT NULL COMMENT '地点',
    reason               VARCHAR(1000) DEFAULT NULL COMMENT '原因',
    measure              VARCHAR(1000) DEFAULT NULL COMMENT '措施',
    issue_image_url      VARCHAR(500)  NOT NULL COMMENT '问题图片地址',
    issue_thumbnail_url  VARCHAR(500)  DEFAULT NULL COMMENT '问题图片缩略图地址',
    result_image_url     VARCHAR(500)  DEFAULT NULL COMMENT '整改结果图片地址',
    result_thumbnail_url VARCHAR(500)  DEFAULT NULL COMMENT '整改结果图片缩略图地址',
    status               VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT '整改状态(pending/rectified)',
    created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted           TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_inspection_user_time (user_id, is_deleted, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='个人隐患检查记录';
