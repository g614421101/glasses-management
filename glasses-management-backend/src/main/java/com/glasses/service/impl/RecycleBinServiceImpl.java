package com.glasses.service.impl;

import cn.hutool.core.date.DateUtil;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.entity.SysUser;
import com.glasses.mapper.CustomerMapper;
import com.glasses.mapper.OptometryRecordMapper;
import com.glasses.mapper.SalesRecordMapper;
import com.glasses.mapper.SysUserMapper;
import com.glasses.service.RecycleBinService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回收站服务实现。
 * <p>
 * 注意：本类直接注入 Mapper 而非 Service，不继承 {@code ServiceImpl}。
 * 原因：
 * <ol>
 *   <li>回收站操作跨越 4 个实体（Customer / OptometryRecord / SalesRecord / SysUser），
 *       无法选择单一 BaseMapper 作为 ServiceImpl 的泛型参数。</li>
 *   <li>所有操作都涉及已软删除（{@code deleted = true}）的记录，必须使用
 *       绕过 {@code @TableLogic} 的 Mapper 自定义方法（如 {@code selectAnyById}、
 *       {@code selectDeletedList}、{@code physicalDeleteById} 等），
 *       Service 层的标准 CRUD 方法会自动附加 {@code deleted = false} 过滤，无法用于回收站场景。</li>
 * </ol>
 */
@Service
public class RecycleBinServiceImpl implements RecycleBinService {

    private static final int RETENTION_DAYS = 30;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private OptometryRecordMapper optometryRecordMapper;

    @Autowired
    private SalesRecordMapper salesRecordMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Map<String, Object> list(String type) {
        Map<String, Object> result = new HashMap<>();
        if ("all".equalsIgnoreCase(type) || "customer".equalsIgnoreCase(type)) {
            result.put("customers", customerMapper.selectDeletedList());
        }
        if ("all".equalsIgnoreCase(type) || "optometry".equalsIgnoreCase(type)) {
            result.put("optometryRecords", optometryRecordMapper.selectDeletedList());
        }
        if ("all".equalsIgnoreCase(type) || "sales".equalsIgnoreCase(type)) {
            result.put("salesRecords", salesRecordMapper.selectDeletedList());
        }
        return result;
    }

    @Override
    @Transactional
    public Result<Boolean> restore(String type, Long id) {
        if ("customer".equalsIgnoreCase(type)) {
            Customer customer = customerMapper.selectAnyById(id);
            if (customer == null || !Boolean.TRUE.equals(customer.getDeleted())) {
                return Result.error("回收站中未找到该顾客");
            }
            customerMapper.restoreByIdIgnoringLogic(id);
            restoreRecordsByCustomer(id);
            return Result.success(true);
        }
        if ("optometry".equalsIgnoreCase(type)) {
            OptometryRecord record = optometryRecordMapper.selectAnyById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("回收站中未找到该验光记录");
            }
            if (isCustomerDeleted(record.getCustomerId())) {
                return Result.error("所属顾客仍在回收站，请先恢复顾客");
            }
            optometryRecordMapper.restoreByIdIgnoringLogic(id);
            return Result.success(true);
        }
        if ("sales".equalsIgnoreCase(type)) {
            SalesRecord record = salesRecordMapper.selectAnyById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("回收站中未找到该配镜记录");
            }
            if (isCustomerDeleted(record.getCustomerId())) {
                return Result.error("所属顾客仍在回收站，请先恢复顾客");
            }
            salesRecordMapper.restoreByIdIgnoringLogic(id);
            return Result.success(true);
        }
        return Result.error("不支持的回收站类型");
    }

    @Override
    @Transactional
    public Result<Boolean> purge(String type, Long id) {
        if ("customer".equalsIgnoreCase(type)) {
            Customer customer = customerMapper.selectAnyById(id);
            if (customer == null || !Boolean.TRUE.equals(customer.getDeleted())) {
                return Result.error("只能彻底删除回收站中的顾客");
            }
            purgeRecordsByCustomer(id);
            customerMapper.physicalDeleteById(id);
            return Result.success(true);
        }
        if ("optometry".equalsIgnoreCase(type)) {
            OptometryRecord record = optometryRecordMapper.selectAnyById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("只能彻底删除回收站中的验光记录");
            }
            optometryRecordMapper.physicalDeleteById(id);
            return Result.success(true);
        }
        if ("sales".equalsIgnoreCase(type)) {
            SalesRecord record = salesRecordMapper.selectAnyById(id);
            if (record == null || !Boolean.TRUE.equals(record.getDeleted())) {
                return Result.error("只能彻底删除回收站中的配镜记录");
            }
            salesRecordMapper.physicalDeleteById(id);
            return Result.success(true);
        }
        return Result.error("不支持的回收站类型");
    }

    @Override
    @Transactional
    public Map<String, Integer> purgeExpired() {
        Date expireBefore = DateUtil.offsetDay(new Date(), -RETENTION_DAYS);
        Map<String, Integer> result = new HashMap<>();

        int sales = salesRecordMapper.physicalDeleteExpired(expireBefore);
        int optometry = optometryRecordMapper.physicalDeleteExpired(expireBefore);
        int customers = customerMapper.physicalDeleteExpired(expireBefore);
        int users = sysUserMapper.physicalDeleteExpired(RoleConstants.ADMIN, expireBefore);

        result.put("sales", sales);
        result.put("optometry", optometry);
        result.put("customers", customers);
        result.put("users", users);
        return result;
    }

    private boolean isCustomerDeleted(Long customerId) {
        Customer customer = customerMapper.selectAnyById(customerId);
        return customer == null || Boolean.TRUE.equals(customer.getDeleted());
    }

    private void restoreRecordsByCustomer(Long customerId) {
        optometryRecordMapper.restoreByCustomerIdIgnoringLogic(customerId);
        salesRecordMapper.restoreByCustomerIdIgnoringLogic(customerId);
    }

    private void purgeRecordsByCustomer(Long customerId) {
        optometryRecordMapper.physicalDeleteByCustomerId(customerId);
        salesRecordMapper.physicalDeleteByCustomerId(customerId);
    }
}
