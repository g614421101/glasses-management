import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Input, Modal, Form, Radio, DatePicker, Tag, message, Popconfirm, Pagination } from 'antd';
import { PlusOutlined, SearchOutlined, EyeOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { customerAPI, Customer } from '@/api/customer';
import { useMobile } from '@/hooks/useMobile';

const CustomerPage: React.FC = () => {
  const navigate = useNavigate();
  const isMobile = useMobile();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<Customer[]>([]);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalTitle, setModalTitle] = useState('新建顾客');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();

  // Set form values after modal becomes visible
  useEffect(() => {
    if (modalVisible) {
      if (editingId !== null) {
        const record = data.find((r) => r.id === editingId);
        if (record) {
          form.setFieldsValue({
            ...record,
            birthday: record.birthday ? dayjs(record.birthday) : null,
          });
        }
      } else {
        form.resetFields();
        form.setFieldsValue({ gender: 0 });
      }
    }
  }, [modalVisible, editingId, data, form]);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await customerAPI.getPage({ keyword, current, size: pageSize });
      setData(res.records);
      setTotal(res.total);
    } catch (e) { /* handled */ } finally {
      setLoading(false);
    }
  }, [keyword, current, pageSize]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleSearch = () => { setCurrent(1); };

  const handleAdd = () => {
    setModalTitle('新建顾客');
    setEditingId(null);
    setModalVisible(true);
  };

  const handleEdit = (record: Customer) => {
    setModalTitle('编辑顾客');
    setEditingId(record.id);
    setModalVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const formData = {
        ...values,
        birthday: values.birthday ? values.birthday.format('YYYY-MM-DD') : null,
      };
      if (form.getFieldValue('id')) {
        await customerAPI.update(formData);
        message.success('更新成功');
      } else {
        await customerAPI.add(formData);
        message.success('新建成功');
      }
      setModalVisible(false);
      loadData();
    } catch (e: any) {
      if (e?.code === 409 && e.data) {
        Modal.confirm({
          title: '提示',
          content: `该手机号已关联顾客：${e.data.name}。是否直接进入该顾客档案？`,
          onOk: () => {
            setModalVisible(false);
            navigate('/archive/' + e.data.id);
          },
        });
      }
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await customerAPI.delete(id);
      message.success('删除成功');
      loadData();
    } catch (e) { /* handled */ }
  };

  const genderTag = (g: number) => {
    const color = g === 1 ? 'blue' : g === 2 ? 'green' : 'default';
    const label = g === 1 ? '男' : g === 2 ? '女' : '未知';
    return <Tag color={color} className="gender-tag">{label}</Tag>;
  };

  const columns = [
    { title: '姓名', dataIndex: 'name', key: 'name' },
    { title: '手机号', dataIndex: 'phone', key: 'phone' },
    { title: '性别', dataIndex: 'gender', key: 'gender', render: (g: number) => genderTag(g) },
    { title: '生日', dataIndex: 'birthday', key: 'birthday' },
    { title: '注册时间', dataIndex: 'createTime', key: 'createTime' },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Customer) => (
        <div className="table-actions">
          <Button className="action-pill" icon={<EyeOutlined />} onClick={() => navigate('/archive/' + record.id)}>档案</Button>
          <Button className="action-pill" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
          <Popconfirm title="确定删除该顾客？将会级联清除其记录!" onConfirm={() => handleDelete(record.id)}>
            <Button className="action-pill action-pill--danger" icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </div>
      ),
    },
  ];

  return (
    <div className="page-shell customer-page">
      <section className="page-hero glass-card customer-hero">
        <div className="hero-copy">
          <h1 className="page-heading">顾客管理</h1>
          <p className="page-title-en">Customer Management</p>
          <div className="page-toolbar">
            <Button type="primary" size="large" icon={<PlusOutlined />} onClick={handleAdd}>新建顾客</Button>
          </div>
        </div>
        <div className="hero-meta">
          <div className="meta-card">
            <span>当前顾客数</span>
            <strong>{total}</strong>
          </div>
          <div className="meta-card">
            <span>每页显示</span>
            <strong>{pageSize}</strong>
          </div>
        </div>
      </section>

      <section className="surface-panel table-card glass-card" style={{ marginTop: 24, padding: 24 }}>
        <div className="table-toolbar" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, marginBottom: 16 }}>
          <div className="toolbar-copy">
            <h3 style={{ margin: 0 }}>顾客列表</h3>
          </div>
        </div>

        <div className="search-shell home-search-shell" style={{ marginBottom: 20 }}>
          <div className="search-icon-badge">
            <SearchOutlined style={{ fontSize: 20 }} />
          </div>
          <Input
            className="search-control"
            placeholder="搜索姓名 / 手机号"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            allowClear
          />
          <Button type="primary" className="search-submit" onClick={handleSearch}>立即搜索</Button>
        </div>

        {!isMobile ? (
          <Table
            columns={columns}
            dataSource={data}
            rowKey="id"
            loading={loading}
            pagination={false}
            className="main-table"
          />
        ) : (
          <div className="mobile-card-list">
            {data.map((row) => (
              <div key={row.id} className="mobile-card glass-card" style={{ padding: 16, marginBottom: 12 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                  <h4 style={{ margin: 0 }}>{row.name}</h4>
                  {genderTag(row.gender)}
                </div>
                <p style={{ margin: '4px 0', color: 'var(--text-secondary)', fontSize: 13 }}>
                  📱 {row.phone}{row.birthday ? ` · 🎂 ${row.birthday}` : ''}
                </p>
                <div style={{ display: 'flex', gap: 8, marginTop: 12, flexWrap: 'wrap' }}>
                  <Button size="small" icon={<EyeOutlined />} onClick={() => navigate('/archive/' + row.id)}>档案</Button>
                  <Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(row)}>编辑</Button>
                  <Popconfirm title="确定删除该顾客？" onConfirm={() => handleDelete(row.id)}>
                    <Button size="small" type="primary" danger icon={<DeleteOutlined />}>删除</Button>
                  </Popconfirm>
                </div>
              </div>
            ))}
            {data.length === 0 && (
              <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>暂无数据</div>
            )}
          </div>
        )}

        <div className="pagination-shell" style={{ marginTop: 16, display: 'flex', justifyContent: isMobile ? 'center' : 'flex-end' }}>
          <Pagination
            current={current}
            pageSize={pageSize}
            total={total}
            showSizeChanger={!isMobile}
            showTotal={!isMobile ? (t) => `共 ${t} 条` : undefined}
            simple={isMobile}
            pageSizeOptions={[10, 20, 50]}
            onChange={(page, size) => {
              setCurrent(page);
              setPageSize(size);
            }}
          />
        </div>
      </section>

      <Modal
        title={modalTitle}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width="min(92vw, 560px)"
        forceRender
      >
        <Form form={form} layout="vertical" className="customer-form" preserve={false}>
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Form.Item name="name" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input placeholder="请输入顾客姓名" />
          </Form.Item>
          <Form.Item
            name="phone"
            label="手机号"
            rules={[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
            ]}
          >
            <Input placeholder="请输入手机号码" />
          </Form.Item>
          <Form.Item name="gender" label="性别">
            <Radio.Group>
              <Radio value={1}>男</Radio>
              <Radio value={2}>女</Radio>
              <Radio value={0}>未知</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item name="birthday" label="生日">
            <DatePicker style={{ width: '100%' }} placeholder="选择生日" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} placeholder="其他需要补充的信息" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CustomerPage;
