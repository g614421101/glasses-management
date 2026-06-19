# AGENTS.md

## 项目概览

这是一个眼镜店视光档案管理系统，用于维护顾客资料、验光记录、配镜销售记录、用户账号、回收站和统计数据。仓库采用前后端分离开发，并提供单机桌面版打包路径。

## 目录结构

- `glasses-management-backend`：Spring Boot 后端，使用 MySQL，适合连接外部数据库部署。
- `glasses-management-backend-h2`：Spring Boot 后端，使用 H2 文件数据库，适合本地单机、Electron 桌面版和安装包交付。
- `glasses-management-frontend-vue`：Vue 3 前端，Vite 构建。
- `glasses-management-frontend-react`：React 18 前端（Ant Design + Redux Toolkit），Vite 构建。与 Vue 前端功能对齐，共享同一套后端 API。
- `glasses-management-electron`：Electron 桌面壳，启动内置 Java 后端并加载本地 Web 应用。
- `glasses-management-android`：Android WebView 版（方案 A），自动通过 mDNS 发现局域网后端。
- `glasses-management-android-native`：Android 原生 Compose 版（方案 B），直接调用后端 API。
- `build-desktop.ps1`：一键构建 H2 桌面版安装包。
- `sync-frontend.ps1`：构建前端并同步 `dist` 到目标后端的静态资源目录。
- `PACKAGING_TUTORIAL.md`：打包流程说明。
- `CLAUDE.md`：Claude Code 开发指引。

## 技术栈

### 后端

- Java 21
- Spring Boot 3.5.14
- MyBatis Plus 3.5.16
- Sa-Token 1.45.0
- Lombok
- Hutool 5.8.44
- iText 9.6.0，用于 PDF 导出
- EasyExcel 4.0.3，用于 Excel 导出
- MySQL 版依赖 `mysql-connector-j`
- H2 版依赖 `com.h2database:h2`，并额外包含 Spring Boot Actuator

### 前端

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios
- `vue3-print-nb`

### React 前端

- React 18
- TypeScript
- Vite
- React Router v6（HashRouter，`@` 路径别名）
- Redux Toolkit + React Redux
- Ant Design 5 + @ant-design/icons
- Axios
- qrcode

### 桌面端

- Electron 30
- electron-builder
- 打包时会把 H2 后端 JAR、`application-local.yml` 和 jlink 生成的 Java runtime 放进 Electron 资源目录。

### Android（WebView 版）

- Kotlin + WebView
- jmdns 3.5.12（mDNS 服务发现）
- 自动发现后端服务，缓存连接地址，IP 变化时自动重连

### Android（原生版）

- Kotlin + Jetpack Compose + Material 3
- Hilt 依赖注入
- Retrofit + OkHttp 网络层
- Navigation Compose 导航
- DataStore Token 持久化
- 包名：`com.glasses.app`（注意：`native` 是 Java 关键字，不能用作包名）

## 本地启动

### MySQL 后端

```powershell
cd glasses-management-backend
mvn spring-boot:run
```

### H2 后端

```powershell
cd glasses-management-backend-h2
mvn spring-boot:run
```

### 前端

```powershell
cd glasses-management-frontend-vue
npm install
npm run dev
```

前端开发服务器通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`。

### React 前端

```powershell
cd glasses-management-frontend-react
npm install
npm run dev
```

React 前端开发服务器同样通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`，默认端口 3000。

## 构建与打包

### 只构建前端

```powershell
cd glasses-management-frontend-vue
npm run build
```

### 同步前端到后端静态资源

```powershell
.\sync-frontend.ps1
```

可选参数：

- `-Backend H2`：只同步到 H2 后端。
- `-Backend MySQL`：只同步到 MySQL 后端。
- `-Frontend React`：同步 React 前端（默认 Vue）。
- `-SkipBuild`：跳过前端构建，直接使用已有 `glasses-management-frontend-vue/dist`。

### 构建桌面版

```powershell
.\build-desktop.ps1
```

该脚本默认走 `frontend + backend-h2 + electron` 路线，要求 Windows、Node.js、JDK 21、Maven，并用 `jlink` 准备内置 Java runtime。Electron 安装包输出到：

```text
glasses-management-electron\dist
```

### 后端原生安装包

