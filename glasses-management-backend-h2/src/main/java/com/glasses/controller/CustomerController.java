package com.glasses.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.mapper.OptometryRecordMapper;
import com.glasses.mapper.SalesRecordMapper;
import com.glasses.service.CustomerService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OptometryRecordMapper optometryRecordMapper;

    @Autowired
    private SalesRecordMapper salesRecordMapper;

    @GetMapping("/page")
    public Result<Page<Customer>> getPage(@RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(customerService.searchCustomer(keyword, current, size));
    }

    @PostMapping("/add")
    public Result<Boolean> addCustomer(@RequestBody Customer customer) {
        customer.setDeleted(false);
        return Result.success(customerService.save(customer));
    }

    @PutMapping("/update")
    public Result<Boolean> updateCustomer(@RequestBody Customer customer) {
        return Result.success(customerService.updateById(customer));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCustomer(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        if (customer == null) {
            return Result.error("顾客不存在");
        }
        Date now = new Date();
        Long loginId = StpUtil.getLoginIdAsLong();
        customer.setDeleted(true);
        customer.setDeletedTime(now);
        customer.setDeletedBy(loginId);
        customerService.updateById(customer);

        OptometryRecord optometryUpdate = new OptometryRecord();
        optometryUpdate.setDeleted(true);
        optometryUpdate.setDeletedTime(now);
        optometryUpdate.setDeletedBy(loginId);
        optometryRecordMapper.update(optometryUpdate, new LambdaQueryWrapper<OptometryRecord>()
                .eq(OptometryRecord::getCustomerId, id)
                .eq(OptometryRecord::getDeleted, false));

        SalesRecord salesUpdate = new SalesRecord();
        salesUpdate.setDeleted(true);
        salesUpdate.setDeletedTime(now);
        salesUpdate.setDeletedBy(loginId);
        salesRecordMapper.update(salesUpdate, new LambdaQueryWrapper<SalesRecord>()
                .eq(SalesRecord::getCustomerId, id)
                .eq(SalesRecord::getDeleted, false));

        return Result.success(true);
    }

    @GetMapping("/{id}")
    public Result<Customer> getCustomer(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        if (customer == null || Boolean.TRUE.equals(customer.getDeleted())) {
            return Result.error("顾客不存在或已删除");
        }
        return Result.success(customer);
    }
}
