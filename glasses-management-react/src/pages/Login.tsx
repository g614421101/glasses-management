import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Form, Input, Button, message } from 'antd';
import { UserOutlined, LockOutlined, EyeOutlined } from '@ant-design/icons';
import { useAuth } from '@/hooks/useAuth';
import { login } from '@/features/auth/authSlice';
import { useMobile } from '@/hooks/useMobile';
import ThemeToggle from '@/components/ThemeToggle';
import LoginCharacters from '@/components/LoginCharacters';

const Login: React.FC = () => {
  const { handleLogin, loading } = useAuth();
  const isMobile = useMobile(1024);
  const [form] = Form.useForm();
  const [passwordValue, setPasswordValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const onFinish = async (values: { username: string; password: string }) => {
    if (!values.username || !values.password) {
      message.warning('请输入账号和密码');
      return;
    }
    setErrorMsg('');
    const result = await handleLogin(values);
    if (login.rejected.match(result)) {
      setErrorMsg('账号或密码错误，请重试');
    }
  };

  // 移动端：原版居中登录卡片
  if (isMobile) {
    return (
      <div className="login-container">
        <div className="theme-toggle-fixed">
          <ThemeToggle />
        </div>
        <div className="login-box glass-card login-anim login-anim--1">
          <div className="sys-title login-anim login-anim--2">
            <EyeOutlined style={{ fontSize: 28, color: 'var(--primary-color)' }} />
            <span>视光档案管理系统</span>
          </div>
          <p className="sys-subtitle login-anim login-anim--3">Optical Record Management System</p>

          <Form form={form} onFinish={onFinish} size="large" className="login-form login-anim login-anim--4">
            <Form.Item name="username" rules={[{ required: true, message: '请输入账号' }]}>
              <Input prefix={<UserOutlined />} placeholder="请输入账号" />
            </Form.Item>
            <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" />
            </Form.Item>
            <div className="login-link">
              <Link to="/register">邀请码商户注册</Link>
            </div>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block className="login-btn">
                登录系统
              </Button>
            </Form.Item>
          </Form>
        </div>
      </div>
    );
  }

  // 桌面端：左右分屏 + 动画角色
  return (
    <div className="login-page-split">
      <div className="login-left-panel">
        <div className="login-left-brand">
          <div className="brand-icon">👁️</div>
          <span>视光档案管理系统</span>
        </div>

        <div className="login-characters-anchor">
          <LoginCharacters
            hasPassword={passwordValue.length > 0}
            passwordVisible={false}
            isTyping={isTyping}
          />
        </div>
      </div>

      <div className="login-right-panel">
        <div className="theme-toggle-fixed">
          <ThemeToggle />
        </div>

        <div className="login-form-wrapper">
          <div className="login-form-header">
            <h1>欢迎回来！</h1>
            <p>请输入您的账号信息</p>
          </div>

          {errorMsg && (
            <div className="login-error-msg">{errorMsg}</div>
          )}

          <Form
            form={form}
            onFinish={onFinish}
            layout="vertical"
            requiredMark={false}
            onFocus={() => setIsTyping(true)}
            onBlur={() => setIsTyping(false)}
          >
            <div className="login-form-section">
              <label>账号</label>
              <Form.Item name="username" rules={[{ required: true, message: '请输入账号' }]} noStyle>
                <Input
                  prefix={<UserOutlined style={{ color: 'var(--text-muted)' }} />}
                  placeholder="请输入账号"
                  autoComplete="off"
                />
              </Form.Item>
            </div>

            <div className="login-form-section">
              <label>密码</label>
              <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]} noStyle>
                <Input.Password
                  prefix={<LockOutlined style={{ color: 'var(--text-muted)' }} />}
                  placeholder="请输入密码"
                  onChange={(e) => setPasswordValue(e.target.value)}
                />
              </Form.Item>
            </div>

            <div className="login-form-actions">
              <Form.Item noStyle>
                <Button type="primary" htmlType="submit" loading={loading}>
                  登录系统
                </Button>
              </Form.Item>
            </div>
          </Form>

          <div className="login-form-footer">
            还没有账号？ <Link to="/register">邀请码商户注册</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
