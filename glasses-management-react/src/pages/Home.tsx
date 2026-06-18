import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input, Button, message } from 'antd';
import { SearchOutlined, ArrowRightOutlined, UserAddOutlined, UnorderedListOutlined, LinkOutlined } from '@ant-design/icons';
import request from '@/utils/request';
import QRCode from 'qrcode';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [lanUrl, setLanUrl] = useState('');
  const qrCanvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const fetchLanInfo = async () => {
      try {
        const res: any = await request.get('/system/lan-info');
        if (res.ip && res.port) {
          const url = `http://${res.ip}:${res.port}`;
          setLanUrl(url);
          if (qrCanvasRef.current) {
            await QRCode.toCanvas(qrCanvasRef.current, url, { width: 160, margin: 2 });
          }
        }
      } catch { /* silent */ }
    };
    fetchLanInfo();
  }, []);

  const clearResults = () => setSearchResults([]);

  const handleSearch = async () => {
    if (!searchQuery) {
      clearResults();
      return;
    }
    try {
      const res: any = await request.get('/customer/page', {
        params: { keyword: searchQuery, current: 1, size: 5 },
      });
      if (res.records && res.records.length > 0) {
        if (res.records.length === 1 && res.records[0].phone === searchQuery) {
          navigate(`/archive/${res.records[0].id}`);
        } else {
          setSearchResults(res.records);
        }
      } else {
        message.warning('未找到对应记录');
        clearResults();
      }
    } catch (e) { /* handled */ }
  };

  const copyLanUrl = async () => {
    try {
      await navigator.clipboard.writeText(lanUrl);
      message.success('链接已复制');
    } catch {
      message.warning('复制失败，请手动复制');
    }
  };

  return (
    <div className="home-container">
      <div className="welcome-section home-anim home-anim--1">
        <h1 className="greeting">工作台</h1>
        <p className="sub-greeting">输入顾客手机号即可快速调出其历史视光与配镜档案</p>
      </div>

      <div className="search-section home-anim home-anim--2">
        <div className="search-shell home-search-shell">
          <div className="search-icon-badge">
            <SearchOutlined />
          </div>
          <Input
            className="search-control home-search-control"
            variant="borderless"
            size="large"
            placeholder="输入顾客手机号或姓名进行搜索..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              if (!e.target.value) clearResults();
            }}
            onPressEnter={handleSearch}
            allowClear
          />
          <Button type="primary" size="large" className="search-submit home-search-submit" onClick={handleSearch}>
            检索档案
          </Button>
        </div>

        {searchResults.length > 0 && (
          <div className="search-results-popper glass-card">
            <div className="results-header">
              <span>搜索结果 ({searchResults.length})</span>
              <Button type="link" size="small" className="close-results-btn" onClick={clearResults}>
                关闭
              </Button>
            </div>
            <div className="results-list">
              {searchResults.map((item) => (
                <div
                  key={item.id}
                  className="result-item"
                  onClick={() => navigate(`/archive/${item.id}`)}
                >
                  <div className="result-info">
                    <span className="r-name">{item.name}</span>
                    <span className="r-phone">{item.phone}</span>
                  </div>
                  <ArrowRightOutlined style={{ color: 'var(--text-muted)' }} />
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="shortcuts-group home-anim home-anim--3">
        <div className="shortcut-card add-customer" onClick={() => navigate('/customer')}>
          <UserAddOutlined style={{ fontSize: 32, color: 'var(--primary-color)' }} />
          <h3>新建顾客</h3>
          <p>录入新到店客户信息</p>
        </div>
        <div className="shortcut-card view-list" onClick={() => navigate('/customer')}>
          <UnorderedListOutlined style={{ fontSize: 32, color: 'var(--primary-color)' }} />
          <h3>所有顾客</h3>
          <p>浏览顾客列表并开单</p>
        </div>
      </div>

      {lanUrl && (
        <div className="lan-card glass-card home-anim home-anim--4">
          <div className="lan-header">
            <LinkOutlined />
            <span>局域网连接</span>
          </div>
          <p className="lan-tip">同一网络下的设备可通过以下地址访问本系统</p>
          <canvas ref={qrCanvasRef} className="lan-qr" />
          <div className="lan-url-row">
            <code className="lan-url">{lanUrl}</code>
            <Button size="small" className="lan-copy-btn" onClick={copyLanUrl}>
              复制链接
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Home;
