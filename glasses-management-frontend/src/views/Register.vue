<template>
  <div class="login-container">
    <div class="register-box glass-card">
      <div class="sys-title login-anim login-anim--1">
        <el-icon :size="28" color="var(--primary-color)"><View /></el-icon>
        <span>商户注册</span>
      </div>
      <p class="sys-subtitle login-anim login-anim--2">Create Your Account</p>

      <el-form :model="regForm" @keyup.enter="handleRegister" label-position="top" class="login-anim login-anim--3">
        <div class="form-grid">
          <el-form-item label="用户名">
            <el-input v-model="regForm.username" placeholder="3-30 位登录用户名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="regForm.phone" placeholder="请输入手机号" :prefix-icon="Iphone" size="large" />
          </el-form-item>
        </div>
        <el-form-item label="密码">
          <el-input
            v-model="regForm.password"
            type="password"
            placeholder="至少 6 位密码"
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="regForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item label="邀请码">
          <el-input v-model="regForm.inviteCode" placeholder="请输入系统邀请码" :prefix-icon="Key" size="large" />
        </el-form-item>
        <div class="form-link">
          <el-link type="primary" :underline="false" @click="$router.push('/login')">返回登录</el-link>
        </div>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" @click="handleRegister" :loading="loading">
            立即注册
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { Iphone, Lock, Key, View, User } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import request from '../utils/request';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(false);

const regForm = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  inviteCode: ''
});

const validate = () => {
  if (!regForm.username || !regForm.phone || !regForm.password || !regForm.confirmPassword || !regForm.inviteCode) {
    ElMessage.warning('请完整填写注册信息');
    return false;
  }
  if (regForm.username.length < 3 || regForm.username.length > 30) {
    ElMessage.warning('用户名长度需为 3-30 位');
    return false;
  }
  if (!/^1[3-9]\d{9}$/.test(regForm.phone)) {
    ElMessage.warning('手机号格式不正确');
    return false;
  }
  if (regForm.password.length < 6) {
    ElMessage.warning('密码至少 6 位');
    return false;
  }
  if (regForm.password !== regForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致');
    return false;
  }
  return true;
};

const handleRegister = async () => {
  if (!validate()) return;
  loading.value = true;
  try {
    await request.post('/auth/register', regForm);
    ElMessage.success('注册成功，请使用用户名或手机号登录');
    router.push('/login');
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
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

.register-box {
  width: min(100%, 620px);
  padding: 44px;
  z-index: 10;
  background: var(--surface-overlay);
  border-radius: 30px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-color);
  animation: register-card-in 0.55s var(--ease-emphasized) both;
}

.login-anim {
  opacity: 0;
  animation: register-rise var(--duration-slow) var(--ease-emphasized) forwards;
}
.login-anim--1 { animation-delay: 120ms; }
.login-anim--2 { animation-delay: 200ms; }
.login-anim--3 { animation-delay: 280ms; }

@keyframes register-card-in {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes register-rise {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.sys-title {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.sys-subtitle {
  text-align: center;
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0 0 30px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.form-link {
  text-align: right;
  margin: -4px 0 18px;
}

.login-btn {
  width: 100%;
  letter-spacing: 2px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 18px;
}

@media (max-width: 640px) {
  .login-container {
    padding: 14px;
  }

  .register-box {
    padding: 28px 20px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .sys-title {
    font-size: 22px;
  }
}
</style>
