package com.glasses.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
    
    // 右眼参数
    private BigDecimal odSph;
    private BigDecimal odCyl;
    private Integer odAxis;
    private String odVa;
    
    // 左眼参数
    private BigDecimal osSph;
    private BigDecimal osCyl;
    private Integer osAxis;
    private String osVa;
    
    // 其他
    private BigDecimal pdFar;
    private BigDecimal pdNear;
    private BigDecimal addPower;
    
    private String optometristName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date examDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;
}
