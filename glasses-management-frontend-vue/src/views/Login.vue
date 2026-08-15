<template>
  <div class="login-container">
    <div class="login-box glass-card">
      <div class="sys-title login-anim login-anim--1">
        <el-icon :size="28" color="var(--primary-color)"><View /></el-icon>
        <span>视光档案管理系统</span>
      </div>
      <p class="sys-subtitle login-anim login-anim--2">
        {{ mode === 'setup' ? '首次使用 · 系统初始化' : 'Optical Record Management System' }}
      </p>

      <!-- 首次初始化表单 -->
      <el-form
        v-if="mode === 'setup'"
        :model="setupForm"
        class="login-anim login-anim--3"
        @keyup.enter="handleSetup"
      >
        <el-form-item>
          <el-input
            v-model="setupForm.username"
            placeholder="设置管理员账号（3-30 位）"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="setupForm.password"
            type="password"
            placeholder="设置登录密码（至少 6 位）"
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="setupForm.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="setupForm.inviteCode"
            placeholder="初始化邀请码（由部署者提供）"
            :prefix-icon="Key"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleSetup"
          >
            初始化系统
          </el-button>
        </el-form-item>
        <div class="setup-tip">初始化只需一次，完成后请使用刚设置的账号登录。</div>
      </el-form>

      <!-- 登录表单 -->
      <el-form v-else :model="loginForm" @keyup.enter="handleLogin" class="login-anim login-anim--3">
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
import { reactive, ref, onMounted } from 'vue';
import { User, Lock, View, Key } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store/auth';
import request from '../utils/request';
import { ElMessage } from 'element-plus';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const mode = ref<'login' | 'setup'>('login');

const loginForm = reactive({
  username: '',
  password: ''
});

const setupForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  inviteCode: ''
});

// 查询初始化状态：未初始化时展示初始化表单
const checkSetupStatus = async () => {
  try {
    const res: any = await request.get('/system/setup-status');
    mode.value = res && res.initialized === false ? 'setup' : 'login';
  } catch (error) {
    console.error('查询初始化状态失败:', error);
    mode.value = 'login';
  }
};

const handleSetup = async () => {
  if (!setupForm.username || !setupForm.password || !setupForm.confirmPassword || !setupForm.inviteCode) {
    return ElMessage.warning('请完整填写初始化信息');
  }
  if (setupForm.password.length < 6) {
    return ElMessage.warning('密码至少 6 位');
  }
  if (setupForm.password !== setupForm.confirmPassword) {
    return ElMessage.warning('两次输入的密码不一致');
  }
  loading.value = true;
  try {
    await request.post('/system/setup', setupForm);
    ElMessage.success('初始化成功，请使用刚设置的账号登录');
    await checkSetupStatus();
  } catch (error) {
    console.error('初始化失败:', error);
  } finally {
    loading.value = false;
  }
};

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
};

onMounted(checkSetupStatus);
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
  animation: login-card-in 0.55s var(--ease-emphasized) both;
}

.login-anim {
  opacity: 0;
  animation: login-rise var(--duration-slow) var(--ease-emphasized) forwards;
}
.login-anim--1 { animation-delay: 120ms; }
.login-anim--2 { animation-delay: 200ms; }
.login-anim--3 { animation-delay: 280ms; }

@keyframes login-card-in {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes login-rise {
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

.setup-tip {
  margin-top: -8px;
  font-size: 13px;
  color: var(--text-secondary);
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
