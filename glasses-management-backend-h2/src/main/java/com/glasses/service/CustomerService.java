package com.glasses.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.glasses.entity.Customer;

import java.util.List;

public interface CustomerService extends IService<Customer> {
    Page<Customer> searchCustomer(String keyword, Integer current, Integer size);

    /**
     * 软删除顾客及其关联的验光/配镜记录
     * @return true 表示删除成功，false 表示顾客不存在或已删除
     */
    boolean softDeleteCustomer(Long id);

    /**
     * 批量软删除顾客及其关联的验光/配镜记录
     * @return 实际删除成功的条数
     */
    int batchSoftDeleteCustomers(List<Long> ids);
}
