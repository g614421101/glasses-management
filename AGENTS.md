# AGENTS.md

## 项目概览

这是一个眼镜店视光档案管理系统，用于维护顾客资料、验光记录、配镜销售记录、用户账号、回收站和统计数据。仓库采用前后端分离开发，并提供单机桌面版打包路径。

## 目录结构

- `glasses-management-backend`：Spring Boot 后端，使用 MySQL，适合连接外部数据库部署。
- `glasses-management-backend-h2`：Spring Boot 后端，使用 H2 文件数据库，适合本地单机、Electron 桌面版和安装包交付。
- `glasses-management-frontend`：Vue 3 前端，Vite 构建。
- `glasses-management-electron`：Electron 桌面壳，启动内置 Java 后端并加载本地 Web 应用。
- `build-desktop.ps1`：一键构建 H2 桌面版安装包。
- `sync-frontend.ps1`：构建前端并同步 `dist` 到目标后端的静态资源目录。
- `PACKAGING_TUTORIAL.md`：打包流程说明。

## 技术栈

### 后端

- Java 21
- Spring Boot 3.2.4
- MyBatis Plus 3.5.5
- Sa-Token 1.37.0
- Lombok
- Hutool
- iText 7，用于 PDF 导出
- EasyExcel，用于 Excel 导出
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

### 桌面端

- Electron 30
- electron-builder
- 打包时会把 H2 后端 JAR、`application-local.yml` 和 jlink 生成的 Java runtime 放进 Electron 资源目录。

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
cd glasses-management-frontend
npm install
npm run dev
```

前端开发服务器通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`。

## 构建与打包

### 只构建前端

```powershell
cd glasses-management-frontend
npm run build
```

### 同步前端到后端静态资源

```powershell
.\sync-frontend.ps1
```

可选参数：

- `-Backend H2`：只同步到 H2 后端。
- `-Backend MySQL`：只同步到 MySQL 后端。
- `-SkipBuild`：跳过前端构建，直接使用已有 `glasses-management-frontend/dist`。

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

H2 开发默认数据库路径为 `./data/glasses_management`，生产配置默认写到用户目录下的 `.glasses_management/data/glasses_management`。MySQL 生产日志默认写到用户目录下的 `.glasses_management_mysql/logs`。

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
- `/api/sys-user`：系统用户列表、启用/禁用、删除/恢复/彻底删除、重置密码。

## 前端结构

- `src/main.ts`：前端入口。
- `src/App.vue`：根组件。
- `src/router/index.ts`：路由和登录守卫。
- `src/layout/BasicLayout.vue`：登录后的基础布局。
- `src/store/auth.ts`：认证状态。
- `src/utils/request.ts`：Axios 实例，`baseURL` 为 `/api`，从 `localStorage` 读取 `token` 写入 `Authorization` 请求头。
- `src/utils/theme.ts`：主题相关工具。
- `src/views/Login.vue`：登录页。
- `src/views/Register.vue`：注册页。
- `src/views/Home.vue`：首页。
- `src/views/Customer.vue`：顾客管理。
- `src/views/Archive.vue`：顾客档案。
- `src/views/SysUser.vue`：用户管理。
- `src/views/Profile.vue`：个人资料。
- `src/views/RecycleBin.vue`：回收站。
- `src/views/Statistics.vue`：统计页。

## 后端结构

两个后端模块的包名都是 `com.glasses`，大部分代码结构一致：

- `GlassesApplication.java`：Spring Boot 入口。
- `controller`：REST API 控制器。
- `service` / `service.impl`：业务逻辑。
- `mapper`：MyBatis Plus Mapper。
- `entity`：数据库实体。
- `dto`：请求/响应 DTO。
- `config`：Web 配置、Sa-Token 权限、初始化器、异常处理、定时清理、浏览器自动启动等。
- `util/Result.java`：统一响应结构。
- `constant/RoleConstants.java`：角色常量。

## 重要实现约定

- 业务删除多为软删除，表中有 `deleted`、`deleted_time`、`deleted_by` 字段；不要随意改成物理删除。
- 两个后端模块高度相似。改动后端业务时，要判断是否需要同步修改 MySQL 版和 H2 版，避免桌面版与服务端版行为不一致。
- 前端请求期望后端返回 `Result` 包装结构，成功码为 `200`，`request.ts` 会直接返回 `res.data`。
- 登录 token 存在 `localStorage` 的 `token`，请求头名为 `Authorization`。
- 角色主要区分 `admin` 和 `merchant`。
- 前端页面和后端接口路径已经耦合，改接口时需要同步检查对应 Vue 页面。
- 打包桌面版主要依赖 H2 后端，不要只改 MySQL 后端后就认为桌面版已更新。
- 版本号位置不同：Electron 安装包版本在 `glasses-management-electron/package.json`，后端原生安装包版本在各自的 `jpackage.cfg`。

## 验证建议

根据改动范围选择最小验证：

- 前端改动：在 `glasses-management-frontend` 运行 `npm run build`。
- 后端改动：在对应后端模块运行 `mvn test` 或至少 `mvn clean package -DskipTests`。
- 涉及桌面版交付：在根目录运行 `.\build-desktop.ps1`。
- 涉及静态资源交付：运行 `.\sync-frontend.ps1` 后再构建后端或桌面版。

## 代码修改原则

- 先确认需求边界，再做最小改动。
- 不做与任务无关的重构、格式化或清理。
- 修改现有代码时优先匹配当前风格。
- 新增后端接口时同步考虑权限、统一返回结构、软删除规则和两个后端模块的一致性。
- 新增前端页面或功能时优先使用已有的 Element Plus、路由、Pinia 和 Axios 封装。
- 不提交 `application-local.yml`、构建产物、数据库文件、日志和本地 IDE 配置。
