-- 创建数据库
CREATE DATABASE IF NOT EXISTS bookkeeping DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bookkeeping;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  openid VARCHAR(64) NOT NULL COMMENT '微信openid',
  unionid VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
  
ickname VARCHAR(64) DEFAULT '' COMMENT '昵称',
  avatar VARCHAR(255) DEFAULT '' COMMENT '头像',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 账目表
CREATE TABLE IF NOT EXISTS account (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '账目ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  	ype TINYINT NOT NULL COMMENT '类型:1收入 2支出 3转账',
  amount DECIMAL(10,2) NOT NULL COMMENT '金额',
  rom_account VARCHAR(64) DEFAULT '' COMMENT '转出账户',
  	o_account VARCHAR(64) DEFAULT '' COMMENT '转入账户',
  category VARCHAR(32) DEFAULT '' COMMENT '分类/原因',
  
emark VARCHAR(255) DEFAULT '' COMMENT '备注',
  date DATE NOT NULL COMMENT '日期',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账目表';

-- 插入测试用户
INSERT INTO user (openid, unionid, 
ickname, avatar) VALUES
('test_openid_001', 'test_unionid_001', '测试用户', '');

-- 插入测试账目数据
INSERT INTO account (user_id, 	ype, amount, rom_account, 	o_account, category,
emark, date) VALUES
(1, 2, 25.00, '微信', '', '午餐', '公司附近餐厅', '2024-01-15'),
(1, 2, 5000.00, '工资卡', '', '工资', '', '2024-01-15'),
(1, 1, 30.00, '', '微信', '交通费', '地铁', '2024-01-16'),
(1, 3, 1000.00, '银行卡', '支付宝', '转账', '', '2024-01-16');