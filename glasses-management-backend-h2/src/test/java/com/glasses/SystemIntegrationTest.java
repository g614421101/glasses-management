package com.glasses;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mybatisflex.core.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glasses.dto.LoginDTO;
import com.glasses.entity.Customer;
import com.glasses.entity.OperationLog;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.entity.SysUser;
import com.glasses.mapper.OperationLogMapper;
import com.glasses.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 测试结束后自动回滚，保持数据库干净
public class SystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    private String saToken;

    @BeforeEach
    public void setup() throws Exception {
        // 创建一个专用的测试管理员账号
        SysUser testAdmin = sysUserMapper.selectOneByQuery(
                QueryWrapper.create()
                        .from(SysUser.class)
                        .where(SysUser::getUsername).eq("testadmin"));
        if (testAdmin == null) {
            testAdmin = new SysUser();
            testAdmin.setUsername("testadmin");
            testAdmin.setPassword(BCrypt.hashpw("123456"));
            testAdmin.setRealName("Test Admin");
            testAdmin.setRole("admin");
            testAdmin.setDeleted(false);
            testAdmin.setDisabled(false);
            testAdmin.setMustChangePassword(false);
            sysUserMapper.insert(testAdmin);
        } else {
            testAdmin.setPassword(BCrypt.hashpw("123456"));
            sysUserMapper.update(testAdmin, true);
        }

        // 使用 MockMvc 调用登录接口获取 token
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testadmin");
        loginDTO.setPassword("123456");

        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String jsonResponse = loginResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        saToken = rootNode.path("data").path("token").asText();
        assertNotNull(saToken, "无法获取测试登录的 Token");
    }

    @Test
    public void testFullSystemLifecycle() throws Exception {
        // 1. 创建顾客 (Create Customer)
        Customer customer = new Customer();
        customer.setName("集成测试顾客");
        customer.setPhone("13999999999");
        customer.setGender(1);
        customer.setBirthday(DateUtil.parse("1990-01-01"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/customer/add")
                .header("Authorization", saToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 查询并获取刚刚创建的顾客 ID
        MvcResult pageResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/customer/page")
                .param("keyword", "13999999999")
                .header("Authorization", saToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        
        String jsonResponse = pageResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        Long customerId = rootNode.path("data").path("records").get(0).path("id").asLong();
        assertTrue(customerId > 0, "成功获取顾客 ID");

        // 2. 创建验光记录 (Create Optometry Record)
        OptometryRecord optometry = new OptometryRecord();
        optometry.setCustomerId(customerId);
        optometry.setOdSph(new BigDecimal("-2.00"));
        optometry.setOsSph(new BigDecimal("-2.50"));
        optometry.setOdCyl(new BigDecimal("-0.50"));
        optometry.setOsCyl(new BigDecimal("-0.75"));
        optometry.setOdAxis(180);
        optometry.setOsAxis(175);
        optometry.setOdPd(new BigDecimal("32.0"));
        optometry.setOsPd(new BigDecimal("33.0"));
        optometry.setPdFar(new BigDecimal("65.0"));
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/optometry/add")
                .header("Authorization", saToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(optometry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult optoListResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/optometry/customer/{customerId}", customerId)
                .header("Authorization", saToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        
        jsonResponse = optoListResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        rootNode = objectMapper.readTree(jsonResponse);
        Long optoId = rootNode.path("data").get(0).path("id").asLong();
        assertTrue(optoId > 0, "成功获取验光记录 ID");

        // 3. 创建销售记录 (Create Sales Record)
        SalesRecord sales = new SalesRecord();
        sales.setCustomerId(customerId);
        sales.setOptometryId(optoId);
        sales.setFrameBrand("测试镜架");
        sales.setFramePrice(new BigDecimal("500.00"));
        sales.setLensBrand("测试镜片");
        sales.setLensPrice(new BigDecimal("800.00"));
        sales.setTotalAmount(new BigDecimal("1300.00"));
        sales.setSalesDate(new Date());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/sales/add")
                .header("Authorization", saToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sales)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult salesListResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/sales/customer/{customerId}", customerId)
                .header("Authorization", saToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        jsonResponse = salesListResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        rootNode = objectMapper.readTree(jsonResponse);
        Long salesId = rootNode.path("data").get(0).path("id").asLong();
        assertTrue(salesId > 0, "成功获取销售记录 ID");

        // 4. 测试档案和统计接口 (Test Archive/Statistics)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/archive/{customerId}", customerId)
                .header("Authorization", saToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 5. 删除记录进行软删除测试 (Soft Delete tests)
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/sales/{id}", salesId)
                .header("Authorization", saToken))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/optometry/{id}", optoId)
                .header("Authorization", saToken))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/customer/{id}", customerId)
                .header("Authorization", saToken))
                .andExpect(status().isOk());

        // 6. 检查回收站 (Check Recycle Bin)
        MvcResult binResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/recycle-bin")
                .param("type", "customer")
                .header("Authorization", saToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        
        jsonResponse = binResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(jsonResponse.contains("13999999999"), "回收站中应包含被删除的顾客信息");

        // 7. 从回收站恢复 (Recover from Recycle Bin)
        mockMvc.perform(MockMvcRequestBuilders.post("/api/recycle-bin/restore/customer/{id}", customerId)
                .header("Authorization", saToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证顾客已经恢复并能够被正常查询
        mockMvc.perform(MockMvcRequestBuilders.get("/api/customer/{id}", customerId)
                .header("Authorization", saToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testOperationLog() throws Exception {
        // 1. admin 执行写操作（新增顾客），应产生一条操作日志
        Customer customer = new Customer();
        customer.setName("日志测试顾客");
        customer.setPhone("13711112222");
        customer.setGender(1);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/customer/add")
                .header("Authorization", saToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 2. admin 查询操作日志：应能看到记录，且参数 JSON 已入库
        MvcResult adminPageResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/operation-log/page")
                .param("action", "ADD")
                .header("Authorization", saToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        String jsonResponse = adminPageResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode adminRecords = objectMapper.readTree(jsonResponse).path("data").path("records");
        assertTrue(adminRecords.size() > 0, "admin 应能查询到操作日志");
        assertTrue(jsonResponse.contains("13711112222"), "操作日志应包含请求参数 JSON");
        assertTrue(jsonResponse.contains("新增顾客：日志测试顾客"),
                "操作日志应包含用户可读的中文描述（动作 + 对象名）");

        // 3. 认证类接口（登录/注册/改密）不属于业务增删改，不应被记录
        for (JsonNode record : adminRecords) {
            assertFalse(record.path("uri").asText().startsWith("/api/auth/"),
                    "认证类接口（auth）不应出现在操作日志中，实际: " + record.path("uri").asText());
        }

        // 4. 创建 merchant 账号并登录
        SysUser merchant = new SysUser();
        merchant.setUsername("testmerchant_log");
        merchant.setPhone("13800001111");
        merchant.setPassword(BCrypt.hashpw("123456"));
        merchant.setRealName("Test Merchant");
        merchant.setRole("merchant");
        merchant.setDeleted(false);
        merchant.setDisabled(false);
        merchant.setMustChangePassword(false);
        sysUserMapper.insert(merchant);

        LoginDTO merchantLogin = new LoginDTO();
        merchantLogin.setUsername("testmerchant_log");
        merchantLogin.setPassword("123456");
        MvcResult merchantLoginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(merchantLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        String merchantToken = objectMapper.readTree(
                        merchantLoginResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .path("data").path("token").asText();
        assertNotNull(merchantToken, "merchant 登录应成功");

        // 5. merchant 查询：只能看到自己的记录，看不到 admin 的
        MvcResult merchantPageResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/operation-log/page")
                .header("Authorization", merchantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode merchantRecords = objectMapper.readTree(
                        merchantPageResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .path("data").path("records");
        for (JsonNode record : merchantRecords) {
            assertNotEquals("testadmin", record.path("operatorName").asText(),
                    "merchant 不应看到 admin 的操作记录");
        }

        // 6. merchant 执行写操作后，应能看到自己的记录
        Customer merchantCustomer = new Customer();
        merchantCustomer.setName("商户顾客");
        merchantCustomer.setPhone("13733334444");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/customer/add")
                .header("Authorization", merchantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(merchantCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult merchantPage2Result = mockMvc.perform(MockMvcRequestBuilders.get("/api/operation-log/page")
                .header("Authorization", merchantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode merchantRecords2 = objectMapper.readTree(
                        merchantPage2Result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .path("data").path("records");
        boolean foundOwn = false;
        for (JsonNode record : merchantRecords2) {
            if ("testmerchant_log".equals(record.path("operatorName").asText())) {
                foundOwn = true;
            }
            assertNotEquals("testadmin", record.path("operatorName").asText(),
                    "merchant 不应看到 admin 的操作记录");
        }
        assertTrue(foundOwn, "merchant 应能看到自己的操作记录");

        // 7. 手动清理测试：插入一条 3 天前的近期日志与一条 31 天前的旧日志，
        //    调用 cleanup 后近期日志应被删除、31 天前的旧日志应保留
        // 先记录清理前 operation-log 模块的记录数，避免测试数据库中存在历史数据时断言不稳定
        MvcResult beforeCleanupResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/operation-log/page")
                .header("Authorization", saToken)
                .param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode beforeCleanupRecords = objectMapper.readTree(
                        beforeCleanupResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .path("data").path("records");
        long beforeOperationLogCount = countOperationLogRecords(beforeCleanupRecords);

        OperationLog oldLog = new OperationLog();
        oldLog.setOperatorId(1L);
        oldLog.setOperatorName("olduser");
        oldLog.setModule("customer");
        oldLog.setAction("ADD");
        oldLog.setMethod("POST");
        oldLog.setUri("/api/customer/add");
        oldLog.setDescription("历史日志记录");
        oldLog.setStatus(200);
        oldLog.setCreateTime(DateUtil.offsetDay(DateUtil.date(), -31));
        operationLogMapper.insert(oldLog);

        OperationLog recentLog = new OperationLog();
        recentLog.setOperatorId(1L);
        recentLog.setOperatorName("recentuser");
        recentLog.setModule("customer");
        recentLog.setAction("UPDATE");
        recentLog.setMethod("POST");
        recentLog.setUri("/api/customer/update");
        recentLog.setDescription("近期日志记录");
        recentLog.setStatus(200);
        recentLog.setCreateTime(DateUtil.offsetDay(DateUtil.date(), -3));
        operationLogMapper.insert(recentLog);

        MvcResult cleanupResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/operation-log/cleanup")
                .header("Authorization", saToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode cleanupData = objectMapper.readTree(
                        cleanupResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .path("data");
        assertTrue(cleanupData.path("count").asInt() >= 1, "手动清理应删除至少 1 条近期日志");
        assertEquals(30, cleanupData.path("days").asInt(), "手动清理天数应为配置的 30 天");

        // 8. 清理后查询：近期日志已删除、31 天前的旧日志保留，且不应新增 operation-log 模块记录
        MvcResult afterCleanupResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/operation-log/page")
                .header("Authorization", saToken)
                .param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode afterCleanupRecords = objectMapper.readTree(
                        afterCleanupResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                .path("data").path("records");
        long afterOperationLogCount = countOperationLogRecords(afterCleanupRecords);
        assertFalse(afterCleanupRecords.toString().contains("近期日志记录"),
                "清理后不应再包含近期日志记录");
        assertTrue(afterCleanupRecords.toString().contains("历史日志记录"),
                "31 天前的旧日志应保留");
        assertTrue(afterOperationLogCount <= beforeOperationLogCount,
                "清理动作本身不应被记录，避免审计闭环自引用");
    }

    private long countOperationLogRecords(JsonNode records) {
        long count = 0;
        for (JsonNode record : records) {
            if ("operation-log".equals(record.path("module").asText())) {
                count++;
            }
        }
        return count;
    }
}
