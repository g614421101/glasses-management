-- Database setup script for Glasses Management System
CREATE DATABASE IF NOT EXISTS `glasses_management` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `glasses_management`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '登录名（新用户使用手机号）',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名/操作员名称',
  `role` varchar(20) NOT NULL DEFAULT 'merchant' COMMENT '角色：admin超管, merchant商户',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 注意：超管账号 admin 由后端 DataInitializer 在应用启动时自动创建/刷新，
-- 密码将由 Java BCrypt.hashpw("REMOVED_ADMIN_PASSWORD") 生成合法哈希值写入数据库。
-- 此处无需手动 INSERT admin 记录。

-- ----------------------------
-- Table structure for customer
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '顾客姓名',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `gender` tinyint DEFAULT '0' COMMENT '性别：1男 2女 0未知',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='顾客表';

-- ----------------------------
-- Table structure for optometry_record
-- ----------------------------
DROP TABLE IF EXISTS `optometry_record`;
CREATE TABLE `optometry_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL COMMENT '所属顾客ID',
  -- 右眼参数
  `od_sph` decimal(5,2) DEFAULT NULL COMMENT '右眼球镜(SPH)',
  `od_cyl` decimal(5,2) DEFAULT NULL COMMENT '右眼柱镜(CYL)',
  `od_axis` int DEFAULT NULL COMMENT '右眼轴位(AXIS)',
  `od_va` varchar(20) DEFAULT NULL COMMENT '右眼矫正视力',
  -- 左眼参数
  `os_sph` decimal(5,2) DEFAULT NULL COMMENT '左眼球镜(SPH)',
  `os_cyl` decimal(5,2) DEFAULT NULL COMMENT '左眼柱镜(CYL)',
  `os_axis` int DEFAULT NULL COMMENT '左眼轴位(AXIS)',
  `os_va` varchar(20) DEFAULT NULL COMMENT '左眼矫正视力',
  -- 其他
  `pd_far` decimal(5,1) DEFAULT NULL COMMENT '远用瞳距',
  `pd_near` decimal(5,1) DEFAULT NULL COMMENT '近用瞳距',
  `add_power` decimal(5,2) DEFAULT NULL COMMENT '下加光(ADD)',
  `optometrist_name` varchar(50) DEFAULT NULL COMMENT '验光师',
  `exam_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '验光日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验光记录表';

-- ----------------------------
-- Table structure for sales_record
-- ----------------------------
DROP TABLE IF EXISTS `sales_record`;
CREATE TABLE `sales_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_no` varchar(50) NOT NULL COMMENT '销售单号',
  `customer_id` bigint NOT NULL COMMENT '所属顾客ID',
  `optometry_id` bigint DEFAULT NULL COMMENT '关联验光记录ID',
  -- 镜架信息
  `frame_brand` varchar(100) DEFAULT NULL COMMENT '镜架品牌',
  `frame_model` varchar(100) DEFAULT NULL COMMENT '镜架型号',
  `frame_price` decimal(10,2) DEFAULT '0.00' COMMENT '镜架价格',
  -- 镜片信息
  `lens_brand` varchar(100) DEFAULT NULL COMMENT '镜片品牌',
  `lens_params` varchar(200) DEFAULT NULL COMMENT '镜片参数(折射率/功能等)',
  `lens_price` decimal(10,2) DEFAULT '0.00' COMMENT '镜片价格',
  
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总金额',
  `sales_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '销售/配镜日期',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配镜销售记录表';

SET FOREIGN_KEY_CHECKS = 1;
