# AGENTS.md

眼镜店视光档案管理系统。前后端分离 + Electron 桌面版 + 两套 Android 客户端。目标平台 Windows，UI/文档/commit 使用中文。全项目统一版本 **3.3.1**（Android `versionName`/`versionCode` 独立维护）。

打包流程见 `PACKAGING_TUTORIAL.md`。

## 模块边界与同步规则

- `glasses-management-backend`：Spring Boot + MySQL，服务端部署形态。
- `glasses-management-backend-h2`：Spring Boot + H2 文件库，**桌面版 / 原生安装包 / 单机场景只依赖它**。
- `glasses-management-frontend-vue`：Vue 3 + Element Plus + Pinia。
- `glasses-management-frontend-react`：React 18 + Ant Design 5 + Redux Toolkit，与 Vue 端共享同一套后端 API。
- `glasses-management-electron`：Electron 30 壳，启动内置 H2 后端 JAR + 本地 Web 静态资源。
- `glasses-management-android`：Android WebView 客户端（mDNS 自动发现后端）。
- `glasses-management-android-native`：Android 原生 Compose 客户端，包名 `com.glasses.app`（`native` 是 Java 关键字，不能作为包名）。

**关键**：两个后端模块共享包名 `com.glasses`，代码结构高度一致，**改后端业务时通常需要同步修改两个模块**。真实差异只有 `MybatisPlusConfig.java` 中的 `DbType`（MYSQL vs H2）和 `SchemaCompatibilityInitializer.java` 中的 SQL 方言适配。无父 POM，两个后端各自独立继承 `spring-boot-starter-parent` 3.5.14。

## 开发 / 验证 / 打包命令

```powershell
# 开发启动
cd glasses-management-backend-h2 && mvn spring-boot:run              # H2 后端（默认 http://localhost:8080）
cd glasses-management-backend && mvn spring-boot:run                  # MySQL 后端（需先配置数据源）
cd glasses-management-frontend-vue && npm install && npm run dev     # Vue 前端
cd glasses-management-frontend-react && npm install && npm run dev   # React 前端（端口 3000）

# 验证
npm run build                                                         # 前端类型检查 + 构建（无单元测试框架）
cd glasses-management-backend-h2 && mvn test                          # H2 后端集成测试
cd glasses-management-backend-h2 && mvn test -Dtest=SystemIntegrationTest#testMethod
cd glasses-management-backend-h2 && mvn test -Dtest=SchemaCompatibilityTest   # schema 自动升级链路（快速、无需数据库）

# 改前端后必须同步到后端静态资源，否则 jar / 安装包里是旧页面
.\sync-frontend.ps1                                                   # 交互式选择前端，同步到两个后端
.\sync-frontend.ps1 -Backend H2 -Frontend React -SkipBuild           # 跳过构建，直接复制已有 dist

# 打包
.\build-desktop.ps1                                                   # H2 + Electron 一键桌面版，产物 glasses-management-electron\dist\视光管理系统_3.3.1.exe
cd glasses-management-backend-h2 && .\build-package.ps1              # jpackage 原生安装包（需 JDK 21 + WiX）
cd glasses-management-backend && .\build-package.ps1                 # 同上，MySQL 版
```

## 本地配置（首次运行前必须）

两个后端启动前必须提供邀请码（MySQL 版还必须配置数据源），否则启动失败：

```powershell
Copy-Item glasses-management-backend\application-local.example.yml glasses-management-backend\application-local.yml
Copy-Item glasses-management-backend-h2\application-local.example.yml glasses-management-backend-h2\application-local.yml
```

**管理员统一预设策略**（两个后端一致，`DataInitializer` 实现，有集成测试覆盖）：

