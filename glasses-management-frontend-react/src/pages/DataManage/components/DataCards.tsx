import React from 'react';
import { Button, Radio, Alert, Table } from 'antd';
import { DownloadOutlined, UploadOutlined, FolderOpenOutlined, DeleteOutlined, WarningFilled } from '@ant-design/icons';
import type { ImportDetail } from '@/api/data';

interface Props {
  exportLoading: boolean;
  importLoading: boolean;
  resetLoading: boolean;
  selectedFile: File | null;
  importMode: 'merge' | 'replace';
  importResult: { message: string; error: boolean; detail?: ImportDetail } | null;
  tableData: { name: string; inserted: number; skipped: number }[];
  fileInputRef: React.RefObject<HTMLInputElement>;
  onExport: () => void;
  onFileChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onModeChange: (m: 'merge' | 'replace') => void;
  onImport: () => void;
  onReset: () => void;
}

const DataCards: React.FC<Props> = (p) => {
  const isReplace = p.importMode === 'replace';
  return (
    <>
      <div className="data-grid">
        {/* Export Card */}
        <section className="glass-card data-card">
          <div className="card-icon export-icon">
            <DownloadOutlined style={{ fontSize: 28 }} />
          </div>
          <h2>导出数据</h2>
          <p>将全部用户、顾客、验光记录和销售记录导出为一个 JSON 文件，可用于备份或迁移到其他设备。</p>
          <Button
            type="primary"
            loading={p.exportLoading}
            onClick={p.onExport}
            icon={<DownloadOutlined />}
            className="action-btn"
          >
            导出 JSON 文件
          </Button>
        </section>

        {/* Import Card */}
        <section className="glass-card data-card">
          <div className="card-icon import-icon">
            <UploadOutlined style={{ fontSize: 28 }} />
          </div>
          <h2>导入数据</h2>
          <p>从之前导出的 JSON 文件中导入数据。已存在的用户（同名）和顾客（同手机号）会跳过，验光记录和销售记录会追加导入。</p>

          <Radio.Group
            value={p.importMode}
            onChange={(e) => p.onModeChange(e.target.value)}
            className="import-mode-group"
          >
            <Radio value="merge">合并追加（不覆盖已有数据）</Radio>
            <Radio value="replace">全量替换（清空后导入）</Radio>
          </Radio.Group>

          <Alert
            type={isReplace ? 'error' : 'info'}
            showIcon
            message={isReplace ? '⚠️ 危险操作：将永久清空现有数据！' : '导入不会覆盖现有数据'}
            description={
              isReplace ? (
                <div style={{ marginTop: 6 }}>
                  <div style={{ fontWeight: 600, color: 'var(--ant-color-error)', marginBottom: 8, fontSize: '14px' }}>
                    此操作不可逆！执行后，您的所有顾客档案、验光记录、销售记录和非管理员账号将被彻底删除。
                  </div>
                  系统将使用您上传的文件重新构建数据库。仅当前管理员账号会被保留。请务必确认您已做好备份！
                </div>
              ) : (
                '同名用户和同手机号顾客已存在时会跳过；验光记录按顾客+日期+度数去重；销售记录按单号去重。'
              )
            }
            className={`import-alert ${isReplace ? 'import-alert-danger' : ''}`}
          />

          <div className="import-actions">
            <input ref={p.fileInputRef} type="file" accept=".json" hidden onChange={p.onFileChange} />
            <Button
              className="action-btn action-btn--file"
              onClick={() => p.fileInputRef.current?.click()}
              icon={<FolderOpenOutlined />}
            >
              <span className="file-btn-text">{p.selectedFile ? p.selectedFile.name : '选择 JSON 文件'}</span>
            </Button>
            <Button
              type="primary"
              danger={isReplace}
              loading={p.importLoading}
              disabled={!p.selectedFile}
              onClick={p.onImport}
              icon={<UploadOutlined />}
              className="action-btn action-btn--import"
            >
              确认导入
            </Button>
          </div>

          {p.importResult && (
            <div className="import-result-container">
              <div className={`import-result-header ${p.importResult.error ? 'import-error' : ''}`}>
                {p.importResult.message}
              </div>
              {!p.importResult.error && p.importResult.detail?.mode === 'merge' && (
                <Table
                  size="small"
                  bordered
                  dataSource={p.tableData}
                  rowKey="name"
                  pagination={false}
                  style={{ marginTop: 16 }}
                  columns={[
                    { title: '数据表', dataIndex: 'name' },
                    {
                      title: '新增数量',
                      dataIndex: 'inserted',
                      align: 'center',
                      render: (v: number) => <span className="text-success">+{v}</span>,
                    },
                    {
                      title: '跳过数量',
                      dataIndex: 'skipped',
                      align: 'center',
                      render: (v: number) => <span className="text-muted">{v}</span>,
                    },
                  ]}
                />
              )}
            </div>
          )}
        </section>
      </div>

      {/* System Reset */}
      <section className="glass-card reset-card">
        <div className="card-icon reset-icon">
          <WarningFilled style={{ fontSize: 28 }} />
        </div>
        <h2>清空系统数据</h2>
        <p>彻底删除全部顾客、验光记录、销售记录和非管理员账号。管理员账号会被保留。此操作不可撤销，请谨慎使用。</p>
        <Button
          danger
          type="primary"
          loading={p.resetLoading}
          onClick={p.onReset}
          icon={<DeleteOutlined />}
          className="action-btn action-btn--reset"
        >
          清空数据
        </Button>
      </section>
    </>
  );
};

export default DataCards;
