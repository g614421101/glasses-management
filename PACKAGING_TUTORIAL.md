# 视光管理系统打包教程

本文档基于当前工作区里真实存在的模块编写：

- `glasses-management-backend`
- `glasses-management-backend-h2`
- `glasses-management-backend-sqlite`
- `glasses-management-frontend-vue`
- `glasses-management-electron`

当前仓库里没有 `glasses-management-desktop-javafx`，因此本教程不再包含旧版 JavaFX 打包说明。

## 先看结论

如果你只是想把当前系统打成一个可以发给别人安装的 Windows 安装包，优先使用下面这条：

- 推荐路线：`build-desktop.ps1` 一键打包 Electron 桌面版（可选 H2 / SQLite / MySQL 后端）

如果你需要直接生成 Spring Boot 原生安装包，而不是 Electron 桌面壳，则使用下面这条：

- 备选路线：运行对应后端目录下的 `build-package.ps1`

## 路线一：一键打包 Electron 桌面版（H2 / SQLite / MySQL）

这是当前项目里最完整、最贴近交付场景的打包方式。它会：

1. 构建前端
2. 把前端 `dist` 拷贝进所选后端模块
3. 打包所选后端的 JAR 并暂存到 `glasses-management-electron\backend-jar`
4. 交给 Electron 生成 Windows 安装包

### 环境要求

- Windows
- Node.js
- JDK 21
- Maven

### 执行命令

在项目根目录运行（交互选择后端与前端，回车默认 H2 / Vue）：

```powershell
.\build-desktop.ps1
```

也可以非交互指定：

```powershell
.\build-desktop.ps1 -Backend SQLite -Frontend Vue
```

### 产物位置

打包完成后，安装包会输出到：

```text
glasses-management-electron\dist
```

安装包文件名带后端标识，避免三种后端同名混淆：

```text
视光管理系统_H2_版本号.exe
视光管理系统_SQLite_版本号.exe
视光管理系统_MySQL_版本号.exe
```

### 什么时候用这条路线

- 想交付一个开箱即用的桌面版
- H2 / SQLite：单机数据库，零配置即可运行
- MySQL：工作站连中央数据库场景，需在首次启动前配置数据源（打包时自动携带所选后端的 `application-local.yml`，或安装后在 `%APPDATA%\视光管理系统\application-local.yml` 配置）
- 希望用户直接安装运行，不自己配浏览器访问

### 版本号在哪里改

Electron 安装包版本号来自：

```text
glasses-management-electron/package.json
```

当前字段是：

```json
"version": "1.1.1"
```

如果你希望生成新版本安装包，先改这里，再执行 `.\build-desktop.ps1`。

## 路线二：打包 Spring Boot 原生 EXE

如果你不想走 Electron，而是想把后端本身直接打成 Windows 原生安装包，可以使用 `jpackage`。

### 环境要求

- Windows
- JDK 21
- Maven
- WiX Toolset

### 推荐方式

H2 原生安装包：

```powershell
cd glasses-management-backend-h2
.\build-package.ps1
cd ..
```

MySQL 原生安装包：

```powershell
cd glasses-management-backend
.\build-package.ps1
cd ..
```

两个脚本都会自动构建前端、同步静态资源、打包后端 JAR，并按当前目录下的 `jpackage.cfg` 输出安装包。

## 只同步前端到后端

如果你只是想让 `http://localhost:8080` 直接看到最新前端，不需要生成安装包，可以在根目录运行：

```powershell
.\sync-frontend.ps1
```

只同步 H2 后端：

```powershell
.\sync-frontend.ps1 -Backend H2
```

只同步 MySQL 后端：

```powershell
.\sync-frontend.ps1 -Backend MySQL
```

如果已经有最新的 `glasses-management-frontend-vue/dist`，也可以跳过前端构建：

```powershell
.\sync-frontend.ps1 -SkipBuild
```

### 手动步骤

无论是 MySQL 版还是 H2 版，都建议先做完这三步：

1. 构建前端

   ```powershell
   cd glasses-management-frontend-vue
   npm install
   npm run build
   cd ..
   ```

2. 把前端产物复制到目标后端的静态资源目录

   H2 版：

   ```powershell
   Remove-Item 'glasses-management-backend-h2\src\main\resources\static\*' -Recurse -Force -ErrorAction SilentlyContinue
   New-Item -ItemType Directory -Force -Path 'glasses-management-backend-h2\src\main\resources\static' -ErrorAction SilentlyContinue
   Copy-Item -Path 'glasses-management-frontend-vue\dist\*' -Destination 'glasses-management-backend-h2\src\main\resources\static' -Recurse -Force
   ```

   MySQL 版：

   ```powershell
   Remove-Item 'glasses-management-backend\src\main\resources\static\*' -Recurse -Force -ErrorAction SilentlyContinue
   New-Item -ItemType Directory -Force -Path 'glasses-management-backend\src\main\resources\static' -ErrorAction SilentlyContinue
   Copy-Item -Path 'glasses-management-frontend-vue\dist\*' -Destination 'glasses-management-backend\src\main\resources\static' -Recurse -Force
   ```

