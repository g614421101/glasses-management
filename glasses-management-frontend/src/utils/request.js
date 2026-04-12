import axios from 'axios';
import { ElMessage } from 'element-plus';

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
});

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code === 200) {
      return res.data;
    } else {
      ElMessage.error(res.msg || 'Error');
      return Promise.reject(new Error(res.msg || 'Error'));
    }
  },
  error => {
    if (error.response && error.response.status === 401) {
      ElMessage.error('请先登录');
      localStorage.removeItem('token');
      window.location.href = '/login';
    } else {
      ElMessage.error(error.message || '网络错误');
    }
    return Promise.reject(error);
  }
);

export default request;
