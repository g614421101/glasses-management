import axios, { InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
});

const LOGIN_REQUIRED_MESSAGE = '\u8bf7\u5148\u767b\u5f55';
const FORBIDDEN_MESSAGE = '\u65e0\u6743\u9650';
const NETWORK_ERROR_MESSAGE = '\u7f51\u7edc\u9519\u8bef';

const getDownloadFileName = (contentDisposition?: string, fallback = 'download') => {
  if (!contentDisposition) return fallback;

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }

  const plainMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
  if (plainMatch?.[1]) {
    return decodeURIComponent(plainMatch[1]);
  }

  return fallback;
};

const requestBlob = async (url: string) => {
  const token = localStorage.getItem('token');
  try {
    return await axios.get(url, {
      baseURL: '/api',
      responseType: 'blob',
      headers: token ? { Authorization: token } : undefined
    });
  } catch (error: any) {
    if (error.response?.status === 401) {
      ElMessage.error(LOGIN_REQUIRED_MESSAGE);
      localStorage.removeItem('token');
      window.location.href = '/#/login';
    } else if (error.response?.status === 403) {
      ElMessage.error(FORBIDDEN_MESSAGE);
    } else {
      ElMessage.error(error.message || NETWORK_ERROR_MESSAGE);
    }
    throw error;
  }
};

export const openBlob = async (url: string) => {
  const target = window.open('', '_blank');
  try {
    const response = await requestBlob(url);
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
  const response = await requestBlob(url);
  const fileName = getDownloadFileName(response.headers['content-disposition'], fallbackFileName);
  const blobUrl = window.URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(blobUrl);
};

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = token;
    }
    return config;
  },
  (error: any) => {
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;
    if (res.code === 200) {
      return res.data;
    } else {
      ElMessage.error(res.msg || 'Error');
      return Promise.reject(new Error(res.msg || 'Error'));
    }
  },
  (error: any) => {
    if (error.response && error.response.status === 401) {
      ElMessage.error(LOGIN_REQUIRED_MESSAGE);
      localStorage.removeItem('token');
      window.location.href = '/#/login';
    } else if (error.response && error.response.status === 403) {
      ElMessage.error(FORBIDDEN_MESSAGE);
    } else {
      ElMessage.error(error.message || NETWORK_ERROR_MESSAGE);
    }
    return Promise.reject(error);
  }
);

export default request;
