import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { authAPI, LoginRequest, UserInfo } from './authAPI';
import { getToken, setToken, removeToken } from '@/utils/token';

interface AuthState {
  token: string | null;
  username: string;
  realName: string;
  phone: string;
  role: string;
  createTime: string;
  mustChangePassword: boolean;
  disabled: boolean;
  deleted: boolean;
  verified: boolean;
  loading: boolean;
}

const initialState: AuthState = {
  token: getToken(),
  username: localStorage.getItem('username') || '',
  realName: localStorage.getItem('realName') || '',
  phone: localStorage.getItem('phone') || '',
  role: localStorage.getItem('role') || '',
  createTime: localStorage.getItem('createTime') || '',
  mustChangePassword: localStorage.getItem('mustChangePassword') === 'true',
  disabled: localStorage.getItem('disabled') === 'true',
  deleted: localStorage.getItem('deleted') === 'true',
  verified: false,
  loading: false,
};

export const login = createAsyncThunk(
  'auth/login',
  async (credentials: LoginRequest) => {
    const response = await authAPI.login(credentials);
    return response;
  }
);

export const verifyToken = createAsyncThunk(
  'auth/verifyToken',
  async () => {
    const response = await authAPI.getUserInfo();
    return response;
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout: (state) => {
      state.token = null;
      state.username = '';
      state.realName = '';
      state.phone = '';
      state.role = '';
      state.createTime = '';
      state.mustChangePassword = false;
      state.disabled = false;
      state.deleted = false;
      state.verified = false;
      removeToken();
      localStorage.removeItem('username');
      localStorage.removeItem('realName');
      localStorage.removeItem('phone');
      localStorage.removeItem('role');
      localStorage.removeItem('createTime');
      localStorage.removeItem('mustChangePassword');
      localStorage.removeItem('disabled');
      localStorage.removeItem('deleted');
    },
    updateProfile: (state, action: PayloadAction<Partial<UserInfo>>) => {
      if (action.payload.realName !== undefined) {
        state.realName = action.payload.realName;
        localStorage.setItem('realName', action.payload.realName);
      }
      if (action.payload.phone !== undefined) {
        state.phone = action.payload.phone;
        localStorage.setItem('phone', action.payload.phone);
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.loading = true;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.token = action.payload.token;
        state.username = action.payload.username;
        state.role = action.payload.role;
        state.realName = action.payload.realName;
        state.phone = action.payload.phone;
        state.createTime = action.payload.createTime;
        state.mustChangePassword = action.payload.mustChangePassword;
        state.disabled = action.payload.disabled;
        state.deleted = action.payload.deleted;
        state.verified = true;
        
        setToken(action.payload.token);
        localStorage.setItem('username', action.payload.username);
        localStorage.setItem('role', action.payload.role);
        localStorage.setItem('realName', action.payload.realName);
        localStorage.setItem('phone', action.payload.phone);
        localStorage.setItem('createTime', action.payload.createTime);
        localStorage.setItem('mustChangePassword', String(action.payload.mustChangePassword));
        localStorage.setItem('disabled', String(action.payload.disabled));
        localStorage.setItem('deleted', String(action.payload.deleted));
      })
      .addCase(login.rejected, (state) => {
        state.loading = false;
      })
      .addCase(verifyToken.fulfilled, (state, action) => {
        state.username = action.payload.username;
        state.role = action.payload.role;
        state.realName = action.payload.realName;
        state.phone = action.payload.phone;
        state.createTime = action.payload.createTime;
        state.mustChangePassword = action.payload.mustChangePassword;
        state.disabled = action.payload.disabled;
        state.deleted = action.payload.deleted;
        state.verified = true;
        
        localStorage.setItem('username', action.payload.username);
        localStorage.setItem('role', action.payload.role);
        localStorage.setItem('realName', action.payload.realName);
        localStorage.setItem('phone', action.payload.phone);
        localStorage.setItem('createTime', action.payload.createTime);
        localStorage.setItem('mustChangePassword', String(action.payload.mustChangePassword));
        localStorage.setItem('disabled', String(action.payload.disabled));
        localStorage.setItem('deleted', String(action.payload.deleted));
      })
      .addCase(verifyToken.rejected, (state) => {
        state.token = null;
        state.verified = false;
        removeToken();
      });
  },
});

export const { logout, updateProfile } = authSlice.actions;
export default authSlice.reducer;
