# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

眼镜店视光档案管理系统，前后端分离架构，提供两种后端变体（MySQL / H2）和 Electron 桌面版。全项目统一版本号 **3.0.0**。UI、文档、commit 均使用中文。平台目标为 Windows。

## 常用命令

```powershell
# 开发启动
cd glasses-management-backend-h2 && mvn spring-boot:run    # H2 后端（本地单机）
cd glasses-management-backend && mvn spring-boot:run        # MySQL 后端
cd glasses-management-frontend && npm install && npm run dev # 前端（Vite 代理 /api → localhost:8080）

# 验证
cd glasses-management-frontend && npm run build             # vue-tsc + vite build
cd glasses-management-backend-h2 && mvn clean package -DskipTests  # 后端打包
cd glasses-management-backend-h2 && mvn test                # 后端测试

# 前端同步到后端静态资源（修改前端后必须执行）
.\sync-frontend.ps1                    # 同步到两个后端
.\sync-frontend.ps1 -Backend H2        # 只同步到 H2
.\sync-frontend.ps1 -SkipBuild         # 跳过构建，直接复制已有 dist

# 一键打包桌面版（H2 + Electron，需要 JDK 21 + Node.js）
.\build-desktop.ps1

# 后端原生安装包（jpackage，需要 JDK 21 + WiX Toolset）
cd glasses-management-backend-h2 && .\build-package.ps1
cd glasses-management-backend && .\build-package.ps1
```

## 架构

```
glasses-management-frontend/     Vue 3 + TypeScript + Element Plus + Pinia
glasses-management-backend/      Spring Boot + MyBatis Plus + Sa-Token + MySQL
glasses-management-backend-h2/   同上，H2 文件数据库（MODE=MySQL），用于桌面版
glasses-management-electron/     Electron 壳，启动 H2 后端 JAR + 内置 JRE
```

**两个后端共享相同的包名和类名**（`com.glasses`），代码结构高度一致。修改后端业务逻辑时必须同步两个变体。

前端构建产物（`dist/`）通过 `sync-frontend.ps1` 复制到后端 `src/main/resources/static/`，由 Spring Boot 直接托管。开发时 Vite 代理 `/api` 到 `localhost:8080`。

无父 POM，两个后端各自独立继承 `spring-boot-starter-parent`。

## 关键约定

- **统一响应**：所有 API 返回 `Result<T>`（`code: 200` 表示成功），前端 `request.ts` 拦截器自动解包返回 `res.data`。
- **认证**：Sa-Token，token 存 `localStorage.token`，请求头 `Authorization`（无 Bearer 前缀），角色区分 `admin` / `merchant`。
- **软删除**：所有业务实体使用 `deleted`、`deleted_time`、`deleted_by` 字段，不要改为物理删除。
- **路由守卫**：`router/index.ts` 中检查 token 并验证，`SysUser` 和 `RecycleBin` 仅 admin 可访问。
- **功能开关**：`src/config/features.ts` 控制导航菜单和路由注册。
- **前端断点**：640px 为移动端断点，表格在手机端通过 `MobileCardList` 组件切换为卡片列表。
- **配置链**：`application.yml` → `application-local.yml`（多个候选路径，全部 optional），本地配置不提交。

## 版本号位置（发版时需同步更新）

- `glasses-management-frontend/package.json`
- `glasses-management-backend/pom.xml`
- `glasses-management-backend-h2/pom.xml`
- `glasses-management-electron/package.json`
- `glasses-management-backend/jpackage.cfg`
- `glasses-management-backend-h2/jpackage.cfg`

## 注意事项

- 桌面版打包依赖 H2 后端，不要只改 MySQL 后端就认为桌面版已更新。
- Electron 启动 Java 时需 `-Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8`，否则 Windows 下日志乱码。
- Electron 桌面版未显式设置 `prod` profile，H2 数据库写入 `%APPDATA%\<应用名>\data\`。
- 不提交 `application-local.yml`、构建产物、数据库文件、日志和本地 IDE 配置。
