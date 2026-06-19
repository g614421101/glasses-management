import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Form, Input, Button, message } from 'antd';
import { UserOutlined, LockOutlined, PhoneOutlined, KeyOutlined } from '@ant-design/icons';
import request from '@/utils/request';
import ThemeToggle from '@/components/ThemeToggle';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await request.post('/auth/register', values);
      message.success('注册成功，请登录');
      navigate('/login');
    } catch (error) {
      console.error('注册失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="theme-toggle-fixed">
        <ThemeToggle />
      </div>
      <div className="login-box glass-card login-anim login-anim--1">
        <div className="sys-title login-anim login-anim--2">
          <KeyOutlined style={{ fontSize: 28, color: 'var(--primary-color)' }} />
          <span>商户注册</span>
        </div>
        <p className="sys-subtitle login-anim login-anim--3">Merchant Registration</p>

        <Form form={form} onFinish={onFinish} size="large" className="login-form login-anim login-anim--4">
          <Form.Item name="inviteCode" rules={[{ required: true, message: '请输入邀请码' }]}>
            <Input prefix={<KeyOutlined />} placeholder="请输入邀请码" />
          </Form.Item>
          <Form.Item name="username" rules={[{ required: true, message: '请输入账号' }]}>
            <Input prefix={<UserOutlined />} placeholder="请输入账号" />
          </Form.Item>
          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" />
          </Form.Item>
          <Form.Item name="phone" rules={[{ required: true, message: '请输入手机号' }]}>
            <Input prefix={<PhoneOutlined />} placeholder="请输入手机号" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block className="login-btn">
              注册
            </Button>
          </Form.Item>
        </Form>

        <div className="register-footer">
          <span>已有账号？</span>
          <Link to="/login">立即登录</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
