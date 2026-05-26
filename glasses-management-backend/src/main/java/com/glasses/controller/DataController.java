package com.glasses.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.glasses.dto.DataExportDTO;
import com.glasses.dto.ImportResultDTO;
import com.glasses.service.DataService;
import com.glasses.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private DataService dataService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @GetMapping("/export")
    public void exportData(HttpServletResponse response) throws IOException {
        DataExportDTO data = dataService.exportAllData();

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        String fileName = "glasses_data_" +
                java.time.LocalDate.now().toString() + ".json";

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        response.getWriter().write(json);
        log.info("数据导出: 操作人={}", StpUtil.getLoginIdAsLong());
    }

    @PostMapping("/import")
    public Result<ImportResultDTO> importData(@RequestParam("file") MultipartFile file,
                                              @RequestParam(defaultValue = "merge") String mode) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要导入的文件");
        }
        try {
            ImportResultDTO result = dataService.importData(file, mode);
            log.info("数据导入成功: mode={}, 文件名={}, 操作人={}", mode, file.getOriginalFilename(), StpUtil.getLoginIdAsLong());
            return Result.success(result);
        } catch (IOException e) {
            log.error("数据导入失败(文件读取): {}", e.getMessage(), e);
            return Result.error("文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("数据导入失败: {}", e.getMessage(), e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @PostMapping("/reset")
    public Result<String> resetData() {
        try {
            int deleted = dataService.resetAllData();
            log.warn("数据重置: 删除{}条记录, 操作人={}", deleted, StpUtil.getLoginIdAsLong());
            return Result.success("已清空 " + deleted + " 条记录（管理员账号已保留）");
        } catch (Exception e) {
            log.error("数据重置失败: {}", e.getMessage(), e);
            return Result.error("清空失败: " + e.getMessage());
        }
    }
}
