package com.glasses.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.glasses.entity.Customer;
import com.glasses.entity.SalesRecord;
import com.glasses.service.CustomerService;
import com.glasses.service.SalesRecordService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sales")
public class SalesRecordController {

    @Autowired
    private SalesRecordService salesRecordService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/add")
    public Result<Boolean> addRecord(@RequestBody SalesRecord record) {
        if (record.getRecordNo() == null || record.getRecordNo().isEmpty()) {
            String timestamp = DateUtil.format(DateUtil.date(), "yyyyMMddHHmmssSSS");
            int random = ThreadLocalRandom.current().nextInt(1000, 10000);
            record.setRecordNo("SR" + timestamp + random);
        }
        if (record.getOperatorId() == null) {
            record.setOperatorId(StpUtil.getLoginIdAsLong());
        }
        if (record.getSalesDate() == null) {
            record.setSalesDate(DateUtil.date());
        }
        if (record.getFrameQuantity() == null) {
            record.setFrameQuantity(1);
        }
        if (record.getLensQuantity() == null) {
            record.setLensQuantity(1);
        }
        return Result.success(salesRecordService.save(record));
    }

    @GetMapping("/customer/{customerId}")
    public Result<?> getByCustomer(@PathVariable Long customerId,
                                   @RequestParam(required = false) Integer current,
                                   @RequestParam(required = false) Integer size) {
        if (current != null && size != null) {
            return Result.success(salesRecordService.listByCustomerId(customerId, current, size));
        }
        return Result.success(salesRecordService.listByCustomerId(customerId));
    }

    @PutMapping("/update")
    public Result<Boolean> updateRecord(@RequestBody SalesRecord record) {
        return Result.success(salesRecordService.updateById(record));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        if (!salesRecordService.softDeleteRecord(id)) {
            return Result.error("配镜记录不存在");
        }
        return Result.success(true);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "false") Boolean showAll) {

        LambdaQueryWrapper<SalesRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesRecord::getDeleted, false);
        if (!showAll && startDate != null && endDate != null) {
            wrapper.ge(SalesRecord::getSalesDate, startDate + " 00:00:00")
                   .le(SalesRecord::getSalesDate, endDate + " 23:59:59");
        }

        // 1. 汇总统计（SQL 聚合查询）
        Map<String, Object> summary = showAll
                ? salesRecordService.getRevenueSummary(null, null)
                : salesRecordService.getRevenueSummary(startDate, endDate);
        BigDecimal totalRevenue = new BigDecimal(summary.get("totalRevenue").toString());
        long orderCount = ((Number) summary.get("orderCount")).longValue();

        // 2. 分页明细
        wrapper.orderByDesc(SalesRecord::getSalesDate)
               .orderByDesc(SalesRecord::getId);
        Page<SalesRecord> page = new Page<>(current, size);
        Page<SalesRecord> recordsPage = salesRecordService.page(page, wrapper);

        // 补充顾客姓名
        if (recordsPage.getRecords() != null && !recordsPage.getRecords().isEmpty()) {
            List<Long> customerIds = recordsPage.getRecords().stream()
                    .map(SalesRecord::getCustomerId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!customerIds.isEmpty()) {
                List<Customer> customers = customerService.listByIds(customerIds);
                Map<Long, String> customerNameMap = customers.stream()
                        .collect(Collectors.toMap(Customer::getId, Customer::getName));
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
