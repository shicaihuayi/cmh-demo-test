import type { AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import {ElNotification, ElMessageBox} from "element-plus";
import {errorCode} from "@/utils/errorCode";
import {tansParams} from "@/utils/utils";
// import useUserStore from "@/stores/user";
// 是否显示重新登录
export let isRelogin = {show: false};

//axios.defaults.headers["Content-Type"] = "application/json;charset=utf-8";
// 创建axios实例
const service = axios.create({
	baseURL: '/api',
	timeout: 10000,
	withCredentials: true
});

// 请求拦截器
service.interceptors.request.use(
	(config: AxiosRequestConfig) => {
		console.log("Request Config:", config);
		
		// 特别调试发布请求
		if (config.url && config.url.includes('submitForReview')) {
			console.log('=== 发布请求详细调试 ===');
			console.log('请求URL:', config.url);
			console.log('请求方法:', config.method);
			console.log('请求数据:', config.data);
			console.log('请求参数:', config.params);
			console.log('=======================');
		}
		
		// 从sessionStorage获取token
		const token = sessionStorage.getItem('token');
		const username = sessionStorage.getItem('username');
		const role = sessionStorage.getItem('role');
		
		console.log("用户认证信息:", {
			原始用户名: username,
			安全用户名: username,
			原始角色: role,
			安全角色: role,
			token: token
		});

		// 如果存在token，添加到请求头
		if (token && config.headers && !config.headers['Authorization']) {
			config.headers['Authorization'] = `Bearer ${token}`;
		}
		
		// 添加用户信息到请求头（对中文用户名进行URL编码）
		if (username && config.headers) {
			try {
				// 对用户名进行URL编码以支持中文字符
				config.headers["username"] = encodeURIComponent(username);
			} catch (e) {
				console.warn("用户名编码失败:", e);
				// 如果编码失败，使用Base64编码作为备选方案
				config.headers["username"] = btoa(unescape(encodeURIComponent(username)));
			}
		}
		if (role && config.headers) {
			config.headers["role"] = role;
		}
		
		// 如果是FormData格式，设置正确的Content-Type
		if (config.data instanceof FormData && config.headers) {
			config.headers['Content-Type'] = 'multipart/form-data';
		} else if (config.headers) {
			config.headers['Content-Type'] = 'application/json';
		}
		
		// get请求映射params参数
		if (config.method === "get" && config.params) {
			let url = config.url + "?" + tansParams(config.params);
			url = url.slice(0, -1);
			config.params = {};
			config.url = url;
		}
		return config;
	},
	(error: AxiosError) => {
		console.error("Request Error:", error);
		return Promise.reject(error);
	}
);

// 响应拦截器
service.interceptors.response.use(
	(response: AxiosResponse) => {
		console.log("Response Headers:", response.headers);
		console.log("Response Status:", response.status);
		console.log("Response Data:", response.data);
		
		// 处理响应
		const res = response.data;
		console.log("Response Code:", response.status, "Message:", res.msg);
		
		// 如果响应中包含新的token，更新sessionStorage
		const newToken = response.headers['authorization'] || response.headers['Authorization'];
		if (newToken) {
			sessionStorage.setItem('token', newToken.replace('Bearer ', ''));
		}
		
		// 如果响应码不是200，抛出错误
		if (response.status !== 200) {
			ElMessage.error(res.msg || '请求失败');
			return Promise.reject(new Error(res.msg || '请求失败'));
		}
		
		// 如果响应中有特定的错误信息，也抛出错误
		if (res.code === 401) {
			ElMessage.error('请先登录');
			return Promise.reject(new Error('请先登录'));
		}
		
		if (res.code === 403) {
			ElMessage.error('权限不足');
			return Promise.reject(new Error('权限不足'));
		}
		
		return res;
	},
	(error: AxiosError) => {
		console.error("Response Error:", error);
		ElMessage.error(error.message || '请求失败');
		return Promise.reject(error);
	}
);

export default service;
