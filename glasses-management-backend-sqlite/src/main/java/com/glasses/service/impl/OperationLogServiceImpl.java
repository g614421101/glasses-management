package com.glasses.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.glasses.constant.RoleConstants;
import com.glasses.entity.OperationLog;
import com.glasses.mapper.OperationLogMapper;
import com.glasses.service.OperationLogService;
import com.glasses.util.OperationLogDescriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    /** 含密码的接口（前缀匹配），不记录请求参数与可能含明文凭据的结果消息 */
    private static final List<String> SENSITIVE_URIS = Arrays.asList(
            "/api/sys-user/reset-password/"
    );

    @Override
    public void recordLog(String action, String method, String uri, String params,
                          Integer status, String message, Long costMs, String ip) {
        try {
            OperationLog logEntry = new OperationLog();
            logEntry.setModule(resolveModule(uri));
            logEntry.setAction(action);
            logEntry.setMethod(method);
            logEntry.setUri(uri);
            String safeParams = isSensitiveUri(uri) ? null : params;
            logEntry.setParams(safeParams);
            logEntry.setDescription(OperationLogDescriber.describe(method, uri, safeParams));
            logEntry.setStatus(status);
            logEntry.setMessage(sanitizeMessage(message));
            logEntry.setCostMs(costMs);
            logEntry.setIp(ip);
            fillOperator(logEntry);
            mapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("操作日志写入失败: uri={}, err={}", uri, e.getMessage());
        }
    }

    @Override
    public Page<OperationLog> pageQuery(String operatorName, String action,
                                        String startTime, String endTime,
                                        Integer current, Integer size) {
        Page<OperationLog> page = Page.of(current, size);
        QueryWrapper query = QueryWrapper.create().from(OperationLog.class);
        // 数据隔离：admin 可见全部，其余角色只能看到自己的记录
        if (!isAdmin()) {
            query.where(OperationLog::getOperatorId).eq(StpUtil.getLoginIdAsLong());
        }
        if (StrUtil.isNotBlank(operatorName)) {
            query.and(OperationLog::getOperatorName).like(operatorName.trim());
        }
        if (StrUtil.isNotBlank(action)) {
            query.and(OperationLog::getAction).eq(action.trim());
        }
        Date start = parseTime(startTime);
        Date end = parseTime(endTime);
        if (start != null) {
            query.and(OperationLog::getCreateTime).ge(start);
        }
        if (end != null) {
            query.and(OperationLog::getCreateTime).le(end);
        }
        query.orderBy(OperationLog::getCreateTime).desc()
                .orderBy(OperationLog::getId).desc();
        page = mapper.paginate(page, query);
        // 存量记录没有 description 时，返回前兜底生成，保证前端始终有可读描述
        page.getRecords().forEach(r -> {
            if (StrUtil.isBlank(r.getDescription())) {
                r.setDescription(OperationLogDescriber.describe(r.getMethod(), r.getUri(), r.getParams()));
            }
        });
        return page;
    }

    @Override
    public int cleanupBefore(int days) {
        if (days <= 0) {
            return 0;
        }
        Date cutoff = DateUtil.offsetDay(DateUtil.date(), -days);
        return mapper.deleteByQuery(
                QueryWrapper.create()
                        .from(OperationLog.class)
                        .where(OperationLog::getCreateTime).lt(cutoff));
    }

    @Override
    public int cleanupWithin(int days) {
        if (days <= 0) {
            return 0;
        }
        Date cutoff = DateUtil.offsetDay(DateUtil.date(), -days);
        return mapper.deleteByQuery(
                QueryWrapper.create()
                        .from(OperationLog.class)
                        .where(OperationLog::getCreateTime).ge(cutoff));
    }

    private boolean isAdmin() {
        try {
            if (StpUtil.isLogin()) {
                return RoleConstants.ADMIN.equals(StpUtil.getSession().getString("role"));
            }
        } catch (Exception ignored) {
            // 未登录或会话已过期
        }
        return false;
    }

    /** 前缀匹配敏感接口（避免尾部斜杠、大小写变化绕过全等比较） */
    private boolean isSensitiveUri(String uri) {
        if (uri == null) {
            return false;
        }
        return SENSITIVE_URIS.stream().anyMatch(uri::startsWith);
    }

    /** 结果消息可能携带明文凭据（如"临时密码：xxx"），过滤后入库 */
    private String sanitizeMessage(String message) {
        if (message != null && message.contains("临时密码")) {
            return null;
        }
        return message;
    }

    private void fillOperator(OperationLog logEntry) {
        try {
            if (StpUtil.isLogin()) {
                logEntry.setOperatorId(StpUtil.getLoginIdAsLong());
                logEntry.setOperatorName(StpUtil.getSession().getString("username"));
            }
        } catch (Exception ignored) {
            // 未登录或会话已过期，保持匿名
        }
    }

    private String resolveModule(String uri) {
        if (StrUtil.isBlank(uri)) {
            return "unknown";
        }
        String[] parts = uri.split("/");
        if (parts.length >= 3 && StrUtil.isNotBlank(parts[2])) {
            return parts[2];
        }
        return uri;
    }

    private Date parseTime(String time) {
        if (StrUtil.isBlank(time)) {
            return null;
        }
        try {
            return DateUtil.parse(time.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
