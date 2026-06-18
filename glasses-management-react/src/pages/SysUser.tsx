import React, { useState, useEffect } from 'react';
import { Card, Button, Table, Switch, Tag, Space, Modal, message } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import { sysUserAPI, SysUser } from '@/api/sysUser';

const SysUserPage: React.FC = () => {
  const [users, setUsers] = useState<SysUser[]>([]);
  const [loading, setLoading] = useState(false);
  const [includeDeleted, setIncludeDeleted] = useState(false);

  const fetchUsers = async (deleted = includeDeleted) => {
    setLoading(true);
    try {
      const res = await sysUserAPI.list(deleted);
      setUsers(res || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers(includeDeleted);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [includeDeleted]);

  const resetPwd = (row: SysUser) => {
    Modal.confirm({
      title: '重置密码',
      content: `确定为 ${row.username} 生成一次性临时密码吗？`,
      okType: 'danger',
      onOk: async () => {
        const temp = await sysUserAPI.resetPassword(row.id);
        Modal.info({ title: '临时密码', content: <code>{String(temp)}</code> });
        message.success('密码已重置，用户下次登录必须修改');
        fetchUsers();
      },
    });
  };

  const toggleDisabled = (row: SysUser) => {
    const action = row.disabled ? '解除封禁' : '封禁';
    Modal.confirm({
      title: action,
      content: `确定${action}账号 ${row.username} 吗？`,
      onOk: async () => {
        if (row.disabled) {
          await sysUserAPI.enable(row.id);
        } else {
          await sysUserAPI.disable(row.id);
        }
        message.success(`${action}成功`);
        fetchUsers();
      },
    });
  };

  const deleteUser = (row: SysUser) => {
    Modal.confirm({
      title: '删除账号',
      content: `删除后账号 ${row.username} 将进入回收状态，可恢复。确认删除吗？`,
      okType: 'danger',
      onOk: async () => {
        await sysUserAPI.delete(row.id);
        message.success('账号已删除');
        fetchUsers();
      },
    });
  };

  const restoreUser = async (row: SysUser) => {
    await sysUserAPI.restore(row.id);
    message.success('账号已恢复');
    fetchUsers();
  };

  const purgeUser = (row: SysUser) => {
    Modal.confirm({
      title: '危险操作',
      content: `彻底删除账号 ${row.username} 后无法恢复，确认继续吗？`,
      okType: 'danger',
      okText: '彻底删除',
      onOk: async () => {
        await sysUserAPI.purge(row.id);
        message.success('账号已彻底删除');
        fetchUsers();
      },
    });
  };

  const columns = [
    { title: '账号ID', dataIndex: 'id', width: 90 },
    { title: '用户名', dataIndex: 'username' },
    { title: '手机号', dataIndex: 'phone' },
    { title: '注册时间', dataIndex: 'createTime' },
    {
      title: '状态',
      width: 120,
      render: (_: any, row: SysUser) =>
        row.deleted ? <Tag>已删除</Tag> : row.disabled ? <Tag color="warning">已封禁</Tag> : <Tag color="success">正常</Tag>,
    },
    {
      title: '操作',
      width: 380,
      align: 'center' as const,
      render: (_: any, row: SysUser) =>
        row.deleted ? (
          <div className="table-actions" style={{ justifyContent: 'center' }}>
            <Button size="small" onClick={() => restoreUser(row)}>恢复</Button>
            <Button size="small" type="primary" danger onClick={() => purgeUser(row)}>彻底删除</Button>
          </div>
        ) : (
          <div className="table-actions" style={{ justifyContent: 'center' }}>
            <Button size="small" onClick={() => resetPwd(row)}>重置密码</Button>
            <Button size="small" onClick={() => toggleDisabled(row)}>{row.disabled ? '解除封禁' : '封禁'}</Button>
            <Button size="small" type="primary" danger onClick={() => deleteUser(row)}>删除</Button>
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
          <h1 className="page-heading" style={{ margin: 0 }}>账号管理中心</h1>
          <p className="page-title-en">SYSTEM USER MANAGEMENT</p>
          <p style={{ margin: '6px 0 0', color: 'var(--text-secondary)' }}>
            管理商户账号状态，支持封禁、恢复、逻辑删除和彻底删除。
          </p>
        </div>
        <Space size={12}>
          <Switch
            checked={includeDeleted}
            checkedChildren="显示已删除"
            unCheckedChildren="显示已删除"
            onChange={setIncludeDeleted}
          />
          <Button type="primary" icon={<ReloadOutlined />} onClick={() => fetchUsers()}>刷新列表</Button>
        </Space>
      </section>

      <Card className="glass-card" style={{ marginTop: 18 }}>
        <Table
          rowKey="id"
          dataSource={users}
          columns={columns}
          loading={loading}
          pagination={false}
          scroll={{ x: 1080 }}
        />
      </Card>
    </div>
  );
};

export default SysUserPage;
