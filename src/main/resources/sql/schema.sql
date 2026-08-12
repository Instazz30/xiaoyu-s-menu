-- 智慧食堂菜谱生成系统 数据库初始化脚本
-- 版本：V1.0
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 引擎：InnoDB

CREATE DATABASE IF NOT EXISTS xingbang_menu
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE xingbang_menu;

-- 菜谱记录表
CREATE TABLE IF NOT EXISTS menu_recipe (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '菜谱唯一编号',
    title             VARCHAR(100)  NOT NULL DEFAULT '今日菜谱' COMMENT '菜谱标题',
    recipe_date       DATE          NOT NULL COMMENT '菜谱日期',
    issue             TINYINT       NOT NULL DEFAULT 1 COMMENT '期数(1-4)',
    canteen_name      VARCHAR(100)  DEFAULT NULL COMMENT '食堂名称',
    group_id          BIGINT        NOT NULL DEFAULT 0 COMMENT '所属小组ID(0=历史未分配)',
    is_current        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否为小组当前菜单',
    creator_id        BIGINT        DEFAULT NULL COMMENT '创建人用户ID',
    original_text     TEXT          DEFAULT NULL COMMENT '原始菜谱文字',
    status            VARCHAR(20)   NOT NULL DEFAULT 'draft' COMMENT '制作状态',
    template_id       VARCHAR(50)   DEFAULT NULL COMMENT '当前模板',
    display_price     TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否显示价格',
    display_date      TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否显示日期',
    display_canteen   TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否显示食堂名称',
    current_poster_id BIGINT        DEFAULT NULL COMMENT '当前海报ID',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_recipe_status (status),
    INDEX idx_recipe_date (recipe_date),
    INDEX idx_recipe_created (created_at DESC),
    INDEX idx_recipe_group (group_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜谱记录';

-- 菜品区域表
CREATE TABLE IF NOT EXISTS menu_section (
    id                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '区域唯一编号',
    recipe_id           BIGINT        NOT NULL COMMENT '所属菜谱ID',
    name                VARCHAR(100)  NOT NULL COMMENT '区域名称',
    price               DECIMAL(10,2) DEFAULT NULL COMMENT '标准价格',
    price_text          VARCHAR(50)   DEFAULT NULL COMMENT '原始价格文字',
    sort_order          INT           NOT NULL DEFAULT 0 COMMENT '区域排序',
    needs_confirmation  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否需要人工确认',
    is_xiaowan          TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否小碗菜区(0=套餐,1=小碗菜)',
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted          TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_section_recipe (recipe_id, is_deleted),
    INDEX idx_section_sort (recipe_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品区域';

-- 菜品表
CREATE TABLE IF NOT EXISTS menu_item (
    id                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '菜品记录唯一编号',
    section_id          BIGINT        NOT NULL COMMENT '所属区域ID',
    recipe_id           BIGINT        NOT NULL COMMENT '所属菜谱ID',
    name                VARCHAR(200)  NOT NULL COMMENT '菜品名称',
    description         VARCHAR(500)  DEFAULT NULL COMMENT '菜品描述',
    sort_order          INT           NOT NULL DEFAULT 0 COMMENT '菜品排序',
    image_id            BIGINT        DEFAULT NULL COMMENT '当前绑定的图片ID',
    image_status        VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT '图片状态',
    needs_confirmation  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否需要人工确认',
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted          TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_item_section (section_id, is_deleted),
    INDEX idx_item_recipe (recipe_id, is_deleted),
    INDEX idx_item_sort (section_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品';

-- 图片资源表
CREATE TABLE IF NOT EXISTS image_asset (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '图片唯一编号',
    recipe_id     BIGINT        NOT NULL COMMENT '所属菜谱ID',
    group_id      BIGINT        DEFAULT 0 COMMENT '所属小组ID',
    uploader_id   BIGINT        DEFAULT NULL COMMENT '上传人用户ID',
    review_status VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT '审核状态(pending/approved/rejected)',
    reviewer_id   BIGINT        DEFAULT NULL COMMENT '审核人用户ID',
    reviewed_at   DATETIME      DEFAULT NULL COMMENT '审核时间',
    review_note   VARCHAR(500)  DEFAULT NULL COMMENT '审核备注',
    item_id       BIGINT        DEFAULT NULL COMMENT '关联菜品ID',
    original_url  VARCHAR(500)  NOT NULL COMMENT '原始图片地址',
    thumbnail_url VARCHAR(500)  DEFAULT NULL COMMENT '缩略图地址',
    file_type     VARCHAR(20)   NOT NULL COMMENT '文件类型',
    file_size     BIGINT        NOT NULL COMMENT '文件大小(字节)',
    width         INT           DEFAULT NULL COMMENT '图片宽度',
    height        INT           DEFAULT NULL COMMENT '图片高度',
    crop_data     JSON          DEFAULT NULL COMMENT '裁剪参数',
    upload_status VARCHAR(20)   NOT NULL DEFAULT 'uploading' COMMENT '上传状态',
    error_message VARCHAR(500)  DEFAULT NULL COMMENT '失败原因',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_image_recipe (recipe_id, is_deleted),
    INDEX idx_image_item (item_id),
    INDEX idx_image_status (upload_status),
    INDEX idx_image_group_review (group_id, review_status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片资源';

-- 海报记录表
CREATE TABLE IF NOT EXISTS menu_poster (
    id                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '海报唯一编号',
    recipe_id         BIGINT         NOT NULL COMMENT '所属菜谱ID',
    group_id          BIGINT         DEFAULT 0 COMMENT '所属小组ID',
    creator_id        BIGINT         DEFAULT NULL COMMENT '生成人用户ID',
    template_id       VARCHAR(50)    NOT NULL COMMENT '模板标识',
    generation_status VARCHAR(20)    NOT NULL DEFAULT 'pending' COMMENT '生成状态',
    page_count        INT            NOT NULL DEFAULT 1 COMMENT '海报页数',
    output_urls       TEXT           DEFAULT NULL COMMENT '生成图片地址（逗号分隔）',
    error_message     VARCHAR(1000)  DEFAULT NULL COMMENT '失败原因',
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    updated_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted        TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_poster_recipe (recipe_id, is_deleted),
    INDEX idx_poster_status (generation_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='海报记录';

-- =====================================================================
-- V2：用户、小组、成员、菜品修改记录
-- =====================================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户唯一编号',
    openid     VARCHAR(64)  NOT NULL COMMENT '微信 openid（唯一）',
    nickname   VARCHAR(50)  NOT NULL DEFAULT '微信用户' COMMENT '昵称',
    avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

-- 小组表
CREATE TABLE IF NOT EXISTS sys_group (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '小组唯一编号',
    name       VARCHAR(100) NOT NULL COMMENT '小组名称',
    group_code VARCHAR(16)  NOT NULL COMMENT '小组码（唯一）',
    owner_id   BIGINT       NOT NULL COMMENT '创建者用户ID',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_code (group_code),
    INDEX idx_group_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组';

-- 小组成员表
CREATE TABLE IF NOT EXISTS group_member (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '成员记录唯一编号',
    group_id   BIGINT      NOT NULL COMMENT '小组ID',
    user_id    BIGINT      NOT NULL COMMENT '用户ID',
    role       VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT '角色(admin/member)',
    album_permission TINYINT(1) NOT NULL DEFAULT 0 COMMENT '相册上传权限(0=未开通,1=申请中,2=已开通)',
    joined_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    left_at    DATETIME    DEFAULT NULL COMMENT '退出时间',
    is_deleted TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除(1=已退出/被移除)',
    PRIMARY KEY (id),
    INDEX idx_member_group (group_id, is_deleted),
    INDEX idx_member_user (user_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组成员';

-- 菜品修改记录表
CREATE TABLE IF NOT EXISTS menu_item_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录唯一编号',
    group_id    BIGINT       NOT NULL COMMENT '所属小组ID',
    recipe_id   BIGINT       NOT NULL COMMENT '所属菜单（菜谱）ID',
    recipe_date DATE         NOT NULL COMMENT '菜单日期',
    item_id     BIGINT       DEFAULT NULL COMMENT '菜品ID（菜品删除后仍保留）',
    item_name   VARCHAR(200) NOT NULL COMMENT '操作时的菜品名称',
    user_id     BIGINT       NOT NULL COMMENT '操作人用户ID',
    user_name   VARCHAR(50)  NOT NULL COMMENT '操作时昵称',
    role        VARCHAR(20)  NOT NULL COMMENT '操作人在小组中的角色',
    action_type VARCHAR(20)  NOT NULL COMMENT '行为类型(create/update/delete)',
    field_name  VARCHAR(50)  DEFAULT NULL COMMENT '被修改字段',
    old_value   VARCHAR(500) DEFAULT NULL COMMENT '修改前内容',
    new_value   VARCHAR(500) DEFAULT NULL COMMENT '修改后内容',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_log_group (group_id, created_at DESC),
    INDEX idx_log_recipe (recipe_id),
    INDEX idx_log_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品修改记录';

-- =====================================================================
-- V3：个人隐患检查
-- =====================================================================

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

-- =====================================================================
-- V4：小组专属海报模板
-- =====================================================================

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
