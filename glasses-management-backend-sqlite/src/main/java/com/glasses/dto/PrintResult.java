package com.glasses.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 打印/导出产物：字节内容 + HTTP 响应头信息。
 */
@Data
@AllArgsConstructor
public class PrintResult {

    /** Content-Type */
    private String contentType;

    /** Content-Disposition（含文件名） */
    private String contentDisposition;

    /** 文件字节内容 */
    private byte[] data;
}
