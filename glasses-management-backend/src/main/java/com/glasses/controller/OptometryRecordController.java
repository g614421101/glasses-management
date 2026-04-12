package com.glasses.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.glasses.entity.OptometryRecord;
import com.glasses.service.OptometryRecordService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/optometry")
public class OptometryRecordController {

    @Autowired
    private OptometryRecordService optometryRecordService;

    @PostMapping("/add")
    public Result<Boolean> addRecord(@RequestBody OptometryRecord record) {
        if (record.getOptometristName() == null) {
            record.setOptometristName(StpUtil.getSession().getString("username")); // 默认当前登录人
        }
        if (record.getExamDate() == null) {
            record.setExamDate(DateUtil.date()); // 使用 Hutool DateTime
        }
        return Result.success(optometryRecordService.save(record));
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<OptometryRecord>> getByCustomer(@PathVariable Long customerId) {
        return Result.success(optometryRecordService.listByCustomerId(customerId));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        return Result.success(optometryRecordService.removeById(id));
    }
}
