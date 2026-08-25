package com.glasses.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Order(0)
public class SchemaCompatibilityInitializer implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        addColumnIfMissing("sys_user", "phone", "TEXT DEFAULT NULL");
        addColumnIfMissing("sys_user", "must_change_password", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing("sys_user", "disabled", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing("sys_user", "disabled_time", "TEXT DEFAULT NULL");
        addColumnIfMissing("sys_user", "deleted", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing("sys_user", "deleted_time", "TEXT DEFAULT NULL");
        addColumnIfMissing("sys_user", "force_reset_time", "TEXT DEFAULT NULL");

        addRecycleColumns("customer");
        addRecycleColumns("optometry_record");
        addColumnIfMissing("optometry_record", "od_ph", "NUMERIC DEFAULT NULL");
        addColumnIfMissing("optometry_record", "os_ph", "NUMERIC DEFAULT NULL");
        addColumnIfMissing("optometry_record", "remark", "TEXT DEFAULT ''");
        addRecycleColumns("sales_record");

        addColumnIfMissing("sales_record", "frame_retail_price", "NUMERIC DEFAULT NULL");
        addColumnIfMissing("sales_record", "lens_retail_price", "NUMERIC DEFAULT NULL");
        addColumnIfMissing("sales_record", "total_retail_price", "NUMERIC DEFAULT NULL");
        addColumnIfMissing("sales_record", "remark", "TEXT DEFAULT ''");
        addColumnIfMissing("sales_record", "frame_quantity", "INTEGER NOT NULL DEFAULT 1");
        addColumnIfMissing("sales_record", "lens_quantity", "INTEGER NOT NULL DEFAULT 1");

        addColumnIfMissing("operation_log", "description", "TEXT DEFAULT NULL");

        addIndexIfMissing("idx_sys_user_deleted", "CREATE INDEX IF NOT EXISTS idx_sys_user_deleted ON sys_user(deleted)");
        addIndexIfMissing("uk_phone", "CREATE UNIQUE INDEX IF NOT EXISTS uk_phone ON sys_user(phone)");
        addIndexIfMissing("idx_sys_user_deleted_time", "CREATE INDEX IF NOT EXISTS idx_sys_user_deleted_time ON sys_user(deleted_time)");
        addIndexIfMissing("idx_customer_deleted", "CREATE INDEX IF NOT EXISTS idx_customer_deleted ON customer(deleted)");
        addIndexIfMissing("idx_customer_deleted_time", "CREATE INDEX IF NOT EXISTS idx_customer_deleted_time ON customer(deleted_time)");
        addIndexIfMissing("idx_customer_id_opto", "CREATE INDEX IF NOT EXISTS idx_customer_id_opto ON optometry_record(customer_id)");
        addIndexIfMissing("idx_opto_deleted", "CREATE INDEX IF NOT EXISTS idx_opto_deleted ON optometry_record(deleted)");
        addIndexIfMissing("idx_opto_deleted_time", "CREATE INDEX IF NOT EXISTS idx_opto_deleted_time ON optometry_record(deleted_time)");
        addIndexIfMissing("idx_customer_id_sales", "CREATE INDEX IF NOT EXISTS idx_customer_id_sales ON sales_record(customer_id)");
        addIndexIfMissing("idx_sales_deleted", "CREATE INDEX IF NOT EXISTS idx_sales_deleted ON sales_record(deleted)");
        addIndexIfMissing("idx_sales_deleted_time", "CREATE INDEX IF NOT EXISTS idx_sales_deleted_time ON sales_record(deleted_time)");

        try {
            List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id, username, phone FROM sys_user WHERE phone IS NULL OR phone = ''");
            for (Map<String, Object> u : users) {
                String username = (String) u.get("username");
                if (username != null && username.matches("^[0-9]{6,20}$")) {
                    jdbcTemplate.update("UPDATE sys_user SET phone = ? WHERE id = ?", username, u.get("id"));
                }
            }
        } catch (Exception e) {
            log.debug("[SchemaCompatibilityInitializer] user phone sync skipped: {}", e.getMessage());
        }

        log.info("[SchemaCompatibilityInitializer] SQLite schema compatibility check completed");
    }

    private void addRecycleColumns(String tableName) {
        addColumnIfMissing(tableName, "deleted", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(tableName, "deleted_time", "TEXT DEFAULT NULL");
        addColumnIfMissing(tableName, "deleted_by", "INTEGER DEFAULT NULL");
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        try {
            List<Map<String, Object>> columns = jdbcTemplate.queryForList("PRAGMA table_info(" + tableName.toLowerCase() + ")");
            boolean exists = columns.stream()
                    .anyMatch(col -> columnName.equalsIgnoreCase(String.valueOf(col.get("name"))));
            if (!exists) {
                jdbcTemplate.execute("ALTER TABLE " + tableName.toLowerCase() + " ADD COLUMN " + columnName.toLowerCase() + " " + definition);
            }
        } catch (Exception e) {
            log.warn("[SchemaCompatibilityInitializer] addColumnIfMissing failed for {}.{}: {}", tableName, columnName, e.getMessage());
        }
    }

    private void addIndexIfMissing(String indexName, String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
            log.debug("[SchemaCompatibilityInitializer] skip index {}", indexName);
        }
    }
}
