import request from '@/utils/request';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
  realName: string;
  phone: string;
  createTime: string;
  mustChangePassword: boolean;
  disabled: boolean;
  deleted: boolean;
}

export interface UserInfo {
  username: string;
  role: string;
  realName: string;
  phone: string;
  createTime: string;
  mustChangePassword: boolean;
  disabled: boolean;
  deleted: boolean;
}

export const authAPI = {
  login: (data: LoginRequest): Promise<LoginResponse> => {
    return request.post('/auth/login', data);
  },
  
  getUserInfo: (): Promise<UserInfo> => {
    return request.get('/auth/info');
  },
  
  changePassword: (data: { oldPassword: string; newPassword: string; confirmPassword: string }): Promise<void> => {
    return request.post('/auth/change-password', data);
  },
  
  updateProfile: (data: { realName: string; phone: string }): Promise<void> => {
    return request.put('/auth/profile', data);
  },
};