- 配置 `glasses.admin.password`（明文）或 `glasses.admin.password-hash`（BCrypt 哈希）**二选一**，两者同时设置启动失败；首次启动时自动创建 admin，此后任何启动都不覆盖密码。
- 两者都留空时不自动创建：登录页自动切换为"系统初始化"表单（`/api/system/setup-status` + `/api/system/setup`，需邀请码），现场创建管理员。
- 每次启动幂等自愈 admin 的 `role`/`real_name`/`deleted`/`disabled`。
- 忘记密码：`application-local.yml` 临时置 `force-reset-password: true` 重启，admin 密码被重置为随机强密码（仅打印一次到日志，`force_reset_time` 记录防重复执行）。
- 登录防爆破：同一账号连续失败 5 次锁定 15 分钟（内存计数，重启失效）。

也可使用环境变量：`APP_INVITE_CODE`、`GLASSES_ADMIN_USERNAME`、`GLASSES_ADMIN_PASSWORD`、`GLASSES_ADMIN_PASSWORD_HASH`、`GLASSES_ADMIN_FORCE_RESET`、`GLASSES_ADMIN_REAL_NAME`、`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`SPRING_PROFILES_ACTIVE`、`SERVER_PORT`。

- MySQL 后端数据源**没有默认值**，必须配置。
- H2 后端默认 `./data/glasses_management`（用户 `sa`，空密码）。

## 数据库 schema 演进

- **H2**：`src/main/resources/sql/schema.sql` 每次启动自动执行（`spring.sql.init.mode: always`）。
- **MySQL**：`sql/schema.sql`（模块根目录）仅作参考，需手动应用到数据库。
- 两个后端都用 `SchemaCompatibilityInitializer`（`@Order(0)` 的 `ApplicationRunner`）在启动时自动 `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` 补齐缺失列和索引，**不需要 Flyway/Liquibase**。
- 表结构变更规范（桌面版/MySQL 安装新版本后启动时自动升级）：
  - **新增表**：在 H2 `schema.sql` 添加建表语句（`CREATE TABLE IF NOT EXISTS`，每次启动自动执行）；在 **MySQL 版** `SchemaCompatibilityInitializer` 添加幂等建表 `CREATE TABLE IF NOT EXISTS` + `addColumnIfMissing`（必须先建表再补列，否则老库 ALTER 不存在的表会启动失败）。H2 版无需建表逻辑（schema.sql 已建）。
  - **已有表新增列/索引/数据迁移**：在两个 `SchemaCompatibilityInitializer` 中添加对应方言的 `addColumnIfMissing` / `addIndexIfMissing` / UPDATE 语句。
  - **限制**：自动升级只支持"加表、加列、加索引、数据迁移"；**不支持列类型变更、删列、重命名**（`addColumnIfMissing` 检测到列已存在会跳过），此类变更需手动执行 SQL 或扩展 `ALTER COLUMN` 机制。
- 自动升级链路有集成测试覆盖：`SchemaCompatibilityTest`（模拟老库缺列/缺索引 → 调用 `run()` 断言补齐）。

## 前后端接口与认证

- 统一响应 `Result<T>`，成功 `code = 200`；前端 `request.ts` 直接返回 `res.data`。
- 认证：Sa-Token，token 存 `localStorage.token`，请求头 `Authorization`（**无 Bearer 前缀**）。
- 免认证接口：`/api/auth/login`、`/api/auth/register`、`/api/system/lan-info`、`/api/system/setup-status`、`/api/system/setup`。
- 角色：`admin`、`merchant`；`SysUser`、`RecycleBin`、`Data`（数据导入/导出/重置）仅 admin 可访问。
- 后端控制器统一在 `/api` 前缀下：`/api/auth`、`/api/customer`、`/api/optometry`、`/api/sales`、`/api/archive`、`/api/print`、`/api/recycle-bin`、`/api/data`、`/api/sys-user`、`/api/operation-log`、`/api/system/lan-info`。
- Jackson 时区配置为 `Asia/Shanghai`。

## 关键实现约定

- **软删除**：业务表有 `deleted`、`deleted_time`、`deleted_by`，MyBatis Plus `@TableLogic` 自动过滤；自定义 Mapper 方法（`selectAnyById` 等）可绕过逻辑删除。删除顾客时级联软删除关联验光和销售记录，**不要改成物理删除**。
- **功能开关**：`src/config/features.ts` 控制导航菜单和路由注册（`CUSTOMER`、`STATISTICS`、`DATA_MANAGE`、`PROFILE`、`RECYCLE_BIN`、`SYS_USER`、`OPERATION_LOG`），两个前端都有。
- **React 路由过渡**：`BasicLayout.tsx` 用 `displayLocation` 状态机实现页面切换动画，`<Routes location={displayLocation}>` 替代 `<Outlet />`；路由定义在 `BasicLayout.tsx` 内部，不在 `App.tsx`。
- **React danger 按钮**：`global.css` 中 `.ant-btn-dangerous` 使用红色渐变实底 + 白字；`theme-overrides.css` 中不要添加只设 color/border 不设 background 的规则，否则会覆盖实底色。
- **前端断点**：640px 移动端表格卡片化；900px 侧边栏收回/抽屉导航；760px 回收站移动端分流。

## Android 与 mDNS

- 后端 mDNS 广播服务类型：`_glasses._tcp.local.`（`MdnsAdvertiser.java`，两个后端各一份，需同步修改）。
- Android WebView 版与原生版都用 jmdns 自动发现局域网后端。
- 原生版 `build.gradle.kts` 需要 `buildFeatures { buildConfig = true }` 才能使用 `BuildConfig.DEBUG`。

## 日志与数据位置

| 场景 | 路径 |
|------|------|
| H2 开发数据库 | `./data/glasses_management.mv.db`（相对工作目录） |
| H2 原生安装包（prod） | `${user.home}/.glasses_management/data/glasses_management.mv.db` |
| Electron 桌面版数据库 | `%APPDATA%\<应用名>\data\glasses_management.mv.db`（Electron 把 CWD 设为 `userData`） |
| MySQL/H2 原生后端日志（prod） | `${user.home}/.glasses_management[_mysql]/logs/app.log` |
| Electron 后端日志 | `%APPDATA%\<应用名>\logs/app.log` |
| Electron 主进程日志 | `%APPDATA%\<应用名>\logs/backend-{YYYYMMDD-HHmmss}.log`（保留最近 30 个） |

**Electron 乱码坑**：Windows 上即使给 Java 加 `-Dfile.encoding=UTF-8`，`System.out` 仍可能按 GBK 输出，导致 Node.js 按 UTF-8 解码乱码。启动 Java 的参数需补充 `-Dstdout.encoding=UTF-8` 和 `-Dstderr.encoding=UTF-8`。

## 版本号位置

发版或改版本时同步更新：

- `glasses-management-frontend-vue/package.json`
- `glasses-management-frontend-react/package.json`
- `glasses-management-backend/pom.xml`
- `glasses-management-backend-h2/pom.xml`
- `glasses-management-electron/package.json`
- `glasses-management-backend/jpackage.cfg`
- `glasses-management-backend-h2/jpackage.cfg`

## Changelog

- 记录文件放在 `changelog/` 目录下，以日期命名 `YYYY-MM-DD.md`（已被 `.gitignore` 排除，本地用）。
- 每次合并功能分支或重要改动后新增一个日期文件。
- **格式规范**（统一照此书写，参照往期文件）：

```markdown
# YYYY-MM-DD — <一句话标题，概括本次改动核心>

**背景**：<为什么做这次改动，一两句话>

**改动**：

- <要点分组，以 **粗体小标题** 开头，子条目缩进两格列出具体改动>
- ...

**测试**：

- <验证方式与结果（mvn test / npm run build 及用例数）>

**涉及模块**：`module-a`、`module-b`。
```

- 四个小节（背景/改动/测试/涉及模块）均为必填；无测试的纯文案改动在"测试"下写人工验证方式。

## 禁止提交

`application-local.yml`、构建产物、数据库文件（`*.mv.db`）、日志、本地 IDE 配置、`.env` 等不应提交。已配置 `.gitignore`。
