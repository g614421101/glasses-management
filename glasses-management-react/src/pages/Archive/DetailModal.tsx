import React from 'react';
import { Button } from 'antd';
import { fmt } from './formatters';
import type { TimelineItem } from '@/api/archive';

interface Props {
  open: boolean;
  detail: TimelineItem | null;
  onClose: () => void;
}

const DetailModal: React.FC<Props> = ({ open, detail, onClose }) => {
  if (!open || !detail) return null;
  const isOpto = detail.type === 'OPTOMETRY';
  const d = detail.data;

  return (
    <div className="custom-modal-mask" onClick={onClose}>
      <div className="custom-modal-body" onClick={(e) => e.stopPropagation()}>
        {isOpto ? (
          <div className="detail-box optometry-sheet">
            <div className="sheet-hero">
              <div>
                <div className="receipt-badge primary">验光记录</div>
                <h3 className="sheet-title">双眼屈光参数总览</h3>
              </div>
            </div>

            <div className="eye-card-grid">
              <div className="eye-card">
                <div className="eye-card-head">
                  <span>右眼</span>
                  <strong>OD</strong>
                </div>
                <div className="eye-metric-grid">
                  <div className="eye-metric"><span>球镜</span><strong>{fmt(d.odSph)}</strong></div>
                  <div className="eye-metric"><span>柱镜</span><strong>{fmt(d.odCyl)}</strong></div>
                  <div className="eye-metric"><span>轴位</span><strong>{d.odAxis || '-'}</strong></div>
                  <div className="eye-metric"><span>视力</span><strong>{d.odVa || '-'}</strong></div>
                </div>
              </div>
              <div className="eye-card">
                <div className="eye-card-head">
                  <span>左眼</span>
                  <strong>OS</strong>
                </div>
                <div className="eye-metric-grid">
                  <div className="eye-metric"><span>球镜</span><strong>{fmt(d.osSph)}</strong></div>
                  <div className="eye-metric"><span>柱镜</span><strong>{fmt(d.osCyl)}</strong></div>
                  <div className="eye-metric"><span>轴位</span><strong>{d.osAxis || '-'}</strong></div>
                  <div className="eye-metric"><span>视力</span><strong>{d.osVa || '-'}</strong></div>
                </div>
              </div>
            </div>

            <div className="extra-grid">
              <div className="info-tag"><span>右眼瞳距</span><strong>{d.odPd || '-'}</strong></div>
              <div className="info-tag"><span>左眼瞳距</span><strong>{d.osPd || '-'}</strong></div>
              <div className="info-tag"><span>远用瞳距</span><strong>{d.pdFar || '-'}</strong></div>
              <div className="info-tag"><span>近用瞳距</span><strong>{d.pdNear || '-'}</strong></div>
              <div className="info-tag"><span>下加光</span><strong>{fmt(d.addPower)}</strong></div>
              <div className="info-tag"><span>验光师</span><strong>{d.optometristName || '-'}</strong></div>
            </div>

            {d.remark && (
              <div style={{ marginTop: 16, padding: 12, background: 'var(--surface-muted)', borderRadius: 12, fontSize: 13, lineHeight: 1.5 }}>
                <span style={{ color: 'var(--text-secondary)', marginRight: 6 }}>备注:</span>
                <span style={{ color: 'var(--text-primary)' }}>{d.remark}</span>
              </div>
            )}
          </div>
        ) : (
          <div className="detail-box sales-sheet">
            <div className="sheet-hero">
              <div>
                <div className="receipt-badge success">配镜信息</div>
                <h3 className="sheet-title">订单 {d.recordNo}</h3>
              </div>
              <div className="total-bubble">
                <small>
                  {d.totalRetailPrice > 0 && (
                    <del style={{ marginRight: 4, opacity: 0.7 }}>￥{d.totalRetailPrice}</del>
                  )}
                  实收总价
                </small>
                <strong style={{ fontSize: 28, color: '#ffffff', fontWeight: 800 }}>￥{d.totalAmount}</strong>
                {d.totalRetailPrice > 0 && (
                  <span style={{ display: 'block', fontSize: 13, marginTop: 4, textAlign: 'right', fontWeight: 600, opacity: 0.9 }}>
                    {((d.totalAmount / d.totalRetailPrice) * 10).toFixed(1)}折
                  </span>
                )}
              </div>
            </div>

            <div className="sales-card-grid">
              <div className="product-card">
                <div className="product-card-head">
                  <span className="product-badge">镜架</span>
                  <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
                    {d.frameRetailPrice > 0 && (
                      <del style={{ color: 'var(--color-strikethrough)', fontSize: 13, fontWeight: 'normal' }}>￥{d.frameRetailPrice}</del>
                    )}
                    <strong style={{ color: 'var(--text-primary)', fontSize: 20, fontWeight: 800 }}>￥{d.framePrice || 0}</strong>
                    {d.frameQuantity > 1 && (
                      <span style={{ fontSize: 12, color: 'var(--primary-color)', fontWeight: 700 }}>×{d.frameQuantity}</span>
                    )}
                  </div>
                </div>
                <h4>{d.frameBrand || '-'}</h4>
                <p>{d.frameModel || '未填写型号'}</p>
              </div>

              <div className="product-card">
                <div className="product-card-head">
                  <span className="product-badge product-badge--soft">镜片</span>
                  <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
                    {d.lensRetailPrice > 0 && (
                      <del style={{ color: 'var(--color-strikethrough)', fontSize: 13, fontWeight: 'normal' }}>￥{d.lensRetailPrice}</del>
                    )}
                    <strong style={{ color: 'var(--text-primary)', fontSize: 20, fontWeight: 800 }}>￥{d.lensPrice || 0}</strong>
                    {d.lensQuantity > 1 && (
                      <span style={{ fontSize: 12, color: 'var(--primary-color)', fontWeight: 700 }}>×{d.lensQuantity}</span>
                    )}
                  </div>
                </div>
                <h4>{d.lensBrand || '-'}</h4>
                <p>{d.lensParams || '未填写参数'}</p>
              </div>
            </div>

            {d.remark && (
              <div style={{ marginTop: 16, padding: 12, background: 'var(--surface-muted)', borderRadius: 12, fontSize: 13, lineHeight: 1.5 }}>
                <span style={{ color: 'var(--text-secondary)', marginRight: 6 }}>备注:</span>
                <span style={{ color: 'var(--text-primary)' }}>{d.remark}</span>
              </div>
            )}
          </div>
        )}

        <div style={{ textAlign: 'center', marginTop: 16 }}>
          <Button type="primary" size="large" shape="round" style={{ minWidth: 120 }} onClick={onClose}>
            关闭
          </Button>
        </div>
      </div>
    </div>
  );
};

export default DetailModal;
