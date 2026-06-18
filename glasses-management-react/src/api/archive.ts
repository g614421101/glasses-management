import request from '@/utils/request';

export interface TimelineItem {
  type: 'OPTOMETRY' | 'SALES';
  date: string;
  title: string;
  subtitle: string;
  data: any;
}

export const archiveAPI = {
  getTimeline: (customerId: number): Promise<TimelineItem[]> => {
    return request.get('/archive/' + customerId);
  },
};
