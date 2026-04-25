# 视光档案管理系统 Electron 桌面端

`glasses-management-electron` 是 H2 单机版的桌面壳项目，用 Electron 启动内置后端并加载本地页面。

## 作用

- 启动 H2 后端 JAR
- 等待本地 `8080` 服务就绪
- 加载桌面窗口
- 关闭应用时清理 Java 后端进程

## 当前运行方式

当前 Electron 版已经改成使用内置 Java 运行时，不再在线下载 JVM。

打包产物里会包含：

- `resources/backend`：H2 后端 JAR
- `resources/runtime/jre`：Electron 使用的内置 Java 运行时

因此交付给客户时，目标机器不需要额外安装 Java。

## 目录说明

- `main.js`：Electron 主进程入口
- `preload.js`：预加载脚本
- `loading.html`：启动加载页
- `runtime/jre`：打包前生成的内置 Java 运行时
- `dist`：Electron 安装包输出目录

## 环境要求

构建 Electron 桌面包时建议准备：

- Node.js 18+
- npm 9+
- JDK 21（用于 `jlink`）
- H2 后端已能正常构建

## 推荐打包方式

回到根目录执行：

```powershell
.\build-desktop.ps1
```

该脚本会自动完成：

1. 构建前端
2. 同步前端到 H2 后端静态目录
3. 构建 H2 后端 JAR
4. 用 `jlink` 生成 `runtime/jre`
5. 用 `electron-builder` 输出安装包

安装包输出目录：

- `dist`

## 单独构建 Electron

如果前端、后端 JAR、`runtime/jre` 都已经准备好了，也可以在当前目录执行：

```bash
npm install
npm run build
```

## 说明

- 当前项目已经单独纳入 git 管理。
- `dist`、`runtime`、`node_modules` 等生成物已在 `.gitignore` 中排除。
