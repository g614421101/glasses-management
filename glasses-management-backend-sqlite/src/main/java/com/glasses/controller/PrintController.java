package com.glasses.controller;

import com.glasses.dto.PrintResult;
import com.glasses.service.PrintService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/print")
public class PrintController {

    @Autowired
    private PrintService printService;

    /**
     * 生成配镜处方单 PDF (A4)
     */
    @GetMapping("/prescription/{recordId}")
    public void printPrescription(@PathVariable Long recordId, HttpServletResponse response) throws IOException {
        PrintResult result = printService.buildPrescriptionPdf(recordId);
        if (result == null) {
            response.setStatus(404);
            return;
        }
        response.setContentType(result.getContentType());
        response.setHeader("Content-Disposition", result.getContentDisposition());
        response.getOutputStream().write(result.getData());
    }

    /**
     * 导出某顾客全部配镜记录为 Excel
     */
    @GetMapping("/export/customer/{customerId}")
    public void exportCustomerRecords(@PathVariable Long customerId, HttpServletResponse response) throws IOException {
        PrintResult result = printService.buildCustomerRecordsExcel(customerId);
        if (result == null) {
            response.setStatus(404);
            return;
        }
        response.setContentType(result.getContentType());
        response.setHeader("Content-Disposition", result.getContentDisposition());
        response.getOutputStream().write(result.getData());
    }

    /**
     * 导出营业额流水为 Excel
     */
    @GetMapping("/export/revenue")
    public void exportRevenue(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "false") Boolean showAll,
            HttpServletResponse response) throws IOException {
        PrintResult result = printService.buildRevenueExcel(startDate, endDate, Boolean.TRUE.equals(showAll));
        response.setContentType(result.getContentType());
        response.setHeader("Content-Disposition", result.getContentDisposition());
        response.getOutputStream().write(result.getData());
    }
}
