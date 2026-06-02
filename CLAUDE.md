# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

眼镜店视光档案管理系统，前后端分离架构，提供两种后端变体（MySQL / H2）、Electron 桌面版和两套 Android 客户端。全项目统一版本号 **3.1.0**（Android 独立版本 1.0.0）。UI、文档、commit 均使用中文。平台目标为 Windows。

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
glasses-management-android/      Android WebView 客户端（Kotlin + mDNS 自动发现）
glasses-management-android-native/  Android 原生 Compose 客户端（Material 3 + Hilt + Retrofit）
```

**两个后端共享相同的包名和类名**（`com.glasses`），代码结构高度一致。修改后端业务逻辑时必须同步两个变体。两后端之间主要代码级差异是 `MybatisPlusConfig.java` 中的 `DbType`（MYSQL vs H2）和 `SchemaCompatibilityInitializer.java` 中的 SQL 方言适配（大小写、数据类型、索引语法等）。

前端构建产物（`dist/`）通过 `sync-frontend.ps1` 复制到后端 `src/main/resources/static/`，由 Spring Boot 直接托管。开发时 Vite 代理 `/api` 到 `localhost:8080`。

无父 POM，两个后端各自独立继承 `spring-boot-starter-parent`（当前 3.5.14）。

## 关键约定

- **统一响应**：所有 API 返回 `Result<T>`（`code: 200` 表示成功），前端 `request.ts` 拦截器自动解包返回 `res.data`。错误码 409 在前端静默处理（用于手机号重复冲突）。
- **认证**：Sa-Token，token 存 `localStorage.token`，请求头 `Authorization`（无 Bearer 前缀），角色区分 `admin` / `merchant`。`/api/auth/login`、`/api/auth/register`、`/api/system/lan-info` 无需认证。
- **软删除**：所有业务实体使用 `deleted`、`deleted_time`、`deleted_by` 字段，不要改为物理删除。MyBatis Plus `@TableLogic` 自动过滤已删除记录，自定义 Mapper 方法（`selectAnyById` 等）可绕过逻辑删除。删除顾客时级联软删除关联的验光和销售记录。
- **路由守卫**：`router/index.ts` 中检查 token 并验证，`SysUser` 和 `RecycleBin` 仅 admin 可访问。
- **功能开关**：`src/config/features.ts` 控制导航菜单和路由注册，可选键：`CUSTOMER`、`STATISTICS`、`DATA_MANAGE`、`PROFILE`、`RECYCLE_BIN`、`SYS_USER`。
- **前端断点**：640px 为通用移动端表格卡片化断点；900px 为侧边栏收回/抽屉导航及顾客档案页分流断点；760px 为回收站移动端分流与排版断点。
- **配置链**：`application.yml` → `application-local.yml`（多个候选路径，全部 optional），本地配置不提交。
- **数据库初始化**：H2 后端通过 `schema.sql` 在启动时建表（`spring.sql.init.mode: always`）；两个后端均使用 `SchemaCompatibilityInitializer` 在启动时自动检测并添加缺失的列和索引（ALTER TABLE ADD COLUMN IF NOT EXISTS），无需 Flyway/Liquibase。
- **Android 包名**：原生 Compose 版包名为 `com.glasses.app`（`native` 是 Java 关键字，不可用作包名）。后端 mDNS 广播服务类型为 `_glasses._tcp.local.`。

## 本地配置（首次运行前）

仓库不提交真实的邀请码、管理员密码和数据库凭据。首次运行前按需创建本地配置：

```powershell
Copy-Item glasses-management-backend\application-local.example.yml glasses-management-backend\application-local.yml
Copy-Item glasses-management-backend-h2\application-local.example.yml glasses-management-backend-h2\application-local.yml
```

缺少邀请码或管理员密码会导致后端启动失败。也可改用环境变量：`APP_INVITE_CODE`、`GLASSES_ADMIN_PASSWORD`、`GLASSES_ADMIN_USERNAME`、`GLASSES_ADMIN_REAL_NAME`、`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`。

## 测试

后端使用 JUnit 5 + MockMvc 进行集成测试。H2 后端测试内嵌数据库自动隔离，MySQL 后端测试使用 `@Transactional` 自动回滚。

```powershell
cd glasses-management-backend-h2 && mvn test                # 运行全部测试
cd glasses-management-backend-h2 && mvn test -Dtest=SystemIntegrationTest  # 运行单个测试类
cd glasses-management-backend-h2 && mvn test -Dtest=SystemIntegrationTest#testMethod  # 运行单个测试方法
```

前端无测试框架，验证以 `npm run build`（vue-tsc 类型检查 + vite 构建）为准。

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
- H2 后端 `application.yml` 默认 JDBC URL 使用相对路径 `./data/glasses_management`；生产配置（`prod` profile）写到 `${user.home}/.glasses_management/data/`。
- 不提交 `application-local.yml`、构建产物、数据库文件（`*.mv.db`）、日志和本地 IDE 配置。
- Jackson 时区配置为 `Asia/Shanghai`。
- 后端原生安装包（jpackage）需要 JDK 21 + WiX Toolset。
