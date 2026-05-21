package com.glasses.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("optometry_record")
public class OptometryRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date examDate;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    @TableLogic
    private Boolean deleted = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date deletedTime;
    private Long deletedBy;
}