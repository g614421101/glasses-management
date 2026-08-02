package com.glasses.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.glasses.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {

    /**
     * 记录一条操作日志。内部自行 try-catch，写入失败不影响业务；
     * 含密码的敏感接口（登录/注册/改密）不记录参数内容。
     */
    void recordLog(String action, String method, String uri, String params,
                   Integer status, String message, Long costMs, String ip);

    /**
     * 分页查询操作日志。admin 可见全部记录，其余角色仅可见自己的记录。
     */
    Page<OperationLog> pageQuery(String operatorName, String action,
                                 String startTime, String endTime,
                                 Integer current, Integer size);

    /**
     * 清理超过指定天数的操作日志（物理删除）。
     *
     * @param days 保留天数，小于等于 0 时不执行
     * @return 清理的记录条数
     */
    int cleanupBefore(int days);

    /**
     * 清理最近指定天数内的操作日志（物理删除），更早的历史记录保留。
     *
     * @param days 清理范围天数，小于等于 0 时不执行
     * @return 清理的记录条数
     */
    int cleanupWithin(int days);
}
