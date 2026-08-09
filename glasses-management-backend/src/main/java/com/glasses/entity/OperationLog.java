package com.glasses.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 操作日志（审计用，物理保留，不参与软删除）。
 */
@Data
@Table("operation_log")
public class OperationLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 操作人 ID（未登录为 null） */
    private Long operatorId;

    /** 操作人用户名 */
    private String operatorName;

    /** 业务模块（如 customer / sales_record） */
    private String module;

    /** 操作类型：ADD / UPDATE / DELETE / OTHER */
    private String action;

    /** HTTP 方法 */
    private String method;

    /** 请求路径 */
    private String uri;

    /** 用户可读的操作描述（如：新增顾客：张三） */
    private String description;

    /** 请求参数 JSON（敏感接口为空） */
    private String params;

    /** 业务结果码（Result.code，异常为 500） */
    private Integer status;

    /** 结果消息 */
    private String message;

    /** 耗时（毫秒） */
    private Long costMs;

    /** 客户端 IP */
    private String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;
}