```powershell
cd glasses-management-backend-h2
.\build-package.ps1
```

```powershell
cd glasses-management-backend
.\build-package.ps1
```

## 配置与密钥

不要提交真实的本地配置和密钥。首次运行前按需复制示例配置：

```powershell
Copy-Item glasses-management-backend\application-local.example.yml glasses-management-backend\application-local.yml
Copy-Item glasses-management-backend-h2\application-local.example.yml glasses-management-backend-h2\application-local.yml
```

常用环境变量：

- `APP_INVITE_CODE`
- `GLASSES_ADMIN_USERNAME`
- `GLASSES_ADMIN_PASSWORD`
- `GLASSES_ADMIN_REAL_NAME`
- `GLASSES_ADMIN_ENABLED`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`

两个后端默认端口都是 `8080`。Jackson 时区配置为 `Asia/Shanghai`。

## 测试

后端使用 JUnit 5 + MockMvc 进行集成测试。H2 后端测试内嵌数据库自动隔离，MySQL 后端测试使用 `@Transactional` 自动回滚。

```powershell
cd glasses-management-backend-h2 && mvn test                # 运行全部测试
cd glasses-management-backend-h2 && mvn test -Dtest=SystemIntegrationTest  # 运行单个测试类
cd glasses-management-backend-h2 && mvn test -Dtest=SystemIntegrationTest#testMethod  # 运行单个测试方法
```

前端无测试框架，验证以 `npm run build`（vue-tsc 类型检查 + vite 构建）为准。

React 前端同样无测试框架，验证以 `npm run build`（tsc 类型检查 + vite 构建）为准。

## 日志与数据存储

### 数据库位置
- H2 开发默认数据库路径为 `./data/glasses_management`。
- 生产配置（原生 `jpackage` 打包激活 `prod` profile 时）默认写到用户目录下的 `.glasses_management/data/glasses_management`。
- **Electron 桌面版专属说明**：Electron 启动后台时并未显式激活 `prod` profile，会回退使用 `dev` 的相对路径 `./data/glasses_management`。由于 Electron 指定了工作目录（CWD）为 `userData`（通常是 `%APPDATA%\<应用名>`），因此 Electron 版的实际 H2 数据库存储在 `%APPDATA%\<应用名>\data\glasses_management.mv.db`。

### 日志系统

#### 后端日志配置（logback-spring.xml）

两个后端均使用 `logback-spring.xml` 进行日志配置（位于 `src/main/resources/`），按 Spring Profile 区分环境：

- **开发环境 (`dev`)**：彩色控制台输出，`DEBUG` 级别，框架包（Spring、MyBatis、Sa-Token）降为 `INFO` 减少噪音。
- **生产环境 (`prod`)**：滚动文件输出到 `${LOG_PATH}/app.log`，`INFO` 级别，按天轮转，单文件最大 50MB，保留 30 天，总上限 1GB。`LOG_PATH` 默认为 `${app.home}/logs`（Electron 通过 `-Dapp.home` 传入 `userData` 路径）。

#### HTTP 请求日志（RequestLoggingFilter）

两个后端均注册了 `RequestLoggingFilter`（`@Component` + `@Order(1)`），自动记录每个 `/api/**` 请求：

```
POST /api/customer/add | user=admin(1) | 200 | 15ms
POST /api/auth/login | user=anonymous | 401 | 3ms
```

4xx/5xx 使用 `WARN` 级别，其余使用 `INFO`。静态资源和非 API 请求自动排除。

#### 业务操作日志

以下 Controller / Service 使用 `@Slf4j` 记录关键业务操作：

- **AuthController**：登录成功/失败（含原因）、注册、修改密码、修改个人资料。
- **CustomerServiceImpl**：软删除顾客。
- **OptometryRecordServiceImpl**：软删除验光记录。
- **SalesRecordServiceImpl**：软删除配镜记录。
- **SysUserController**：封禁、解封、删除（软）、恢复、彻底删除（`WARN`）、重置密码。
- **RecycleBinController**：恢复、彻底删除（`WARN`）、清空回收站（`WARN`）。
- **DataController**：导出、导入成功/失败、数据重置（`WARN`）。
- **RecycleCleanupScheduler**：定时清理结果和异常。

#### 异常日志（GlobalExceptionHandler）

`GlobalExceptionHandler` 使用 `@Slf4j`：
- `RuntimeException`：`log.warn(“业务异常: {}”, e.getMessage())`
- `Exception`：`log.error(“未处理异常: {}”, e.getMessage(), e)` 输出完整堆栈

#### MyBatis SQL 日志

- `application.yml`（dev）：`log-impl: StdOutImpl`，SQL 输出到控制台。
- `application-prod.yml`（prod）：`log-impl: NoLoggingImpl`，关闭 SQL 日志避免文件膨胀。

#### 日志存放路径

| 场景 | 路径 |
|------|------|
| MySQL 版原生后端 (prod) | `${user.home}/.glasses_management_mysql/logs/app.log` |
| H2 版原生安装包 (prod) | `${user.home}/.glasses_management/logs/app.log` |
| Electron 桌面版 (后端日志) | `%APPDATA%\<应用名>\logs\app.log` |
| Electron 桌面版 (Electron 层) | `%APPDATA%\<应用名>\logs\backend-{YYYYMMDD-HHmmss}.log` |

#### Electron 层日志（main.js）

Electron 主进程维护独立的日志系统：
- 每次启动生成一个带时间戳的日志文件，同时捕获 Java 子进程的 stdout/stderr。
- 自动清理：保留最近 30 个日志文件，超出的自动删除。
- 提供 `logInfo`、`logError`、`logDebug`（仅开发模式）三个级别。
- 通过 IPC 暴露 `get-log-path` 和 `open-log-folder` 供渲染进程使用。

#### 避坑指南（Electron 乱码问题）

Windows 平台上，即便给 Java 启动参数指定了 `-Dfile.encoding=UTF-8`，控制台标准输出（System.out）仍可能使用操作系统的本地编码（GBK），导致 Node.js 捕获时按 UTF-8 解码产生乱码。解决办法是在 Electron 启动 Java 的参数中补充 `-Dstdout.encoding=UTF-8` 和 `-Dstderr.encoding=UTF-8`。

## 主要业务模型

- `sys_user`：系统用户，包含用户名、手机号、密码、真实姓名、角色、是否禁用、是否删除、是否必须改密等字段。
- `customer`：顾客，包含姓名、手机号、性别、生日、备注、软删除信息。
- `optometry_record`：验光记录，包含左右眼球镜、柱镜、轴位、视力、瞳距、下加光、验光师、检查日期等字段。
- `sales_record`：销售记录，包含单号、顾客、关联验光记录、镜架、镜片、金额、销售时间、操作人和软删除信息。

## 主要 API 分组

后端控制器统一在 `/api` 下：

- `/api/auth`：登录、注册、当前用户信息、修改密码、更新个人资料。
- `/api/customer`：顾客分页、创建、更新、删除、详情。
- `/api/archive`：顾客档案时间线/汇总。
- `/api/optometry`：验光记录创建、按顾客查询、更新、删除。
- `/api/sales`：销售记录创建、按顾客查询、更新、删除、统计。
- `/api/print`：处方打印、顾客档案导出、营收导出。
- `/api/recycle-bin`：回收站查询、恢复、彻底删除、清理过期数据。
- `/api/data`：数据导出、导入（合并/替换）、重置。
- `/api/sys-user`：系统用户列表、启用/禁用、删除/恢复/彻底删除、重置密码。
- `/api/system/lan-info`：返回局域网 IP 和端口（无需认证），用于 QR 码和连接验证。

## 前端结构

- `src/main.ts`：前端入口。
- `src/App.vue`：根组件。
- `src/router/index.ts`：路由和登录守卫。
- `src/layout/BasicLayout.vue`：登录后的基础布局。
- `src/components/MobileCardList.vue`：移动端通用卡片列表展示组件。
- `src/store/auth.ts`：认证状态。
- `src/utils/request.ts`：Axios 实例，`baseURL` 为 `/api`，从 `localStorage` 读取 `token` 写入 `Authorization` 请求头。
- `src/utils/theme.ts`：主题相关工具。
- `src/config/features.ts`：功能开关配置，控制导航菜单和路由是否注册（`CUSTOMER`、`STATISTICS`、`DATA_MANAGE`、`PROFILE`、`RECYCLE_BIN`、`SYS_USER`）。
- `src/views/Login.vue`：登录页。
- `src/views/Register.vue`：注册页。
- `src/views/Home.vue`：首页。
- `src/views/Customer.vue`：顾客管理。
- `src/views/Archive.vue`：顾客档案。
- `src/views/SysUser.vue`：用户管理。
- `src/views/Profile.vue`：个人资料。
- `src/views/RecycleBin.vue`：回收站。
- `src/views/Statistics.vue`：统计页。
- `src/views/DataManage.vue`：数据管理（导入/导出/重置）。

## React 前端结构

- `src/main.tsx`：前端入口。
- `src/App.tsx`：根组件，包含 HashRouter 和顶级路由定义。
- `src/layout/BasicLayout.tsx`：登录后的基础布局，包含子路由定义和 `displayLocation` 过渡动画状态机。
- `src/components/PrivateRoute.tsx`：路由守卫组件。
- `src/store/index.ts`：Redux store。
- `src/features/auth/authSlice.ts`：认证状态。
- `src/utils/request.ts`：Axios 实例，`baseURL` 为 `/api`，从 `localStorage` 读取 `token` 写入 `Authorization` 请求头。
- `src/utils/theme.ts`：主题相关工具。
- `src/hooks/useTheme.ts`：主题切换 hook。
- `src/hooks/useMobile.ts`：移动端检测 hook。
- `src/hooks/useAuth.ts`：认证 hook。
- `src/config/features.ts`：功能开关配置，控制导航菜单和路由是否注册（`CUSTOMER`、`STATISTICS`、`DATA_MANAGE`、`PROFILE`、`RECYCLE_BIN`、`SYS_USER`）。
- `src/pages/Login.tsx`：登录页。
- `src/pages/Register.tsx`：注册页。
- `src/pages/Home.tsx`：首页。
- `src/pages/Customer.tsx`：顾客管理。
- `src/pages/Archive.tsx`：顾客档案（含子组件 DetailModal、TimelineCard、OptometryFormModal、SalesFormModal）。
- `src/pages/SysUser.tsx`：用户管理。
- `src/pages/Profile.tsx`：个人资料。
- `src/pages/RecycleBin.tsx`：回收站。
- `src/pages/Statistics.tsx`：统计页。
- `src/pages/DataManage.tsx`：数据管理（导入/导出/重置，含子组件 DataCards）。
- `src/styles/global.css`：全局样式（路由过渡、按钮、表格、页面布局）。
- `src/styles/variables.css`：CSS 变量（浅色/暗色主题）。
- `src/styles/theme-overrides.css`：Ant Design 主题覆盖。

## 后端结构

两个后端模块的包名都是 `com.glasses`，大部分代码结构一致：

- `GlassesApplication.java`：Spring Boot 入口。
- `controller`：REST API 控制器。
- `service` / `service.impl`：业务逻辑。
- `mapper`：MyBatis Plus Mapper。
- `entity`：数据库实体。
- `dto`：请求/响应 DTO。
- `config`：Web 配置、Sa-Token 权限、初始化器、异常处理、定时清理、浏览器自动启动、HTTP 请求日志过滤器等。
- `util/Result.java`：统一响应结构。
- `constant/RoleConstants.java`：角色常量。

## 重要实现约定

- 业务删除多为软删除，表中有 `deleted`、`deleted_time`、`deleted_by` 字段；不要随意改成物理删除。MyBatis Plus `@TableLogic` 自动过滤已删除记录，自定义 Mapper 方法（`selectAnyById` 等）可绕过逻辑删除。删除顾客时级联软删除关联的验光和销售记录。
- 两个后端模块高度相似，主要代码级差异是 `MybatisPlusConfig.java` 中的 `DbType`（MYSQL vs H2）和 `SchemaCompatibilityInitializer.java` 中的 SQL 方言适配（大小写、数据类型、索引语法等）。改动后端业务时，要判断是否需要同步修改 MySQL 版和 H2 版，避免桌面版与服务端版行为不一致。
- 前端请求期望后端返回 `Result` 包装结构，成功码为 `200`，`request.ts` 会直接返回 `res.data`。错误码 409 在前端静默处理（用于手机号重复冲突）。
- 登录 token 存在 `localStorage` 的 `token`，请求头名为 `Authorization`（无 Bearer 前缀）。`/api/auth/login`、`/api/auth/register`、`/api/system/lan-info` 无需认证。
- 角色主要区分 `admin` 和 `merchant`。
- 前端页面和后端接口路径已经耦合，改接口时需要同步检查对应 Vue 页面。
- 打包桌面版主要依赖 H2 后端，不要只改 MySQL 后端后就认为桌面版已更新。
- 版本号位置不同：Electron 安装包版本在 `glasses-management-electron/package.json`，前端版本在 `glasses-management-frontend-vue/package.json`，后端版本在各自的 `pom.xml`。后端原生安装包版本另在 `jpackage.cfg`。当前全项目统一版本为 **3.1.0**（Android 独立版本 1.0.0）。
- **React 前端路由过渡**：使用 `displayLocation` 状态机（非 `react-transition-group`）实现页面切换动画。`BasicLayout.tsx` 中 `displayLocation` 滞后于真实 `location`，退出动画期间冻结旧页面内容，退出完成后才切换 `displayLocation` 并播进入动画。内部用 `<Routes location={displayLocation}>` 替代 `<Outlet />`。修改路由过渡逻辑时需理解此机制，避免引入闪屏。路由定义在 `BasicLayout.tsx` 内部（非 `App.tsx`）。
- **React 前端按钮样式**：`global.css` 中 danger 按钮（`.ant-btn-dangerous`）使用红色渐变实底 + 白字（对齐 Vue Element Plus）。`theme-overrides.css` 中不要添加只设 color/border 不设 background 的 danger 覆盖规则，否则会覆盖实底色。

## 验证建议

根据改动范围选择最小验证：

- 前端改动：在 `glasses-management-frontend-vue` 运行 `npm run build`。
- React 前端改动：在 `glasses-management-frontend-react` 运行 `npm run build`。
- 后端改动：在对应后端模块运行 `mvn test` 或至少 `mvn clean package -DskipTests`。
- 涉及桌面版交付：在根目录运行 `.\build-desktop.ps1`。
- 涉及静态资源交付：运行 `.\sync-frontend.ps1`（或 `.\sync-frontend.ps1 -Frontend React`）后再构建后端或桌面版。

## 数据库架构演进

- H2 后端通过 `schema.sql` 在启动时建表（`spring.sql.init.mode: always`）。
- 两个后端均使用 `SchemaCompatibilityInitializer`（`ApplicationRunner`，`@Order(0)`）在启动时自动检测并添加缺失的列和索引（ALTER TABLE ADD COLUMN IF NOT EXISTS），无需 Flyway/Liquibase。
- 新增字段时，在 `schema.sql`（H2）中添加列定义，并在 `SchemaCompatibilityInitializer` 中添加对应的 ALTER TABLE 语句以兼容已有数据库。

## 代码修改原则

- 先确认需求边界，再做最小改动。
- 不做与任务无关的重构、格式化或清理。
- 修改现有代码时优先匹配当前风格。
- 新增后端接口时同步考虑权限、统一返回结构、软删除规则和两个后端模块的一致性。
- 新增前端页面或功能时优先使用已有的 Element Plus、路由、Pinia 和 Axios 封装。
- 新增 React 前端页面或功能时优先使用已有的 Ant Design、路由、Redux Toolkit 和 Axios 封装。
- 不提交 `application-local.yml`、构建产物、数据库文件、日志和本地 IDE 配置。

## Android 项目注意事项

- Android 原生项目包名是 `com.glasses.app`，不是 `com.glasses.native`（`native` 是 Java 关键字）。
- 后端 mDNS 广播服务类型为 `_glasses._tcp.local.`，Android 端和后端使用相同的 jmdns 库。
- 后端同时向 MySQL 版和 H2 版添加了 `MdnsAdvertiser.java` 和 `MdnsProperties.java`，改动时需同步。
- Android 项目的 `build.gradle.kts` 需要 `buildFeatures { buildConfig = true }` 才能使用 `BuildConfig.DEBUG`。
- Kotlin 文件中的中文字符串如果被 `git filter-branch` 等工具损坏，会出现 `�?` 乱码导致编译报错。遇到 `Expecting '"'` 或 `Unresolved reference` 等莫名错误时，先检查文件编码。
- `.gradle/`、`build/`、`local.properties` 已在 `.gitignore` 中排除，不要手动提交。
