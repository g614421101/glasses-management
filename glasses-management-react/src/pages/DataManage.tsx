import React, { useState, useRef } from 'react';
import { Modal, message, Input } from 'antd';
import { dataAPI, ImportDetail } from '@/api/data';
import DataCards from './DataManage/DataCards';

const DataManage: React.FC = () => {
  const [exportLoading, setExportLoading] = useState(false);
  const [importLoading, setImportLoading] = useState(false);
  const [resetLoading, setResetLoading] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [importMode, setImportMode] = useState<'merge' | 'replace'>('merge');
  const [importResult, setImportResult] = useState<{ message: string; error: boolean; detail?: ImportDetail } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleExport = async () => {
    setExportLoading(true);
    try {
      await dataAPI.exportJson();
      message.success('数据导出成功');
    } catch (e: any) {
      message.error('导出失败: ' + (e.message || '未知错误'));
    } finally {
      setExportLoading(false);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setImportResult(null);
    }
  };

  const doImport = async () => {
    if (!selectedFile) return;
    setImportLoading(true);
    setImportResult(null);
    try {
      const res = await dataAPI.importJson(selectedFile, importMode);
      const isReplaceFinished = res.mode === 'replace';
      const totalInserted = res.sysUserInserted + res.customerInserted + res.optometryInserted + res.salesInserted;
      const msg = isReplaceFinished ? `成功导入 ${totalInserted} 条记录（全量替换）` : '导入完成';
      setImportResult({ message: msg, error: false, detail: res });
      message.success(msg);
      if (fileInputRef.current) fileInputRef.current.value = '';
      setSelectedFile(null);
    } catch (e: any) {
      const msg = e?.message || '导入失败';
      setImportResult({ message: msg, error: true });
      message.error(msg);
    } finally {
      setImportLoading(false);
    }
  };

  const handleImport = () => {
    if (!selectedFile) {
      message.warning('请先选择要导入的文件');
      return;
    }
    const isReplace = importMode === 'replace';
    Modal.confirm({
      title: '确认导入',
      content: isReplace
        ? '将清空全部顾客、验光记录、销售记录和非管理员账号，然后从文件导入所有数据。此操作不可撤销，确定要继续吗？'
        : '导入将追加数据，已存在的用户和顾客不会重复导入。确定要继续吗？',
      okType: 'danger',
      okText: isReplace ? '确认清空并导入' : '确认导入',
      onOk: doImport,
    });
  };

  const doReset = async () => {
    setResetLoading(true);
    try {
      const res = await dataAPI.reset();
      message.success(String(res || '数据已清空'));
    } catch (e: any) {
      message.error(e?.message || '清空失败');
    } finally {
      setResetLoading(false);
    }
  };

  const handleReset = () => {
    Modal.confirm({
      title: '确认清空数据',
      content: '将彻底删除全部顾客、验光记录、销售记录和非管理员账号。管理员账号会被保留。此操作不可撤销！',
      okType: 'danger',
      onOk: () => {
        let typed = '';
        Modal.confirm({
          title: '二次确认',
          content: (
            <div>
              <p>请输入「清空数据」以确认操作：</p>
              <Input onChange={(e) => { typed = e.target.value; }} placeholder="清空数据" />
            </div>
          ),
          okType: 'danger',
          okText: '确认清空',
          onOk: () => {
            if (typed !== '清空数据') {
              message.warning('输入不匹配，操作已取消');
              return Promise.reject();
            }
            return doReset();
          },
        });
      },
    });
  };

  const tableData = importResult?.detail
    ? [
        { name: '用户数据', inserted: importResult.detail.sysUserInserted, skipped: importResult.detail.sysUserSkipped },
        { name: '顾客档案', inserted: importResult.detail.customerInserted, skipped: importResult.detail.customerSkipped },
        { name: '验光记录', inserted: importResult.detail.optometryInserted, skipped: importResult.detail.optometrySkipped },
        { name: '销售记录', inserted: importResult.detail.salesInserted, skipped: importResult.detail.salesSkipped },
      ]
    : [];

  return (
    <div className="page-shell" style={{ maxWidth: 900, margin: '0 auto' }}>
      <section className="page-hero glass-card">
        <h1 className="page-heading" style={{ margin: 0 }}>数据管理</h1>
        <p className="page-title-en">DATA MANAGEMENT</p>
        <p style={{ margin: '12px 0 0', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
          导出全部业务数据为 JSON 文件，或从 JSON 文件导入数据。同名用户和手机号已有记录不会重复导入。
        </p>
      </section>
      <DataCards
        exportLoading={exportLoading}
        importLoading={importLoading}
        resetLoading={resetLoading}
        selectedFile={selectedFile}
        importMode={importMode}
        importResult={importResult}
        tableData={tableData}
        fileInputRef={fileInputRef}
        onExport={handleExport}
        onFileChange={handleFileChange}
        onModeChange={setImportMode}
        onImport={handleImport}
        onReset={handleReset}
      />
    </div>
  );
};

export default DataManage;
