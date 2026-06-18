import React, { useState, useEffect } from 'react';
import { Button, Input, Form, Tag, message, Space } from 'antd';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '@/store';
import { verifyToken } from '@/features/auth/authSlice';
import { useAuth } from '@/hooks/useAuth';
import { authAPI } from '@/features/auth/authAPI';

const Profile: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const auth = useAuth();
  const [editing, setEditing] = useState(false);
  const [profileLoading, setProfileLoading] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [profileForm] = Form.useForm();
  const [pwdForm] = Form.useForm();

  const displayName = auth.realName || auth.username || '-';
  const initial = (displayName || '用').charAt(0);
  const roleLabel = auth.role === 'admin' ? '超级管理员' : '商户账号';

  useEffect(() => {
    profileForm.setFieldsValue({
      username: auth.username || '',
      phone: auth.phone || '',
      realName: auth.realName || '',
    });
  }, [auth.username, auth.phone, auth.realName, profileForm]);

  const startEdit = () => {
    profileForm.setFieldsValue({
      username: auth.username || '',
      phone: auth.phone || '',
      realName: auth.realName || '',
    });
    setEditing(true);
  };

  const cancelEdit = () => {
    profileForm.setFieldsValue({
      username: auth.username || '',
      phone: auth.phone || '',
      realName: auth.realName || '',
    });
    setEditing(false);
  };

  const saveProfile = async () => {
    try {
      const values = await profileForm.validateFields();
      setProfileLoading(true);
      try {
        await authAPI.updateProfile({
          realName: values.realName,
          phone: values.phone,
        });
        message.success('个人资料已更新');
        await dispatch(verifyToken());
        setEditing(false);
      } finally {
        setProfileLoading(false);
      }
    } catch {
      // validation handled
    }
  };

  const changePassword = async () => {
    try {
      const values = await pwdForm.validateFields();
      setPasswordLoading(true);
      try {
        await authAPI.changePassword({
          oldPassword: values.oldPassword,
          newPassword: values.newPassword,
          confirmPassword: values.confirmPassword,
        });
        message.success('密码修改成功');
        pwdForm.resetFields();
        await dispatch(verifyToken());
      } finally {
        setPasswordLoading(false);
      }
    } catch {
      // validation handled
    }
  };

  return (
    <div className="page-shell profile-page">
      <section
        className="page-hero glass-card profile-hero"
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 18 }}
      >
        <div>
          <h1 className="page-heading" style={{ margin: 0 }}>个人主页</h1>
          <p className="page-title-en">ACCOUNT PROFILE</p>
        </div>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <Button onClick={startEdit}>{editing ? '继续编辑' : '编辑资料'}</Button>
        </div>
      </section>

      <div className="profile-grid">
        {/* Avatar Card */}
        <section className="profile-card glass-card identity-card" style={{ textAlign: 'center' }}>
          <div className="profile-avatar" style={{ margin: '0 auto 20px' }}>
            {initial}
          </div>
          <h2>{displayName}</h2>
          <p>{auth.username || '-'}</p>
          <div className="profile-status" style={{ justifyContent: 'center' }}>
            <Tag color={auth.role === 'admin' ? 'red' : 'green'}>{roleLabel}</Tag>
            {auth.disabled ? <Tag color="warning">已封禁</Tag> : <Tag color="success">正常</Tag>}
          </div>
        </section>

        {/* Account Info */}
        <section className="profile-card glass-card profile-detail">
          <h3>账号信息</h3>
          <p>维护你的登录用户名、手机号和展示名称。</p>

          {!editing ? (
            <div className="detail-list">
              {[
                { label: '显示名称', value: auth.realName || '-' },
                { label: '用户名', value: auth.username || '-' },
                { label: '手机号', value: auth.phone || '-' },
                { label: '角色', value: roleLabel },
                { label: '注册时间', value: auth.createTime || '-' },
              ].map((item) => (
                <div key={item.label}>
                  <span>{item.label}</span>
                  <strong>{item.value}</strong>
                </div>
              ))}
            </div>
          ) : (
            <Form form={profileForm} layout="vertical" style={{ maxWidth: 680 }}>
              <Form.Item label="显示名称" name="realName">
                <Input maxLength={50} placeholder="用于页面展示的名称" showCount />
              </Form.Item>
              <Form.Item label="用户名" name="username">
                <Input maxLength={30} placeholder="3-30 位，可用于登录" />
              </Form.Item>
              <Form.Item label="手机号" name="phone">
                <Input maxLength={11} placeholder="请输入手机号，也可用于登录" />
              </Form.Item>
              <Space>
                <Button onClick={cancelEdit}>取消</Button>
                <Button type="primary" loading={profileLoading} onClick={saveProfile}>保存资料</Button>
              </Space>
            </Form>
          )}
        </section>

        {/* Password */}
        <section className="profile-card glass-card password-card">
          <h3>修改密码</h3>
          <p className="card-subtitle">
            建议定期更换密码，避免继续使用默认密码。
          </p>
          <Form form={pwdForm} layout="vertical" style={{ maxWidth: 680 }}>
            <Form.Item label="当前密码" name="oldPassword">
              <Input.Password placeholder="请输入当前密码" />
            </Form.Item>
            <Form.Item label="新密码" name="newPassword">
              <Input.Password placeholder="至少 6 位" />
            </Form.Item>
            <Form.Item label="确认新密码" name="confirmPassword">
              <Input.Password placeholder="请再次输入新密码" />
            </Form.Item>
            <Button type="primary" loading={passwordLoading} onClick={changePassword} style={{ minWidth: 160 }}>
              保存新密码
            </Button>
          </Form>
        </section>
      </div>
    </div>
  );
};

export default Profile;
