package com.glasses.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Table("sales_record")
public class SalesRecord {
    @Id(keyType = KeyType.Auto)
    private Long id;

    private String recordNo;
    private Long customerId;
    private Long optometryId;

    @Column(ignore = true)
    private String customerName;

    // 镜架信息
    private String frameBrand;
    private String frameModel;
    private Integer frameQuantity;
    private BigDecimal frameRetailPrice;
    private BigDecimal framePrice;

    // 镜片信息
    private String lensBrand;
    private String lensParams;
    private Integer lensQuantity;
    private BigDecimal lensRetailPrice;
    private BigDecimal lensPrice;

    private BigDecimal totalRetailPrice;
    private BigDecimal totalAmount;
    
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date salesDate;
    private Long operatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;

    @Column(isLogicDelete = true)
    private Boolean deleted = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date deletedTime;
    private Long deletedBy;
}