package com.glasses.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sales_record")
public class SalesRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String recordNo;
    private Long customerId;
    private Long optometryId;

    // 镜架信息
    private String frameBrand;
    private String frameModel;
    private BigDecimal framePrice;

    // 镜片信息
    private String lensBrand;
    private String lensParams;
    private BigDecimal lensPrice;

    private BigDecimal totalAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date salesDate;
    private Long operatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;

    @TableLogic
    private Boolean deleted = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date deletedTime;
    private Long deletedBy;
}
