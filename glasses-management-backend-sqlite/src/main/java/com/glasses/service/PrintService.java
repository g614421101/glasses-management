package com.glasses.service;

import com.glasses.dto.PrintResult;

import java.io.IOException;

/**
 * 打印与导出服务：配镜处方单 PDF、配镜记录/营业额流水 Excel。
 */
public interface PrintService {

    /**
     * 生成配镜处方单 PDF。
     *
     * @return 产物（含响应头信息）；记录不存在返回 null
     */
    PrintResult buildPrescriptionPdf(Long recordId) throws IOException;

    /**
     * 导出某顾客全部配镜记录为 Excel。
     *
     * @return 产物；顾客不存在返回 null
     */
    PrintResult buildCustomerRecordsExcel(Long customerId);

    /**
     * 导出营业额流水为 Excel。
     */
    PrintResult buildRevenueExcel(String startDate, String endDate, boolean showAll);
}
