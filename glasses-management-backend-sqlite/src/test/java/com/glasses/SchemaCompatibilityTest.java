package com.glasses;

import com.glasses.config.SchemaCompatibilityInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 SchemaCompatibilityInitializer 在老版本 SQLite 数据库上的自动升级能力:
 * 老库缺列、缺索引、缺数据时,调用 run() 后应全部自动补齐(模拟"安装新版本启动"场景)。
 */
public class SchemaCompatibilityTest {

    @Test
    public void testUpgradeOldSchema() throws Exception {
        JdbcTemplate jdbc = createOldSchemaDatabase();
        SchemaCompatibilityInitializer initializer = new SchemaCompatibilityInitializer();
        Field field = SchemaCompatibilityInitializer.class.getDeclaredField("jdbcTemplate");
        field.setAccessible(true);
        field.set(initializer, jdbc);
        initializer.run(new DefaultApplicationArguments(new String[0]));

        // 后期新增的列应被自动补齐
        assertColumn(jdbc, "sys_user", "phone");
        assertColumn(jdbc, "sys_user", "must_change_password");
        assertColumn(jdbc, "sys_user", "disabled");
        assertColumn(jdbc, "sys_user", "deleted");
        assertColumn(jdbc, "sys_user", "force_reset_time");
        assertColumn(jdbc, "customer", "deleted");
        assertColumn(jdbc, "customer", "deleted_by");
        assertColumn(jdbc, "optometry_record", "od_ph");
        assertColumn(jdbc, "optometry_record", "os_ph");
        assertColumn(jdbc, "optometry_record", "remark");
        assertColumn(jdbc, "sales_record", "frame_quantity");
        assertColumn(jdbc, "sales_record", "lens_quantity");
        assertColumn(jdbc, "sales_record", "total_retail_price");
        assertColumn(jdbc, "operation_log", "description");

        // 索引应被自动补齐
        assertIndex(jdbc, "idx_sys_user_deleted");
        assertIndex(jdbc, "uk_phone");
        assertIndex(jdbc, "idx_customer_id_opto");
        assertIndex(jdbc, "idx_sales_deleted_time");

        // 数据回填:纯数字用户名 → phone
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = '13800138000' AND phone = '13800138000'",
                Integer.class);
        assertEquals(1, count, "纯数字用户名的 phone 应被回填");
    }

    /** 模拟老版本库:5 张表均已存在,但缺后期新增的列与索引 */
    private JdbcTemplate createOldSchemaDatabase() {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource(
                "jdbc:sqlite::memory:?busy_timeout=5000", true);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE sys_user ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL, "
                + "password TEXT NOT NULL, "
                + "real_name TEXT DEFAULT NULL, "
                + "role TEXT NOT NULL DEFAULT 'merchant', "
                + "create_time TEXT DEFAULT (datetime('now', 'localtime')))");
        jdbc.execute("CREATE TABLE customer ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "phone TEXT NOT NULL, "
                + "create_time TEXT DEFAULT (datetime('now', 'localtime')))");
        jdbc.execute("CREATE TABLE optometry_record ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "customer_id INTEGER NOT NULL, "
                + "create_time TEXT DEFAULT (datetime('now', 'localtime')))");
        jdbc.execute("CREATE TABLE sales_record ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "record_no TEXT NOT NULL, "
                + "customer_id INTEGER NOT NULL, "
                + "frame_price NUMERIC DEFAULT 0.00, "
                + "lens_price NUMERIC DEFAULT 0.00, "
                + "total_amount NUMERIC NOT NULL DEFAULT 0.00, "
                + "create_time TEXT DEFAULT (datetime('now', 'localtime')))");
        jdbc.execute("CREATE TABLE operation_log ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "module TEXT NOT NULL, "
                + "action TEXT NOT NULL, "
                + "method TEXT NOT NULL, "
                + "uri TEXT NOT NULL, "
                + "create_time TEXT DEFAULT (datetime('now', 'localtime')))");
        // 老用户:纯数字用户名、无 phone,等待启动时回填
        jdbc.update("INSERT INTO sys_user (username, password) VALUES ('13800138000', 'x')");
        return jdbc;
    }

    private void assertColumn(JdbcTemplate jdbc, String table, String column) {
        List<Map<String, Object>> columns = jdbc.queryForList("PRAGMA table_info(" + table.toLowerCase() + ")");
        boolean exists = columns.stream()
                .anyMatch(col -> column.equalsIgnoreCase(String.valueOf(col.get("name"))));
        assertTrue(exists, "列 " + table + "." + column + " 应被自动补齐");
    }

    private void assertIndex(JdbcTemplate jdbc, String index) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'index' AND name = ?",
                Integer.class, index.toLowerCase());
        assertEquals(1, count, "索引 " + index + " 应被自动补齐");
    }
}
