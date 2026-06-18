import request from '@/utils/request';

export interface SalesRecord {
  id: number;
  customerId: number;
  optometryId: number | null;
  recordNo?: string;
  frameBrand: string;
  frameModel: string;
  frameQuantity: number;
  frameRetailPrice: number;
  framePrice: number;
  lensBrand: string;
  lensParams: string;
  lensQuantity: number;
  lensRetailPrice: number;
  lensPrice: number;
  totalRetailPrice: number;
  totalAmount: number;
  remark: string;
  operatorName?: string;
  createTime?: string;
}

export const salesAPI = {
  add: (data: Partial<SalesRecord>): Promise<void> => request.post('/sales/add', data),
  update: (data: Partial<SalesRecord>): Promise<void> => request.put('/sales/update', data),
  delete: (id: number): Promise<void> => request.delete('/sales/' + id),
};
