# 视光档案管理系统 Electron 桌面端

`glasses-management-electron` 是桌面壳项目，用 Electron 启动内置后端并加载本地页面。内置后端在打包时由 `build-desktop.ps1` 选择（H2 / SQLite / MySQL）。

## 作用

- 启动内置后端 JAR（H2 / SQLite / MySQL，由打包时决定）
- 等待本地 `8080` 服务就绪
- 加载桌面窗口
- 关闭应用时清理 Java 后端进程

## 当前运行方式

当前 Electron 版已经改成使用内置 Java 运行时，不再在线下载 JVM。

打包产物里会包含：

- `resources/backend`：所选后端的 JAR 与 `application-local.yml`
- `resources/runtime/jre`：Electron 使用的内置 Java 运行时

因此交付给客户时，目标机器不需要额外安装 Java。

## 安装后数据位置

Electron 版会把后端的工作目录设置到 Electron 的用户数据目录（`-Dapp.home`），因此安装后数据默认写在当前 Windows 用户的 `AppData\Roaming` 下：

- 数据库目录：`C:\Users\<用户名>\AppData\Roaming\视光管理系统\data`
- H2 后端：`glasses_management.mv.db`（可能伴随 `glasses_management.trace.db`）
- SQLite 后端：`glasses_management.db`（伴随 WAL 文件 `glasses_management.db-wal` / `glasses_management.db-shm`，备份时需一并带上）
- MySQL 后端：本地无业务数据（依赖外部 MySQL），仅日志写入用户数据目录

如果需要备份或迁移 Electron 单机版数据，直接备份上述 `data` 目录即可，不需要备份安装目录里的程序文件。

## 目录说明

- `main.js`：Electron 主进程入口
- `preload.js`：预加载脚本
- `loading.html`：启动加载页
- `backend-jar`：打包脚本暂存的后端 JAR（打包前由 `build-desktop.ps1` 生成）
- `packaging-config`：打包时携带的 `application-local.yml`
- `runtime/jre`：打包前生成的内置 Java 运行时
- `dist`：Electron 安装包输出目录

## 环境要求

构建 Electron 桌面包时建议准备：

- Node.js 18+
- npm 9+
- JDK 21（用于 `jlink`）
- 所选后端已能正常构建

## 推荐打包方式

回到根目录执行（交互选择后端与前端，回车默认 H2 / Vue）：

```powershell
.\build-desktop.ps1
```

也可以非交互指定：

```powershell
.\build-desktop.ps1 -Backend SQLite -Frontend Vue
```

该脚本会自动完成：

1. 构建前端并同步到所选后端静态目录
2. 构建所选后端的 JAR 并暂存到 `backend-jar`
3. 用 `jlink` 生成 `runtime/jre`
4. 用 `electron-builder` 输出安装包（文件名带后端标识：`视光管理系统_H2/SQLite/MySQL_版本号.exe`）

安装包输出目录：

- `dist`

## 单独构建 Electron

如果前端、后端 JAR、`runtime/jre` 都已经准备好了，也可以在当前目录执行：

```bash
npm install
npm run build
```

注意：`npm run build` 从 `backend-jar` 目录读取后端 JAR，请先执行 `build-desktop.ps1`（或手动把所选后端的 JAR 复制进 `backend-jar`，目录内有且仅有一个 JAR）。

## 说明

- 当前项目已合并到根目录 monorepo 中统一管理。
- `dist`、`runtime`、`node_modules` 等生成物已由 `.gitignore` 排除。
