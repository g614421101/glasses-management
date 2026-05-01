package com.glasses;

import cn.hutool.core.date.DateUtil;
import com.glasses.entity.Customer;
import com.glasses.mapper.CustomerMapper;
import com.glasses.service.CustomerService;
import com.glasses.service.RecycleBinService;
import com.glasses.util.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LogicDeleteConfigTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private RecycleBinService recycleBinService;

    @Test
    void logicDeleteKeepsNormalQueriesAndRecycleBinWorking() {
        Customer customer = new Customer();
        customer.setName("逻辑删除配置测试");
        customer.setPhone("139" + String.format("%08d", System.nanoTime() % 100_000_000));
        customer.setGender(0);
        assertTrue(customerService.save(customer));

        Long id = customer.getId();
        assertNotNull(id);
        assertFalse(Boolean.TRUE.equals(customerMapper.selectById(id).getDeleted()));

        assertEquals(1, customerMapper.softDeleteById(id, DateUtil.date(), 1L));

        assertNull(customerMapper.selectById(id));
        Customer deletedCustomer = customerMapper.selectAnyById(id);
        assertNotNull(deletedCustomer);
        assertTrue(Boolean.TRUE.equals(deletedCustomer.getDeleted()));

        Result<Boolean> restoreResult = recycleBinService.restore("customer", id);
        assertEquals(200, restoreResult.getCode());
        assertNotNull(customerMapper.selectById(id));

        assertEquals(1, customerMapper.softDeleteById(id, DateUtil.date(), 1L));

        Result<Boolean> purgeResult = recycleBinService.purge("customer", id);
        assertEquals(200, purgeResult.getCode());
        assertNull(customerMapper.selectAnyById(id));
    }
}
