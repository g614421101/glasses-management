package com.glasses.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.glasses.constant.RoleConstants;
import com.glasses.service.RecycleBinService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        return recycleBinService.restore(type, id);
    }

    @DeleteMapping("/purge/{type}/{id}")
    public Result<Boolean> purge(@PathVariable String type, @PathVariable Long id) {
        return recycleBinService.purge(type, id);
    }

    @DeleteMapping("/purge-expired")
    public Result<Map<String, Integer>> purgeExpired() {
        return Result.success(recycleBinService.purgeExpired());
    }

    @DeleteMapping("/empty")
    public Result<Map<String, Integer>> empty() {
        return Result.success(recycleBinService.empty());
    }
}
