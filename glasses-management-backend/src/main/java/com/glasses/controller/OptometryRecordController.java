package com.glasses.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        if (record.getOptometristName() == null || record.getOptometristName().trim().isEmpty()) {
            String realName = StpUtil.getSession().getString("realName");
            if (realName == null || realName.trim().isEmpty()) {
                realName = StpUtil.getSession().getString("username");
            }
            record.setOptometristName(realName);
        }
        if (record.getExamDate() == null) {
            record.setExamDate(DateUtil.date());
        }
        return Result.success(optometryRecordService.save(record));
    }

    @GetMapping("/customer/{customerId}")
    public Result<?> getByCustomer(@PathVariable Long customerId,
                                   @RequestParam(required = false) Integer current,
                                   @RequestParam(required = false) Integer size) {
        if (current != null && size != null) {
            return Result.success(optometryRecordService.listByCustomerId(customerId, current, size));
        }
        return Result.success(optometryRecordService.listByCustomerId(customerId));
    }

    @PutMapping("/update")
    public Result<Boolean> updateRecord(@RequestBody OptometryRecord record) {
        return Result.success(optometryRecordService.updateById(record));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        if (!optometryRecordService.softDeleteRecord(id)) {
            return Result.error("验光记录不存在");
        }
        return Result.success(true);
    }
}