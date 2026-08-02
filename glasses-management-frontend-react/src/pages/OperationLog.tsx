import React, { useState, useEffect, useCallback } from 'react';
import { Table, Button, Input, Select, DatePicker, Tag, Pagination, Modal, message } from 'antd';
import { ReloadOutlined, SearchOutlined, DeleteOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { operationLogAPI, OperationLog } from '@/api/operationLog';
import { useMobile } from '@/hooks/useMobile';
import { useAuth } from '@/hooks/useAuth';

const { RangePicker } = DatePicker;

const actionColor = (action: string) => {
  const map: Record<string, string> = { ADD: 'blue', UPDATE: 'orange', DELETE: 'red', OTHER: 'default' };
  return map[action] || 'default';
};

const actionText = (action: string) => {
  const map: Record<string, string> = { ADD: '新增', UPDATE: '修改', DELETE: '删除', OTHER: '其他' };
  return map[action] || action || '-';
};

const methodColor = (method: string) => {
  const map: Record<string, string> = { GET: 'green', POST: 'blue', PUT: 'orange', PATCH: 'orange', DELETE: 'red' };
  return map[(method || 'GET').toUpperCase()] || 'default';
};

const OperationLogPage: React.FC = () => {
  const isMobile = useMobile();
  const { isAdmin } = useAuth();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<OperationLog[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [operatorName, setOperatorName] = useState('');
  const [action, setAction] = useState<string | undefined>(undefined);
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const params: any = { current, size: pageSize };
      if (operatorName) params.operatorName = operatorName.trim();
      if (action) params.action = action;
      if (dateRange) {
        params.startTime = dateRange[0].format('YYYY-MM-DD') + ' 00:00:00';
        params.endTime = dateRange[1].format('YYYY-MM-DD') + ' 23:59:59';
      }
      const res = await operationLogAPI.getPage(params);
      setData(res.records);
      setTotal(res.total);
    } catch (e) { /* handled */ } finally {
      setLoading(false);
    }
  }, [operatorName, action, dateRange, current, pageSize]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleSearch = () => { setCurrent(1); };

  const resetFilters = () => {
    setOperatorName('');
    setAction(undefined);
    setDateRange(null);
    setCurrent(1);
  };

  /** 手动清理近期操作日志（仅 admin，天数由后端配置决定，更早的历史记录保留） */
  const handleCleanup = () => {
    Modal.confirm({
      title: '清理操作日志',
      content: '将永久删除最近保留期内的操作日志，更早的历史记录将保留，此操作不可恢复，确认继续吗？',
      okText: '确认清理',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => operationLogAPI.cleanup().then(async (res) => {
        await loadData();
        message.success(`已清理最近 ${res.days} 天内的 ${res.count} 条日志`);
      }),
    });
  };

  const columns = [
    {
      title: '操作内容', dataIndex: 'description', key: 'description', minWidth: 240,
      render: (v: string) => v || '操作记录',
    },
    { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
    { title: '操作人', dataIndex: 'operatorName', key: 'operatorName', width: 110, render: (v: string) => v || 'anonymous' },
    { title: '模块', dataIndex: 'module', key: 'module', width: 110 },
    {
      title: '操作', dataIndex: 'action', key: 'action', width: 90, align: 'center' as const,
      render: (v: string) => <Tag color={actionColor(v)}>{actionText(v)}</Tag>,
    },
    {
      title: '结果', key: 'status', minWidth: 160,
      render: (_: any, record: OperationLog) => (
        <>
          <Tag color={record.status === 200 ? 'success' : 'error'}>{record.status}</Tag>
          {record.message && record.message !== 'success' && (
            <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>{record.message}</span>
          )}
        </>
      ),
    },
    { title: '耗时', dataIndex: 'costMs', key: 'costMs', width: 90, render: (v: number) => `${v}ms` },
    { title: 'IP', dataIndex: 'ip', key: 'ip', width: 120 },
  ];

  return (
    <div className="page-shell operation-log-page">
      <section className="page-hero glass-card">
        <div>
          <h1 className="page-heading">操作日志</h1>
          <p className="page-title-en">Operation Log</p>
        </div>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          {isAdmin && (
            <Button danger className="action-pill" icon={<DeleteOutlined />} onClick={handleCleanup}>清理近期日志</Button>
          )}
          <Button className="action-pill" icon={<ReloadOutlined />} onClick={loadData}>刷新</Button>
        </div>
      </section>

      <section className="surface-panel table-card glass-card" style={{ marginTop: 24, padding: 24 }}>
        <div className="table-toolbar" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, marginBottom: 16 }}>
          <div className="toolbar-copy">
            <h3 style={{ margin: 0 }}>日志列表</h3>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, alignItems: 'center' }}>
            <Input
              placeholder="操作人"
              value={operatorName}
              onChange={(e) => setOperatorName(e.target.value)}
              onPressEnter={handleSearch}
              allowClear
              style={{ width: 140 }}
            />
            <Select
              placeholder="操作类型"
              value={action}
              onChange={setAction}
              allowClear
              style={{ width: 140 }}
              options={[
                { label: '新增', value: 'ADD' },
                { label: '修改', value: 'UPDATE' },
                { label: '删除', value: 'DELETE' },
                { label: '其他', value: 'OTHER' },
              ]}
            />
            <RangePicker value={dateRange as any} onChange={(v) => setDateRange(v as any)} />
            <Button type="primary" className="search-submit" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
            <Button className="search-submit" onClick={resetFilters}>重置</Button>
          </div>
        </div>

        {!isMobile ? (
          <Table
            columns={columns}
            dataSource={data}
            rowKey="id"
            loading={loading}
            pagination={false}
            className="main-table"
            expandable={{
              expandedRowRender: (record: OperationLog) => (
                <div style={{ fontSize: 13, lineHeight: 1.8, padding: '4px 8px' }}>
                  <p style={{ margin: 0 }}>
                    <strong style={{ color: 'var(--text-muted)', marginRight: 8 }}>请求：</strong>
                    <span style={{ fontFamily: 'Consolas, Monaco, monospace', fontSize: 12, wordBreak: 'break-all' }}>
                      {record.method} {record.uri}
                    </span>
                  </p>
                  <p style={{ margin: 0 }}>
                    <strong style={{ color: 'var(--text-muted)', marginRight: 8 }}>参数：</strong>
                    <span style={{ wordBreak: 'break-all' }}>{record.params || '-'}</span>
                  </p>
                </div>
              ),
            }}
          />
        ) : (
          <div className="mobile-card-list">
            {data.map((row) => (
              <div key={row.id} className="mobile-card glass-card" style={{ padding: 16, marginBottom: 12 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                  <h4 style={{ margin: 0 }}>{row.description || '操作记录'}</h4>
                  <Tag color={actionColor(row.action)}>{actionText(row.action)}</Tag>
                </div>
                <p style={{ margin: '4px 0', color: 'var(--text-secondary)', fontSize: 13, lineHeight: 1.7 }}>
                  🕐 {row.createTime} · 👤 {row.operatorName || 'anonymous'} · 📦 {row.module}<br />
                  🔗 <Tag color={methodColor(row.method)} style={{ marginRight: 4 }}>{row.method}</Tag>{row.uri}<br />
                  📄 {row.params || '-'}<br />
                  ✅ {row.status}{row.message && row.message !== 'success' ? ` ${row.message}` : ''} · ⏱ {row.costMs}ms · 🌐 {row.ip || '-'}
                </p>
              </div>
            ))}
            {data.length === 0 && (
              <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>暂无操作日志</div>
            )}
          </div>
        )}

        <div className="pagination-shell" style={{ marginTop: 16, display: 'flex', justifyContent: isMobile ? 'center' : 'flex-end' }}>
          <Pagination
            current={current}
            pageSize={pageSize}
            total={total}
            showSizeChanger={!isMobile}
            showTotal={!isMobile ? (t: number) => `共 ${t} 条` : undefined}
            onChange={(page, size) => {
              setCurrent(page);
              setPageSize(size);
            }}
          />
        </div>
      </section>
    </div>
  );
};

export default OperationLogPage;
