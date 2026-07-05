# AGENTS.md

眼镜店视光档案管理系统。前后端分离 + Electron 桌面版 + 两套 Android 客户端。全项目统一版本 **3.2.0**（Android 独立版本号）。UI、文档、commit 均使用中文。目标平台 Windows。

更详细的开发指引见 `CLAUDE.md`；打包流程见 `PACKAGING_TUTORIAL.md`。

## 模块边界

- `glasses-management-backend`：Spring Boot + MySQL 后端，服务端部署形态。
- `glasses-management-backend-h2`：Spring Boot + H2 文件库后端，单机 / Electron 桌面版 / 原生安装包使用。**桌面版只依赖这个**，改了 MySQL 后端不等于桌面版已更新。
- `glasses-management-frontend-vue`：Vue 3 + Element Plus + Pinia 前端。
- `glasses-management-frontend-react`：React 18 + Ant Design 5 + Redux Toolkit 前端，与 Vue 前端功能对齐，共享同一套后端 API。
- `glasses-management-electron`：Electron 30 桌面壳，启动内置 H2 后端 JAR 并加载本地 Web 静态资源。
- `glasses-management-android`：Android WebView 版（方案 A），mDNS 自动发现后端。
- `glasses-management-android-native`：Android 原生 Compose 版（方案 B），直接调用后端 API。包名 `com.glasses.app`（`native` 是 Java 关键字，不能用作包名）。

两个后端模块代码高度相似，主要差异在 `MybatisPlusConfig.java`（`DbType`）和 `SchemaCompatibilityInitializer.java`（SQL 方言适配）。**改后端业务时通常需要同步修改两个模块。**

## 常用命令

```powershell
# 开发启动
cd glasses-management-backend-h2 && mvn spring-boot:run    # H2 后端
cd glasses-management-backend && mvn spring-boot:run        # MySQL 后端（需先配置数据源）
cd glasses-management-frontend-vue && npm install && npm run dev
cd glasses-management-frontend-react && npm install && npm run dev  # 默认端口 3000

# 验证（前端无测试框架，以 build 为准）
cd glasses-management-frontend-vue && npm run build         # vue-tsc + vite build
cd glasses-management-frontend-react && npm run build       # tsc + vite build
cd glasses-management-backend-h2 && mvn test                # H2 后端测试（内嵌数据库自动隔离）
cd glasses-management-backend && mvn test                   # MySQL 后端测试（@Transactional 自动回滚）

# 单个测试
cd glasses-management-backend-h2 && mvn test -Dtest=SystemIntegrationTest
cd glasses-management-backend-h2 && mvn test -Dtest=SystemIntegrationTest#testMethod

# 前端同步到后端静态资源（改前端后必须执行，否则后端 jar 里的静态资源是旧的）
.\sync-frontend.ps1                    # 交互式选择前端 + 同步到两个后端
.\sync-frontend.ps1 -Backend H2 -Frontend React -SkipBuild  # 跳过构建直接复制已有 dist

# 一键打包桌面版（Vue/React 二选一，H2 + jlink JRE + electron-builder）
.\build-desktop.ps1
# 输出：glasses-management-electron\dist\视光管理系统_3.2.0.exe

# 后端原生安装包（jpackage，需 JDK 21 + WiX）
cd glasses-management-backend-h2 && .\build-package.ps1
cd glasses-management-backend && .\build-package.ps1
```

前端开发服务器通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`。两个后端默认端口都是 `8080`。

## 必须先配置才能启动

两个后端都需要 `application-local.yml`（已被 `.gitignore` 忽略）或环境变量提供邀请码和管理员密码，**否则启动失败**：

```powershell
Copy-Item glasses-management-backend\application-local.example.yml glasses-management-backend\application-local.yml
Copy-Item glasses-management-backend-h2\application-local.example.yml glasses-management-backend-h2\application-local.yml
```

关键环境变量：`APP_INVITE_CODE`、`GLASSES_ADMIN_USERNAME`、`GLASSES_ADMIN_PASSWORD`、`GLASSES_ADMIN_REAL_NAME`、`GLASSES_ADMIN_ENABLED`、`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`SPRING_PROFILES_ACTIVE`、`SERVER_PORT`。

**MySQL 后端的数据源 URL/用户名/密码没有默认值**，必须通过 `application-local.yml` 或环境变量提供。H2 后端有默认值（`./data/glasses_management`，用户 `sa`，空密码）。

## 数据库 schema 演进

- **H2 后端**：`src/main/resources/sql/schema.sql` 在启动时自动执行（`spring.sql.init.mode: always`）。
- **MySQL 后端**：`sql/schema.sql`（模块根目录）仅作参考，不会自动执行，需手动应用到数据库。
- 两个后端都用 `SchemaCompatibilityInitializer`（`ApplicationRunner`，`@Order(0)`）在启动时通过 `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` 自动补齐缺失列和索引，无需 Flyway/Liquibase。
- **新增字段时**：在 H2 的 `schema.sql` 添加列定义，并在两个模块的 `SchemaCompatibilityInitializer` 中添加对应 ALTER 语句（注意 SQL 方言差异）。

## 关键实现约定

