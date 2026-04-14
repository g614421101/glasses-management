-- H2 Compatible Schema for Glasses Management System
-- Database setup
-- H2 creates the database based on the connection string URL

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'merchant',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
);

-- ----------------------------
-- Table structure for customer
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `gender` tinyint DEFAULT 0,
  `birthday` date DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
);

-- ----------------------------
-- Table structure for optometry_record
-- ----------------------------
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
  `pd_far` decimal(5,1) DEFAULT NULL,
  `pd_near` decimal(5,1) DEFAULT NULL,
  `add_power` decimal(5,2) DEFAULT NULL,
  `optometrist_name` varchar(50) DEFAULT NULL,
  `exam_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
CREATE INDEX `idx_customer_id_opto` ON `optometry_record` (`customer_id`);

-- ----------------------------
-- Table structure for sales_record
-- ----------------------------
DROP TABLE IF EXISTS `sales_record`;
CREATE TABLE `sales_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_no` varchar(50) NOT NULL,
  `customer_id` bigint NOT NULL,
  `optometry_id` bigint DEFAULT NULL,
  `frame_brand` varchar(100) DEFAULT NULL,
  `frame_model` varchar(100) DEFAULT NULL,
  `frame_price` decimal(10,2) DEFAULT 0.00,
  `lens_brand` varchar(100) DEFAULT NULL,
  `lens_params` varchar(200) DEFAULT NULL,
  `lens_price` decimal(10,2) DEFAULT 0.00,
  `total_amount` decimal(10,2) NOT NULL DEFAULT 0.00,
  `sales_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `operator_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
);
CREATE INDEX `idx_customer_id_sales` ON `sales_record` (`customer_id`);
