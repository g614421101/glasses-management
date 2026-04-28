# 视光档案管理系统 H2 后端

`glasses-management-backend-h2` 是单机版 Spring Boot 后端，内置 H2 文件数据库，适合本地运行、原生安装包和 Electron 桌面版。

## 私密配置

仓库不会提交真实邀请码或管理员初始密码。首次运行前，在本目录创建本地配置：

```powershell
Copy-Item application-local.example.yml application-local.yml
```

至少需要填写：

- `app.invite-code`
- `glasses.admin.password`

H2 数据库路径、用户名和空密码是本地单机默认配置，不属于远程服务凭据；如需修改，也可以在 `application-local.yml` 覆盖。真实的 `application-local.yml` 已被根目录 `.gitignore` 忽略，不要提交。

## 本地启动

```powershell
mvn spring-boot:run
```

首次启动会自动执行 `src/main/resources/sql/schema.sql` 初始化表结构。

默认访问地址：

- `http://localhost:8080`
- `http://localhost:8080/h2-console`，仅开发环境开启。

## 打包

```powershell
.\build-package.ps1
```

桌面版可回到根目录执行：

```powershell
.\build-desktop.ps1
```

H2 生产配置会把数据和日志放到当前 Windows 用户目录下，避免业务数据写入安装目录。