- **软删除**：业务表有 `deleted`、`deleted_time`、`deleted_by` 字段，MyBatis Plus `@TableLogic` 自动过滤。自定义 Mapper 方法（`selectAnyById` 等）可绕过逻辑删除。删除顾客时级联软删除关联验光和销售记录。不要改成物理删除。
- **统一响应结构**：后端返回 `Result` 包装，成功码 `200`。前端 `request.ts` 直接返回 `res.data`。错误码 `409`（手机号重复冲突）在前端静默处理。
- **认证**：Sa-Token，token 存 `localStorage` 的 `token`，请求头 `Authorization`（无 Bearer 前缀）。`/api/auth/login`、`/api/auth/register`、`/api/system/lan-info` 无需认证。角色分 `admin` 和 `merchant`。
- **前后端接口耦合**：改接口路径需同步检查 Vue/React 页面。后端控制器统一在 `/api` 下（`/api/auth`、`/api/customer`、`/api/optometry`、`/api/sales`、`/api/archive`、`/api/print`、`/api/recycle-bin`、`/api/data`、`/api/sys-user`、`/api/system/lan-info`）。
- **功能开关**：`src/config/features.ts` 控制导航菜单和路由注册（`CUSTOMER`、`STATISTICS`、`DATA_MANAGE`、`PROFILE`、`RECYCLE_BIN`、`SYS_USER`），两个前端都有。
- **React 路由过渡**：`BasicLayout.tsx` 用 `displayLocation` 状态机（非 `react-transition-group`）实现页面切换动画，`<Routes location={displayLocation}>` 替代 `<Outlet />`。路由定义在 `BasicLayout.tsx` 内部（非 `App.tsx`）。修改路由过渡逻辑时需理解此机制，避免闪屏。
- **React danger 按钮样式**：`global.css` 中 `.ant-btn-dangerous` 使用红色渐变实底 + 白字。`theme-overrides.css` 中不要添加只设 color/border 不设 background 的 danger 覆盖规则，否则会覆盖实底色。

## mDNS（Android 与后端配对）

- 后端服务类型：`_glasses._tcp.local.`（`MdnsAdvertiser.java`，两个后端模块都有，需同步修改）。
- Android WebView 版和原生版都用 jmdns 3.5.12 自动发现局域网后端。
- 后端 `MdnsAdvertiser.java` 和 `MdnsProperties.java` 在两个后端模块各有一份，改动时同步。

## 日志与数据位置

| 场景 | 路径 |
|------|------|
| H2 开发数据库 | `./data/glasses_management.mv.db`（相对工作目录） |
| H2 原生安装包（prod） | `${user.home}/.glasses_management/data/glasses_management.mv.db` |
| Electron 桌面版数据库 | `%APPDATA%\<应用名>\data\glasses_management.mv.db`（Electron 把 CWD 设为 `userData`） |
| MySQL/H2 原生后端日志（prod） | `${user.home}/.glasses_management[_mysql]/logs/app.log` |
| Electron 后端日志 | `%APPDATA%\<应用名>\logs\app.log` |
| Electron 主进程日志 | `%APPDATA%\<应用名>\logs\backend-{YYYYMMDD-HHmmss}.log`（保留最近 30 个） |

**Electron 乱码坑**：Windows 上即使给 Java 加 `-Dfile.encoding=UTF-8`，`System.out` 仍可能用 GBK，导致 Node.js 按 UTF-8 解码乱码。Electron 启动 Java 的参数需补充 `-Dstdout.encoding=UTF-8` 和 `-Dstderr.encoding=UTF-8`。

## 版本号位置

改版本号时要同步多处：
- 前端：`glasses-management-frontend-vue/package.json`、`glasses-management-frontend-react/package.json`
- 后端：两个 `pom.xml`
- Electron：`glasses-management-electron/package.json`
- 后端原生安装包：两个 `jpackage.cfg`（`--app-version`）

## Android 注意事项

- 原生版包名 `com.glasses.app`，不是 `com.glasses.native`（`native` 是 Java 关键字）。
- 原生版 `build.gradle.kts` 需要 `buildFeatures { buildConfig = true }` 才能用 `BuildConfig.DEBUG`。
- Kotlin 文件中的中文字符串若被 `git filter-branch` 等工具损坏会出现 `�?` 乱码导致编译报错。遇到 `Expecting '"'` 或 `Unresolved reference` 等莫名错误，先检查文件编码。
- `.gradle/`、`build/`、`local.properties` 已在 `.gitignore` 中排除，不要手动提交。

## 代码修改原则

- 先确认需求边界，再做最小改动；不做与任务无关的重构、格式化或清理。
- 修改现有代码时优先匹配当前风格。
- 新增后端接口时同步考虑权限、`Result` 包装、软删除规则和两个后端模块一致性。
- 新增前端页面或功能时优先复用已有的 Element Plus / Ant Design、路由、状态管理和 Axios 封装。
- 不提交 `application-local.yml`、构建产物、数据库文件、日志和本地 IDE 配置。

## Changelog

- 记录文件放在 `changelog/` 目录下，以日期命名 `YYYY-MM-DD.md`（已被 `.gitignore` 排除，本地用）。
- 每次合并功能分支或重要改动后新增一个日期文件，一两句话描述改动内容和背景。
