import request, { downloadBlob } from '@/utils/request';

export interface ImportDetail {
  mode: string;
  sysUserInserted: number;
  sysUserSkipped: number;
  customerInserted: number;
  customerSkipped: number;
  optometryInserted: number;
  optometrySkipped: number;
  salesInserted: number;
  salesSkipped: number;
}

export const dataAPI = {
  exportJson: () => downloadBlob('/data/export', 'glasses_data.json'),

  importJson: (file: File, mode: 'merge' | 'replace'): Promise<ImportDetail> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('mode', mode);
    return request.post('/data/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  reset: (): Promise<string> => {
    return request.post('/data/reset');
  },
};
