package com.glasses;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glasses.dto.LoginDTO;
import com.glasses.dto.RegisterDTO;
import com.glasses.dto.SetupDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 管理员统一预设策略的接口级集成测试：
 * 未配置初始密码时启动不自动创建 admin，登录页通过 setup 引导完成初始化；
 * 登录防爆破；/api/data 仅 admin 可访问。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin_init_it;MODE=MySQL;DATABASE_TO_UPPER=FALSE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "app.invite-code=TESTINVITE",
        "glasses.admin.username=admin",
        "glasses.admin.password=",
        "glasses.admin.password-hash=",
        "glasses.admin.enabled=true",
        "glasses.admin.force-reset-password=false"
})
@AutoConfigureMockMvc
@Transactional
public class AdminInitStrategyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSetupFlow() throws Exception {
        // 1. 未初始化状态
        mockMvc.perform(MockMvcRequestBuilders.get("/api/system/setup-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.initialized").value(false));

        // 2. 未初始化时登录被拒
        LoginDTO login = new LoginDTO();
        login.setUsername("admin");
        login.setPassword("x123456");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("系统尚未初始化，请先完成管理员初始化"));

        // 3. 未初始化时注册被拒
        RegisterDTO register = new RegisterDTO();
        register.setUsername("merchant01");
        register.setPhone("13800000001");
        register.setPassword("Passw0rd123");
        register.setConfirmPassword("Passw0rd123");
        register.setInviteCode("TESTINVITE");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("系统尚未初始化，请先完成管理员初始化"));

        // 4. 邀请码错误
        SetupDTO bad = setupDto("boss", "Passw0rd123", "WRONGCODE");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/system/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("邀请码不正确"));

        // 5. 初始化成功
        SetupDTO ok = setupDto("boss", "Passw0rd123", "TESTINVITE");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/system/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ok)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. 状态翻转为已初始化
        mockMvc.perform(MockMvcRequestBuilders.get("/api/system/setup-status"))
                .andExpect(jsonPath("$.data.initialized").value(true));

        // 7. 新管理员可登录
        login.setUsername("boss");
        login.setPassword("Passw0rd123");
        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("admin"))
                .andReturn();
        String token = objectMapper.readTree(
                        loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").path("token").asText();
        assertNotNull(token);

        // 8. 重复初始化被拒
        mockMvc.perform(MockMvcRequestBuilders.post("/api/system/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ok)))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("系统已初始化，无需重复操作"));
    }

    @Test
    public void testLoginBruteForceLock() throws Exception {
        // 初始化 + 注册商户
        SetupDTO ok = setupDto("boss2", "Passw0rd123", "TESTINVITE");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/system/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ok)))
                .andExpect(jsonPath("$.code").value(200));

        RegisterDTO register = new RegisterDTO();
        register.setUsername("lockuser1");
        register.setPhone("13800000002");
        register.setPassword("Passw0rd123");
        register.setConfirmPassword("Passw0rd123");
        register.setInviteCode("TESTINVITE");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(jsonPath("$.code").value(200));

        // 连续 5 次错误密码
        LoginDTO wrong = new LoginDTO();
        wrong.setUsername("lockuser1");
        wrong.setPassword("WrongPwd999");
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrong)))
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
        }

        // 第 6 次即使密码正确也被锁定
        LoginDTO correct = new LoginDTO();
        correct.setUsername("lockuser1");
        correct.setPassword("Passw0rd123");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correct)))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("失败次数过多，请15分钟后再试"));
    }

    @Test
    public void testDataApiRequiresAdmin() throws Exception {
        // 初始化管理员 + 注册并登录商户
        SetupDTO ok = setupDto("boss3", "Passw0rd123", "TESTINVITE");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/system/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ok)))
                .andExpect(jsonPath("$.code").value(200));

        RegisterDTO register = new RegisterDTO();
        register.setUsername("merchant03");
        register.setPhone("13800000003");
        register.setPassword("Passw0rd123");
        register.setConfirmPassword("Passw0rd123");
        register.setInviteCode("TESTINVITE");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(jsonPath("$.code").value(200));

        LoginDTO merchantLogin = new LoginDTO();
        merchantLogin.setUsername("merchant03");
        merchantLogin.setPassword("Passw0rd123");
        MvcResult merchantResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(merchantLogin)))
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode merchantData = objectMapper.readTree(
                merchantResult.getResponse().getContentAsString(StandardCharsets.UTF_8)).path("data");
        String merchantToken = merchantData.path("token").asText();
        assertNotNull(merchantToken);

        // 商户访问数据重置 → 403
        mockMvc.perform(MockMvcRequestBuilders.post("/api/data/reset")
                        .header("Authorization", merchantToken))
                .andExpect(status().isForbidden());

        // 管理员可以
        LoginDTO adminLogin = new LoginDTO();
        adminLogin.setUsername("boss3");
        adminLogin.setPassword("Passw0rd123");
        MvcResult adminResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        String adminToken = objectMapper.readTree(
                        adminResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").path("token").asText();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/data/reset")
                        .header("Authorization", adminToken))
                .andExpect(jsonPath("$.code").value(200));
    }

    private SetupDTO setupDto(String username, String password, String inviteCode) {
        SetupDTO dto = new SetupDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setConfirmPassword(password);
        dto.setInviteCode(inviteCode);
        return dto;
    }
}
