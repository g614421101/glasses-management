CREATE DATABASE IF NOT EXISTS `glasses_management` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `glasses_management`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT 'login username',
  `phone` varchar(20) DEFAULT NULL COMMENT 'phone number',
  `password` varchar(100) NOT NULL COMMENT 'bcrypt password',
  `real_name` varchar(50) DEFAULT NULL COMMENT 'display name',
  `role` varchar(20) NOT NULL DEFAULT 'merchant' COMMENT 'admin or merchant',
  `must_change_password` tinyint(1) NOT NULL DEFAULT '0',
  `disabled` tinyint(1) NOT NULL DEFAULT '0',
  `disabled_time` datetime DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `deleted_time` datetime DEFAULT NULL,
  `force_reset_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_sys_user_deleted` (`deleted`),
  KEY `idx_sys_user_deleted_time` (`deleted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='system users';

DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT 'customer name',
  `phone` varchar(20) NOT NULL COMMENT 'phone number',
  `gender` tinyint DEFAULT '0' COMMENT '1 male, 2 female, 0 unknown',
  `birthday` date DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `deleted_time` datetime DEFAULT NULL,
  `deleted_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_customer_deleted` (`deleted`),
  KEY `idx_customer_deleted_time` (`deleted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='customers';

DROP TABLE IF EXISTS `optometry_record`;
CREATE TABLE `optometry_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint NOT NULL,
  `od_sph` decimal(5,2) DEFAULT NULL,
  `od_cyl` decimal(5,2) DEFAULT NULL,
  `od_axis` int DEFAULT NULL,
  `od_va` varchar(20) DEFAULT NULL,
  `os_sph` decimal(5,2) DEFAULT NULL,
  `os_cyl` decimal(5,2) DEFAULT NULL,
  `os_axis` int DEFAULT NULL,
  `os_va` varchar(20) DEFAULT NULL,
  `od_pd` decimal(5,1) DEFAULT NULL,
  `os_pd` decimal(5,1) DEFAULT NULL,
  `od_ph` decimal(5,1) DEFAULT NULL,
  `os_ph` decimal(5,1) DEFAULT NULL,
  `pd_far` decimal(5,1) DEFAULT NULL,
  `pd_near` decimal(5,1) DEFAULT NULL,
  `add_power` decimal(5,2) DEFAULT NULL,
  `optometrist_name` varchar(50) DEFAULT NULL,
  `exam_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `deleted_time` datetime DEFAULT NULL,
  `deleted_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_opto_deleted` (`deleted`),
  KEY `idx_opto_deleted_time` (`deleted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='optometry records';

DROP TABLE IF EXISTS `sales_record`;
CREATE TABLE `sales_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_no` varchar(50) NOT NULL,
  `customer_id` bigint NOT NULL,
  `optometry_id` bigint DEFAULT NULL,
  `frame_brand` varchar(100) DEFAULT NULL,
  `frame_model` varchar(100) DEFAULT NULL,
  `frame_quantity` int NOT NULL DEFAULT '1',
  `frame_price` decimal(10,2) DEFAULT '0.00',
  `lens_brand` varchar(100) DEFAULT NULL,
  `lens_params` varchar(200) DEFAULT NULL,
  `lens_quantity` int NOT NULL DEFAULT '1',
  `lens_price` decimal(10,2) DEFAULT '0.00',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `sales_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `operator_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `deleted_time` datetime DEFAULT NULL,
  `deleted_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_sales_deleted` (`deleted`),
  KEY `idx_sales_deleted_time` (`deleted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='sales records';

DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `operator_id` bigint DEFAULT NULL COMMENT 'operator user id',
  `operator_name` varchar(50) DEFAULT NULL COMMENT 'operator username',
  `module` varchar(50) NOT NULL COMMENT 'business module',
  `action` varchar(20) NOT NULL COMMENT 'ADD/UPDATE/DELETE/OTHER',
  `method` varchar(10) NOT NULL COMMENT 'HTTP method',
  `uri` varchar(200) NOT NULL COMMENT 'request URI',
  `description` varchar(500) DEFAULT NULL COMMENT 'human readable description',
  `params` text COMMENT 'request params JSON (empty for sensitive APIs)',
  `status` int NOT NULL DEFAULT '200' COMMENT 'result code',
  `message` varchar(500) DEFAULT NULL COMMENT 'result message',
  `cost_ms` bigint DEFAULT NULL COMMENT 'cost ms',
  `ip` varchar(50) DEFAULT NULL COMMENT 'client ip',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_operator_id` (`operator_id`),
  KEY `idx_operation_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='operation audit logs';

SET FOREIGN_KEY_CHECKS = 1;