3. 打包后端 JAR

   H2 版：

   ```powershell
   cd glasses-management-backend-h2
   mvn clean package -DskipTests
   cd ..
   ```

   MySQL 版：

   ```powershell
   cd glasses-management-backend
   mvn clean package -DskipTests
   cd ..
   ```

## 手动打 H2 原生 EXE

### 适用场景

- 想打一个不依赖 Electron 的 H2 单机版安装包
- 希望产物更接近传统 Java 原生安装程序

### 操作步骤

1. 进入 H2 后端目录：

   ```powershell
   cd glasses-management-backend-h2
   ```

2. 准备 `jpackage` 输入目录：

   ```powershell
   New-Item -ItemType Directory -Force -Path 'jpackage-temp'
   Copy-Item -Path 'target\glasses-management-backend-h2-*.jar' -Destination 'jpackage-temp\'
   ```

3. 执行打包：

   ```powershell
   jpackage --type exe --name "视光管理系统" --app-version "1.1.1" --input jpackage-temp/ --main-jar glasses-management-backend-h2-0.0.1-SNAPSHOT.jar --dest dist-install --win-shortcut --win-menu --win-dir-chooser --win-console --java-options "-Dspring.profiles.active=prod"
   ```

4. 清理临时目录：

   ```powershell
   Remove-Item 'jpackage-temp' -Recurse -Force
   cd ..
   ```

### 产物位置

```text
glasses-management-backend-h2\dist-install
```

### 需要注意

- H2 版生产环境数据库路径在 `application-prod.yml` 中，默认落到用户目录下的 `.glasses_management\data`
- **管理员初始化（统一策略）**：`application-local.yml` 中配置了 `glasses.admin.password`（或 `password-hash`）则首次启动自动创建管理员；两者都留空时，首次打开页面会在登录页引导完成初始化（需邀请码）；忘记密码可临时置 `force-reset-password: true` 重启，密码被重置为随机强密码（仅打印一次到日志）
- 当前 `jpackage` 参数保存在 `glasses-management-backend-h2/jpackage.cfg`，如果你改版本号，优先同步这里

## 手动打 MySQL 原生 EXE

### 适用场景

- 需要连接外部 MySQL
- 不需要 Electron 桌面壳

### 操作步骤

1. 进入 MySQL 后端目录：

   ```powershell
   cd glasses-management-backend
   ```

2. 准备 `jpackage` 输入目录：

   ```powershell
   New-Item -ItemType Directory -Force -Path 'jpackage-temp'
   Copy-Item -Path 'target\glasses-management-backend-*.jar' -Destination 'jpackage-temp\'
   ```

3. 执行打包：

   ```powershell
   jpackage --type exe --name "视光管理系统" --app-version "1.0.5" --input jpackage-temp/ --main-jar glasses-management-backend-0.0.1-SNAPSHOT.jar --dest dist-install --win-shortcut --win-menu --win-dir-chooser --win-console --java-options "-Dspring.profiles.active=prod"
   ```

4. 清理临时目录：

   ```powershell
   Remove-Item 'jpackage-temp' -Recurse -Force
   cd ..
   ```

### 产物位置

```text
glasses-management-backend\dist-install
```

### 需要注意

- MySQL 版不会把数据库一起打进去，它依赖外部 MySQL 服务
- 打包前请确认 `application-local.yml` 中的数据库地址、账号、密码已经是你要交付的配置
- **管理员初始化（统一策略）**：`application-local.yml` 中配置了 `glasses.admin.password`（或 `password-hash`）则首次启动自动创建管理员；两者都留空时，首次打开页面会在登录页引导完成初始化（需邀请码）；忘记密码可临时置 `force-reset-password: true` 重启，密码被重置为随机强密码（仅打印一次到日志）
- 当前 `jpackage` 参数保存在 `glasses-management-backend/jpackage.cfg`，如果你改版本号，优先同步这里

## 常见问题

### 1. 为什么改了前端，安装包里页面还是旧的

因为前端改动只有在重新执行：

1. `npm run build`
2. 同步 `dist` 到后端 `static`
3. 重新打包

之后才会进入安装包。

### 2. build-desktop.ps1 可以打哪些后端

根目录的 `build-desktop.ps1` 支持 H2 / SQLite / MySQL 三种后端，打包时交互选择或用 `-Backend` 参数指定：

- H2（默认）/ SQLite：单机文件数据库，开箱即用
- MySQL：需连接外部 MySQL 服务，首次启动前必须配置数据源，适合工作站连中央数据库场景

如果你要打不依赖 Electron 的原生安装包，请走各后端目录下的 `build-package.ps1`（H2 / SQLite / MySQL 均有）。

### 3. 版本号改哪里

- Electron 安装包版本：`glasses-management-electron/package.json`
- H2 原生安装包版本：`glasses-management-backend-h2/jpackage.cfg`
- MySQL 原生安装包版本：`glasses-management-backend/jpackage.cfg`

### 4. 哪条路线更推荐

通常建议：

- 本地单机交付：优先 `build-desktop.ps1`
- 服务器或外部数据库场景：优先 MySQL 后端正常部署，不一定要打本地 EXE
- 确实需要原生 Java 安装包时：使用对应后端目录的 `jpackage`
