<template>
  <div class="login-container">
    <div class="login-box glass-card">
      <div class="sys-title">
        <el-icon :size="28" color="var(--primary-color)"><View /></el-icon>
        <span>商户注册平台</span>
      </div>
      <p class="sys-subtitle">Create Your Account</p>
      
      <el-form :model="regForm" @keyup.enter="handleRegister">
        <el-form-item>
          <el-input 
            v-model="regForm.phone" 
            placeholder="请输入您的手机号" 
            :prefix-icon="Iphone"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="regForm.password" 
            type="password" 
            placeholder="请设置您的登录密码" 
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="regForm.inviteCode" 
            placeholder="请输入系统邀请码" 
            :prefix-icon="Key"
            size="large"
          />
        </el-form-item>
        <div style="text-align: right; margin-top: -10px; margin-bottom: 20px;">
          <el-link type="primary" :underline="false" @click="$router.push('/login')">返回登录</el-link>
        </div>
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="login-btn" 
            @click="handleRegister" 
            :loading="loading">
            立即注册
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { Iphone, Lock, Key, View } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(false);

const regForm = reactive({
  phone: '',
  password: '',
  inviteCode: ''
});

const handleRegister = async () => {
  if (!regForm.phone || !regForm.password || !regForm.inviteCode) {
    return ElMessage.warning('请完整填写注册信息');
  }
  loading.value = true;
  try {
    await request.post('/auth/register', regForm);
    ElMessage.success('注册成功，请使用新账号登录！');
    router.push('/login');
  } catch (error) {
    console.error(error);
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
  background: rgba(255, 255, 255, 0.9);
  border-radius: 28px;
  box-shadow: 0 28px 60px rgba(37, 99, 235, 0.14);
  border: 1px solid rgba(148, 163, 184, 0.16);
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
