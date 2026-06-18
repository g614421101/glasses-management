import request from '@/utils/request';

export interface OptometryRecord {
  id: number;
  customerId: number;
  odSph: string | number;
  odCyl: string | number;
  odAxis: string;
  odVa: string;
  osSph: string | number;
  osCyl: string | number;
  osAxis: string;
  osVa: string;
  odPd: string;
  osPd: string;
  pdFar: string;
  pdNear: string;
  addPower: string | number;
  optometristName: string;
  remark: string;
  createTime?: string;
}

export const optometryAPI = {
  add: (data: Partial<OptometryRecord>): Promise<void> => request.post('/optometry/add', data),
  update: (data: Partial<OptometryRecord>): Promise<void> => request.put('/optometry/update', data),
  delete: (id: number): Promise<void> => request.delete('/optometry/' + id),
};
