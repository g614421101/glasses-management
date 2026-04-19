import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '');
  const username = ref(localStorage.getItem('username') || '');
  const role = ref(localStorage.getItem('role') || '');
  // 内存标记：本次运行期间是否已向后端验证过 Token
  // 不保存到 localStorage，每次应用重启时从 false 开始，强制重新验证
  const verified = ref(false);

  function login(newToken: string, newUsername: string, newRole: string) {
    token.value = newToken;
    username.value = newUsername;
    role.value = newRole;
    verified.value = true;
    localStorage.setItem('token', newToken);
    localStorage.setItem('username', newUsername);
    if(newRole) localStorage.setItem('role', newRole);
  }

  function logout() {
    token.value = '';
    username.value = '';
    role.value = '';
    verified.value = false;
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
  }

  async function verifyToken() {
    if (!token.value) return;
    try {
      const data: any = await import('../utils/request').then(m => m.default.get('/auth/info'));
      username.value = data.username;
      role.value = data.role;
      verified.value = true;
      localStorage.setItem('username', data.username);
      localStorage.setItem('role', data.role);
    } catch (e) {
      logout();
      throw e;
    }
  }

  return { token, username, role, verified, login, logout, verifyToken };
});
