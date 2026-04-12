<template>
  <div class="login-container">
    <div class="login-box glass-card">
      <div class="sys-title">
        <el-icon :size="28" color="var(--primary-color)"><View /></el-icon>
        <span>视光档案管理系统</span>
      </div>
      <p class="sys-subtitle">专业、高效的验光配镜管理工作台</p>
      
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

<script setup>
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
    const res = await request.post('/auth/login', loginForm);
    if (!res || !res.token) {
      ElMessage.error('登录返回数据异常，请重试');
      return;
    }
    authStore.login(res.token, res.username, res.role);
    ElMessage.success('登录成功');
    await router.replace('/');
  } catch (error) {
    console.error('登录失败:', error);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -20%;
  left: -10%;
  width: 50vw;
  height: 50vw;
  background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0) 70%);
  border-radius: 50%;
}

.login-box {
  width: 400px;
  padding: 40px;
  text-align: center;
  z-index: 10;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  border: 1px solid rgba(255,255,255,0.5);
}

.sys-title {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 10px;
}

.sys-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 30px;
}

.login-btn {
  width: 100%;
  letter-spacing: 2px;
  font-weight: 600;
  background: linear-gradient(90deg, #4f46e5 0%, #6366f1 100%);
  border: none;
}

.login-btn:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.4);
}
</style>
