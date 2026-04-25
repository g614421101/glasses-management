# 眼镜店视光档案管理系统

面向眼镜店的轻量视光档案系统，用于快速录入顾客、验光、配镜与营收数据，并集中查看顾客历史时间轴档案。

## 项目结构

- `glasses-management-backend`：MySQL 版后端，适合连接外部 MySQL 数据库部署
- `glasses-management-backend-h2`：H2 单机版后端，适合本地单机运行和桌面打包
- `glasses-management-frontend`：Vue 3 前端
- `glasses-management-electron`：Electron 桌面打包项目
- `build-desktop.ps1`：一键打包桌面版脚本（默认打 H2 版）

## 技术栈

- 后端：Java 21、Spring Boot 3.2、MyBatis Plus、Sa-Token、iText7、EasyExcel
- MySQL 版数据库：MySQL 8+
- H2 版数据库：H2 文件数据库
- 前端：Vue 3、Vite、Element Plus、Pinia、Vue Router
- 桌面端：Electron、electron-builder

## 默认账号

- 超级管理员账号：`admin`
- 超级管理员初始密码：`REMOVED_ADMIN_PASSWORD`
- 注册邀请码：`REMOVED_INVITE_CODE`

## 开发启动

### 方式一：MySQL 版后端 + 前端

1. 确保本地已安装 JDK 21、Node.js 18+、MySQL 8+。
2. 在 MySQL 中创建数据库并导入 `glasses-management-backend/sql/schema.sql`。
3. 检查 `glasses-management-backend/src/main/resources/application.yml` 里的数据库连接信息。
4. 启动 MySQL 后端：

   ```bash
   cd glasses-management-backend
   mvn spring-boot:run
   ```

5. 启动前端：

   ```bash
   cd glasses-management-frontend
   npm install
   npm run dev
   ```

6. 打开浏览器访问前端控制台输出的地址，开发环境下会自动把 `/api` 代理到 `http://localhost:8080`。

### 方式二：H2 版后端 + 前端

1. 确保本地已安装 JDK 21、Node.js 18+。
2. 启动 H2 后端：

   ```bash
   cd glasses-management-backend-h2
   mvn spring-boot:run
   ```

3. 首次启动会自动执行 `glasses-management-backend-h2/src/main/resources/sql/schema.sql` 初始化库表。
4. 再启动前端：

   ```bash
   cd glasses-management-frontend
   npm install
   npm run dev
   ```

5. 开发环境同样通过 Vite 代理访问 `http://localhost:8080`。

## 直接运行后端静态页

两套后端都可以直接托管前端构建产物；如果你已经执行过前端打包，并把 `dist` 内容同步到后端 `src/main/resources/static`，也可以直接访问：

- `http://localhost:8080`

## 桌面版打包

根目录脚本会按下面流程打包 H2 桌面版：

1. 构建前端
2. 将前端 `dist` 复制到 `glasses-management-backend-h2/src/main/resources/static`
3. 打包 H2 后端 JAR
4. 交给 Electron 生成 Windows 安装包

执行命令：

```powershell
.\build-desktop.ps1
```

安装包输出目录：

- `glasses-management-electron/dist`

## 核心功能

- 极速检索：按手机号或姓名快速定位顾客
- 档案时间轴：统一查看顾客验光记录和配镜记录
- 处方打印：生成 PDF 处方单
- Excel 导出：导出顾客配镜记录和营收流水
- 账号管理：管理员可管理商户账号和重置密码

## 补充说明

- 当前前端默认依赖 `/api/auth/info` 做登录态校验，两套后端现已保持一致。
- 账号权限目前主要用于登录与管理后台账号，业务数据默认未做商户级隔离。
- 如需打包 MySQL 版原生安装包，可参考 `glasses-management-backend/jpackage.cfg`。
