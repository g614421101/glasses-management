<template>
  <div class="login-container">
    <div class="login-box glass-card">
      <div class="sys-title">
        <el-icon :size="28" color="var(--primary-color)"><View /></el-icon>
        <span>视光档案管理系统</span>
      </div>
      <p class="sys-subtitle">Optical Record Management System</p>
      
      <el-form :model="loginForm" @keyup.enter="handleLogin">
        <el-form-item>
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入账号" 
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        <div style="text-align: right; margin-top: -10px; margin-bottom: 20px;">
          <el-link type="primary" :underline="false" @click="$router.push('/register')">邀请码商户注册</el-link>
        </div>
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="login-btn" 
            @click="handleLogin" 
            :loading="loading">
            登录系统
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { User, Lock, View } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store/auth';
import request from '../utils/request';
import { ElMessage } from 'element-plus';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: ''
});

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning('请输入账号和密码');
  }
  loading.value = true;
  try {
    const res: any = await request.post('/auth/login', loginForm);
    if (!res || !res.token) {
      ElMessage.error('登录返回数据异常，请重试');
      return;
    }
    authStore.login(res.token, res.username, res.role, res);
    ElMessage.success('登录成功');
    await router.replace(res.mustChangePassword ? '/profile' : '/');
  } catch (error) {
    console.error('登录失败:', error);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  width: 100%;
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: transparent;
}

.login-box {
  width: min(100%, 460px);
  padding: 48px;
  text-align: center;
  z-index: 10;
  background: var(--surface-overlay);
  border-radius: 28px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-color);
}

.sys-title {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.sys-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 36px;
}

.login-btn {
  width: 100%;
  letter-spacing: 2px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 18px;
}

@media (max-width: 640px) {
  .login-container {
    padding: 14px;
  }

  .login-box {
    padding: 30px 22px;
  }

  .sys-title {
    font-size: 22px;
  }
}
</style>
