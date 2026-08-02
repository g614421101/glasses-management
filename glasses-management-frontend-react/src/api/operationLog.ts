import request from '@/utils/request';

export interface OperationLog {
  id: number;
  operatorId: number | null;
  operatorName: string | null;
  module: string;
  action: string;
  method: string;
  uri: string;
  description: string | null;
  params: string | null;
  status: number;
  message: string | null;
  costMs: number;
  ip: string | null;
  createTime: string;
}

export interface OperationLogPageParams {
  operatorName?: string;
  action?: string;
  startTime?: string;
  endTime?: string;
  current: number;
  size: number;
}

export interface OperationLogPageResult {
  records: OperationLog[];
  total: number;
}

export const operationLogAPI = {
  getPage: (params: OperationLogPageParams): Promise<OperationLogPageResult> => {
    return request.get('/operation-log/page', { params });
  },

  cleanup: (): Promise<{ count: number; days: number }> => {
    return request.post('/operation-log/cleanup');
  },
};
