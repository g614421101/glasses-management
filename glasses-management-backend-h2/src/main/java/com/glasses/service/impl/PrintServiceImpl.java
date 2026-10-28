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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
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

        // 基础信息：不打印单号，日期取打印时刻
        doc.add(new Paragraph("日期: " + DateUtil.formatDateTime(new Date()))
                .setFont(chineseFont).setFontSize(10).setMarginBottom(5));
        String genderStr = customer.getGender() != null ? (customer.getGender() == 1 ? "男" : "女") : "-";
        doc.add(new Paragraph("顾客姓名: " + nvl(customer.getName()) + "    性别: " + genderStr
                + "    联系电话: " + nvl(customer.getPhone()))
                .setFont(chineseFont).setFontSize(10).setMarginBottom(10));

        // 验光数据：表格只放球镜/柱镜/轴位/矫正视力，眼别留空，其余数据移到表格下方
        if (opto != null) {
            doc.add(bold(new Paragraph("【验光数据】").setFont(chineseFont).setFontSize(12)).setMarginBottom(3));
            doc.add(new Paragraph("验光日期: " + fmtDate(opto.getExamDate())
                    + "          验光师: " + nvl(opto.getOptometristName()))
                    .setFont(chineseFont).setFontSize(10).setMarginBottom(5));

            Table optoTable = new Table(UnitValue.createPercentArray(new float[] { 20, 20, 20, 20, 20 }))
                    .useAllAvailableWidth().setMarginBottom(8);

            String[] headers = { "", "球镜 SPH", "柱镜 CYL", "轴位 AXIS", "矫正视力 VA" };
            for (String h : headers) {
                optoTable.addHeaderCell(new Cell().add(bold(new Paragraph(h).setFont(chineseFont).setFontSize(9)))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // 眼别列头留空，行内标注右/左眼
            optoTable.addCell(cell("右眼(OD)", chineseFont));
            optoTable.addCell(cell(fmtDiopter(opto.getOdSph()), chineseFont));
            optoTable.addCell(cell(fmtDiopter(opto.getOdCyl()), chineseFont));
            optoTable.addCell(cell(intText(opto.getOdAxis()), chineseFont));
            optoTable.addCell(cell(nvl(opto.getOdVa()), chineseFont));

            optoTable.addCell(cell("左眼(OS)", chineseFont));
            optoTable.addCell(cell(fmtDiopter(opto.getOsSph()), chineseFont));
            optoTable.addCell(cell(fmtDiopter(opto.getOsCyl()), chineseFont));
            optoTable.addCell(cell(intText(opto.getOsAxis()), chineseFont));
            optoTable.addCell(cell(nvl(opto.getOsVa()), chineseFont));

            doc.add(optoTable);

            // 瞳距 / 瞳高 写在表格下方
            doc.add(new Paragraph("瞳距 PD: 右 " + num(opto.getOdPd()) + "    左 " + num(opto.getOsPd())
                    + "        瞳高 PH: 右 " + num(opto.getOdPh()) + "    左 " + num(opto.getOsPh()))
                    .setFont(chineseFont).setFontSize(10).setMarginBottom(3));
            doc.add(new Paragraph("远用瞳距: " + num(opto.getPdFar())
                    + "    近用瞳距: " + num(opto.getPdNear())
                    + "    下加光(ADD): " + fmtDiopter(opto.getAddPower()))
                    .setFont(chineseFont).setFontSize(10).setMarginBottom(3));
            if (!isBlank(opto.getRemark())) {
                doc.add(new Paragraph("验光备注: " + opto.getRemark())
                        .setFont(chineseFont).setFontSize(10));
            }
        }

        // 配镜明细：全部字段列出
        doc.add(bold(new Paragraph("【配镜明细】").setFont(chineseFont).setFontSize(12))
                .setMarginTop(12).setMarginBottom(5));

        Table salesTable = new Table(UnitValue.createPercentArray(new float[] { 10, 15, 21, 8, 15, 15, 16 }))
                .useAllAvailableWidth().setMarginBottom(8);

        String[] sHeaders = { "项目", "品牌", "型号/参数", "数量", "零售单价", "实际售价", "小计(元)" };
        for (String h : sHeaders) {
            salesTable.addHeaderCell(new Cell().add(bold(new Paragraph(h).setFont(chineseFont).setFontSize(9)))
                    .setTextAlignment(TextAlignment.CENTER));
        }

        int fq = salesRecord.getFrameQuantity() != null ? salesRecord.getFrameQuantity() : 1;
        BigDecimal frameRetail = salesRecord.getFrameRetailPrice() != null
                ? salesRecord.getFrameRetailPrice() : BigDecimal.ZERO;
        BigDecimal framePrice = salesRecord.getFramePrice() != null
                ? salesRecord.getFramePrice() : BigDecimal.ZERO;

        salesTable.addCell(cell("镜架", chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getFrameBrand()), chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getFrameModel()), chineseFont));
        salesTable.addCell(cell(String.valueOf(fq), chineseFont));
        salesTable.addCell(cell(money(frameRetail), chineseFont));
        salesTable.addCell(cell(money(framePrice), chineseFont));
        salesTable.addCell(cell(money(framePrice.multiply(BigDecimal.valueOf(fq))), chineseFont));

        int lq = salesRecord.getLensQuantity() != null ? salesRecord.getLensQuantity() : 1;
        BigDecimal lensRetail = salesRecord.getLensRetailPrice() != null
                ? salesRecord.getLensRetailPrice() : BigDecimal.ZERO;
        BigDecimal lensPrice = salesRecord.getLensPrice() != null
                ? salesRecord.getLensPrice() : BigDecimal.ZERO;

        salesTable.addCell(cell("镜片", chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getLensBrand()), chineseFont));
        salesTable.addCell(cell(nvl(salesRecord.getLensParams()), chineseFont));
        salesTable.addCell(cell(String.valueOf(lq), chineseFont));
        salesTable.addCell(cell(money(lensRetail), chineseFont));
        salesTable.addCell(cell(money(lensPrice), chineseFont));
        salesTable.addCell(cell(money(lensPrice.multiply(BigDecimal.valueOf(lq))), chineseFont));

        doc.add(salesTable);

        // 合计
        doc.add(new Paragraph("零售总价: ￥" + money(salesRecord.getTotalRetailPrice()))
                .setFont(chineseFont).setFontSize(10).setMarginBottom(3));

        if (!isBlank(salesRecord.getRemark())) {
            doc.add(new Paragraph("备注: " + salesRecord.getRemark())
                    .setFont(chineseFont).setFontSize(10).setMarginBottom(3));
        }

        doc.add(bold(new Paragraph("实收总价:  ￥" + money(salesRecord.getTotalAmount()))
                .setFont(chineseFont).setFontSize(16))
                .setTextAlignment(TextAlignment.RIGHT).setMarginTop(6).setMarginBottom(30));

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
        dto.setOdPh(opto.getOdPh() != null ? opto.getOdPh().toString() : "");
        dto.setOsSph(fmtDiopter(opto.getOsSph()));
        dto.setOsCyl(fmtDiopter(opto.getOsCyl()));
        dto.setOsAxis(opto.getOsAxis() != null ? opto.getOsAxis().toString() : "");
        dto.setOsPd(opto.getOsPd() != null ? opto.getOsPd().toString() : "");
        dto.setOsPh(opto.getOsPh() != null ? opto.getOsPh().toString() : "");
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
        return s != null && !s.trim().isEmpty() ? s : "-";
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String fmtDate(java.util.Date date) {
        return date != null ? DateUtil.formatDate(date) : "-";
    }

    private String num(BigDecimal v) {
        return v != null ? v.toPlainString() : "-";
    }

    private String intText(Integer v) {
        return v != null ? v.toString() : "-";
    }

    private String money(BigDecimal v) {
        if (v == null) {
            return "0.00";
        }
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
