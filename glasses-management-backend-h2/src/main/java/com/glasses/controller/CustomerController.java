package com.glasses.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.glasses.entity.Customer;
import com.glasses.service.CustomerService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/page")
    public Result<Page<Customer>> getPage(@RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(customerService.searchCustomer(keyword, current, size));
    }

    @PostMapping("/add")
    public Result<Boolean> addCustomer(@RequestBody Customer customer) {
        return Result.success(customerService.save(customer));
    }

    @PutMapping("/update")
    public Result<Boolean> updateCustomer(@RequestBody Customer customer) {
        return Result.success(customerService.updateById(customer));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCustomer(@PathVariable Long id) {
        return Result.success(customerService.removeById(id));
    }

    @GetMapping("/{id}")
    public Result<Customer> getCustomer(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }
}
