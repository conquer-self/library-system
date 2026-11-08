import axios from "axios";
import { ElMessage } from "element-plus";
import router from "@/router";

const request = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code === 200) {
      return res;
    } else {
      ElMessage.error(res.message || "操作失败");
      return Promise.reject(new Error(res.message));
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("userInfo");
      localStorage.removeItem("role");
      ElMessage.error("登录已过期，请重新登录");
      const currentPath = router.currentRoute.value.path;
      if (currentPath.startsWith("/admin")) {
        router.push("/admin/login");
      } else {
        router.push("/login");
      }
    } else {
      ElMessage.error(error.message || "网络错误");
    }
    return Promise.reject(error);
  }
);

export default request;
