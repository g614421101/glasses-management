package com.glasses.controller;

import com.glasses.dto.TimelineItemDTO;
import com.glasses.entity.OptometryRecord;
import com.glasses.entity.SalesRecord;
import com.glasses.service.OptometryRecordService;
import com.glasses.service.SalesRecordService;
import com.glasses.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/archive")
public class ArchiveController {

    @Autowired
    private OptometryRecordService optometryRecordService;

    @Autowired
    private SalesRecordService salesRecordService;

    @GetMapping("/{customerId}")
    public Result<List<TimelineItemDTO>> getCustomerArchive(@PathVariable Long customerId) {
        List<TimelineItemDTO> timeline = new ArrayList<>();

        // 查询验光记录
        List<OptometryRecord> optometryRecords = optometryRecordService.listByCustomerId(customerId);
        for (OptometryRecord record : optometryRecords) {
            TimelineItemDTO item = new TimelineItemDTO();
            item.setType("OPTOMETRY");
            item.setDate(record.getExamDate());
            item.setTitle("验光记录");
            item.setSubtitle("验光师: " + (record.getOptometristName() != null ? record.getOptometristName() : "未知"));
            item.setData(record);
            timeline.add(item);
        }

        // 查询配镜记录
        List<SalesRecord> salesRecords = salesRecordService.listByCustomerId(customerId);
        for (SalesRecord record : salesRecords) {
            TimelineItemDTO item = new TimelineItemDTO();
            item.setType("SALES");
            item.setDate(record.getSalesDate());
            item.setTitle("配镜记录");
            item.setSubtitle("总计金额: " + record.getTotalAmount() + "元");
            
            // 为了前端方便，如果选了验光单也可以在这里直接查出来一起塞进去
            // 此处精简，前端可根据optometryId自行获取或传递
            item.setData(record);
            timeline.add(item);
        }

        // 统一按时间倒序排列
        timeline.sort((o1, o2) -> {
            if (o1.getDate() == null) return 1;
            if (o2.getDate() == null) return -1;
            return o2.getDate().compareTo(o1.getDate());
        });

        return Result.success(timeline);
    }
}
