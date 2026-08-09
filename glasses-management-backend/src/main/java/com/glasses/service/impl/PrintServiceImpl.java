package com.glasses.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.mybatisflex.core.query.QueryWrapper;
import com.glasses.dto.PrintResult;
import com.glasses.dto.SalesRecordExcelDTO;
import com.glasses.entity.Customer;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.service.CustomerService;
import com.glasses.service.OptometryRecordService;
import com.glasses.service.PrintService;
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
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PrintServiceImpl implements PrintService {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private SalesRecordService salesRecordService;

    @Autowired
    private OptometryRecordService optometryRecordService;

    @Autowired
    private CustomerService customerService;

    @Override
    public PrintResult buildPrescriptionPdf(Long recordId) throws IOException {
        SalesRecord salesRecord = salesRecordService.getById(recordId);
        if (salesRecord == null || Boolean.TRUE.equals(salesRecord.getDeleted())) {
            return null;
        }

        Customer customer = customerService.getById(salesRecord.getCustomerId());
        if (customer == null || Boolean.TRUE.equals(customer.getDeleted())) {
            return null;
        }

        OptometryRecord opto = null;
        if (salesRecord.getOptometryId() != null) {
            opto = optometryRecordService.getById(salesRecord.getOptometryId());
            if (opto != null && Boolean.TRUE.equals(opto.getDeleted())) {
                opto = null;
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
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
        doc.add(bold(new Paragraph("配 镜 处 方 单")
                .setFont(chineseFont).setFontSize(22))
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));
        doc.add(new Paragraph("Glasses Prescription")
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // 基础信息行
        String dateStr = salesRecord.getSalesDate() != null ? DateUtil.formatDateTime(salesRecord.getSalesDate()) : "-";
        doc.add(new Paragraph("单号: " + salesRecord.getRecordNo() + "          日期: " + dateStr)
                .setFont(chineseFont).setFontSize(10).setMarginBottom(5));
        String genderStr = customer.getGender() != null ? (customer.getGender() == 1 ? "男" : "女") : "";
        doc.add(new Paragraph("顾客姓名: " + customer.getName() + "    性别: " + genderStr
                + "    联系电话: " + customer.getPhone())
                .setFont(chineseFont).setFontSize(10).setMarginBottom(10));

        // 验光数据表格
        if (opto != null) {
            doc.add(bold(new Paragraph("【验光数据】").setFont(chineseFont).setFontSize(12)).setMarginBottom(5));

            Table optoTable = new Table(UnitValue.createPercentArray(new float[] { 15, 17, 17, 17, 17, 17 }))
                    .useAllAvailableWidth().setMarginBottom(10);

            // 表头
            String[] headers = { "眼别", "球镜 SPH", "柱镜 CYL", "轴位 AXIS", "矫正视力 VA", "瞳距 PD" };
            for (String h : headers) {
                optoTable.addHeaderCell(new Cell().add(bold(new Paragraph(h).setFont(chineseFont).setFontSize(9)))
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
                pdInfo.append("下加光(ADD): ").append(fmtDiopter(opto.getAddPower()));
            if (!pdInfo.isEmpty()) {
                doc.add(new Paragraph(pdInfo.toString())
                        .setFont(chineseFont).setFontSize(10).setMarginBottom(10));
            }
        }

        // 配镜信息表格
        doc.add(bold(new Paragraph("【配镜明细】").setFont(chineseFont).setFontSize(12)).setMarginBottom(5));

        Table salesTable = new Table(UnitValue.createPercentArray(new float[] { 15, 25, 22, 13, 25 }))
                .useAllAvailableWidth().setMarginBottom(15);

        String[] sHeaders = { "项目", "品牌/参数", "型号/规格", "数量", "小计(元)" };
        for (String h : sHeaders) {
            salesTable.addHeaderCell(new Cell().add(bold(new Paragraph(h).setFont(chineseFont).setFontSize(9)))
                    .setTextAlignment(TextAlignment.CENTER));
        }

        salesTable.addCell(cell("镜架", chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getFrameBrand()), chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getFrameModel()), chineseFont));
        int fq = salesRecord.getFrameQuantity() != null ? salesRecord.getFrameQuantity() : 1;
        salesTable.addCell(cell(String.valueOf(fq), chineseFont));
        java.math.BigDecimal frameSubtotal = salesRecord.getFramePrice() != null
                ? salesRecord.getFramePrice().multiply(java.math.BigDecimal.valueOf(fq)) : java.math.BigDecimal.ZERO;
        salesTable.addCell(cell(frameSubtotal.toString(), chineseFont));

        salesTable.addCell(cell("镜片", chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getLensBrand()), chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getLensParams()), chineseFont));
        int lq = salesRecord.getLensQuantity() != null ? salesRecord.getLensQuantity() : 1;
        salesTable.addCell(cell(String.valueOf(lq), chineseFont));
        java.math.BigDecimal lensSubtotal = salesRecord.getLensPrice() != null
                ? salesRecord.getLensPrice().multiply(java.math.BigDecimal.valueOf(lq)) : java.math.BigDecimal.ZERO;
        salesTable.addCell(cell(lensSubtotal.toString(), chineseFont));

        doc.add(salesTable);

        // 合计金额
        doc.add(bold(new Paragraph("实收合计:  ￥" + salesRecord.getTotalAmount())
                .setFont(chineseFont).setFontSize(16))
                .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(30));

        // 签名区域
        Table signTable = new Table(UnitValue.createPercentArray(new float[] { 50, 50 })).useAllAvailableWidth();
        signTable.addCell(new Cell().add(new Paragraph("验光师签字:_______________").setFont(chineseFont).setFontSize(10))
                .setBorder(null));
        signTable.addCell(new Cell().add(new Paragraph("顾客签字:_______________").setFont(chineseFont).setFontSize(10))
                .setBorder(null));
        doc.add(signTable);

        doc.close();

        return new PrintResult("application/pdf",
                "inline; filename=prescription_" + recordId + ".pdf", out.toByteArray());
    }

    @Override
    public PrintResult buildCustomerRecordsExcel(Long customerId) {
        Customer customer = customerService.getById(customerId);
        if (customer == null || Boolean.TRUE.equals(customer.getDeleted())) {
            return null;
        }
        List<SalesRecord> salesRecords = salesRecordService.listByCustomerId(customerId);

        // 批量查询关联的验光记录，避免 N+1
        Map<Long, OptometryRecord> optometryMap = new java.util.HashMap<>();
        List<Long> optometryIds = salesRecords.stream()
                .map(SalesRecord::getOptometryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!optometryIds.isEmpty()) {
            List<OptometryRecord> optometryRecords = optometryRecordService.listByIds(optometryIds);
            for (OptometryRecord o : optometryRecords) {
                if (!Boolean.TRUE.equals(o.getDeleted())) {
                    optometryMap.put(o.getId(), o);
                }
            }
        }

        List<SalesRecordExcelDTO> dataList = new ArrayList<>();
        for (SalesRecord sr : salesRecords) {
            SalesRecordExcelDTO dto = toExcelDTO(sr, customer.getName(), customer.getPhone());
            // 从批量查询的 Map 中取验光数据
            OptometryRecord opto = optometryMap.get(sr.getOptometryId());
            fillOptometry(dto, opto);
            dataList.add(dto);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, SalesRecordExcelDTO.class)
                .sheet("配镜记录")
                .doWrite(dataList);

        String fileName = "配镜记录_" + customer.getName() + ".xlsx";
        return new PrintResult(EXCEL_CONTENT_TYPE, attachmentHeader(fileName), out.toByteArray());
    }

    @Override
    public PrintResult buildRevenueExcel(String startDate, String endDate, boolean showAll) {
        QueryWrapper query = QueryWrapper.create()
                .from(SalesRecord.class)
                .where(SalesRecord::getDeleted).eq(false);
        if (!showAll && startDate != null && endDate != null) {
            query.and(SalesRecord::getSalesDate).ge(startDate + " 00:00:00")
                    .and(SalesRecord::getSalesDate).le(endDate + " 23:59:59");
        }
        query.orderBy(SalesRecord::getSalesDate).desc();

        List<SalesRecord> records = salesRecordService.list(query);

        // 批量查询顾客信息，避免 N+1
        Map<Long, Customer> customerMap = new java.util.HashMap<>();
        List<Long> customerIds = records.stream()
                .map(SalesRecord::getCustomerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!customerIds.isEmpty()) {
            List<Customer> customers = customerService.listByIds(customerIds);
            for (Customer c : customers) {
                if (!Boolean.TRUE.equals(c.getDeleted())) {
                    customerMap.put(c.getId(), c);
                }
            }
        }

        // 批量查询验光记录，避免 N+1
        Map<Long, OptometryRecord> optometryMap = new java.util.HashMap<>();
        List<Long> optometryIds = records.stream()
                .map(SalesRecord::getOptometryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!optometryIds.isEmpty()) {
            List<OptometryRecord> optometryRecords = optometryRecordService.listByIds(optometryIds);
            for (OptometryRecord o : optometryRecords) {
                if (!Boolean.TRUE.equals(o.getDeleted())) {
                    optometryMap.put(o.getId(), o);
                }
            }
        }

        List<SalesRecordExcelDTO> dataList = new ArrayList<>();
        for (SalesRecord sr : records) {
            Customer customer = customerMap.get(sr.getCustomerId());
            SalesRecordExcelDTO dto = toExcelDTO(sr,
                    customer != null ? customer.getName() : "-",
                    customer != null ? customer.getPhone() : "-");
            fillOptometry(dto, optometryMap.get(sr.getOptometryId()));
            dataList.add(dto);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, SalesRecordExcelDTO.class)
                .sheet("营业额流水")
                .doWrite(dataList);

        String fileName = "营业额流水_" + startDate + "_至_" + endDate + ".xlsx";
        return new PrintResult(EXCEL_CONTENT_TYPE, attachmentHeader(fileName), out.toByteArray());
    }

    // ========== 工具方法 ==========

    private String attachmentHeader(String fileName) {
        return "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    }

    private SalesRecordExcelDTO toExcelDTO(SalesRecord sr, String customerName, String phone) {
        SalesRecordExcelDTO dto = new SalesRecordExcelDTO();
        dto.setCustomerName(customerName);
        dto.setPhone(phone);
        dto.setRecordNo(sr.getRecordNo());
        dto.setSalesDate(sr.getSalesDate() != null ? DateUtil.formatDateTime(sr.getSalesDate()) : "-");
        dto.setFrameBrand(nvl(sr.getFrameBrand()));
        dto.setFrameModel(nvl(sr.getFrameModel()));
        dto.setFramePrice(sr.getFramePrice() != null ? sr.getFramePrice().toString() : "0");
        dto.setLensBrand(nvl(sr.getLensBrand()));
        dto.setLensParams(nvl(sr.getLensParams()));
        dto.setLensPrice(sr.getLensPrice() != null ? sr.getLensPrice().toString() : "0");
        dto.setTotalAmount(sr.getTotalAmount() != null ? sr.getTotalAmount().toString() : "0");
        dto.setFrameQuantity(sr.getFrameQuantity() != null ? sr.getFrameQuantity().toString() : "1");
        dto.setLensQuantity(sr.getLensQuantity() != null ? sr.getLensQuantity().toString() : "1");
        return dto;
    }

    private void fillOptometry(SalesRecordExcelDTO dto, OptometryRecord opto) {
        if (opto == null) {
            return;
        }
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
        dto.setAddPower(opto.getAddPower() != null ? fmtDiopter(opto.getAddPower()) : "");
    }

    private Cell cell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Paragraph bold(Paragraph paragraph) {
        paragraph.setProperty(Property.BOLD_SIMULATION, true);
        return paragraph;
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
