package com.glasses.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.glasses.entity.Customer;
import com.glasses.mapper.CustomerMapper;
import com.glasses.mapper.OptometryRecordMapper;
import com.glasses.mapper.SalesRecordMapper;
import com.glasses.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    private OptometryRecordMapper optometryRecordMapper;

    @Autowired
    private SalesRecordMapper salesRecordMapper;

    @Override
    public Page<Customer> searchCustomer(String keyword, Integer current, Integer size) {
        Page<Customer> page = Page.of(current, size);
        QueryWrapper query = QueryWrapper.create()
                .from(Customer.class)
                .where(Customer::getDeleted).eq(false);
        if (StrUtil.isNotBlank(keyword)) {
            query.and(Customer::getPhone).like(keyword)
                    .or(Customer::getName).like(keyword);
        }
        query.orderBy(Customer::getCreateTime).desc()
                .orderBy(Customer::getId).desc();
        return mapper.paginate(page, query);
    }

    @Override
    @Transactional
    public boolean softDeleteCustomer(Long id) {
        Customer customer = mapper.selectAnyById(id);
        if (customer == null || Boolean.TRUE.equals(customer.getDeleted())) {
            return false;
        }
        Date now = DateUtil.date();
        Long loginId = StpUtil.getLoginIdAsLong();
        mapper.softDeleteById(id, now, loginId);
        optometryRecordMapper.softDeleteByCustomerId(id, now, loginId);
        salesRecordMapper.softDeleteByCustomerId(id, now, loginId);
        log.info("软删除顾客: id={}, 操作人={}", id, loginId);
        return true;
    }
}
