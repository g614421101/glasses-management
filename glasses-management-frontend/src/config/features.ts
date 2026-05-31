/**
 * 功能开关配置
 *
 * 控制方式：将对应功能设为 false 即可关闭，设为 true 即可开启。
 * 关闭后：
 *   - 前端导航菜单不再显示对应入口
 *   - 前端路由不再注册，直接访问 URL 会 404
 *   - 后端接口不受影响（仍可通过直接 API 调用访问，但已有角色权限保护）
 * 修改后需重新构建前端（npm run build）生效。
 */
export const FEATURES = {
  /** 顾客管理 */
  CUSTOMER: true,
  /** 营收统计 */
  STATISTICS: true,
  /** 数据管理 */
  DATA_MANAGE: true,
  /** 个人主页 */
  PROFILE: true,
  /** 回收站（仅超管可见） */
  RECYCLE_BIN: true,
  /** 账号管理 / 超管功能 */
  SYS_USER: true,
} as const;
