import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Form, Input, Button, message } from 'antd';
import { UserOutlined, LockOutlined, EyeOutlined, KeyOutlined } from '@ant-design/icons';
import { useAuth } from '@/hooks/useAuth';
import { login } from '@/features/auth/authSlice';
import { useMobile } from '@/hooks/useMobile';
import { systemAPI, SetupRequest } from '@/api/system';
import ThemeToggle from '@/components/ThemeToggle';
import LoginCharacters from './components/LoginCharacters';
import './index.css';

const Login: React.FC = () => {
  const { handleLogin, loading } = useAuth();
  const isMobile = useMobile(1024);
  const [form] = Form.useForm();
  const [setupForm] = Form.useForm();
  const [passwordValue, setPasswordValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  // 未初始化时展示初始化表单（首次使用引导创建管理员）
  const [setupMode, setSetupMode] = useState(false);
  const [setupLoading, setSetupLoading] = useState(false);

  useEffect(() => {
    systemAPI
      .setupStatus()
      .then((res) => {
        if (res && res.initialized === false) {
          setSetupMode(true);
        }
      })
      .catch(() => {
        // 查询失败保持登录表单，错误已由拦截器提示
      });
  }, []);

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

  const onFinishSetup = async (values: SetupRequest) => {
    if (values.password && values.password.length < 6) {
      message.warning('密码至少 6 位');
      return;
    }
    if (values.password !== values.confirmPassword) {
      message.warning('两次输入的密码不一致');
      return;
    }
    setSetupLoading(true);
    try {
      await systemAPI.setup(values);
      message.success('初始化成功，请使用刚设置的账号登录');
      const status = await systemAPI.setupStatus();
      setSetupMode(!(status && status.initialized));
      setupForm.resetFields();
    } catch (error: any) {
      message.error(error?.message || '初始化失败');
    } finally {
      setSetupLoading(false);
    }
  };

  const renderSetupForm = () => (
    <Form form={setupForm} onFinish={onFinishSetup} size="large" className="login-form login-anim login-anim--4">
      <Form.Item name="username" rules={[{ required: true, message: '请设置管理员账号' }]}>
        <Input prefix={<UserOutlined />} placeholder="设置管理员账号（3-30 位）" autoComplete="off" />
      </Form.Item>
      <Form.Item name="password" rules={[{ required: true, message: '请设置登录密码' }]}>
        <Input.Password prefix={<LockOutlined />} placeholder="设置登录密码（至少 6 位）" />
      </Form.Item>
      <Form.Item name="confirmPassword" rules={[{ required: true, message: '请再次输入密码' }]}>
        <Input.Password prefix={<LockOutlined />} placeholder="再次输入密码" />
      </Form.Item>
      <Form.Item name="inviteCode" rules={[{ required: true, message: '请输入初始化邀请码' }]}>
        <Input prefix={<KeyOutlined />} placeholder="初始化邀请码（由部署者提供）" autoComplete="off" />
      </Form.Item>
      <Form.Item>
        <Button type="primary" htmlType="submit" loading={setupLoading} block className="login-btn">
          初始化系统
        </Button>
      </Form.Item>
      <div className="login-link">初始化只需一次，完成后请使用刚设置的账号登录。</div>
    </Form>
  );

  // 移动端：原版居中卡片
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
          <p className="sys-subtitle login-anim login-anim--3">
            {setupMode ? '首次使用 · 系统初始化' : 'Optical Record Management System'}
          </p>

          {setupMode ? (
            renderSetupForm()
          ) : (
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
          )}
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
            <h1>{setupMode ? '首次使用' : '欢迎回来！'}</h1>
            <p>{setupMode ? '请先完成系统初始化，创建管理员账号' : '请输入您的账号信息'}</p>
          </div>

          {setupMode ? (
            <Form
              form={setupForm}
              onFinish={onFinishSetup}
              layout="vertical"
              requiredMark={false}
            >
              <div className="login-form-section">
                <label>管理员账号</label>
                <Form.Item name="username" rules={[{ required: true, message: '请设置管理员账号' }]} noStyle>
                  <Input
                    prefix={<UserOutlined style={{ color: 'var(--text-muted)' }} />}
                    placeholder="3-30 位账号"
                    autoComplete="off"
                  />
                </Form.Item>
              </div>

              <div className="login-form-section">
                <label>登录密码</label>
                <Form.Item name="password" rules={[{ required: true, message: '请设置登录密码' }]} noStyle>
                  <Input.Password
                    prefix={<LockOutlined style={{ color: 'var(--text-muted)' }} />}
                    placeholder="至少 6 位"
                  />
                </Form.Item>
              </div>

              <div className="login-form-section">
                <label>确认密码</label>
                <Form.Item name="confirmPassword" rules={[{ required: true, message: '请再次输入密码' }]} noStyle>
                  <Input.Password
                    prefix={<LockOutlined style={{ color: 'var(--text-muted)' }} />}
                    placeholder="再次输入密码"
                  />
                </Form.Item>
              </div>

              <div className="login-form-section">
                <label>初始化邀请码</label>
                <Form.Item name="inviteCode" rules={[{ required: true, message: '请输入初始化邀请码' }]} noStyle>
                  <Input
                    prefix={<KeyOutlined style={{ color: 'var(--text-muted)' }} />}
                    placeholder="由部署者提供"
                    autoComplete="off"
                  />
                </Form.Item>
              </div>

              <div className="login-form-actions">
                <Form.Item noStyle>
                  <Button type="primary" htmlType="submit" loading={setupLoading}>
                    初始化系统
                  </Button>
                </Form.Item>
              </div>
            </Form>
          ) : (
            <>
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
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Login;
