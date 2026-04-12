package com.glasses.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.glasses.entity.SalesRecord;
import com.glasses.service.SalesRecordService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesRecordController {

    @Autowired
    private SalesRecordService salesRecordService;

    @PostMapping("/add")
    public Result<Boolean> addRecord(@RequestBody SalesRecord record) {
        if (record.getRecordNo() == null || record.getRecordNo().isEmpty()) {
            record.setRecordNo("SR" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss"));
        }
        if (record.getOperatorId() == null) {
            record.setOperatorId(StpUtil.getLoginIdAsLong());
        }
        if (record.getSalesDate() == null) {
            record.setSalesDate(DateUtil.date());
        }
        return Result.success(salesRecordService.save(record));
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<SalesRecord>> getByCustomer(@PathVariable Long customerId) {
        return Result.success(salesRecordService.listByCustomerId(customerId));
    }

    @PutMapping("/update")
    public Result<Boolean> updateRecord(@RequestBody SalesRecord record) {
        return Result.success(salesRecordService.updateById(record));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        return Result.success(salesRecordService.removeById(id));
    }
}
