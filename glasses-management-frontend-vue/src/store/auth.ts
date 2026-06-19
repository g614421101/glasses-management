import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '');
  const username = ref(localStorage.getItem('username') || '');
  const realName = ref(localStorage.getItem('realName') || '');
  const phone = ref(localStorage.getItem('phone') || '');
  const role = ref(localStorage.getItem('role') || '');
  const createTime = ref(localStorage.getItem('createTime') || '');
  const mustChangePassword = ref(localStorage.getItem('mustChangePassword') === 'true');
  const disabled = ref(localStorage.getItem('disabled') === 'true');
  const deleted = ref(localStorage.getItem('deleted') === 'true');
  // 内存标记：本次运行期间是否已向后端验证过 Token
  // 不保存到 localStorage，每次应用重启时从 false 开始，强制重新验证
  const verified = ref(false);

  function login(newToken: string, newUsername: string, newRole: string, profile: any = {}) {
    token.value = newToken;
    username.value = newUsername;
    role.value = newRole;
    realName.value = profile.realName || '';
    phone.value = profile.phone || '';
    createTime.value = profile.createTime || '';
    mustChangePassword.value = !!profile.mustChangePassword;
    disabled.value = !!profile.disabled;
    deleted.value = !!profile.deleted;
    verified.value = true;
    localStorage.setItem('token', newToken);
    localStorage.setItem('username', newUsername);
    localStorage.setItem('realName', realName.value);
    if(newRole) localStorage.setItem('role', newRole);
    localStorage.setItem('phone', phone.value);
    localStorage.setItem('createTime', createTime.value);
    localStorage.setItem('mustChangePassword', String(mustChangePassword.value));
    localStorage.setItem('disabled', String(disabled.value));
    localStorage.setItem('deleted', String(deleted.value));
  }

  function logout() {
    token.value = '';
    username.value = '';
    realName.value = '';
    phone.value = '';
    role.value = '';
    createTime.value = '';
    mustChangePassword.value = false;
    disabled.value = false;
    deleted.value = false;
    verified.value = false;
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('realName');
    localStorage.removeItem('phone');
    localStorage.removeItem('role');
    localStorage.removeItem('createTime');
    localStorage.removeItem('mustChangePassword');
    localStorage.removeItem('disabled');
    localStorage.removeItem('deleted');
  }

  async function verifyToken() {
    if (!token.value) return;
    try {
      const data: any = await import('../utils/request').then(m => m.default.get('/auth/info'));
      username.value = data.username;
      realName.value = data.realName || '';
      phone.value = data.phone || '';
      role.value = data.role;
      createTime.value = data.createTime || '';
      mustChangePassword.value = !!data.mustChangePassword;
      disabled.value = !!data.disabled;
      deleted.value = !!data.deleted;
      verified.value = true;
      localStorage.setItem('username', data.username);
      localStorage.setItem('realName', realName.value);
      localStorage.setItem('phone', phone.value);
      localStorage.setItem('role', data.role);
      localStorage.setItem('createTime', createTime.value);
      localStorage.setItem('mustChangePassword', String(mustChangePassword.value));
      localStorage.setItem('disabled', String(disabled.value));
      localStorage.setItem('deleted', String(deleted.value));
    } catch (e) {
      logout();
      throw e;
    }
  }

  return { token, username, realName, phone, role, createTime, mustChangePassword, disabled, deleted, verified, login, logout, verifyToken };
});
