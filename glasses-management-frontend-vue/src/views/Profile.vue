<template>
  <div class="page-shell profile-page">
    <section class="page-hero glass-card profile-hero">
      <div>
        <h1 class="page-heading">个人主页</h1>
        <p class="page-title-en">ACCOUNT PROFILE</p>
      </div>
      <div class="hero-profile-actions">
        <el-tag v-if="authStore.mustChangePassword" type="warning" effect="dark">建议修改默认密码</el-tag>
        <el-button class="action-pill" @click="startEdit">
          {{ editing ? '继续编辑' : '编辑资料' }}
        </el-button>
      </div>
    </section>

    <div class="profile-grid">
      <section class="profile-card glass-card identity-card">
        <div class="profile-avatar">{{ initial }}</div>
        <h2>{{ displayName }}</h2>
        <p>{{ authStore.username || '-' }}</p>
        <div class="profile-status">
          <el-tag :type="authStore.role === 'admin' ? 'danger' : 'success'">{{ roleLabel }}</el-tag>
          <el-tag v-if="authStore.disabled" type="warning">已封禁</el-tag>
          <el-tag v-else type="success">正常</el-tag>
        </div>
      </section>

      <section class="profile-card glass-card profile-detail">
        <div class="card-title-row">
          <div>
            <h3>账号信息</h3>
            <p>维护你的登录用户名、手机号和展示名称。</p>
          </div>
          <el-button v-if="!editing" class="action-pill" @click="startEdit">编辑资料</el-button>
        </div>

        <div v-if="!editing" class="detail-list">
          <div><span>显示名称</span><strong>{{ authStore.realName || '-' }}</strong></div>
          <div><span>用户名</span><strong>{{ authStore.username || '-' }}</strong></div>
          <div><span>手机号</span><strong>{{ authStore.phone || '-' }}</strong></div>
          <div><span>角色</span><strong>{{ roleLabel }}</strong></div>
          <div><span>注册时间</span><strong>{{ authStore.createTime || '-' }}</strong></div>
        </div>

        <el-form v-else :model="profileForm" label-position="top" class="profile-form">
          <el-form-item label="显示名称">
            <el-input v-model="profileForm.realName" maxlength="50" show-word-limit placeholder="用于页面展示的名称" />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" maxlength="30" placeholder="3-30 位，可用于登录" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" maxlength="11" placeholder="请输入手机号，也可用于登录" />
          </el-form-item>
          <div class="form-actions">
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" :loading="profileLoading" @click="saveProfile">保存资料</el-button>
          </div>
        </el-form>
      </section>

      <section class="profile-card glass-card password-card">
        <h3>修改密码</h3>
        <p class="card-subtitle">建议定期更换密码，避免继续使用默认密码。</p>
        <el-form :model="pwdForm" label-position="top">
          <el-form-item label="当前密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-button type="primary" :loading="passwordLoading" @click="changePassword">保存新密码</el-button>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../store/auth';
import request from '../utils/request';

const authStore = useAuthStore();
const profileLoading = ref(false);
const passwordLoading = ref(false);
const editing = ref(false);

const profileForm = reactive({
  username: '',
  phone: '',
  realName: ''
});

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const displayName = computed(() => authStore.realName || authStore.username || '-');
const initial = computed(() => (displayName.value || '用').charAt(0));
const roleLabel = computed(() => authStore.role === 'admin' ? '超级管理员' : '商户账号');

const fillProfileForm = () => {
  profileForm.username = authStore.username || '';
  profileForm.phone = authStore.phone || '';
  profileForm.realName = authStore.realName || '';
};

const startEdit = () => {
  fillProfileForm();
  editing.value = true;
};

const cancelEdit = () => {
  fillProfileForm();
  editing.value = false;
};

const saveProfile = async () => {
  const username = profileForm.username.trim();
  const phone = profileForm.phone.trim();
  const realName = profileForm.realName.trim();
  if (!username || !phone) {
    ElMessage.warning('请填写用户名和手机号');
    return;
  }
  if (username.length < 3 || username.length > 30) {
    ElMessage.warning('用户名长度需要为 3-30 位');
    return;
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('请输入正确的手机号');
    return;
  }

  profileLoading.value = true;
  try {
    await request.put('/auth/profile', { username, phone, realName });
    await authStore.verifyToken();
    fillProfileForm();
    editing.value = false;
    ElMessage.success('个人资料已更新');
  } finally {
    profileLoading.value = false;
  }
};

const changePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请完整填写密码信息');
    return;
  }
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少 6 位');
    return;
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致');
    return;
  }
  passwordLoading.value = true;
  try {
    await request.post('/auth/change-password', pwdForm);
    ElMessage.success('密码修改成功');
    pwdForm.oldPassword = '';
    pwdForm.newPassword = '';
    pwdForm.confirmPassword = '';
    await authStore.verifyToken();
  } finally {
    passwordLoading.value = false;
  }
};

fillProfileForm();
</script>

<style scoped>
.profile-page {
  padding-top: 6px;
}

.profile-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
}

.hero-profile-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.profile-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 22px;
}

.profile-card {
  padding: 26px;
  position: relative;
  overflow: hidden;
}

.profile-card h2,
.profile-card h3 {
  margin: 0 0 14px;
}

.profile-card > p,
.card-subtitle,
.card-title-row p {
  color: var(--text-secondary);
  margin: 0 0 18px;
}

.profile-avatar {
  width: 86px;
  height: 86px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 34px;
  font-weight: 900;
  background: var(--gradient-main);
  box-shadow: var(--shadow-card);
  margin-bottom: 20px;
}

.profile-status,
.form-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.profile-detail,
.password-card {
  grid-column: 2;
}

.card-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.detail-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.detail-list div {
  padding: 16px;
  border-radius: 18px;
  background: var(--surface-muted);
  border: 1px solid var(--border-color);
}

.detail-list span {
  display: block;
  margin-bottom: 8px;
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 800;
}

.detail-list strong {
  color: var(--text-primary);
  overflow-wrap: anywhere;
}

.profile-form {
  max-width: 680px;
}

.password-card .el-button {
  min-width: 160px;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-detail,
  .password-card {
    grid-column: auto;
  }
}

@media (max-width: 640px) {
  .profile-hero,
  .card-title-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-profile-actions,
  .hero-profile-actions > * {
    width: 100%;
  }

  .detail-list {
    grid-template-columns: 1fr;
  }
}
</style>
