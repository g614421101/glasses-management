import request from '@/utils/request';

export interface SysUser {
  id: number;
  username: string;
  phone: string;
  role: string;
  realName: string;
  createTime: string;
  disabled: boolean;
  deleted: boolean;
}

export const sysUserAPI = {
  list: (includeDeleted = false): Promise<SysUser[]> => {
    return request.get('/sys-user/list', { params: { includeDeleted } });
  },

  resetPassword: (id: number): Promise<string> => {
    return request.post(`/sys-user/reset-password/${id}`);
  },

  enable: (id: number): Promise<void> => {
    return request.post(`/sys-user/enable/${id}`);
  },

  disable: (id: number): Promise<void> => {
    return request.post(`/sys-user/disable/${id}`);
  },

  delete: (id: number): Promise<void> => {
    return request.delete(`/sys-user/${id}`);
  },

  restore: (id: number): Promise<void> => {
    return request.post(`/sys-user/restore/${id}`);
  },

  purge: (id: number): Promise<void> => {
    return request.delete(`/sys-user/purge/${id}`);
  },
};
