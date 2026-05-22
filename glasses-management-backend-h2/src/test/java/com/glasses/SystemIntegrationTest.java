package com.glasses;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glasses.dto.LoginDTO;
import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.entity.SysUser;
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

    private String saToken;

    @BeforeEach
    public void setup() throws Exception {
        // 创建一个专用的测试管理员账号
        SysUser testAdmin = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "testadmin"));
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
            sysUserMapper.updateById(testAdmin);
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
}
