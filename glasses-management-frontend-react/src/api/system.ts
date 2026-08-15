import request from '@/utils/request';

export interface SetupStatus {
  initialized: boolean;
}

export interface SetupRequest {
  username: string;
  password: string;
  confirmPassword: string;
  inviteCode: string;
}

export const systemAPI = {
  /** 查询系统是否已初始化（未初始化时登录页展示初始化表单） */
  setupStatus: (): Promise<SetupStatus> => {
    return request.get('/system/setup-status');
  },

  /** 首次初始化：创建管理员账号 */
  setup: (data: SetupRequest): Promise<string> => {
    return request.post('/system/setup', data);
  },
};
