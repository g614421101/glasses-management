import React, { useState, useEffect, useRef, Suspense, lazy } from 'react';
import { useNavigate, useLocation, Routes, Route } from 'react-router-dom';
import { Layout, Menu, Button, Dropdown, Drawer, Spin } from 'antd';
import {
  MenuOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  HomeOutlined,
  TeamOutlined,
  BarChartOutlined,
  FolderOutlined,
  DeleteOutlined,
  ExpandOutlined,
  ShrinkOutlined,
} from '@ant-design/icons';
import { useAuth } from '@/hooks/useAuth';
import { useMobile } from '@/hooks/useMobile';
import { FEATURES } from '@/config/features';
import ThemeToggle from '@/components/ThemeToggle';
import PrivateRoute from '@/components/PrivateRoute';

const Home = lazy(() => import('@/pages/Home'));
const Customer = lazy(() => import('@/pages/Customer'));
const Archive = lazy(() => import('@/pages/Archive'));
const Statistics = lazy(() => import('@/pages/Statistics'));
const DataManage = lazy(() => import('@/pages/DataManage'));
const Profile = lazy(() => import('@/pages/Profile'));
const RecycleBin = lazy(() => import('@/pages/RecycleBin'));
const SysUser = lazy(() => import('@/pages/SysUser'));

const { Content } = Layout;

