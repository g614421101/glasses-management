import axios from 'axios';
import { message } from 'antd';
import { getToken, removeToken } from './token';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

request.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers['Authorization'] = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code === 200) {
      return res.data;
    } else {
      if (res.code !== 409) {
        message.error(res.msg || 'Error');
      }
      const err = new Error(res.msg || 'Error') as any;
      err.code = res.code;
      err.data = res.data;
      return Promise.reject(err);
    }
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      message.error('请先登录');
      removeToken();
      window.location.href = '/#/login';
    } else if (error.response && error.response.status === 403) {
      message.error('无权限');
    } else {
      message.error(error.message || '网络错误');
    }
    return Promise.reject(error);
  }
);

export const openBlob = async (url: string) => {
  const token = getToken();
  const target = window.open('', '_blank');
  try {
    const response = await axios.get(url, {
      baseURL: '/api',
      responseType: 'blob',
      headers: token ? { Authorization: token } : undefined,
    });
    const blobUrl = window.URL.createObjectURL(response.data);
    if (target) {
      target.location.href = blobUrl;
    } else {
      window.open(blobUrl, '_blank');
    }
    window.setTimeout(() => window.URL.revokeObjectURL(blobUrl), 60_000);
  } catch (error) {
    target?.close();
    throw error;
  }
};

export const downloadBlob = async (url: string, fallbackFileName: string) => {
  const token = getToken();
  const response = await axios.get(url, {
    baseURL: '/api',
    responseType: 'blob',
    headers: token ? { Authorization: token } : undefined,
  });
  
  const contentDisposition = response.headers['content-disposition'];
  let fileName = fallbackFileName;
  if (contentDisposition) {
    const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8Match?.[1]) {
      fileName = decodeURIComponent(utf8Match[1]);
    } else {
      const plainMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
      if (plainMatch?.[1]) {
        fileName = decodeURIComponent(plainMatch[1]);
      }
    }
  }
  
  const blobUrl = window.URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(blobUrl);
};

export default request;
