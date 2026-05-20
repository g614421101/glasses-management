package com.glasses.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.glasses.dto.DataExportDTO;
import com.glasses.dto.ImportResultDTO;
import com.glasses.service.DataService;
import com.glasses.util.Result;
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
    }

    @PostMapping("/import")
    public Result<ImportResultDTO> importData(@RequestParam("file") MultipartFile file,
                                              @RequestParam(defaultValue = "merge") String mode) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要导入的文件");
        }
        try {
            ImportResultDTO result = dataService.importData(file, mode);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @PostMapping("/reset")
    public Result<String> resetData() {
        try {
            int deleted = dataService.resetAllData();
            return Result.success("已清空 " + deleted + " 条记录（管理员账号已保留）");
        } catch (Exception e) {
            return Result.error("清空失败: " + e.getMessage());
        }
    }
}
