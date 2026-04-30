package com.glasses.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.glasses.entity.OptometryRecord;
import com.glasses.mapper.OptometryRecordMapper;
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

    @Autowired
    private OptometryRecordMapper optometryRecordMapper;

    @PostMapping("/add")
    public Result<Boolean> addRecord(@RequestBody OptometryRecord record) {
        if (record.getOptometristName() == null || record.getOptometristName().trim().isEmpty()) {
            String realName = StpUtil.getSession().getString("realName");
            if (realName == null || realName.trim().isEmpty()) {
                realName = StpUtil.getSession().getString("username");
            }
            record.setOptometristName(realName); // 默认当前登录人
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

    @PutMapping("/update")
    public Result<Boolean> updateRecord(@RequestBody OptometryRecord record) {
        return Result.success(optometryRecordService.updateById(record));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        OptometryRecord record = optometryRecordService.getById(id);
        if (record == null) {
            return Result.error("验光记录不存在");
        }
        return Result.success(optometryRecordMapper.softDeleteById(id, DateUtil.date(), StpUtil.getLoginIdAsLong()) > 0);
    }
}
