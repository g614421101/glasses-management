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
@Table("optometry_record")
public class OptometryRecord {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long customerId;

    @Column(ignore = true)
    private String customerName;

    // Right eye values
    private BigDecimal odSph;
    private BigDecimal odCyl;
    private Integer odAxis;
    private String odVa;

    // Left eye values
    private BigDecimal osSph;
    private BigDecimal osCyl;
    private Integer osAxis;
    private String osVa;

    // Pupillary distance values
    private BigDecimal odPd;      // Right eye PD
    private BigDecimal osPd;      // Left eye PD
    private BigDecimal pdFar;     // Far PD
    private BigDecimal pdNear;    // Near PD
    private BigDecimal addPower;  // Addition power

    private String optometristName;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date examDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    @Column(isLogicDelete = true)
    private Boolean deleted = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date deletedTime;
    private Long deletedBy;
}