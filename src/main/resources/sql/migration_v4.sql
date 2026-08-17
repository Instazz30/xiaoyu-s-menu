-- PRD_4 阶段 1：包厢菜单管理（复用 group_member 的 admin/member 权限）
USE xingbang_menu;

CREATE TABLE IF NOT EXISTS private_room (
  id BIGINT NOT NULL AUTO_INCREMENT, group_id BIGINT NOT NULL, name VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'enabled', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT(1) NOT NULL DEFAULT 0, PRIMARY KEY(id),
  UNIQUE KEY uk_private_room_name(group_id,name,is_deleted), KEY idx_private_room_group(group_id,is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='包厢';

CREATE TABLE IF NOT EXISTS private_room_config (
  id BIGINT NOT NULL AUTO_INCREMENT, group_id BIGINT NOT NULL, reviewer_id BIGINT DEFAULT NULL,
  notification_email VARCHAR(200) DEFAULT NULL, email_enabled TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(id), UNIQUE KEY uk_private_room_config_group(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='包厢服务配置';

CREATE TABLE IF NOT EXISTS dish_catalog (
  id BIGINT NOT NULL AUTO_INCREMENT, group_id BIGINT NOT NULL, dish_code VARCHAR(32) DEFAULT NULL,
  name VARCHAR(200) NOT NULL, category VARCHAR(30) NOT NULL, min_people INT NOT NULL DEFAULT 1,
  max_people INT NOT NULL DEFAULT 999, ingredients VARCHAR(1000) NOT NULL DEFAULT '',
  allergens VARCHAR(500) DEFAULT NULL, flavors VARCHAR(500) DEFAULT NULL, spicy_level VARCHAR(20) DEFAULT NULL,
  cooking_methods VARCHAR(500) DEFAULT NULL, supply_status VARCHAR(20) NOT NULL DEFAULT 'available',
  reservation_required TINYINT(1) NOT NULL DEFAULT 0, remark VARCHAR(500) DEFAULT NULL,
  version INT NOT NULL DEFAULT 1, created_by BIGINT NOT NULL, updated_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT(1) NOT NULL DEFAULT 0, PRIMARY KEY(id),
  KEY idx_dish_group_filter(group_id,is_deleted,category,supply_status), KEY idx_dish_group_name(group_id,name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='包厢正式菜品';

CREATE TABLE IF NOT EXISTS dish_spec (
  id BIGINT NOT NULL AUTO_INCREMENT, dish_id BIGINT NOT NULL, name VARCHAR(80) NOT NULL,
  price_type VARCHAR(20) NOT NULL DEFAULT 'fixed', price DECIMAL(12,2) DEFAULT NULL,
  market_price_date DATE DEFAULT NULL, status VARCHAR(20) NOT NULL DEFAULT 'enabled',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT(1) NOT NULL DEFAULT 0, PRIMARY KEY(id), KEY idx_dish_spec_dish(dish_id,is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品规格价格';

CREATE TABLE IF NOT EXISTS dish_change_log (
  id BIGINT NOT NULL AUTO_INCREMENT, group_id BIGINT NOT NULL, dish_id BIGINT NOT NULL,
  operator_id BIGINT NOT NULL, field_name VARCHAR(50) NOT NULL, old_value TEXT, new_value TEXT,
  reason VARCHAR(500) DEFAULT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(id), KEY idx_dish_log(group_id,dish_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='正式菜品字段修改日志';

CREATE TABLE IF NOT EXISTS dish_import_batch (
  id BIGINT NOT NULL AUTO_INCREMENT, group_id BIGINT NOT NULL, source_filename VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'preview', operator_id BIGINT NOT NULL,
  total_count INT NOT NULL DEFAULT 0, success_count INT NOT NULL DEFAULT 0,
  update_count INT NOT NULL DEFAULT 0, ignore_count INT NOT NULL DEFAULT 0, exception_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, committed_at DATETIME DEFAULT NULL,
  PRIMARY KEY(id), KEY idx_import_batch_group(group_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品导入批次';

CREATE TABLE IF NOT EXISTS dish_import_row (
  id BIGINT NOT NULL AUTO_INCREMENT, batch_id BIGINT NOT NULL, row_type VARCHAR(20) NOT NULL DEFAULT 'dish',
  source_sheet VARCHAR(100) NOT NULL, source_cell VARCHAR(30) NOT NULL, raw_name VARCHAR(300) DEFAULT NULL,
  raw_spec VARCHAR(100) DEFAULT NULL, raw_price VARCHAR(100) DEFAULT NULL, dish_name VARCHAR(200) DEFAULT NULL,
  category VARCHAR(30) DEFAULT NULL, spec_name VARCHAR(80) DEFAULT NULL, price_type VARCHAR(20) DEFAULT NULL,
  price DECIMAL(12,2) DEFAULT NULL, action_type VARCHAR(20) NOT NULL,
  matched_dish_id BIGINT DEFAULT NULL, blocking TINYINT(1) NOT NULL DEFAULT 0,
  exception_reason VARCHAR(1000) DEFAULT NULL, confirmed TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(id), KEY idx_import_row_batch(batch_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品导入预览行';

CREATE TABLE IF NOT EXISTS meal_rule (
  id BIGINT NOT NULL AUTO_INCREMENT, group_id BIGINT NOT NULL, name VARCHAR(100) NOT NULL,
  people_min INT NOT NULL, people_max INT NOT NULL, standard_min DECIMAL(12,2) NOT NULL,
  standard_max DECIMAL(12,2) NOT NULL, cold_count INT NOT NULL DEFAULT 0, meat_count INT NOT NULL,
  half_meat_count INT NOT NULL DEFAULT 0, vegetable_count INT NOT NULL, soup_count INT NOT NULL DEFAULT 0,
  staple_count INT NOT NULL DEFAULT 0, min_budget_usage DECIMAL(5,2) NOT NULL DEFAULT 0,
  recent_repeat_days INT NOT NULL DEFAULT 0, priority INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'enabled', version INT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT(1) NOT NULL DEFAULT 0, PRIMARY KEY(id), KEY idx_meal_rule_match(group_id,status,is_deleted,priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配餐规则';

CREATE TABLE IF NOT EXISTS dining_task (
  id BIGINT NOT NULL AUTO_INCREMENT, group_id BIGINT NOT NULL, room_id BIGINT NOT NULL,
  dining_date DATE NOT NULL, meal_time VARCHAR(30) NOT NULL, people_count INT NOT NULL,
  per_capita_standard DECIMAL(12,2) NOT NULL, total_budget DECIMAL(12,2) NOT NULL,
  avoid_tags VARCHAR(500) DEFAULT NULL, allergens VARCHAR(500) DEFAULT NULL, taste_preferences VARCHAR(500) DEFAULT NULL,
  reception_scene VARCHAR(100) DEFAULT NULL, remark VARCHAR(500) DEFAULT NULL,
  matched_rule_id BIGINT DEFAULT NULL, creator_id BIGINT NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'draft',
  version INT NOT NULL DEFAULT 1, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT(1) NOT NULL DEFAULT 0, PRIMARY KEY(id),
  KEY idx_dining_task_group_date(group_id,dining_date,status,is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='包厢用餐任务';

CREATE TABLE IF NOT EXISTS dining_menu_item (
  id BIGINT NOT NULL AUTO_INCREMENT, task_id BIGINT NOT NULL, dish_id BIGINT NOT NULL, spec_id BIGINT NOT NULL,
  quantity INT NOT NULL, sort_order INT NOT NULL DEFAULT 0, dish_name_snapshot VARCHAR(200) NOT NULL,
  category_snapshot VARCHAR(30) NOT NULL, spec_name_snapshot VARCHAR(80) NOT NULL,
  unit_price_snapshot DECIMAL(12,2) NOT NULL, subtotal DECIMAL(12,2) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(id), UNIQUE KEY uk_task_dish_spec(task_id,dish_id,spec_id), KEY idx_menu_item_task(task_id,sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='当前菜单项';

CREATE TABLE IF NOT EXISTS dining_menu_version (
  id BIGINT NOT NULL AUTO_INCREMENT, task_id BIGINT NOT NULL, version_no INT NOT NULL, version_type VARCHAR(30) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL, snapshot_json LONGTEXT NOT NULL, operator_id BIGINT NOT NULL,
  reason VARCHAR(500) DEFAULT NULL, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(id), UNIQUE KEY uk_task_version(task_id,version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单不可变版本';

CREATE TABLE IF NOT EXISTS menu_review (
  id BIGINT NOT NULL AUTO_INCREMENT, task_id BIGINT NOT NULL, version_id BIGINT NOT NULL,
  reviewer_id BIGINT NOT NULL, result VARCHAR(20) NOT NULL, note VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(id), KEY idx_menu_review_task(task_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单审核';

CREATE TABLE IF NOT EXISTS business_notification (
  id BIGINT NOT NULL AUTO_INCREMENT, group_id BIGINT NOT NULL, recipient_id BIGINT NOT NULL,
  type VARCHAR(30) NOT NULL, business_id BIGINT NOT NULL, title VARCHAR(200) NOT NULL, content VARCHAR(500) DEFAULT NULL,
  read_at DATETIME DEFAULT NULL, acknowledged_at DATETIME DEFAULT NULL, closed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(id),
  KEY idx_notification_recipient(recipient_id,closed_at,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='包厢业务站内待办';
