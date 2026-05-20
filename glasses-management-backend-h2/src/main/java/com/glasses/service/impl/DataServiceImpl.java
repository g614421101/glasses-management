package com.glasses.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.glasses.constant.RoleConstants;
import com.glasses.dto.DataExportDTO;
import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.entity.SysUser;
import com.glasses.mapper.CustomerMapper;
import com.glasses.mapper.OptometryRecordMapper;
import com.glasses.mapper.SalesRecordMapper;
import com.glasses.mapper.SysUserMapper;
import com.glasses.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataServiceImpl implements DataService {

    private static final Logger log = LoggerFactory.getLogger(DataServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private OptometryRecordMapper optometryRecordMapper;
    @Autowired
    private SalesRecordMapper salesRecordMapper;

    @Override
    public DataExportDTO exportAllData() {
        DataExportDTO dto = new DataExportDTO();
        dto.setExportTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dto.setVersion("1.0");
        dto.setSysUsers(sysUserMapper.selectAllIncludingDeleted());
        dto.setCustomers(customerMapper.selectAllIncludingDeleted());
        dto.setOptometryRecords(optometryRecordMapper.selectAllIncludingDeleted());
        dto.setSalesRecords(salesRecordMapper.selectAllIncludingDeleted());
        return dto;
    }

    @Override
    @Transactional
    public int importData(MultipartFile file, String mode) throws IOException {
        DataExportDTO dto = objectMapper.readValue(file.getInputStream(), DataExportDTO.class);

        if ("replace".equals(mode)) {
            return importReplace(dto);
        }
        return importMerge(dto);
    }

    /**
     * Full replace: clear all tables (except admin) then insert everything fresh.
     */
    private int importReplace(DataExportDTO dto) {
        // Clear in reverse dependency order
        salesRecordMapper.deleteAll();
        optometryRecordMapper.deleteAll();
        customerMapper.deleteAll();
        sysUserMapper.deleteAllNonAdmin(RoleConstants.ADMIN);
        log.info("All data cleared (admin preserved). Starting import...");

        int totalImported = 0;

        // 1. sys_user: skip admin if already exists (preserved by deleteAllNonAdmin)
        Map<Long, Long> userIdMap = new HashMap<>();
        if (dto.getSysUsers() != null) {
            for (SysUser user : dto.getSysUsers()) {
                if (RoleConstants.ADMIN.equals(user.getRole())) {
                    SysUser existingAdmin = sysUserMapper.selectAnyByRole(RoleConstants.ADMIN);
                    if (existingAdmin != null) {
                        userIdMap.put(user.getId(), existingAdmin.getId());
                        continue;
                    }
                }
                boolean wasDeleted = Boolean.TRUE.equals(user.getDeleted());
                Long oldId = user.getId();
                user.setId(null);
                user.setDeleted(false);
                user.setDeletedTime(null);
                sysUserMapper.insert(user);
                if (wasDeleted) {
                    user.setDeleted(true);
                    sysUserMapper.updateById(user);
                }
                userIdMap.put(oldId, user.getId());
                totalImported++;
            }
        }

        // 2. customer: no dedup needed (table is empty)
        Map<Long, Long> customerIdMap = new HashMap<>();
        if (dto.getCustomers() != null) {
            for (Customer customer : dto.getCustomers()) {
                boolean wasDeleted = Boolean.TRUE.equals(customer.getDeleted());
                if (customer.getDeletedBy() != null) {
                    customer.setDeletedBy(userIdMap.get(customer.getDeletedBy()));
                }
                Long oldId = customer.getId();
                customer.setId(null);
                customer.setDeleted(false);
                customer.setDeletedTime(null);
                customer.setDeletedBy(null);
                customerMapper.insert(customer);
                if (wasDeleted) {
                    customer.setDeleted(true);
                    customerMapper.updateById(customer);
                }
                customerIdMap.put(oldId, customer.getId());
                totalImported++;
            }
        }

        // 3. optometry_record: no dedup needed
        Map<Long, Long> optometryIdMap = new HashMap<>();
        if (dto.getOptometryRecords() != null) {
            for (OptometryRecord record : dto.getOptometryRecords()) {
                boolean wasDeleted = Boolean.TRUE.equals(record.getDeleted());
                if (record.getCustomerId() != null) {
                    record.setCustomerId(customerIdMap.get(record.getCustomerId()));
                }
                if (record.getDeletedBy() != null) {
                    record.setDeletedBy(userIdMap.get(record.getDeletedBy()));
                }
                Long oldId = record.getId();
                record.setId(null);
                record.setDeleted(false);
                record.setDeletedTime(null);
                record.setDeletedBy(null);
                optometryRecordMapper.insert(record);
                if (wasDeleted) {
                    record.setDeleted(true);
                    optometryRecordMapper.updateById(record);
                }
                optometryIdMap.put(oldId, record.getId());
                totalImported++;
            }
        }

        // 4. sales_record: no dedup needed
        if (dto.getSalesRecords() != null) {
            for (SalesRecord record : dto.getSalesRecords()) {
                boolean wasDeleted = Boolean.TRUE.equals(record.getDeleted());
                if (record.getCustomerId() != null) {
                    record.setCustomerId(customerIdMap.get(record.getCustomerId()));
                }
                if (record.getOptometryId() != null) {
                    record.setOptometryId(optometryIdMap.get(record.getOptometryId()));
                }
                if (record.getOperatorId() != null) {
                    record.setOperatorId(userIdMap.get(record.getOperatorId()));
                }
                if (record.getDeletedBy() != null) {
                    record.setDeletedBy(userIdMap.get(record.getDeletedBy()));
                }
                record.setId(null);
                record.setDeleted(false);
                record.setDeletedTime(null);
                record.setDeletedBy(null);
                salesRecordMapper.insert(record);
                if (wasDeleted) {
                    record.setDeleted(true);
                    salesRecordMapper.updateById(record);
                }
                totalImported++;
            }
        }

        log.info("Replace import completed. Total records imported: {}", totalImported);
        return totalImported;
    }

    /**
     * Merge append: dedup by business keys, skip existing records.
     */
    private int importMerge(DataExportDTO dto) {
        int totalImported = 0;

        // 1. sys_user: dedup by username
        Map<Long, Long> userIdMap = new HashMap<>();
        if (dto.getSysUsers() != null) {
            for (SysUser user : dto.getSysUsers()) {
                SysUser existing = sysUserMapper.selectAnyByUsername(user.getUsername());
                if (existing != null) {
                    userIdMap.put(user.getId(), existing.getId());
                } else {
                    boolean wasDeleted = Boolean.TRUE.equals(user.getDeleted());
                    Long oldId = user.getId();
                    user.setId(null);
                    user.setDisabled(false);
                    user.setDisabledTime(null);
                    user.setDeleted(false);
                    user.setDeletedTime(null);
                    sysUserMapper.insert(user);
                    if (wasDeleted) {
                        user.setDeleted(true);
                        sysUserMapper.updateById(user);
                    }
                    userIdMap.put(oldId, user.getId());
                    totalImported++;
                }
            }
        }

        // 2. customer: dedup by phone
        Map<Long, Long> customerIdMap = new HashMap<>();
        if (dto.getCustomers() != null) {
            for (Customer customer : dto.getCustomers()) {
                Customer existing = findCustomerByPhone(customer.getPhone());
                if (existing != null) {
                    customerIdMap.put(customer.getId(), existing.getId());
                } else {
                    boolean wasDeleted = Boolean.TRUE.equals(customer.getDeleted());
                    if (customer.getDeletedBy() != null) {
                        customer.setDeletedBy(userIdMap.get(customer.getDeletedBy()));
                    }
                    Long oldId = customer.getId();
                    customer.setId(null);
                    customer.setDeleted(false);
                    customer.setDeletedTime(null);
                    customer.setDeletedBy(null);
                    customerMapper.insert(customer);
                    if (wasDeleted) {
                        customer.setDeleted(true);
                        customerMapper.updateById(customer);
                    }
                    customerIdMap.put(oldId, customer.getId());
                    totalImported++;
                }
            }
        }

        // 3. optometry_record: dedup by customer + exam_date + measurements
        Map<Long, Long> optometryIdMap = new HashMap<>();
        if (dto.getOptometryRecords() != null) {
            for (OptometryRecord record : dto.getOptometryRecords()) {
                if (record.getCustomerId() != null) {
                    record.setCustomerId(customerIdMap.get(record.getCustomerId()));
                }
                if (record.getDeletedBy() != null) {
                    record.setDeletedBy(userIdMap.get(record.getDeletedBy()));
                }

                boolean found = false;
                List<OptometryRecord> candidates = optometryRecordMapper.findByCustomerAndExamDate(
                        record.getCustomerId(), record.getExamDate());
                for (OptometryRecord existing : candidates) {
                    if (java.util.Objects.equals(existing.getOdSph(), record.getOdSph())
                            && java.util.Objects.equals(existing.getOdCyl(), record.getOdCyl())
                            && java.util.Objects.equals(existing.getOdAxis(), record.getOdAxis())
                            && java.util.Objects.equals(existing.getOsSph(), record.getOsSph())
                            && java.util.Objects.equals(existing.getOsCyl(), record.getOsCyl())
                            && java.util.Objects.equals(existing.getOsAxis(), record.getOsAxis())
                            && java.util.Objects.equals(existing.getOptometristName(), record.getOptometristName())) {
                        optometryIdMap.put(record.getId(), existing.getId());
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }

                boolean wasDeleted = Boolean.TRUE.equals(record.getDeleted());
                Long oldId = record.getId();
                record.setId(null);
                record.setDeleted(false);
                record.setDeletedTime(null);
                record.setDeletedBy(null);
                optometryRecordMapper.insert(record);
                if (wasDeleted) {
                    record.setDeleted(true);
                    optometryRecordMapper.updateById(record);
                }
                optometryIdMap.put(oldId, record.getId());
                totalImported++;
            }
        }

        // 4. sales_record: dedup by record_no, remap FK
        if (dto.getSalesRecords() != null) {
            for (SalesRecord record : dto.getSalesRecords()) {
                SalesRecord existing = findSalesRecordByNo(record.getRecordNo());
                if (existing != null) {
                    continue;
                }
                boolean wasDeleted = Boolean.TRUE.equals(record.getDeleted());
                if (record.getCustomerId() != null) {
                    record.setCustomerId(customerIdMap.get(record.getCustomerId()));
                }
                if (record.getOptometryId() != null) {
                    record.setOptometryId(optometryIdMap.get(record.getOptometryId()));
                }
                if (record.getOperatorId() != null) {
                    record.setOperatorId(userIdMap.get(record.getOperatorId()));
                }
                if (record.getDeletedBy() != null) {
                    record.setDeletedBy(userIdMap.get(record.getDeletedBy()));
                }
                record.setId(null);
                record.setDeleted(false);
                record.setDeletedTime(null);
                record.setDeletedBy(null);
                salesRecordMapper.insert(record);
                if (wasDeleted) {
                    record.setDeleted(true);
                    salesRecordMapper.updateById(record);
                }
                totalImported++;
            }
        }

        log.info("Merge import completed. Total records imported: {}", totalImported);
        return totalImported;
    }

    private Customer findCustomerByPhone(String phone) {
        return customerMapper.selectByPhoneIncludingDeleted(phone);
    }

    private SalesRecord findSalesRecordByNo(String recordNo) {
        return salesRecordMapper.selectByRecordNoIncludingDeleted(recordNo);
    }

    @Override
    @Transactional
    public int resetAllData() {
        int deleted = 0;
        deleted += salesRecordMapper.deleteAll();
        deleted += optometryRecordMapper.deleteAll();
        deleted += customerMapper.deleteAll();
        deleted += sysUserMapper.deleteAllNonAdmin(RoleConstants.ADMIN);
        log.info("Data reset completed. {} records deleted (admin preserved).", deleted);
        return deleted;
    }
}
