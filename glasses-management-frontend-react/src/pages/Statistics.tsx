import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Card, Button, Table, DatePicker, Switch, Space, message } from 'antd';
import { DownloadOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { statsAPI } from '@/api/stats';
import { useNavigate } from 'react-router-dom';
import { useCountUp } from '@/hooks/useCountUp';

const { RangePicker } = DatePicker;

const Statistics: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [showAll, setShowAll] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageParams, setPageParams] = useState({ current: 1, size: 10 });
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
    dayjs().startOf('month'),
    dayjs().endOf('month'),
  ]);
  const [statsData, setStatsData] = useState<{ totalRevenue: number; orderCount: number; records: any[] }>({
    totalRevenue: 0,
    orderCount: 0,
    records: [],
  });

  // Count-up animation (port of Vue useCountUp)
  const displayRevenue = useCountUp(statsData.totalRevenue || 0);
  const displayOrders = useCountUp(statsData.orderCount || 0);
  const avgValue = statsData.orderCount > 0 ? statsData.totalRevenue / statsData.orderCount : 0;
  const displayAvg = useCountUp(avgValue);

  // Pulse effect on data refresh
  const [pulsing, setPulsing] = useState(false);
  const pulseTimer = useRef<ReturnType<typeof setTimeout>>();
  const triggerPulse = () => {
    setPulsing(false);
    requestAnimationFrame(() => {
      setPulsing(true);
      if (pulseTimer.current) clearTimeout(pulseTimer.current);
      pulseTimer.current = setTimeout(() => setPulsing(false), 400);
    });
  };

  const fetchStats = useCallback(async () => {
    setLoading(true);
    try {
      const params: any = { ...pageParams, showAll };
      if (!showAll && dateRange[0] && dateRange[1]) {
        params.startDate = dateRange[0].format('YYYY-MM-DD');
        params.endDate = dateRange[1].format('YYYY-MM-DD');
      }
      const res = await statsAPI.getStats(params);
      setStatsData({
        totalRevenue: res.totalRevenue || 0,
        orderCount: res.orderCount || 0,
        records: res.records?.records || [],
      });
      setTotal(res.records?.total || 0);
      triggerPulse();
    } catch {
      message.error('获取统计数据失败');
    } finally {
      setLoading(false);
    }
  }, [pageParams, showAll, dateRange]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const handleDateChange = () => {
    setPageParams(p => ({ ...p, current: 1 }));
  };

  const handleShowAllChange = (val: boolean) => {
    setShowAll(val);
    setPageParams(p => ({ ...p, current: 1 }));
  };

  const pulseCls = pulsing ? ' stat-value--pulse' : '';

  const columns = [
    { title: '日期', dataIndex: 'salesDate' },
    { title: '单号', dataIndex: 'recordNo' },
    { title: '顾客姓名', dataIndex: 'customerName' },
    {
      title: '商品项目',
      render: (_: any, row: any) => (
        <Space size={4} wrap>
          {row.frameBrand && <span style={{ background: 'var(--primary-soft)', borderRadius: 999, padding: '2px 8px', fontSize: 12, fontWeight: 700 }}>{row.frameBrand} {row.frameModel}</span>}
          {row.lensBrand && <span style={{ background: 'var(--surface-muted)', borderRadius: 999, padding: '2px 8px', fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)' }}>{row.lensBrand}</span>}
        </Space>
      ),
    },
    {
      title: '金额',
      dataIndex: 'totalAmount',
      render: (v: any) => <span style={{ color: 'var(--primary-color)', fontWeight: 800 }}>￥{v}</span>,
    },
    {
      title: '操作',
      width: 120,
      render: (_: any, row: any) => (
        <Button size="small" onClick={() => navigate(`/archive/${row.customerId}`)}>查看详情</Button>
      ),
    },
  ];

  const handleExport = async () => {
    try {
      await statsAPI.exportRevenue(
        showAll,
        showAll ? undefined : dateRange[0].format('YYYY-MM-DD'),
        showAll ? undefined : dateRange[1].format('YYYY-MM-DD')
      );
    } catch {
      // handled by request interceptor
    }
  };

  return (
    <div className="page-shell">
      <section
        className="page-hero glass-card"
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}
      >
        <div>
          <h1 className="page-heading" style={{ margin: 0 }}>营收统计</h1>
          <p className="page-title-en">Revenue Statistics</p>
        </div>
        <Space wrap size={12}>
          <Switch
            checked={showAll}
            onChange={handleShowAllChange}
            checkedChildren="展示所有记录"
            unCheckedChildren="展示所有记录"
          />
          <RangePicker
            value={dateRange}
            onChange={(dates) => {
              if (dates?.[0] && dates?.[1]) {
                setDateRange([dates[0], dates[1]]);
                handleDateChange();
              }
            }}
            disabled={showAll}
          />
          <Button type="primary" icon={<DownloadOutlined />} onClick={handleExport}>导出流水</Button>
        </Space>
      </section>

      <div className="summary-grid stagger-children" style={{ margin: '20px 0' }}>
        <Card className="glass-card" style={{ padding: 8 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 18 }}>
            <div className="stat-icon stat-icon--revenue">¥</div>
            <div>
              <p style={{ margin: '0 0 6px', color: 'var(--text-secondary)', fontSize: 14 }}>累计营收 (元)</p>
              <h3 className={`stat-value${pulseCls}`}>￥{displayRevenue.toFixed(2)}</h3>
            </div>
          </div>
        </Card>
        <Card className="glass-card" style={{ padding: 8 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 18 }}>
            <div className="stat-icon stat-icon--orders">#</div>
            <div>
              <p style={{ margin: '0 0 6px', color: 'var(--text-secondary)', fontSize: 14 }}>订单总量 (笔)</p>
              <h3 className={`stat-value${pulseCls}`}>{Math.round(displayOrders)}</h3>
            </div>
          </div>
        </Card>
        <Card className="glass-card" style={{ padding: 8 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 18 }}>
            <div className="stat-icon stat-icon--average">∑</div>
            <div>
              <p style={{ margin: '0 0 6px', color: 'var(--text-secondary)', fontSize: 14 }}>平均客单价 (元)</p>
              <h3 className={`stat-value${pulseCls}`}>￥{displayAvg.toFixed(2)}</h3>
            </div>
          </div>
        </Card>
      </div>

      <Card
        className="glass-card"
        title={<h3 style={{ margin: 0 }}>收支流水明细</h3>}
      >
        <Table
          rowKey="id"
          dataSource={statsData.records}
          columns={columns}
          loading={loading}
          pagination={{
            current: pageParams.current,
            pageSize: pageParams.size,
            total,
            showTotal: (t) => `共 ${t} 条`,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50', '100'],
            onChange: (c, s) => setPageParams({ current: c, size: s }),
          }}
          scroll={{ x: 880 }}
        />
      </Card>
    </div>
  );
};

export default Statistics;