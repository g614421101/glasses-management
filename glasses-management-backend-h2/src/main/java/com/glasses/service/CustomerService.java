package com.glasses.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.glasses.entity.Customer;

public interface CustomerService extends IService<Customer> {
    Page<Customer> searchCustomer(String keyword, Integer current, Integer size);

    /**
     * 软删除顾客及其关联的验光/配镜记录
     * @return true 表示删除成功，false 表示顾客不存在或已删除
     */
    boolean softDeleteCustomer(Long id);
}
