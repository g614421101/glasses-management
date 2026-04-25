# 视光档案管理系统 MySQL 后端

`glasses-management-backend` 是面向外部 MySQL 数据库部署的后端项目，负责登录鉴权、顾客管理、档案记录、营收统计、打印与账号管理等接口。

## 技术栈

- Java 21
- Spring Boot 3.2
- MyBatis Plus
- Sa-Token
- MySQL 8+
- iText7
- EasyExcel

## 目录说明

- `src/main/java`：业务代码
- `src/main/resources`：配置文件、Mapper、静态资源
- `sql/schema.sql`：MySQL 初始化脚本
- `jpackage.cfg`：原生 EXE 打包参数

## 默认配置

- 服务端口：`8080`
- 默认超管账号：`admin`
- 默认超管密码：`REMOVED_ADMIN_PASSWORD`
- 注册邀请码：`REMOVED_INVITE_CODE`

数据库连接默认写在 [application.yml](./src/main/resources/application.yml) 中：

- 地址：`jdbc:mysql://127.0.0.1:3306/glasses_management`
- 用户名：`REMOVED_MYSQL_USERNAME`
- 密码：`REMOVED_MYSQL_PASSWORD`

## 本地启动

1. 准备 MySQL 8+，并创建数据库 `glasses_management`
2. 执行 [sql/schema.sql](./sql/schema.sql) 初始化表结构
3. 按需修改 [application.yml](./src/main/resources/application.yml) 中的数据库配置
4. 在当前目录运行：

```bash
mvn spring-boot:run
```

启动后默认访问：

- `http://localhost:8080`

如果前端构建产物已经同步到 `src/main/resources/static`，后端也可以直接托管整套页面。

## 打包

先构建 JAR：

```bash
mvn clean package -DskipTests
```

如果需要打 Windows 原生安装包，可使用当前目录下的 [jpackage.cfg](./jpackage.cfg)。

## 说明

- 当前前端依赖 `/api/auth/info` 做登录态校验，MySQL 版后端已经补齐对应接口。
- 当前权限控制主要集中在登录和账号管理，业务数据默认没有商户级隔离。
