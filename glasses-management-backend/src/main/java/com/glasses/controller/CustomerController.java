package com.glasses.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.glasses.entity.Customer;
import com.glasses.mapper.CustomerMapper;
import com.glasses.service.CustomerService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerMapper customerMapper;

    @GetMapping("/page")
    public Result<Page<Customer>> getPage(@RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(customerService.searchCustomer(keyword, current, size));
    }

    @PostMapping("/add")
    public Result<Object> addCustomer(@RequestBody Customer customer) {
        if (customer.getPhone() != null && !customer.getPhone().trim().isEmpty()) {
            Customer existing = customerMapper.selectByPhoneIncludingDeleted(customer.getPhone());
            if (existing != null) {
                Result<Object> conflictResult = new Result<>();
                conflictResult.setCode(409);
                conflictResult.setMsg("该手机号已关联顾客：" + existing.getName());
                conflictResult.setData(existing);
                return conflictResult;
            }
        }
        customer.setDeleted(false);
        return Result.success(customerService.save(customer));
    }

    @PutMapping("/update")
    public Result<Boolean> updateCustomer(@RequestBody Customer customer) {
        if (customer.getPhone() != null && !customer.getPhone().trim().isEmpty()) {
            Customer existing = customerMapper.selectByPhoneIncludingDeleted(customer.getPhone());
            if (existing != null && !existing.getId().equals(customer.getId())) {
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    return Result.error("该手机号对应的顾客档案在回收站中，请先将其恢复或彻底删除");
                } else {
                    return Result.error("该手机号已关联其他活跃顾客，请更换");
                }
            }
        }
        return Result.success(customerService.updateById(customer));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCustomer(@PathVariable Long id) {
        if (!customerService.softDeleteCustomer(id)) {
            return Result.error("顾客不存在");
        }
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
