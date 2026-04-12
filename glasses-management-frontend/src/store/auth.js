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

  return { token, username, role, login, logout };
});
