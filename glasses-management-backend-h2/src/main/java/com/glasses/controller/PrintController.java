package com.glasses.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.glasses.dto.SalesRecordExcelDTO;
import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.service.CustomerService;
import com.glasses.service.OptometryRecordService;
import com.glasses.service.SalesRecordService;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/print")
public class PrintController {

    @Autowired
    private SalesRecordService salesRecordService;

    @Autowired
    private OptometryRecordService optometryRecordService;

    @Autowired
    private CustomerService customerService;

    /**
     * 生成配镜处方单 PDF (A4)
     */
    @GetMapping("/prescription/{recordId}")
    public void printPrescription(@PathVariable Long recordId, HttpServletResponse response) throws IOException {
        SalesRecord salesRecord = salesRecordService.getById(recordId);
        if (salesRecord == null || Boolean.TRUE.equals(salesRecord.getDeleted())) {
            response.setStatus(404);
            return;
        }

        Customer customer = customerService.getById(salesRecord.getCustomerId());
        if (customer == null || Boolean.TRUE.equals(customer.getDeleted())) {
            response.setStatus(404);
            return;
        }

        OptometryRecord opto = null;
        if (salesRecord.getOptometryId() != null) {
            opto = optometryRecordService.getById(salesRecord.getOptometryId());
            if (opto != null && Boolean.TRUE.equals(opto.getDeleted())) {
                opto = null;
            }
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=prescription_" + recordId + ".pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 50, 40, 50);

        // 加载中文字体 - Windows 系统自带宋体
        PdfFont chineseFont;
        try {
            chineseFont = PdfFontFactory.createFont("C:/Windows/Fonts/simsun.ttc,0", PdfEncodings.IDENTITY_H);
        } catch (Exception e) {
            // 如果宋体不存在则尝试微软雅黑
            try {
                chineseFont = PdfFontFactory.createFont("C:/Windows/Fonts/msyh.ttc,0", PdfEncodings.IDENTITY_H);
            } catch (Exception e2) {
                // 全部失败则用默认字体（英文 fallback）
                chineseFont = PdfFontFactory.createFont();
            }
        }

        // 标题
        doc.add(new Paragraph("配 镜 处 方 单")
                .setFont(chineseFont).setFontSize(22).setBold()
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));
        doc.add(new Paragraph("Glasses Prescription")
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // 基础信息行
        String dateStr = salesRecord.getSalesDate() != null ? DateUtil.formatDateTime(salesRecord.getSalesDate()) : "-";
        doc.add(new Paragraph("单号: " + salesRecord.getRecordNo() + "          日期: " + dateStr)
                .setFont(chineseFont).setFontSize(10).setMarginBottom(5));
        if (customer != null) {
            String genderStr = customer.getGender() != null ? (customer.getGender() == 1 ? "男" : "女") : "";
            doc.add(new Paragraph("顾客姓名: " + customer.getName() + "    性别: " + genderStr
                    + "    联系电话: " + customer.getPhone())
                    .setFont(chineseFont).setFontSize(10).setMarginBottom(10));
        }

        // 验光数据表格
        if (opto != null) {
            doc.add(new Paragraph("【验光数据】").setFont(chineseFont).setFontSize(12).setBold().setMarginBottom(5));

            Table optoTable = new Table(UnitValue.createPercentArray(new float[] { 15, 17, 17, 17, 17, 17 }))
                    .useAllAvailableWidth().setMarginBottom(10);

            // 表头
            String[] headers = { "眼别", "球镜 SPH", "柱镜 CYL", "轴位 AXIS", "矫正视力 VA", "瞳距 PD" };
            for (String h : headers) {
                optoTable.addHeaderCell(new Cell().add(new Paragraph(h).setFont(chineseFont).setFontSize(9).setBold())
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // 右眼
            optoTable.addCell(cell("右眼(OD)", chineseFont));
            optoTable.addCell(cell(fmtDiopter(opto.getOdSph()), chineseFont));
            optoTable.addCell(cell(fmtDiopter(opto.getOdCyl()), chineseFont));
            optoTable.addCell(cell(opto.getOdAxis() != null ? opto.getOdAxis().toString() : "-", chineseFont));
            optoTable.addCell(cell(opto.getOdVa() != null ? opto.getOdVa() : "-", chineseFont));
            optoTable.addCell(cell(opto.getOdPd() != null ? opto.getOdPd().toString() : "-", chineseFont));

            // 左眼
            optoTable.addCell(cell("左眼(OS)", chineseFont));
            optoTable.addCell(cell(fmtDiopter(opto.getOsSph()), chineseFont));
            optoTable.addCell(cell(fmtDiopter(opto.getOsCyl()), chineseFont));
            optoTable.addCell(cell(opto.getOsAxis() != null ? opto.getOsAxis().toString() : "-", chineseFont));
            optoTable.addCell(cell(opto.getOsVa() != null ? opto.getOsVa() : "-", chineseFont));
            optoTable.addCell(cell(opto.getOsPd() != null ? opto.getOsPd().toString() : "-", chineseFont));

            doc.add(optoTable);

            // 瞳距与下加光汇总
            StringBuilder pdInfo = new StringBuilder();
            if (opto.getPdFar() != null)
                pdInfo.append("瞳距: ").append(opto.getPdFar()).append("    ");
            if (opto.getPdNear() != null)
                pdInfo.append("近用瞳距: ").append(opto.getPdNear()).append("    ");
            if (opto.getAddPower() != null)
                pdInfo.append("下加光(ADD): ").append(opto.getAddPower());
            if (!pdInfo.isEmpty()) {
                doc.add(new Paragraph(pdInfo.toString())
                        .setFont(chineseFont).setFontSize(10).setMarginBottom(10));
            }
        }

        // 配镜信息表格
        doc.add(new Paragraph("【配镜明细】").setFont(chineseFont).setFontSize(12).setBold().setMarginBottom(5));

        Table salesTable = new Table(UnitValue.createPercentArray(new float[] { 20, 30, 25, 25 }))
                .useAllAvailableWidth().setMarginBottom(15);

        String[] sHeaders = { "项目", "品牌/参数", "型号/规格", "价格(元)" };
        for (String h : sHeaders) {
            salesTable.addHeaderCell(new Cell().add(new Paragraph(h).setFont(chineseFont).setFontSize(9).setBold())
                    .setTextAlignment(TextAlignment.CENTER));
        }

        salesTable.addCell(cell("镜架", chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getFrameBrand()), chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getFrameModel()), chineseFont));
        salesTable.addCell(
                cell(salesRecord.getFramePrice() != null ? salesRecord.getFramePrice().toString() : "0", chineseFont));

        salesTable.addCell(cell("镜片", chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getLensBrand()), chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getLensParams()), chineseFont));
        salesTable.addCell(
                cell(salesRecord.getLensPrice() != null ? salesRecord.getLensPrice().toString() : "0", chineseFont));

        doc.add(salesTable);

        // 合计金额
        doc.add(new Paragraph("实收合计:  ￥" + salesRecord.getTotalAmount())
                .setFont(chineseFont).setFontSize(16).setBold()
                .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(30));

        // 签名区域
        Table signTable = new Table(UnitValue.createPercentArray(new float[] { 50, 50 })).useAllAvailableWidth();
        signTable.addCell(new Cell().add(new Paragraph("验光师签字:_______________").setFont(chineseFont).setFontSize(10))
                .setBorder(null));
        signTable.addCell(new Cell().add(new Paragraph("顾客签字:_______________").setFont(chineseFont).setFontSize(10))
                .setBorder(null));
        doc.add(signTable);

        doc.close();
    }

    /**
     * 导出某顾客全部配镜记录为 Excel
     */
    @GetMapping("/export/customer/{customerId}")
    public void exportCustomerRecords(@PathVariable Long customerId, HttpServletResponse response) throws IOException {
        Customer customer = customerService.getById(customerId);
        if (customer == null || Boolean.TRUE.equals(customer.getDeleted())) {
            response.setStatus(404);
            return;
        }
        List<SalesRecord> salesRecords = salesRecordService.listByCustomerId(customerId);

        List<SalesRecordExcelDTO> dataList = new ArrayList<>();
        for (SalesRecord sr : salesRecords) {
            SalesRecordExcelDTO dto = new SalesRecordExcelDTO();
            dto.setCustomerName(customer != null ? customer.getName() : "-");
            dto.setPhone(customer != null ? customer.getPhone() : "-");
            dto.setRecordNo(sr.getRecordNo());
            dto.setSalesDate(sr.getSalesDate() != null ? DateUtil.formatDateTime(sr.getSalesDate()) : "-");
            dto.setFrameBrand(nvl(sr.getFrameBrand()));
            dto.setFrameModel(nvl(sr.getFrameModel()));
            dto.setFramePrice(sr.getFramePrice() != null ? sr.getFramePrice().toString() : "0");
            dto.setLensBrand(nvl(sr.getLensBrand()));
            dto.setLensParams(nvl(sr.getLensParams()));
            dto.setLensPrice(sr.getLensPrice() != null ? sr.getLensPrice().toString() : "0");
            dto.setTotalAmount(sr.getTotalAmount() != null ? sr.getTotalAmount().toString() : "0");

            // 如果关联了验光单则填充验光数据
            if (sr.getOptometryId() != null) {
                OptometryRecord opto = optometryRecordService.getById(sr.getOptometryId());
                if (opto != null && !Boolean.TRUE.equals(opto.getDeleted())) {
                    dto.setOdSph(fmtDiopter(opto.getOdSph()));
                    dto.setOdCyl(fmtDiopter(opto.getOdCyl()));
                    dto.setOdAxis(opto.getOdAxis() != null ? opto.getOdAxis().toString() : "");
                    dto.setOdPd(opto.getOdPd() != null ? opto.getOdPd().toString() : "");
                    dto.setOsSph(fmtDiopter(opto.getOsSph()));
                    dto.setOsCyl(fmtDiopter(opto.getOsCyl()));
                    dto.setOsAxis(opto.getOsAxis() != null ? opto.getOsAxis().toString() : "");
                    dto.setOsPd(opto.getOsPd() != null ? opto.getOsPd().toString() : "");
                    dto.setPdFar(opto.getPdFar() != null ? opto.getPdFar().toString() : "");
                    dto.setPdNear(opto.getPdNear() != null ? opto.getPdNear().toString() : "");
                    dto.setAddPower(opto.getAddPower() != null ? opto.getAddPower().toString() : "");
                }
            }
            dataList.add(dto);
        }

        String fileName = "配镜记录_" + (customer != null ? customer.getName() : customerId) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" +
                URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        EasyExcel.write(response.getOutputStream(), SalesRecordExcelDTO.class)
                .sheet("配镜记录")
                .doWrite(dataList);
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
        
        QueryWrapper<SalesRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);
        if (!showAll && startDate != null && endDate != null) {
            wrapper.ge("sales_date", startDate + " 00:00:00")
                   .le("sales_date", endDate + " 23:59:59");
        }
        wrapper.orderByDesc("sales_date");
        
        List<SalesRecord> records = salesRecordService.list(wrapper);
        List<SalesRecordExcelDTO> dataList = new ArrayList<>();
        
        for (SalesRecord sr : records) {
            Customer customer = customerService.getById(sr.getCustomerId());
            if (customer != null && Boolean.TRUE.equals(customer.getDeleted())) {
                customer = null;
            }
            SalesRecordExcelDTO dto = new SalesRecordExcelDTO();
            dto.setCustomerName(customer != null ? customer.getName() : "-");
            dto.setPhone(customer != null ? customer.getPhone() : "-");
            dto.setRecordNo(sr.getRecordNo());
            dto.setSalesDate(sr.getSalesDate() != null ? DateUtil.formatDateTime(sr.getSalesDate()) : "-");
            dto.setFrameBrand(nvl(sr.getFrameBrand()));
            dto.setFrameModel(nvl(sr.getFrameModel()));
            dto.setFramePrice(sr.getFramePrice() != null ? sr.getFramePrice().toString() : "0");
            dto.setLensBrand(nvl(sr.getLensBrand()));
            dto.setLensParams(nvl(sr.getLensParams()));
            dto.setLensPrice(sr.getLensPrice() != null ? sr.getLensPrice().toString() : "0");
            dto.setTotalAmount(sr.getTotalAmount() != null ? sr.getTotalAmount().toString() : "0");

            if (sr.getOptometryId() != null) {
                OptometryRecord opto = optometryRecordService.getById(sr.getOptometryId());
                if (opto != null && !Boolean.TRUE.equals(opto.getDeleted())) {
                    dto.setOdSph(fmtDiopter(opto.getOdSph()));
                    dto.setOdCyl(fmtDiopter(opto.getOdCyl()));
                    dto.setOdAxis(opto.getOdAxis() != null ? opto.getOdAxis().toString() : "");
                    dto.setOdPd(opto.getOdPd() != null ? opto.getOdPd().toString() : "");
                    dto.setOsSph(fmtDiopter(opto.getOsSph()));
                    dto.setOsCyl(fmtDiopter(opto.getOsCyl()));
                    dto.setOsAxis(opto.getOsAxis() != null ? opto.getOsAxis().toString() : "");
                    dto.setOsPd(opto.getOsPd() != null ? opto.getOsPd().toString() : "");
                    dto.setPdFar(opto.getPdFar() != null ? opto.getPdFar().toString() : "");
                    dto.setPdNear(opto.getPdNear() != null ? opto.getPdNear().toString() : "");
                    dto.setAddPower(opto.getAddPower() != null ? opto.getAddPower().toString() : "");
                }
            }
            dataList.add(dto);
        }

        String fileName = "营业额流水_" + startDate + "_至_" + endDate + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" +
                URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        EasyExcel.write(response.getOutputStream(), SalesRecordExcelDTO.class)
                .sheet("营业额流水")
                .doWrite(dataList);
    }

    // ========== 工具方法 ==========

    private Cell cell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER);
    }

    private String fmtDiopter(java.math.BigDecimal val) {
        if (val == null)
            return "-";
        double d = val.doubleValue();
        return d > 0 ? "+" + val.toPlainString() : val.toPlainString();
    }

    private String nvl(String s) {
        return s != null ? s : "-";
    }
}
