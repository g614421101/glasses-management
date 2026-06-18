import request, { downloadBlob } from '@/utils/request';

export interface StatsParams {
  current: number;
  size: number;
  showAll?: boolean;
  startDate?: string;
  endDate?: string;
}

export interface StatsResult {
  totalRevenue: number;
  orderCount: number;
  records: {
    records: any[];
    total: number;
  };
}

export const statsAPI = {
  getStats: (params: StatsParams): Promise<StatsResult> => {
    return request.get('/sales/stats', { params });
  },

  exportRevenue: (showAll: boolean, startDate?: string, endDate?: string) => {
    let url = `/print/export/revenue?showAll=${showAll}`;
    if (!showAll && startDate && endDate) {
      url += `&startDate=${startDate}&endDate=${endDate}`;
    }
    return downloadBlob(url, 'revenue.xlsx');
  },
};
