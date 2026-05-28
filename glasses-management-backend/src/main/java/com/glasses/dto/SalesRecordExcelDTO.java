package com.glasses.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

@Data
@HeadRowHeight(20)
@HeadFontStyle(fontName = "微软雅黑", fontHeightInPoints = 11)
public class SalesRecordExcelDTO {

    @ExcelProperty("顾客姓名")
    @ColumnWidth(12)
    private String customerName;

    @ExcelProperty("手机号")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("销售单号")
    @ColumnWidth(22)
    private String recordNo;

    @ExcelProperty("配镜日期")
    @ColumnWidth(20)
    private String salesDate;

    @ExcelProperty("镜架品牌")
    @ColumnWidth(14)
    private String frameBrand;

    @ExcelProperty("镜架型号")
    @ColumnWidth(14)
    private String frameModel;

    @ExcelProperty("镜架数量")
    @ColumnWidth(10)
    private String frameQuantity;

    @ExcelProperty("镜架售价(元)")
    @ColumnWidth(14)
    private String framePrice;

    @ExcelProperty("镜片品牌")
    @ColumnWidth(14)
    private String lensBrand;

    @ExcelProperty("镜片参数")
    @ColumnWidth(18)
    private String lensParams;

    @ExcelProperty("镜片数量")
    @ColumnWidth(10)
    private String lensQuantity;

    @ExcelProperty("镜片售价(元)")
    @ColumnWidth(14)
    private String lensPrice;

    @ExcelProperty("实收合计(元)")
    @ColumnWidth(14)
    private String totalAmount;

    @ExcelProperty("右眼SPH")
    @ColumnWidth(10)
    private String odSph;

    @ExcelProperty("右眼CYL")
    @ColumnWidth(10)
    private String odCyl;

    @ExcelProperty("右眼AXIS")
    @ColumnWidth(10)
    private String odAxis;

    @ExcelProperty("右眼PD")
    @ColumnWidth(10)
    private String odPd;

    @ExcelProperty("左眼SPH")
    @ColumnWidth(10)
    private String osSph;

    @ExcelProperty("左眼CYL")
    @ColumnWidth(10)
    private String osCyl;

    @ExcelProperty("左眼AXIS")
    @ColumnWidth(10)
    private String osAxis;

    @ExcelProperty("左眼PD")
    @ColumnWidth(10)
    private String osPd;

    @ExcelProperty("总瞳距")
    @ColumnWidth(10)
    private String pdFar;

    @ExcelProperty("近用PD")
    @ColumnWidth(10)
    private String pdNear;

    @ExcelProperty("下加光ADD")
    @ColumnWidth(10)
    private String addPower;
}
