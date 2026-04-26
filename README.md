# 眼镜店视光档案管理系统

面向眼镜店的轻量视光档案系统，用于快速录入顾客、验光、配镜与营收数据，并集中查看顾客历史时间轴档案。

## 项目结构

当前仓库采用 monorepo 方式统一管理，四个子项目已经合并到同一个 Git 仓库中，后续开发、分支、提交和发版都在根目录统一进行。

- `glasses-management-backend`：MySQL 版后端，适合连接外部 MySQL 数据库部署
- `glasses-management-backend-h2`：H2 单机版后端，适合本地单机运行和桌面打包
- `glasses-management-frontend`：Vue 3 前端
- `glasses-management-electron`：Electron 桌面打包项目
- `build-desktop.ps1`：一键打包桌面版脚本（默认打 H2 版）

## Git 管理方式

本项目现在使用一个根仓库统一管理所有模块，不再把四个子项目作为独立 Git 仓库维护。

- 根目录是唯一 Git 工作区。
- 子项目目录中不再保留各自的 `.git` 目录。
- 原子提交建议按业务改动组织，可以一次提交同时包含前端、后端和打包脚本的配套修改。
- `target`、`dist`、`runtime`、`node_modules`、H2 数据文件和后端静态构建产物都由根 `.gitignore` 排除。

历史导入时保留了各子项目原来的提交记录，提交图中可以看到对应模块的历史分支。因为文件被移动到了各自子目录下，查看旧历史时可以结合新旧路径查询，例如：

```bash
git log --oneline -- glasses-management-frontend/src/views/Archive.vue
git log --oneline --all -- src/views/Archive.vue
```

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

## 后端原生安装包

如果不需要 Electron 桌面壳，也可以分别在两个后端目录里生成 Windows 原生安装包：

```powershell
cd glasses-management-backend
.\build-package.ps1
```

```powershell
cd glasses-management-backend-h2
.\build-package.ps1
```

产物分别输出到对应后端目录下的 `dist-install`。

## 安装后数据目录

如果你把系统安装给客户本地单机使用，业务数据默认不写在安装目录，而是写在当前 Windows 用户目录下：

- `Electron` 版：`C:\Users\<用户名>\AppData\Roaming\视光管理系统\data`
- `H2 原生版`：`C:\Users\<用户名>\.glasses_management\data`
- `H2 原生版` 日志：`C:\Users\<用户名>\.glasses_management\logs`

建议做数据备份时优先备份这些目录中的 H2 数据文件，例如 `glasses_management.mv.db`。

## 核心功能

- 极速检索：按手机号或姓名快速定位顾客
- 档案时间轴：统一查看顾客验光记录和配镜记录
- 处方打印：生成 PDF 处方单
- Excel 导出：导出顾客配镜记录和营收流水
- 账号管理：管理员可管理商户账号和重置密码

## 补充说明

- 当前前端默认依赖 `/api/auth/info` 做登录态校验，两套后端现已保持一致。
- 账号权限目前主要用于登录与管理后台账号，业务数据默认未做商户级隔离。
- 如需打包后端原生安装包，可优先使用对应后端目录下的 `build-package.ps1`。
