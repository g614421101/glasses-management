import React, { useState, useEffect } from 'react';
import { Card, Button, Table, Select, Space, Modal, message } from 'antd';
import { recycleBinAPI } from '@/api/recycleBin';

const RecycleBin: React.FC = () => {
  const [activeType, setActiveType] = useState('all');
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState<any[]>([]);
  const [optometryRecords, setOptometryRecords] = useState<any[]>([]);
  const [salesRecords, setSalesRecords] = useState<any[]>([]);

  const showCustomers = activeType === 'all' || activeType === 'customer';
  const showOptometry = activeType === 'all' || activeType === 'optometry';
  const showSales = activeType === 'all' || activeType === 'sales';

  const loadData = async (type = activeType) => {
    setLoading(true);
    try {
      const data = await recycleBinAPI.list(type);
      setCustomers(data.customers || []);
      setOptometryRecords(data.optometryRecords || []);
      setSalesRecords(data.salesRecords || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData(activeType);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeType]);

  const restore = async (type: string, id: number) => {
    await recycleBinAPI.restore(type, id);
    message.success('恢复成功');
    loadData();
  };

  const purge = (type: string, id: number) => {
    Modal.confirm({
      title: '危险操作',
      content: '彻底删除后无法恢复，确认继续吗？',
      okText: '彻底删除',
      okType: 'danger',
      onOk: async () => {
        await recycleBinAPI.purge(type, id);
        message.success('已彻底删除');
        loadData();
      },
    });
  };

  const purgeExpired = () => {
    Modal.confirm({
      title: '清理回收站',
      content: '将彻底清理删除超过 30 天的数据，确认继续吗？',
      okType: 'danger',
      okText: '开始清理',
      onOk: async () => {
        await recycleBinAPI.purgeExpired();
        message.success('过期数据清理完成');
        loadData();
      },
    });
  };

  const emptyBin = () => {
    Modal.confirm({
      title: '极度危险操作',
      content: '这将会彻底清空回收站中所有的记录，此操作不可逆！确认继续吗？',
      okType: 'danger',
      okText: '确定清空',
      onOk: async () => {
        const res = await recycleBinAPI.empty();
        message.success(`清空成功！共清理：顾客 ${res.customers} 条, 验光单 ${res.optometry} 条, 配镜单 ${res.sales} 条`);
        loadData();
      },
    });
  };

  const customerCols = [
    { title: '姓名', dataIndex: 'name' },
    { title: '手机号', dataIndex: 'phone' },
    { title: '删除时间', dataIndex: 'deletedTime' },
    {
      title: '操作',
      width: 240,
      align: 'center' as const,
      render: (_: any, row: any) => (
        <div className="table-actions" style={{ justifyContent: 'center' }}>
          <Button size="small" onClick={() => restore('customer', row.id)}>恢复</Button>
          <Button size="small" type="primary" danger onClick={() => purge('customer', row.id)}>彻底删除</Button>
        </div>
      ),
    },
  ];

  const optoCols = [
    { title: '顾客姓名', dataIndex: 'customerName' },
    { title: '验光师', dataIndex: 'optometristName' },
    { title: '验光时间', dataIndex: 'examDate' },
    { title: '删除时间', dataIndex: 'deletedTime' },
    {
      title: '操作',
      width: 240,
      align: 'center' as const,
      render: (_: any, row: any) => (
        <div className="table-actions" style={{ justifyContent: 'center' }}>
          <Button size="small" onClick={() => restore('optometry', row.id)}>恢复</Button>
          <Button size="small" type="primary" danger onClick={() => purge('optometry', row.id)}>彻底删除</Button>
        </div>
      ),
    },
  ];

  const salesCols = [
    { title: '单号', dataIndex: 'recordNo' },
    { title: '顾客姓名', dataIndex: 'customerName' },
    { title: '金额', dataIndex: 'totalAmount', width: 120 },
    { title: '删除时间', dataIndex: 'deletedTime' },
    {
      title: '操作',
      width: 240,
      align: 'center' as const,
      render: (_: any, row: any) => (
        <div className="table-actions" style={{ justifyContent: 'center' }}>
          <Button size="small" onClick={() => restore('sales', row.id)}>恢复</Button>
          <Button size="small" type="primary" danger onClick={() => purge('sales', row.id)}>彻底删除</Button>
        </div>
      ),
    },
  ];

  return (
    <div className="page-shell">
      <section
        className="page-hero glass-card"
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}
      >
        <div>
          <h1 className="page-heading" style={{ margin: 0 }}>回收站</h1>
          <p className="page-title-en">RECYCLE BIN</p>
        </div>
        <Space wrap size={12}>
          <Select
            value={activeType}
            style={{ width: 150 }}
            onChange={setActiveType}
            options={[
              { value: 'all', label: '全部' },
              { value: 'customer', label: '顾客' },
              { value: 'optometry', label: '验光记录' },
              { value: 'sales', label: '配镜记录' },
            ]}
          />
          <Button onClick={() => loadData()}>刷新</Button>
          <Button type="primary" danger onClick={purgeExpired}>清理超过 30 天</Button>
          <Button danger type="primary" onClick={emptyBin}>一键清空</Button>
        </Space>
      </section>

      {showCustomers && (
        <Card className="glass-card" style={{ marginTop: 18 }} title={<span>已删除顾客 <span style={{ color: 'var(--text-muted)', fontWeight: 800, marginLeft: 8 }}>{customers.length} 条</span></span>}>
          <Table rowKey="id" dataSource={customers} columns={customerCols} loading={loading} pagination={false} scroll={{ x: 720 }} />
        </Card>
      )}

      {showOptometry && (
        <Card className="glass-card" style={{ marginTop: 18 }} title={<span>已删除验光记录 <span style={{ color: 'var(--text-muted)', fontWeight: 800, marginLeft: 8 }}>{optometryRecords.length} 条</span></span>}>
          <Table rowKey="id" dataSource={optometryRecords} columns={optoCols} loading={loading} pagination={false} scroll={{ x: 820 }} />
        </Card>
      )}

      {showSales && (
        <Card className="glass-card" style={{ marginTop: 18 }} title={<span>已删除配镜记录 <span style={{ color: 'var(--text-muted)', fontWeight: 800, marginLeft: 8 }}>{salesRecords.length} 条</span></span>}>
          <Table rowKey="id" dataSource={salesRecords} columns={salesCols} loading={loading} pagination={false} scroll={{ x: 880 }} />
        </Card>
      )}
    </div>
  );
};

export default RecycleBin;
