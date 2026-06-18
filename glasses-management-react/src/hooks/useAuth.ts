import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { RootState, AppDispatch } from '@/store';
import { login, logout, verifyToken, updateProfile } from '@/features/auth/authSlice';
import { LoginRequest } from '@/features/auth/authAPI';

export const useAuth = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { token, username, realName, phone, role, createTime, mustChangePassword, disabled, deleted, verified, loading } = useSelector(
    (state: RootState) => state.auth
  );

  const handleLogin = async (credentials: LoginRequest) => {
    const result = await dispatch(login(credentials));
    if (login.fulfilled.match(result)) {
      if (result.payload.mustChangePassword) {
        navigate('/profile');
      } else {
        navigate('/');
      }
    }
    return result;
  };

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  const handleVerifyToken = () => {
    if (token && !verified) {
      dispatch(verifyToken());
    }
  };

  const handleUpdateProfile = (data: { realName: string; phone: string }) => {
    dispatch(updateProfile(data));
  };

  return {
    token,
    username,
    realName,
    phone,
    role,
    createTime,
    mustChangePassword,
    disabled,
    deleted,
    verified,
    loading,
    isAdmin: role === 'admin',
    handleLogin,
    handleLogout,
    handleVerifyToken,
    handleUpdateProfile,
  };
};