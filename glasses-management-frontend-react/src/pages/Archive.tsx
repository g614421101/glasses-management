import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Timeline, Descriptions, Empty, Modal, message } from 'antd';
import {
  ArrowLeftOutlined,
  FileAddOutlined,
  ShoppingCartOutlined,
  DownloadOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { customerAPI, Customer } from '@/api/customer';
import { archiveAPI, TimelineItem } from '@/api/archive';
import './Archive/Archive.css';
import { optometryAPI } from '@/api/optometry';
import { salesAPI } from '@/api/sales';
import { downloadBlob, openBlob } from '@/utils/request';
import { useMobile } from '@/hooks/useMobile';
import DetailModal from './Archive/DetailModal';
import OptometryFormModal, { OptoForm, buildEmptyOpto, prepareOptoForEdit } from './Archive/OptometryFormModal';
import SalesFormModal, { SalesForm, buildEmptySales } from './Archive/SalesFormModal';
import TimelineCard from './Archive/TimelineCard';

const Archive: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isMobile = useMobile();
  const customerId = Number(id);

  const [customerInfo, setCustomerInfo] = useState<Customer | null>(null);
  const [timelineData, setTimelineData] = useState<TimelineItem[]>([]);

  const [detailVisible, setDetailVisible] = useState(false);
  const [currentDetail, setCurrentDetail] = useState<TimelineItem | null>(null);

  const [optoOpen, setOptoOpen] = useState(false);
  const [optoInitial, setOptoInitial] = useState<OptoForm | null>(null);

  const [salesOpen, setSalesOpen] = useState(false);
  const [salesInitial, setSalesInitial] = useState<SalesForm | null>(null);

  useEffect(() => {
    loadCustomer();
    loadTimeline();
  }, [customerId]);

  const loadCustomer = async () => {
    try {
      const res = await customerAPI.getById(customerId);
      setCustomerInfo(res);
    } catch (e) { /* handled by request */ }
  };

  const loadTimeline = async () => {
    try {
      const res = await archiveAPI.getTimeline(customerId);
      setTimelineData(res);
    } catch (e) { /* handled by request */ }
  };

  const latestRecordLabel = useMemo(() => {
    if (!timelineData.length) return '暂无';
    return timelineData[0].type === 'OPTOMETRY' ? '验光' : '配镜';
  }, [timelineData]);

  const handleView = (item: TimelineItem) => {
    setCurrentDetail(item);
    setDetailVisible(true);
  };

  const handleOpenOpto = () => {
    setOptoInitial(buildEmptyOpto(customerId));
    setOptoOpen(true);
  };

  const handleOpenSales = () => {
    setSalesInitial(buildEmptySales(customerId));
    setSalesOpen(true);
  };

  const handleEdit = (item: TimelineItem) => {
    if (item.type === 'OPTOMETRY') {
      setOptoInitial(prepareOptoForEdit(item.data));
      setOptoOpen(true);
    } else {
      setSalesInitial({ ...buildEmptySales(customerId), ...item.data });
      setSalesOpen(true);
    }
  };

  const handleDelete = (item: TimelineItem) => {
    Modal.confirm({
      title: '提示',
      content: '确定要删除这条记录吗？',
      okType: 'danger',
      onOk: async () => {
        if (item.type === 'OPTOMETRY') {
          await optometryAPI.delete(item.data.id);
        } else {
          await salesAPI.delete(item.data.id);
        }
        message.success('删除成功');
        loadTimeline();
      },
    });
  };

  const handlePrint = async (id: number) => {
    try {
      await openBlob(`/print/prescription/${id}`);
    } catch { /* handled */ }
  };

  const handleExportExcel = async () => {
    try {
      await downloadBlob(`/print/export/customer/${customerId}`, 'customer-records.xlsx');
    } catch { /* handled */ }
  };

  const handleDeleteCustomer = () => {
    Modal.confirm({
      title: '危险操作',
      content: '确定要删除该顾客及其所有验光单、配镜单吗？删除后数据将移入回收站。',
      okType: 'danger',
      okText: '确定删除',
      onOk: async () => {
        await customerAPI.delete(customerId);
        message.success('已删除并移入回收站');
        navigate('/customer');
      },
    });
  };

  return (
    <div className="page-shell archive-page">
      <section className="page-hero glass-card archive-hero">
        <div className="hero-copy">
          <div className="hero-breadcrumb">
            <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate(-1)} style={{ paddingLeft: 0 }}>返回</Button>
            <span className="breadcrumb-divider" />
            <span>顾客档案中心</span>
          </div>
          <h1 className="page-heading">顾客档案</h1>
          <p className="page-title-en">Customer Archive</p>
        </div>

        <div className="page-toolbar hero-actions">
          <Button type="primary" icon={<FileAddOutlined />} onClick={handleOpenOpto}>录入验光单</Button>
          <Button className="action-pill" icon={<ShoppingCartOutlined />} onClick={handleOpenSales}>我要开单</Button>
          <Button className="action-pill" icon={<DownloadOutlined />} onClick={handleExportExcel}>导出 Excel</Button>
          <Button className="action-pill action-pill--danger" icon={<DeleteOutlined />} onClick={handleDeleteCustomer}>删除档案</Button>
        </div>
      </section>

      <div className="archive-content">
        <aside className="info-card glass-card">
          <div className="info-hero">
            <div className="avatar-wrap">
              <div className="avatar">{customerInfo?.name?.charAt(0) || '客'}</div>
            </div>
            <h3 className="c-name">{customerInfo?.name || '-'}</h3>
            <p className="c-phone">{customerInfo?.phone || '-'}</p>
          </div>

          <div className="customer-summary">
            <div className="summary-chip">
              <span>档案条目</span>
              <strong>{timelineData.length}</strong>
            </div>
            <div className="summary-chip">
              <span>最新记录</span>
              <strong>{latestRecordLabel}</strong>
            </div>
          </div>

          <div className="base-info">
            <Descriptions column={1} size="small" bordered className="customer-meta">
              <Descriptions.Item label="性别">
                {customerInfo?.gender === 1 ? '男' : customerInfo?.gender === 2 ? '女' : '未知'}
              </Descriptions.Item>
              <Descriptions.Item label="生日">{customerInfo?.birthday || '-'}</Descriptions.Item>
              <Descriptions.Item label="备注">{customerInfo?.remark || '无'}</Descriptions.Item>
            </Descriptions>
          </div>
        </aside>

        <section className="timeline-card glass-card">
          <div className="timeline-head">
            <div>
              <h3 className="section-title">档案记录</h3>
              <p className="timeline-tip">
                按时间查看顾客的验光和配镜流转，支持预览、编辑、打印和删除。
              </p>
            </div>
            <div className="timeline-count">{timelineData.length} 条记录</div>
          </div>

          {timelineData.length === 0 ? (
            <Empty description="暂无历史记录" />
          ) : (
            <Timeline
              items={timelineData.map((item, idx) => ({
                key: idx,
                color: item.type === 'OPTOMETRY' ? 'blue' : 'green',
                children: (
                  <TimelineCard
                    item={item}
                    isMobile={isMobile}
                    onView={() => handleView(item)}
                    onEdit={() => handleEdit(item)}
                    onDelete={() => handleDelete(item)}
                    onPrint={item.type === 'SALES' ? () => handlePrint(item.data.id) : undefined}
                  />
                ),
              }))}
            />
          )}
        </section>
      </div>

      <DetailModal
        open={detailVisible}
        detail={currentDetail}
        onClose={() => setDetailVisible(false)}
      />

      <OptometryFormModal
        open={optoOpen}
        initial={optoInitial}
        onClose={() => setOptoOpen(false)}
        onSaved={loadTimeline}
      />

      <SalesFormModal
        open={salesOpen}
        initial={salesInitial}
        optoOptions={timelineData}
        onClose={() => setSalesOpen(false)}
        onSaved={loadTimeline}
      />
    </div>
  );
};

export default Archive;
