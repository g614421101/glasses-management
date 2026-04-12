<template>
  <div class="login-container">
    <div class="login-box glass-card">
      <div class="sys-title">
        <el-icon :size="28" color="var(--primary-color)"><View /></el-icon>
        <span>商户注册平台</span>
      </div>
      <p class="sys-subtitle">输入专属邀请码，完成极速注册开通</p>
      
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

<script setup>
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
    const res = await request.post('/auth/register', regForm);
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
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
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
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 12px;
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
  background: linear-gradient(90deg, #10b981 0%, #059669 100%);
  border: none;
}

.login-btn:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}
</style>
