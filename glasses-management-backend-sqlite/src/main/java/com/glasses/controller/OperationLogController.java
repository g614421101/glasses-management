package com.glasses.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.OperationLog;
import com.glasses.service.OperationLogService;
import com.glasses.util.PageAdapter;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志查询。登录即可访问，
 * 数据隔离在 OperationLogService 中实现：admin 可见全部，其余角色仅见自己的记录。
 */
@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /** 手动清理保留天数（application.yml 配置，0 表示禁用手动清理） */
    @Value("${app.operation-log.manual-cleanup-days:30}")
    private int manualCleanupDays;

    @GetMapping("/page")
    public Result<PageAdapter<OperationLog>> getPage(@RequestParam(required = false) String operatorName,
                                                     @RequestParam(required = false) String action,
                                                     @RequestParam(required = false) String startTime,
                                                     @RequestParam(required = false) String endTime,
                                                     @RequestParam(defaultValue = "1") Integer current,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(PageAdapter.of(
                operationLogService.pageQuery(operatorName, action, startTime, endTime, current, size)));
    }

    /**
     * 手动清理最近配置天数内的操作日志（物理删除），更早的历史记录保留。仅 admin 可操作，
     * 天数由 app.operation-log.manual-cleanup-days 配置决定。
     */
    @SaCheckRole(RoleConstants.ADMIN)
    @PostMapping("/cleanup")
    public Result<Map<String, Object>> cleanup() {
        int count = operationLogService.cleanupWithin(manualCleanupDays);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        data.put("days", manualCleanupDays);
        return Result.success(data);
    }
}
