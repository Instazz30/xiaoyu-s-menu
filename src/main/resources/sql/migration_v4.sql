-- 智慧食堂菜谱生成系统 V4 迁移脚本
-- 新增：小组专属海报模板
USE xingbang_menu;

CREATE TABLE IF NOT EXISTS group_poster_template (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '专属模板ID',
    group_id            BIGINT       NOT NULL COMMENT '所属小组ID',
    creator_id          BIGINT       NOT NULL COMMENT '模板创建者ID',
    name                VARCHAR(100) NOT NULL COMMENT '模板名称',
    base_template_id    VARCHAR(50)  NOT NULL COMMENT '公共基础模板ID',
    background_url      VARCHAR(500) DEFAULT NULL COMMENT '自定义背景图',
    logo_url            VARCHAR(500) DEFAULT NULL COMMENT 'Logo图片',
    logo_slot           VARCHAR(30)  DEFAULT 'top_left' COMMENT 'Logo固定点位',
    qr_code_url         VARCHAR(500) DEFAULT NULL COMMENT '二维码图片',
    qr_code_slot        VARCHAR(30)  DEFAULT 'top_right' COMMENT '二维码固定点位',
    display_price       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '默认显示价格',
    display_date        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '默认显示日期',
    display_canteen     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '默认显示食堂名称',
    status              VARCHAR(20)  NOT NULL DEFAULT 'draft' COMMENT 'draft/published/disabled',
    is_default          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否为小组默认模板',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted          TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_group_template (group_id, is_deleted, status),
    INDEX idx_group_template_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组专属海报模板';
