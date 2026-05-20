package com.glasses.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.glasses.entity.SalesRecord;
import com.glasses.mapper.SalesRecordMapper;
import com.glasses.service.CustomerService;
import com.glasses.service.SalesRecordService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SalesRecordController {

    @Autowired
    private SalesRecordService salesRecordService;

    @Autowired
    private SalesRecordMapper salesRecordMapper;

    @Autowired
    private CustomerService customerService;

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
        SalesRecord record = salesRecordService.getById(id);
        if (record == null) {
            return Result.error("配镜记录不存在");
        }
        return Result.success(salesRecordMapper.softDeleteById(id, DateUtil.date(), StpUtil.getLoginIdAsLong()) > 0);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "false") Boolean showAll) {
        
        QueryWrapper<SalesRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);
        if (!showAll && startDate != null && endDate != null) {
            wrapper.ge("sales_date", startDate + " 00:00:00")
                   .le("sales_date", endDate + " 23:59:59");
        }
        
        // 1. 计算总汇总数据 (不分页)
        QueryWrapper<SalesRecord> summaryWrapper = wrapper.clone();
        summaryWrapper.select("IFNULL(SUM(total_amount), 0) as totalRevenue", "COUNT(*) as orderCount");
        Map<String, Object> summary = salesRecordService.getMap(summaryWrapper);
        
        BigDecimal totalRevenue = new BigDecimal(summary.get("totalRevenue").toString());
        Long orderCount = (Long) summary.get("orderCount");

        // 2. 获取分页明细数据
        wrapper.orderByDesc("sales_date").orderByDesc("id");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SalesRecord> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SalesRecord> recordsPage = 
                salesRecordService.page(page, wrapper);
        
        // 补充顾客姓名
        if (recordsPage.getRecords() != null && !recordsPage.getRecords().isEmpty()) {
            List<Long> customerIds = recordsPage.getRecords().stream()
                    .map(SalesRecord::getCustomerId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            if (!customerIds.isEmpty()) {
                List<com.glasses.entity.Customer> customers = customerService.listByIds(customerIds);
                Map<Long, String> customerNameMap = customers.stream()
                        .collect(java.util.stream.Collectors.toMap(com.glasses.entity.Customer::getId, com.glasses.entity.Customer::getName));
                recordsPage.getRecords().forEach(record -> {
                    if (record.getCustomerId() != null) {
                        record.setCustomerName(customerNameMap.get(record.getCustomerId()));
                    }
                });
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("orderCount", orderCount);
        result.put("records", recordsPage);
        
        return Result.success(result);
    }
}
