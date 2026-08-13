-- 智慧食堂菜谱生成系统 V2 迁移脚本（幂等：可重复执行，不会因列/索引已存在而报错）
-- 新增：用户 / 小组 / 小组成员 / 菜品修改记录
-- 扩展：菜谱归属小组、当前菜单标记、菜品描述、图片审核字段、海报归属
USE xingbang_menu;

-- ============ 用户表 ============
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

-- ============ 小组表 ============
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

-- ============ 小组成员表 ============
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

-- ============ 菜品修改记录表 ============
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
-- 幂等辅助存储过程：仅当列/索引不存在时才执行 ALTER
-- =====================================================================
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

CREATE PROCEDURE add_index_if_not_exists(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND INDEX_NAME = p_index
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index, '` ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- ============ 现有表扩展 ============
-- 菜谱：归属小组 / 当前菜单标记 / 创建人
CALL add_column_if_not_exists('menu_recipe', 'group_id',
    'BIGINT NOT NULL DEFAULT 0 COMMENT ''所属小组ID(0=历史未分配)'' AFTER canteen_name');
CALL add_column_if_not_exists('menu_recipe', 'is_current',
    'TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否为小组当前菜单'' AFTER group_id');
CALL add_column_if_not_exists('menu_recipe', 'creator_id',
    'BIGINT DEFAULT NULL COMMENT ''创建人用户ID'' AFTER is_current');
CALL add_index_if_not_exists('menu_recipe', 'idx_recipe_group',
    '(group_id, is_deleted)');

-- 菜品：描述
CALL add_column_if_not_exists('menu_item', 'description',
    'VARCHAR(500) DEFAULT NULL COMMENT ''菜品描述'' AFTER name');

-- 图片：小组 / 上传人 / 审核字段
CALL add_column_if_not_exists('image_asset', 'group_id',
    'BIGINT DEFAULT 0 COMMENT ''所属小组ID'' AFTER recipe_id');
CALL add_column_if_not_exists('image_asset', 'uploader_id',
    'BIGINT DEFAULT NULL COMMENT ''上传人用户ID'' AFTER group_id');
CALL add_column_if_not_exists('image_asset', 'review_status',
    'VARCHAR(20) NOT NULL DEFAULT ''pending'' COMMENT ''审核状态(pending/approved/rejected)'' AFTER uploader_id');
CALL add_column_if_not_exists('image_asset', 'reviewer_id',
    'BIGINT DEFAULT NULL COMMENT ''审核人用户ID'' AFTER review_status');
CALL add_column_if_not_exists('image_asset', 'reviewed_at',
    'DATETIME DEFAULT NULL COMMENT ''审核时间'' AFTER reviewer_id');
CALL add_column_if_not_exists('image_asset', 'review_note',
    'VARCHAR(500) DEFAULT NULL COMMENT ''审核备注'' AFTER reviewed_at');
CALL add_index_if_not_exists('image_asset', 'idx_image_group_review',
    '(group_id, review_status, is_deleted)');

-- 海报：归属小组 / 生成人
CALL add_column_if_not_exists('menu_poster', 'group_id',
    'BIGINT DEFAULT 0 COMMENT ''所属小组ID'' AFTER recipe_id');
CALL add_column_if_not_exists('menu_poster', 'creator_id',
    'BIGINT DEFAULT NULL COMMENT ''生成人用户ID'' AFTER group_id');

-- 成员：相册上传权限
CALL add_column_if_not_exists('group_member', 'album_permission',
    'TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''相册上传权限(0=未开通,1=申请中,2=已开通)''');

-- 清理辅助存储过程（可选，保留亦无副作用）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
