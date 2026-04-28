# 视光档案管理系统 MySQL 后端

`glasses-management-backend` 是连接外部 MySQL 的 Spring Boot 后端，提供登录鉴权、顾客管理、档案记录、统计、打印和账号管理接口。

## 私密配置

仓库中的 `src/main/resources/application.yml` 只保留公开占位配置。真实邀请码、管理员初始密码和数据库连接信息请放在本目录的 `application-local.yml`，或通过环境变量传入。

创建本地配置：

```powershell
Copy-Item application-local.example.yml application-local.yml
```

需要填写：

- `app.invite-code`
- `glasses.admin.password`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

`application-local.yml` 已被根目录 `.gitignore` 忽略，不要提交。

## 本地启动

1. 准备 JDK 21 和 MySQL 8+。
2. 创建业务数据库，并执行 `sql/schema.sql` 初始化表结构。
3. 填写本地私密配置。
4. 启动后端：

```powershell
mvn spring-boot:run
```

默认访问地址：

- `http://localhost:8080`

## 打包

```powershell
.\build-package.ps1
```

脚本会构建前端、同步静态资源、打包后端 JAR，并按 `jpackage.cfg` 生成 Windows 原生安装包。
