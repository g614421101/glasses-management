package com.glasses.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(0)
public class SchemaCompatibilityInitializer implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        addColumnIfMissing("sys_user", "phone", "varchar(20) DEFAULT NULL");
        addColumnIfMissing("sys_user", "must_change_password", "tinyint(1) NOT NULL DEFAULT 0");
        addColumnIfMissing("sys_user", "disabled", "tinyint(1) NOT NULL DEFAULT 0");
        addColumnIfMissing("sys_user", "disabled_time", "datetime DEFAULT NULL");
        addColumnIfMissing("sys_user", "deleted", "tinyint(1) NOT NULL DEFAULT 0");
        addColumnIfMissing("sys_user", "deleted_time", "datetime DEFAULT NULL");

        addRecycleColumns("customer");
        addRecycleColumns("optometry_record");
        addColumnIfMissing("optometry_record", "remark", "varchar(500) DEFAULT NULL");
        addRecycleColumns("sales_record");

        addColumnIfMissing("sales_record", "frame_retail_price", "decimal(10,2) DEFAULT NULL");
        addColumnIfMissing("sales_record", "lens_retail_price", "decimal(10,2) DEFAULT NULL");
        addColumnIfMissing("sales_record", "total_retail_price", "decimal(10,2) DEFAULT NULL");
        addColumnIfMissing("sales_record", "remark", "varchar(500) DEFAULT NULL");

        addIndexIfMissing("sys_user", "uk_phone", "CREATE UNIQUE INDEX uk_phone ON sys_user(phone)");
        addIndexIfMissing("sys_user", "idx_sys_user_deleted", "CREATE INDEX idx_sys_user_deleted ON sys_user(deleted)");
        addIndexIfMissing("sys_user", "idx_sys_user_deleted_time", "CREATE INDEX idx_sys_user_deleted_time ON sys_user(deleted_time)");
        addIndexIfMissing("customer", "idx_customer_deleted", "CREATE INDEX idx_customer_deleted ON customer(deleted)");
        addIndexIfMissing("customer", "idx_customer_deleted_time", "CREATE INDEX idx_customer_deleted_time ON customer(deleted_time)");
        addIndexIfMissing("optometry_record", "idx_opto_deleted", "CREATE INDEX idx_opto_deleted ON optometry_record(deleted)");
        addIndexIfMissing("optometry_record", "idx_opto_deleted_time", "CREATE INDEX idx_opto_deleted_time ON optometry_record(deleted_time)");
        addIndexIfMissing("sales_record", "idx_sales_deleted", "CREATE INDEX idx_sales_deleted ON sales_record(deleted)");
        addIndexIfMissing("sales_record", "idx_sales_deleted_time", "CREATE INDEX idx_sales_deleted_time ON sales_record(deleted_time)");

        jdbcTemplate.update("UPDATE sys_user SET phone = username WHERE (phone IS NULL OR phone = '') AND username REGEXP '^[0-9]{6,20}$'");
        log.info("[SchemaCompatibilityInitializer] schema compatibility check completed");
    }

    private void addRecycleColumns(String tableName) {
        addColumnIfMissing(tableName, "deleted", "tinyint(1) NOT NULL DEFAULT 0");
        addColumnIfMissing(tableName, "deleted_time", "datetime DEFAULT NULL");
        addColumnIfMissing(tableName, "deleted_by", "bigint DEFAULT NULL");
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private void addIndexIfMissing(String tableName, String indexName, String sql) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class,
                tableName,
                indexName
        );
        if (count != null && count == 0) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception ignored) {
                log.debug("[SchemaCompatibilityInitializer] skip index {} on {}", indexName, tableName);
            }
        }
    }
}
