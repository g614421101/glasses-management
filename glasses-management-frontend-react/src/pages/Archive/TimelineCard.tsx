import React from 'react';
import { Button } from 'antd';
import { EditOutlined, DeleteOutlined, UserOutlined, EyeOutlined } from '@ant-design/icons';
import type { TimelineItem } from '@/api/archive';
import { fmt } from './formatters';

interface Props {
  item: TimelineItem;
  isMobile: boolean;
  onView: () => void;
  onEdit: () => void;
  onDelete: () => void;
  onPrint?: () => void;
}

const TimelineCard: React.FC<Props> = ({ item, isMobile, onView, onEdit, onDelete, onPrint }) => {
  const isOpto = item.type === 'OPTOMETRY';
  const d = item.data;

  return (
    <div className={`timeline-detail-card ${isOpto ? 'is-optometry' : 'is-sales'}`} onClick={onView}>
      {/* Decorative glow */}
      <div className="card-glow" />

      <p className="record-date">{item.date}</p>

      <div className="record-top">
        <div className="record-title-wrap">
          <span className="record-badge">{isOpto ? '验光单' : '配镜单'}</span>
          <h4>{item.title}</h4>
          <p className="subtitle">{item.subtitle}</p>
        </div>
        {!isOpto && (
          <div className="record-amount">
            ￥{d.totalAmount || '0.00'}
            {((d.frameQuantity > 1) || (d.lensQuantity > 1)) && (
              <span className="qty-badge">
                {d.frameQuantity > 1 && <>架×{d.frameQuantity}</>}
                {d.frameQuantity > 1 && d.lensQuantity > 1 && ' '}
                {d.lensQuantity > 1 && <>片×{d.lensQuantity}</>}
              </span>
            )}
          </div>
        )}
      </div>

      <div className="record-body">
        {isOpto ? (
          isMobile ? (
            <>
              <div className="mini-optometry-table">
                <div className="table-header-row">
                  <div className="cell title-cell"></div>
                  <div className="cell">球镜</div>
                  <div className="cell">柱镜</div>
                  <div className="cell">轴位</div>
                  <div className="cell">视力</div>
                </div>
                <div className="table-body-row">
                  <div className="cell title-cell label-od">右眼</div>
                  <div className="cell">{fmt(d.odSph)}</div>
                  <div className="cell">{fmt(d.odCyl)}</div>
                  <div className="cell">{d.odAxis || '-'}</div>
                  <div className="cell">{d.odVa || '-'}</div>
                </div>
                <div className="table-body-row">
                  <div className="cell title-cell label-os">左眼</div>
                  <div className="cell">{fmt(d.osSph)}</div>
                  <div className="cell">{fmt(d.osCyl)}</div>
                  <div className="cell">{d.osAxis || '-'}</div>
                  <div className="cell">{d.osVa || '-'}</div>
                </div>
              </div>
              <div className="optometry-footer">
                {d.optometristName && (
                  <span className="optometrist-tag"><UserOutlined /> 验光师: <strong>{d.optometristName}</strong></span>
                )}
                {d.pdFar && <span className="pd-tag">瞳距: <strong>{d.pdFar} mm</strong></span>}
                {d.addPower && <span className="add-tag">下加光: <strong>{fmt(d.addPower)}</strong></span>}
              </div>
            </>
          ) : (
            <div className="record-glance">
              <div className="glance-item"><span>右眼</span><strong>{fmt(d.odSph)} / {fmt(d.odCyl)}</strong></div>
              <div className="glance-item"><span>左眼</span><strong>{fmt(d.osSph)} / {fmt(d.osCyl)}</strong></div>
              <div className="glance-item"><span>验光师</span><strong>{d.optometristName || '-'}</strong></div>
            </div>
          )
        ) : isMobile ? (
          <>
            <div className="sales-product-glance">
              {(d.frameBrand || d.frameModel) && (
                <div className="product-item">
                  <div className="p-icon">🔍</div>
                  <div className="p-info">
                    <span className="p-label">镜架</span>
                    <strong className="p-val">{d.frameBrand || '-'}{d.frameModel && <small> {d.frameModel}</small>}</strong>
                  </div>
                </div>
              )}
              {(d.lensBrand || d.lensParams) && (
                <div className="product-item">
                  <div className="p-icon">🔍</div>
                  <div className="p-info">
                    <span className="p-label">镜片</span>
                    <strong className="p-val">{d.lensBrand || '-'}{d.lensParams && <small> {d.lensParams}</small>}</strong>
                  </div>
                </div>
              )}
            </div>
            <div className="sales-footer">
              {d.recordNo && <span className="sales-no-tag">单号: <code>{d.recordNo}</code></span>}
              {d.operatorName && <span className="operator-tag">经手人: <strong>{d.operatorName}</strong></span>}
            </div>
          </>
        ) : (
          <div className="record-glance">
            <div className="glance-item"><span>镜架</span><strong>{d.frameBrand || '-'}</strong></div>
            <div className="glance-item"><span>镜片</span><strong>{d.lensBrand || '-'}</strong></div>
            <div className="glance-item glance-item--record-no"><span>单号</span><strong className="record-no-text" title={d.recordNo || '-'}>{d.recordNo || '-'}</strong></div>
          </div>
        )}
      </div>

      <div className="action-bar" onClick={(e) => e.stopPropagation()}>
        {!isOpto && onPrint && <Button className="action-pill" size="small" icon={<EyeOutlined />} onClick={onPrint}>预览处方</Button>}
        <Button className="action-pill" size="small" icon={<EditOutlined />} onClick={onEdit}>编辑</Button>
        <Button className="action-pill action-pill--danger" size="small" icon={<DeleteOutlined />} onClick={onDelete}>删除</Button>
      </div>
    </div>
  );
};

export default TimelineCard;
