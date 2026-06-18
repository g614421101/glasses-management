import request from '@/utils/request';

export interface RecycleBinData {
  customers: any[];
  optometryRecords: any[];
  salesRecords: any[];
}

export const recycleBinAPI = {
  list: (type = 'all'): Promise<RecycleBinData> => {
    return request.get('/recycle-bin', { params: { type } });
  },

  restore: (type: string, id: number): Promise<void> => {
    return request.post(`/recycle-bin/restore/${type}/${id}`);
  },

  purge: (type: string, id: number): Promise<void> => {
    return request.delete(`/recycle-bin/purge/${type}/${id}`);
  },

  purgeExpired: (): Promise<void> => {
    return request.delete('/recycle-bin/purge-expired');
  },

  empty: (): Promise<{ customers: number; optometry: number; sales: number }> => {
    return request.delete('/recycle-bin/empty');
  },
};