const BasicLayout: React.FC = () => {
  const { username, isAdmin, handleLogout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const isMobile = useMobile();
  const [collapsed, setCollapsed] = useState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);

  const [displayLocation, setDisplayLocation] = useState(location);
  const [animClass, setAnimClass] = useState('');
  const prevPathRef = useRef(location.pathname);

  useEffect(() => {
    if (location.pathname !== prevPathRef.current) {
      prevPathRef.current = location.pathname;
      setAnimClass('fade-exit');
    }
  }, [location.pathname]);

  const handleAnimEnd = () => {
    if (animClass === 'fade-exit') {
      setDisplayLocation(location);
      setAnimClass('fade-enter');
    } else if (animClass === 'fade-enter') {
      setAnimClass('');
    }
  };

  const menuItems = [
    { key: '/', icon: <HomeOutlined />, label: '工作平台' },
    FEATURES.CUSTOMER && { key: '/customer', icon: <TeamOutlined />, label: '顾客管理' },
    FEATURES.STATISTICS && { key: '/stats', icon: <BarChartOutlined />, label: '营收统计' },
    FEATURES.DATA_MANAGE && { key: '/data-manage', icon: <FolderOutlined />, label: '数据管理' },
    FEATURES.PROFILE && { key: '/profile', icon: <UserOutlined />, label: '个人主页' },
    FEATURES.RECYCLE_BIN && isAdmin && { key: '/recycle-bin', icon: <DeleteOutlined />, label: '回收站' },
    FEATURES.SYS_USER && isAdmin && { key: '/sys-user', icon: <SettingOutlined />, label: '账号管理(超管)' },
  ].filter(Boolean) as any[];

  const handleMenuClick = (info: { key: string }) => {
    navigate(info.key);
    if (isMobile) setDrawerVisible(false);
  };

  const userMenuItems = [
    { key: 'profile', label: '个人主页', icon: <UserOutlined /> },
    { key: 'logout', label: '退出登录', icon: <LogoutOutlined />, danger: true },
  ];

  const handleUserMenuClick = (info: { key: string }) => {
    if (info.key === 'profile') navigate('/profile');
    else if (info.key === 'logout') handleLogout();
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <header
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: isMobile ? '10px 14px' : '14px 28px',
          minHeight: isMobile ? 56 : 74,
          height: 'auto',
          background: 'var(--surface-overlay)',
          borderBottom: '1px solid var(--border-color)',
          position: 'sticky',
          top: 0,
          zIndex: 20,
          backdropFilter: 'blur(18px)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          {isMobile && (
            <Button type="text" icon={<MenuOutlined />} onClick={() => setDrawerVisible(true)} style={{ marginRight: 8 }} />
          )}
          <div
            style={{
              width: 44, height: 44, borderRadius: 16,
              background: 'var(--gradient-main)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              cursor: 'pointer', flexShrink: 0,
              boxShadow: '0 14px 28px rgba(37, 99, 235, 0.24)',
            }}
            onClick={() => navigate('/')}
          >
            <span style={{ color: '#fff', fontSize: isMobile ? 18 : 22 }}>👓</span>
          </div>
          {!isMobile ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4, cursor: 'pointer' }} onClick={() => navigate('/')}>
              <span style={{ fontSize: 18, fontWeight: 700, color: 'var(--text-primary)', letterSpacing: '-0.02em', fontFamily: 'Sora, sans-serif' }}>视光档案管理系统</span>
              <span style={{ fontSize: 12, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.18em' }}>Optical Record Management System</span>
            </div>
          ) : (
            <span style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-primary)', cursor: 'pointer' }} onClick={() => navigate('/')}>视光档案</span>
          )}
        </div>

        {!isMobile ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div
              onClick={() => navigate('/profile')}
              style={{
                display: 'inline-flex', alignItems: 'center', gap: 10,
                padding: '10px 14px', borderRadius: 999,
                background: 'var(--surface-muted)',
                border: '1px solid var(--border-color)',
                boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.18)',
                cursor: 'pointer',
              }}
            >
              <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>当前账号</span>
              <strong style={{ fontSize: 14, color: 'var(--text-primary)' }}>{username}</strong>
            </div>
            <ThemeToggle />
            <Button
              onClick={handleLogout}
              style={{
                height: 36, padding: '0 16px',
                borderRadius: 999,
                border: '1px solid var(--border-color)',
                background: 'var(--surface-muted)',
                color: 'var(--primary-color)',
                fontWeight: 800,
              }}
            >
              退出登录
            </Button>
          </div>
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <ThemeToggle />
            <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} trigger={['click']} placement="bottomRight">
              <Button shape="circle" icon={<UserOutlined />} />
            </Dropdown>
          </div>
        )}
      </header>

      {/* Body */}
      <div style={{ flex: 1, display: 'flex', overflow: 'visible', background: 'transparent', minHeight: 0 }}>
        {/* Desktop Sidebar */}
        {!isMobile && (
          <div
            style={{
              width: collapsed ? 76 : 220,
              background: 'var(--surface-color)',
              border: '1px solid var(--border-color)',
              zIndex: 10,
              position: 'sticky',
              top: 86,
              height: 'fit-content',
              margin: '24px 0 24px 24px',
              padding: collapsed ? '20px 6px 14px' : '20px 12px 14px',
              borderRadius: 26,
              backdropFilter: 'blur(18px)',
              boxShadow: '0 18px 35px rgba(15, 23, 42, 0.07)',
              transition: 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1), padding 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              overflow: 'hidden',
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            {!collapsed && (
              <div style={{ padding: '0 14px 12px', fontSize: 12, fontWeight: 800, color: 'var(--text-muted)', letterSpacing: '0.14em', textTransform: 'uppercase', whiteSpace: 'nowrap', overflow: 'hidden' }}>
                导航菜单
              </div>
            )}
            <div style={{ flex: 1 }}>
              <Menu
                mode="inline"
                inlineCollapsed={collapsed}
                selectedKeys={[location.pathname]}
                items={menuItems}
                onClick={handleMenuClick}
                style={{ background: 'transparent', borderRight: 'none' }}
              />
            </div>
            <div style={{ marginTop: 20, paddingTop: 12, borderTop: '1px solid var(--border-color)', display: 'flex', justifyContent: 'center' }}>
              <Button
                type="text"
                onClick={() => setCollapsed(!collapsed)}
                style={{
                  width: '100%', height: 40,
                  display: 'flex', alignItems: 'center',
                  justifyContent: collapsed ? 'center' : 'flex-start',
                  gap: 12,
                  padding: collapsed ? 0 : '0 16px',
                  color: 'var(--text-secondary)',
                  borderRadius: 12,
                }}
              >
                {collapsed ? <ExpandOutlined /> : <><ShrinkOutlined /><span>收起菜单</span></>}
              </Button>
            </div>
          </div>
        )}

        {/* Mobile Drawer */}
        <Drawer
          placement="left"
          open={drawerVisible}
          onClose={() => setDrawerVisible(false)}
          width={250}
          styles={{ body: { padding: 0 } }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '20px 20px 16px', borderBottom: '1px solid var(--border-color)' }}>
            <div style={{ width: 44, height: 44, borderRadius: 16, background: 'var(--gradient-main)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <span style={{ color: '#fff', fontSize: 20 }}>👓</span>
            </div>
            <span style={{ fontSize: 18, fontWeight: 700, color: 'var(--text-primary)' }}>视光档案</span>
          </div>
          <div style={{ padding: '12px 20px', fontSize: 12, fontWeight: 800, color: 'var(--text-muted)', letterSpacing: '0.14em', textTransform: 'uppercase' }}>导航菜单</div>
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={handleMenuClick}
            style={{ background: 'transparent', borderRight: 'none' }}
          />
        </Drawer>

        {/* Main Content */}
        <Content className="main-content">
          <div
            key={displayLocation.pathname}
            className={`transition-wrapper ${animClass}`}
            onAnimationEnd={handleAnimEnd}
          >
            <Suspense fallback={<div className="route-fallback"><Spin /></div>}>
              <Routes location={displayLocation}>
                <Route index element={<Home />} />
                {FEATURES.CUSTOMER && <Route path="customer" element={<Customer />} />}
                <Route path="archive/:id" element={<Archive />} />
                {FEATURES.STATISTICS && <Route path="stats" element={<Statistics />} />}
                {FEATURES.DATA_MANAGE && <Route path="data-manage" element={<DataManage />} />}
                {FEATURES.PROFILE && <Route path="profile" element={<Profile />} />}
                {FEATURES.RECYCLE_BIN && (
                  <Route path="recycle-bin" element={<PrivateRoute adminOnly><RecycleBin /></PrivateRoute>} />
                )}
                {FEATURES.SYS_USER && (
                  <Route path="sys-user" element={<PrivateRoute adminOnly><SysUser /></PrivateRoute>} />
                )}
              </Routes>
            </Suspense>
          </div>
        </Content>
      </div>
    </div>
  );
};

export default BasicLayout;
