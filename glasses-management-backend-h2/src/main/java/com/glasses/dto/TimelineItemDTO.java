package com.glasses.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class TimelineItemDTO {
    // 类型: "OPTOMETRY" 或 "SALES"
    private String type;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date date;
    
    // 摘要信息
    private String title;
    private String subtitle;
    
    // 原始数据对象，方便前端展示
    private Object data;
}
