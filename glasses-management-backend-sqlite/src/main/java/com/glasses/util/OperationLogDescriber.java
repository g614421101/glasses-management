package com.glasses.util;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 操作日志中文描述生成器：将 HTTP 方法 + URI + 请求参数转换为用户可读的中文描述，
 * 如 "新增顾客：张三"、"从回收站恢复顾客"、"登录系统"。
 * <p>
 * 规则按注册顺序匹配（更具体的路径先注册）；未命中规则时兜底为 "动作中文 + 模块中文"。
 * 模板中 {field} 表示从参数 JSON（arg0 等）提取对应字段，提取失败则省略该片段。
 */
@Slf4j
public final class OperationLogDescriber {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern FIELD_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    /** 规则表：key = "HTTP方法 uri前缀"（先注册的先匹配，精确命中优先于前缀命中） */
    private static final Map<String, String> RULES = new LinkedHashMap<>();

    /** 模块英文 → 中文 */
    private static final Map<String, String> MODULE_CN = new LinkedHashMap<>();

    /** 动作英文 → 中文 */
    private static final Map<String, String> ACTION_CN = new LinkedHashMap<>();

    /** 回收站 type → 中文 */
    private static final Map<String, String> TYPE_CN = new LinkedHashMap<>();

    static {
        // 顾客
        RULES.put("POST /api/customer/add", "新增顾客：{name}");
        RULES.put("PUT /api/customer/update", "修改顾客：{name}");
        RULES.put("DELETE /api/customer/", "删除顾客");
        // 验光
        RULES.put("POST /api/optometry/add", "新增验光记录");
        RULES.put("PUT /api/optometry/update", "修改验光记录");
        RULES.put("DELETE /api/optometry/", "删除验光记录");
        // 配镜
        RULES.put("POST /api/sales/add", "新增配镜记录：单号 {recordNo}");
        RULES.put("PUT /api/sales/update", "修改配镜记录：单号 {recordNo}");
        RULES.put("DELETE /api/sales/", "删除配镜记录");
        // 回收站（具体路径先注册）
        RULES.put("POST /api/recycle-bin/restore/", "从回收站恢复{type}");
        RULES.put("DELETE /api/recycle-bin/purge-expired", "清理超过 30 天的回收站数据");
        RULES.put("DELETE /api/recycle-bin/purge/", "彻底删除{type}");
        RULES.put("DELETE /api/recycle-bin/empty", "清空回收站");
        // 数据管理
        RULES.put("POST /api/data/import", "导入数据");
        RULES.put("POST /api/data/reset", "重置数据");
        // 账号管理（具体路径先注册）
        RULES.put("POST /api/sys-user/disable/", "封禁账号");
        RULES.put("POST /api/sys-user/enable/", "解除账号封禁");
        RULES.put("POST /api/sys-user/restore/", "恢复账号");
        RULES.put("DELETE /api/sys-user/purge/", "彻底删除账号");
        RULES.put("POST /api/sys-user/reset-password/", "重置账号密码");
        RULES.put("DELETE /api/sys-user/", "删除账号");
        // 操作日志
        RULES.put("POST /api/operation-log/cleanup", "清理操作日志");

        MODULE_CN.put("customer", "顾客");
        MODULE_CN.put("optometry", "验光记录");
        MODULE_CN.put("sales", "配镜记录");
        MODULE_CN.put("recycle-bin", "回收站");
        MODULE_CN.put("data", "数据");
        MODULE_CN.put("sys-user", "账号");
        MODULE_CN.put("print", "打印");

        ACTION_CN.put("ADD", "新增");
        ACTION_CN.put("UPDATE", "修改");
        ACTION_CN.put("DELETE", "删除");
        ACTION_CN.put("OTHER", "其他");

        TYPE_CN.put("customer", "顾客");
        TYPE_CN.put("optometry", "验光记录");
        TYPE_CN.put("sales", "配镜记录");
    }

    private OperationLogDescriber() {
    }

    /**
     * 生成用户可读的中文操作描述。
     *
     * @param method HTTP 方法（POST/PUT/DELETE 等）
     * @param uri    请求路径（/api/customer/add）
     * @param params 请求参数 JSON（可能为 null）
     */
    public static String describe(String method, String uri, String params) {
        if (StrUtil.isBlank(uri)) {
            return null;
        }
        String template = matchTemplate(method, uri);
        if (template != null) {
            return render(template, params);
        }
        return fallback(method, uri);
    }

    private static String matchTemplate(String method, String uri) {
        String m = method == null ? "" : method.toUpperCase();
        for (Map.Entry<String, String> entry : RULES.entrySet()) {
            String[] parts = entry.getKey().split(" ", 2);
            if (parts.length != 2 || !parts[0].equals(m)) {
                continue;
            }
            String ruleUri = parts[1];
            if (uri.equals(ruleUri) || uri.startsWith(ruleUri)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String render(String template, String params) {
        JsonNode root = parseParams(params);
        Matcher matcher = FIELD_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        while (matcher.find()) {
            sb.append(template, idx, matcher.start());
            String value = resolveField(matcher.group(1), root);
            if (StrUtil.isNotBlank(value)) {
                sb.append(value);
            }
            idx = matcher.end();
        }
        sb.append(template.substring(idx));
        // 字段提取失败时去掉末尾的冒号/空格（如 "新增顾客：" → "新增顾客"）
        String result = sb.toString().replaceAll("[：:]\\s*$", "").trim();
        return result;
    }

    private static String resolveField(String field, JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        if ("type".equals(field)) {
            // 在 arg0/arg1 等标量参数中找 recycle-bin 的类型
            for (JsonNode arg : root) {
                if (arg.isTextual() && TYPE_CN.containsKey(arg.asText())) {
                    return TYPE_CN.get(arg.asText());
                }
            }
            return null;
        }
        // 从 arg0 对象中提取字段（如 name、recordNo）
        JsonNode arg0 = root.path("arg0");
        if (arg0.isObject()) {
            JsonNode value = arg0.path(field);
            if (value.isTextual() && StrUtil.isNotBlank(value.asText())) {
                return value.asText();
            }
        }
        return null;
    }

    private static JsonNode parseParams(String params) {
        if (StrUtil.isBlank(params)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(params);
        } catch (Exception e) {
            log.debug("操作日志参数解析失败: {}", e.getMessage());
            return null;
        }
    }

    private static String fallback(String method, String uri) {
        String[] parts = uri.split("/");
        String module = parts.length >= 3 ? parts[2] : "";
        String moduleCn = MODULE_CN.getOrDefault(module, "");
        String actionCn = ACTION_CN.getOrDefault(resolveAction(method), "");
        if (StrUtil.isNotBlank(actionCn) && StrUtil.isNotBlank(moduleCn)) {
            return actionCn + moduleCn;
        }
        return "执行了操作";
    }

    private static String resolveAction(String method) {
        if (method == null) {
            return "OTHER";
        }
        switch (method.toUpperCase()) {
            case "POST":
                return "ADD";
            case "PUT":
            case "PATCH":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                return "OTHER";
        }
    }
}
