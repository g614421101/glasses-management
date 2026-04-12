package com.glasses.controller;

import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.service.CustomerService;
import com.glasses.service.OptometryRecordService;
import com.glasses.service.SalesRecordService;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/print")
public class PrintController {

    @Autowired
    private SalesRecordService salesRecordService;

    @Autowired
    private OptometryRecordService optometryRecordService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/prescription/{recordId}")
    public void printPrescription(@PathVariable Long recordId, HttpServletResponse response) throws IOException {
        SalesRecord salesRecord = salesRecordService.getById(recordId);
        if (salesRecord == null) {
            response.setStatus(404);
            return;
        }

        Customer customer = customerService.getById(salesRecord.getCustomerId());
        
        OptometryRecord optometryRecord = null;
        if (salesRecord.getOptometryId() != null) {
            optometryRecord = optometryRecordService.getById(salesRecord.getOptometryId());
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=prescription_" + recordId + ".pdf");

        // Generate PDF using iText7
        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        
        // 此处为了避免字体中文乱码的问题，简单方案是直接使用英文或拼音，
        // 或者需要加载中文字体库（比如从系统加载）。
        // 简单实现为了不依赖本地字体，我们尽可能打印英文标签或默认不涉及复杂中文渲染。
        // 或者我们可以直接采用内置中文字体。但为了最简环境兼容，我们先添加基础文本。
        
        document.add(new Paragraph("Glasses Prescription / Sales Record")
                .setTextAlignment(TextAlignment.CENTER).setFontSize(20));
        document.add(new Paragraph(" "));
        
        
        document.add(new Paragraph("Date: " + cn.hutool.core.date.DateUtil.formatDateTime(salesRecord.getSalesDate())));
        document.add(new Paragraph("Record No: " + salesRecord.getRecordNo()));
        if (customer != null) {
            document.add(new Paragraph("Customer Name: " + customer.getName()));
            document.add(new Paragraph("Phone: " + customer.getPhone()));
        }
        
        document.add(new Paragraph("--------------------------------------------------"));
        
        if (optometryRecord != null) {
            document.add(new Paragraph("Optometry Data:"));
            document.add(new Paragraph(String.format("OD (Right): SPH %s | CYL %s | AXIS %s | VA %s", 
                    optometryRecord.getOdSph(), optometryRecord.getOdCyl(), optometryRecord.getOdAxis(), optometryRecord.getOdVa())));
            document.add(new Paragraph(String.format("OS (Left) : SPH %s | CYL %s | AXIS %s | VA %s", 
                    optometryRecord.getOsSph(), optometryRecord.getOsCyl(), optometryRecord.getOsAxis(), optometryRecord.getOsVa())));
            document.add(new Paragraph(String.format("PD Far: %s | PD Near: %s | ADD: %s", 
                    optometryRecord.getPdFar(), optometryRecord.getPdNear(), optometryRecord.getAddPower())));
            document.add(new Paragraph("--------------------------------------------------"));
        }
        
        document.add(new Paragraph("Sales Details:"));
        document.add(new Paragraph(String.format("Frame: %s %s - %s", salesRecord.getFrameBrand(), salesRecord.getFrameModel(), salesRecord.getFramePrice())));
        document.add(new Paragraph(String.format("Lens: %s %s - %s", salesRecord.getLensBrand(), salesRecord.getLensParams(), salesRecord.getLensPrice())));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Amount: " + salesRecord.getTotalAmount()).setFontSize(16));
        
        document.close();
    }
}
