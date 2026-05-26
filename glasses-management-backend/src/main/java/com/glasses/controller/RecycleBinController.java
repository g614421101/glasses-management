package com.glasses.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.glasses.constant.RoleConstants;
import com.glasses.service.RecycleBinService;
import com.glasses.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/recycle-bin")
@SaCheckRole(RoleConstants.ADMIN)
public class RecycleBinController {

    @Autowired
    private RecycleBinService recycleBinService;

    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "all") String type) {
        return Result.success(recycleBinService.list(type));
    }

    @PostMapping("/restore/{type}/{id}")
    public Result<Boolean> restore(@PathVariable String type, @PathVariable Long id) {
        Result<Boolean> result = recycleBinService.restore(type, id);
        if (result.isSuccess()) {
            log.info("恢复数据: type={}, id={}, 操作人={}", type, id, StpUtil.getLoginIdAsLong());
        }
        return result;
    }

    @DeleteMapping("/purge/{type}/{id}")
    public Result<Boolean> purge(@PathVariable String type, @PathVariable Long id) {
        Result<Boolean> result = recycleBinService.purge(type, id);
        if (result.isSuccess()) {
            log.warn("彻底删除数据: type={}, id={}, 操作人={}", type, id, StpUtil.getLoginIdAsLong());
        }
        return result;
    }

    @DeleteMapping("/purge-expired")
    public Result<Map<String, Integer>> purgeExpired() {
        Map<String, Integer> result = recycleBinService.purgeExpired();
        log.info("清理过期回收站: 结果={}", result);
        return Result.success(result);
    }

    @DeleteMapping("/empty")
    public Result<Map<String, Integer>> empty() {
        Map<String, Integer> result = recycleBinService.empty();
        log.warn("清空回收站: 操作人={}, 结果={}", StpUtil.getLoginIdAsLong(), result);
        return Result.success(result);
    }
}
