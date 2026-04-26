# 视光档案管理系统 H2 后端

`glasses-management-backend-h2` 是单机版后端，内置 H2 文件数据库，适合本地运行、原生 EXE 打包和 Electron 桌面版集成。

## 技术栈

- Java 21
- Spring Boot 3.2
- MyBatis Plus
- Sa-Token
- H2 Database
- iText7
- EasyExcel

## 目录说明

- `src/main/java`：业务代码
- `src/main/resources`：配置文件、SQL、Mapper、静态资源
- `src/main/resources/sql/schema.sql`：H2 初始化脚本
- `jpackage.cfg`：原生 EXE 打包参数
- `data`：开发环境本地 H2 数据文件

## 默认配置

- 服务端口：`8080`
- 默认超管账号：`admin`
- 默认超管密码：`REMOVED_ADMIN_PASSWORD`
- 注册邀请码：`REMOVED_INVITE_CODE`

开发环境数据库默认写在 [application.yml](./src/main/resources/application.yml) 中：

- 地址：`jdbc:h2:file:./data/glasses_management`
- 用户名：`sa`
- 密码：空

生产环境配置见 [application-prod.yml](./src/main/resources/application-prod.yml)：

- 数据库路径默认落到 `~/.glasses_management/data`
- 日志默认落到 `~/.glasses_management/logs`

## 安装后数据位置

H2 原生安装包启动时会按 [application-prod.yml](./src/main/resources/application-prod.yml) 使用生产配置，因此安装后数据默认写到当前 Windows 用户目录下：

- 数据库目录：`C:\Users\<用户名>\.glasses_management\data`
- 日志目录：`C:\Users\<用户名>\.glasses_management\logs`

常见的 H2 数据文件包括：

- `glasses_management.mv.db`
- `glasses_management.trace.db`（如果运行过程中生成）

如果需要迁移或备份单机数据，优先备份上述 `data` 目录即可，安装目录本身通常不保存业务数据。

## 本地启动

直接在当前目录运行：

```bash
mvn spring-boot:run
```

首次启动会自动执行 [schema.sql](./src/main/resources/sql/schema.sql) 初始化库表。

启动后默认访问：

- `http://localhost:8080`
- `http://localhost:8080/h2-console`（仅开发环境开启）

如果前端构建产物已经同步到 `src/main/resources/static`，后端也可以直接托管整套页面。

## 打包

推荐直接运行当前目录下的打包脚本：

```powershell
.\build-package.ps1
```

脚本会自动完成：

1. 构建前端
2. 同步前端到 H2 后端静态目录
3. 打包 H2 后端 JAR
4. 按 [jpackage.cfg](./jpackage.cfg) 生成 Windows 原生安装包

安装包输出目录：

- `dist-install`

如果只需要构建 JAR，也可以运行：

```bash
mvn clean package -DskipTests
```

### 打 Electron 桌面版

推荐回到根目录执行：

```powershell
.\build-desktop.ps1
```

该脚本会自动：

1. 构建前端
2. 同步前端到 H2 后端静态目录
3. 打包 H2 后端 JAR
4. 生成 Electron 使用的内置 Java 运行时
5. 输出桌面安装包

## 说明

- H2 版后端与当前前端接口已对齐，可直接配合使用。
- 这是桌面单机版的主后端，也是 Electron 项目默认集成的后端。
