# 视光档案管理系统前端

`glasses-management-frontend` 是系统的 Vue 3 前端项目，负责登录注册、工作台、顾客管理、档案时间轴、营收统计和账号管理等页面。

## 技术栈

- Vue 3
- Vite
- TypeScript
- Element Plus
- Pinia
- Vue Router
- Axios

## 目录说明

- `src/views`：页面
- `src/layout`：主布局
- `src/router`：路由
- `src/stores`：状态管理
- `src/api`：接口请求封装
- `src/style.css`：全局样式

## 环境要求

- Node.js 18+
- npm 9+

## 本地开发

安装依赖：

```bash
npm install
```

启动开发服务器：

```bash
npm run dev
```

默认通过 Vite 代理把 `/api` 转发到：

- `http://localhost:8080`

因此开发时需要同时启动任意一个后端：

- `glasses-management-backend`
- `glasses-management-backend-h2`

## 构建

```bash
npm run build
```

构建产物输出到：

- `dist`

如果要让后端直接托管页面，需要把 `dist` 内容同步到后端的 `src/main/resources/static`。

项目根目录提供了同步脚本：

```powershell
cd ..
.\sync-frontend.ps1
```

## 说明

- 当前 UI 已统一为蓝白配色，并针对顾客管理、档案卡片、分页和响应式布局做过优化。
- 当前前端默认依赖 `/api/auth/info` 做登录态校验，MySQL 版与 H2 版后端都已对齐。
