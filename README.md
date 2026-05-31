# 眼镜店视光档案管理系统

面向眼镜店的轻量视光档案系统，用于录入顾客、验光、配镜和营收数据，并集中查看顾客历史档案。支持多端响应式适配与桌面单机一键打包。

## 主要特性

- **多端响应式自适应**：
  - 针对桌面端（> 900px）设计了可折叠侧边栏，支持在极简图标模式（`76px`）与完整菜单模式（`220px`）间平滑切换。
  - 针对移动端（<= 900px）自动隐藏侧边栏并改用侧滑抽屉导航，表格自动转换为专为移动端定制的卡片列表（`MobileCardList`），防出界与溢出。
  - 顾客档案与回收站按钮进行窄屏下的响应式与 CSS Grid 布局重构，彻底解决手机小屏下的多按钮折行挤压、横向溢出问题。
- **局域网连接与扫码访问**：首页自动检测服务器局域网 IP 并生成二维码，移动设备扫码即可轻松接入系统。
- **灵活的部署形态**：支持轻量级 H2 单机版后端与 MySQL 多人协作版后端，可通过 Electron 壳一键打包生成 Windows 桌面应用。

## 项目结构

- `glasses-management-backend`: MySQL 版后端，适合连接外部 MySQL 部署。
- `glasses-management-backend-h2`: H2 单机版后端，适合本地运行、原生安装包和 Electron 桌面版。
- `glasses-management-frontend`: Vue 3 前端。
- `glasses-management-electron`: Electron 桌面打包项目。
- `build-desktop.ps1`: 一键构建桌面版，默认使用 H2 后端。

## 本地私密配置

仓库不会提交真实邀请码、管理员初始密码或 MySQL 账号密码。首次运行前，请在对应后端目录创建本地配置：

```powershell
Copy-Item glasses-management-backend\application-local.example.yml glasses-management-backend\application-local.yml
Copy-Item glasses-management-backend-h2\application-local.example.yml glasses-management-backend-h2\application-local.yml
```

然后把 `application-local.yml` 里的占位值替换为你的本机私密值。真实的 `application-local.yml` 已被 `.gitignore` 忽略，不应提交到 Git。

也可以不用本地文件，改用环境变量：

- `APP_INVITE_CODE`
- `GLASSES_ADMIN_PASSWORD`
- `GLASSES_ADMIN_USERNAME`
- `GLASSES_ADMIN_REAL_NAME`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

如果缺少邀请码或管理员初始密码，后端会启动失败，避免公开仓库里的默认弱密码被误用。

## 开发启动

MySQL 版：

```powershell
cd glasses-management-backend
mvn spring-boot:run
```

H2 版：

```powershell
cd glasses-management-backend-h2
mvn spring-boot:run
```

前端：

```powershell
cd glasses-management-frontend
npm install
npm run dev
```

开发环境下前端通过 `/api` 代理到 `http://localhost:8080`。

## 打包

同步前端到后端静态目录：

```powershell
.\sync-frontend.ps1
```

构建 Electron 桌面版：

```powershell
.\build-desktop.ps1
```

分别构建后端原生安装包：

```powershell
cd glasses-management-backend
.\build-package.ps1
```

```powershell
cd glasses-management-backend-h2
.\build-package.ps1
```

## 上传 GitHub 前

如果旧提交里曾经包含真实配置，公开仓库前需要重写 Git 历史。推荐先备份仓库，再用 `git filter-repo` 或 BFG 清理旧值，清理后重新扫描全仓库和历史提交。

注意：历史重写会改变 commit hash；如果已有协作者，需要通知他们重新克隆或按新的历史同步。
