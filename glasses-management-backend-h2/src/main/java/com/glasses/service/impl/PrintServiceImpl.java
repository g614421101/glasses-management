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
import com.itextpdf.layout.borders.Border;
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

    /** 纸张 13.8cm × 21.5cm，横向打印：宽 215mm、高 138mm */
    private static final float PAGE_WIDTH = 609.45f;
    private static final float PAGE_HEIGHT = 391.18f;
    /** 四边留白（pt），约 9mm */
    private static final float PAGE_MARGIN = 16f;

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
        PdfDocument pdf = new PdfDocument(new PdfWriter(out));
        Document doc = new Document(pdf, new PageSize(PAGE_WIDTH, PAGE_HEIGHT));
        doc.setMargins(PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN, PAGE_MARGIN);
        renderPrescription(doc, createChineseFont(), salesRecord, customer, opto);
        doc.close();

        return new PrintResult("application/pdf",
                "inline; filename=prescription_" + recordId + ".pdf", out.toByteArray());
    }

    /** 处方单版面：13.8cm × 21.5cm 的纸横向打，验光表与配镜表上下紧贴、不留分节标题 */
    private void renderPrescription(Document doc, PdfFont chineseFont, SalesRecord salesRecord,
                                    Customer customer, OptometryRecord opto) {
        // 标题
        doc.add(bold(new Paragraph("配 镜 处 方 单").setFont(chineseFont).setFontSize(16))
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
        doc.add(new Paragraph("Glasses Prescription").setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(8));

        // 基础信息：不打印单号，日期取打印时刻
        doc.add(new Paragraph("日期: " + DateUtil.formatDateTime(new Date()))
                .setFont(chineseFont).setFontSize(8.5f).setMarginBottom(1));
        String genderStr = customer.getGender() != null ? (customer.getGender() == 1 ? "男" : "女") : "-";
        doc.add(new Paragraph("顾客: " + nvl(customer.getName()) + "    性别: " + genderStr
                + "    电话: " + nvl(customer.getPhone()))
                .setFont(chineseFont).setFontSize(8.5f).setMarginBottom(6));

        // 验光数据：表格只放球镜/柱镜/轴位/矫正视力，其余数据在表外
        if (opto != null) {
            doc.add(bold(new Paragraph("【验光数据】").setFont(chineseFont).setFontSize(10)).setMarginBottom(3));
            doc.add(new Paragraph("验光日期: " + fmtDate(opto.getExamDate())
                    + "    验光师: " + nvl(opto.getOptometristName()))
                    .setFont(chineseFont).setFontSize(8.5f).setMarginBottom(3));

            Table optoTable = new Table(UnitValue.createPercentArray(new float[] { 16, 21, 21, 21, 21 }))
                    .useAllAvailableWidth().setMarginBottom(4);

            String[] headers = { "", "球镜 SPH", "柱镜 CYL", "轴位 AXIS", "矫正视力 VA" };
            for (String h : headers) {
                optoTable.addHeaderCell(new Cell()
                        .add(bold(new Paragraph(h).setFont(chineseFont).setFontSize(8)))
                        .setTextAlignment(TextAlignment.CENTER).setPadding(2));
            }

            // 眼别列头留空，先右眼后左眼各一行
            optoTable.addCell(cell("右眼(OD)", chineseFont));
            optoTable.addCell(cellBold(fmtDiopter(opto.getOdSph()), chineseFont));
            optoTable.addCell(cellBold(fmtDiopter(opto.getOdCyl()), chineseFont));
            optoTable.addCell(cell(intText(opto.getOdAxis()), chineseFont));
            optoTable.addCell(cell(nvl(opto.getOdVa()), chineseFont));

            optoTable.addCell(cell("左眼(OS)", chineseFont));
            optoTable.addCell(cellBold(fmtDiopter(opto.getOsSph()), chineseFont));
            optoTable.addCell(cellBold(fmtDiopter(opto.getOsCyl()), chineseFont));
            optoTable.addCell(cell(intText(opto.getOsAxis()), chineseFont));
            optoTable.addCell(cell(nvl(opto.getOsVa()), chineseFont));

            doc.add(optoTable);

            // 瞳距 / 瞳高 / 瞳距汇总 / 下加光 / 备注：全部表格外展示
            doc.add(new Paragraph("瞳距: 右 " + num(opto.getOdPd()) + " / 左 " + num(opto.getOsPd())
                    + "    瞳高: 右 " + num(opto.getOdPh()) + " / 左 " + num(opto.getOsPh())
                    + "    远用: " + num(opto.getPdFar())
                    + "    近用: " + num(opto.getPdNear())
                    + "    ADD: " + fmtDiopter(opto.getAddPower()))
                    .setFont(chineseFont).setFontSize(8.5f).setMarginBottom(3));
            if (!isBlank(opto.getRemark())) {
                doc.add(new Paragraph("验光备注: " + opto.getRemark())
                        .setFont(chineseFont).setFontSize(8.5f).setMarginBottom(2));
            }
        }

        // 配镜明细：紧接验光部分，不另起分节标题
        Table salesTable = new Table(UnitValue.createPercentArray(new float[] { 10, 18, 30, 8, 12, 11, 11 }))
                .useAllAvailableWidth().setMarginTop(4).setMarginBottom(6);

        String[] sHeaders = { "项目", "品牌", "型号/参数", "数量", "零售单价", "实际售价", "小计" };
        for (String h : sHeaders) {
            salesTable.addHeaderCell(new Cell()
                    .add(bold(new Paragraph(h).setFont(chineseFont).setFontSize(8)))
                    .setTextAlignment(TextAlignment.CENTER).setPadding(2));
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

        // 合计：零售总价与实收总价同一行，实收突出显示
        if (!isBlank(salesRecord.getRemark())) {
            doc.add(new Paragraph("备注: " + salesRecord.getRemark())
                    .setFont(chineseFont).setFontSize(8.5f).setMarginBottom(2));
        }

        Table totalRow = new Table(UnitValue.createPercentArray(new float[] { 45, 55 }))
                .useAllAvailableWidth().setMarginTop(2).setBorder(Border.NO_BORDER);
        totalRow.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0)
                .add(new Paragraph("零售总价: ￥" + money(salesRecord.getTotalRetailPrice()))
                        .setFont(chineseFont).setFontSize(8.5f)));
        totalRow.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0)
                .add(bold(new Paragraph("实收总价: ￥" + money(salesRecord.getTotalAmount()))
                        .setFont(chineseFont).setFontSize(15))
                        .setTextAlignment(TextAlignment.RIGHT)));
        doc.add(totalRow);
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

    private PdfFont createChineseFont() {
        try {
            return PdfFontFactory.createFont("C:/Windows/Fonts/simsun.ttc,0", PdfEncodings.IDENTITY_H);
        } catch (Exception e) {
            try {
                return PdfFontFactory.createFont("C:/Windows/Fonts/msyh.ttc,0", PdfEncodings.IDENTITY_H);
            } catch (Exception e2) {
                try {
                    return PdfFontFactory.createFont();
                } catch (Exception e3) {
                    throw new RuntimeException(e3);
                }
            }
        }
    }

    /** 度数单元格：加粗 + 略放大，避免点阵打印机把细笔画的 + 打成 - */
    private Cell cellBold(String text, PdfFont font) {
        return new Cell().add(bold(new Paragraph(text).setFont(font).setFontSize(10)))
                .setTextAlignment(TextAlignment.CENTER).setPadding(2);
    }

    private Cell cell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontSize(9))
                .setTextAlignment(TextAlignment.CENTER).setPadding(3);
    }

    private Paragraph bold(Paragraph paragraph) {
        paragraph.setProperty(Property.BOLD_SIMULATION, true);
        return paragraph;
    }

    /** 度数固定两位小数（-1 也要打成 -1.00），正数带 + 号 */
    private String fmtDiopter(java.math.BigDecimal val) {
        if (val == null) {
            return "-";
        }
        BigDecimal scaled = val.setScale(2, RoundingMode.HALF_UP);
        return scaled.signum() > 0 ? "+" + scaled.toPlainString() : scaled.toPlainString();
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

    /** 瞳距 / 瞳高固定一位小数（63 也要打成 63.0） */
    private String num(BigDecimal v) {
        return v != null ? v.setScale(1, RoundingMode.HALF_UP).toPlainString() : "-";
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
