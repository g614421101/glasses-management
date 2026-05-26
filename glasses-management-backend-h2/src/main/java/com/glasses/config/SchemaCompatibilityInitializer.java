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
        addColumnIfMissing("SYS_USER", "PHONE", "varchar(20) DEFAULT NULL");
        addColumnIfMissing("SYS_USER", "MUST_CHANGE_PASSWORD", "boolean NOT NULL DEFAULT false");
        addColumnIfMissing("SYS_USER", "DISABLED", "boolean NOT NULL DEFAULT false");
        addColumnIfMissing("SYS_USER", "DISABLED_TIME", "datetime DEFAULT NULL");
        addColumnIfMissing("SYS_USER", "DELETED", "boolean NOT NULL DEFAULT false");
        addColumnIfMissing("SYS_USER", "DELETED_TIME", "datetime DEFAULT NULL");

        addRecycleColumns("CUSTOMER");
        addRecycleColumns("OPTOMETRY_RECORD");
        addColumnIfMissing("OPTOMETRY_RECORD", "REMARK", "varchar(500) DEFAULT NULL");
        addRecycleColumns("SALES_RECORD");

        addColumnIfMissing("SALES_RECORD", "FRAME_RETAIL_PRICE", "decimal(10,2) DEFAULT NULL");
        addColumnIfMissing("SALES_RECORD", "LENS_RETAIL_PRICE", "decimal(10,2) DEFAULT NULL");
        addColumnIfMissing("SALES_RECORD", "TOTAL_RETAIL_PRICE", "decimal(10,2) DEFAULT NULL");
        addColumnIfMissing("SALES_RECORD", "REMARK", "varchar(500) DEFAULT NULL");

        addIndexIfMissing("IDX_SYS_USER_DELETED", "CREATE INDEX IF NOT EXISTS idx_sys_user_deleted ON sys_user(deleted)");
        addIndexIfMissing("UK_PHONE", "CREATE UNIQUE INDEX IF NOT EXISTS uk_phone ON sys_user(phone)");
        addIndexIfMissing("IDX_SYS_USER_DELETED_TIME", "CREATE INDEX IF NOT EXISTS idx_sys_user_deleted_time ON sys_user(deleted_time)");
        addIndexIfMissing("IDX_CUSTOMER_DELETED", "CREATE INDEX IF NOT EXISTS idx_customer_deleted ON customer(deleted)");
        addIndexIfMissing("IDX_CUSTOMER_DELETED_TIME", "CREATE INDEX IF NOT EXISTS idx_customer_deleted_time ON customer(deleted_time)");
        addIndexIfMissing("IDX_CUSTOMER_ID_OPTO", "CREATE INDEX IF NOT EXISTS idx_customer_id_opto ON optometry_record(customer_id)");
        addIndexIfMissing("IDX_OPTO_DELETED", "CREATE INDEX IF NOT EXISTS idx_opto_deleted ON optometry_record(deleted)");
        addIndexIfMissing("IDX_OPTO_DELETED_TIME", "CREATE INDEX IF NOT EXISTS idx_opto_deleted_time ON optometry_record(deleted_time)");
        addIndexIfMissing("IDX_CUSTOMER_ID_SALES", "CREATE INDEX IF NOT EXISTS idx_customer_id_sales ON sales_record(customer_id)");
        addIndexIfMissing("IDX_SALES_DELETED", "CREATE INDEX IF NOT EXISTS idx_sales_deleted ON sales_record(deleted)");
        addIndexIfMissing("IDX_SALES_DELETED_TIME", "CREATE INDEX IF NOT EXISTS idx_sales_deleted_time ON sales_record(deleted_time)");

        jdbcTemplate.update("UPDATE sys_user SET phone = username WHERE (phone IS NULL OR phone = '') AND REGEXP_LIKE(username, '^[0-9]{6,20}$')");
        log.info("[SchemaCompatibilityInitializer] H2 schema compatibility check completed");
    }

    private void addRecycleColumns(String tableName) {
        addColumnIfMissing(tableName, "DELETED", "boolean NOT NULL DEFAULT false");
        addColumnIfMissing(tableName, "DELETED_TIME", "datetime DEFAULT NULL");
        addColumnIfMissing(tableName, "DELETED_BY", "bigint DEFAULT NULL");
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = ? AND UPPER(COLUMN_NAME) = ?",
                Integer.class,
                tableName,
                columnName
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName.toLowerCase() + " ADD COLUMN " + columnName.toLowerCase() + " " + definition);
        }
    }

    private void addIndexIfMissing(String indexName, String sql) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES WHERE UPPER(INDEX_NAME) = ?",
                Integer.class,
                indexName
        );
        if (count != null && count == 0) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception ignored) {
                log.debug("[SchemaCompatibilityInitializer] skip index {}", indexName);
            }
        }
    }
}
