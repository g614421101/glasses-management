import request from '@/utils/request';

export interface Customer {
  id: number;
  name: string;
  phone: string;
  gender: number;
  birthday: string;
  remark: string;
  createTime: string;
}

export interface CustomerPageParams {
  keyword?: string;
  current: number;
  size: number;
}

export interface CustomerPageResult {
  records: Customer[];
  total: number;
}

export const customerAPI = {
  getPage: (params: CustomerPageParams): Promise<CustomerPageResult> => {
    return request.get('/customer/page', { params });
  },
  
  getById: (id: number): Promise<Customer> => {
    return request.get('/customer/' + id);
  },
  
  add: (data: Partial<Customer>): Promise<void> => {
    return request.post('/customer/add', data);
  },
  
  update: (data: Partial<Customer>): Promise<void> => {
    return request.put('/customer/update', data);
  },
  
  delete: (id: number): Promise<void> => {
    return request.delete('/customer/' + id);
  },
};
