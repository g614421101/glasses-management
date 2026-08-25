package com.glasses.dto;

import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.entity.SysUser;
import lombok.Data;

import java.util.List;

@Data
public class DataExportDTO {
    private String exportTime;
    private String version;
    private List<SysUser> sysUsers;
    private List<Customer> customers;
    private List<OptometryRecord> optometryRecords;
    private List<SalesRecord> salesRecords;
}
