import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '');
  const username = ref(localStorage.getItem('username') || '');
  const role = ref(localStorage.getItem('role') || '');

  function login(newToken, newUsername, newRole) {
    token.value = newToken;
    username.value = newUsername;
    role.value = newRole;
    localStorage.setItem('token', newToken);
    localStorage.setItem('username', newUsername);
    if(newRole) localStorage.setItem('role', newRole);
  }

  function logout() {
    token.value = '';
    username.value = '';
    role.value = '';
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
  }

  async function verifyToken() {
    if (!token.value) return;
    try {
      const data = await import('../utils/request').then(m => m.default.get('/auth/info'));
      username.value = data.username;
      role.value = data.role;
      localStorage.setItem('username', data.username);
      localStorage.setItem('role', data.role);
    } catch (e) {
      logout();
      throw e;
    }
  }

  return { token, username, role, login, logout, verifyToken };
});
