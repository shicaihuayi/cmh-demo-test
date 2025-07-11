// @ts-nocheck
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
	(config: any) => {
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
		
		// 从sessionStorage获取用户信息
		const userId = sessionStorage.getItem('id');
		const username = sessionStorage.getItem('username');
		const role = sessionStorage.getItem('role');
		
		console.log("用户认证信息:", {
			原始用户名: username,
			安全用户名: username,
			原始角色: role,
			安全角色: role,
			用户ID: userId
		});

		// 如果用户信息存在，添加到请求头（按照后端LoginInterceptor期望的格式）
		if (userId && username && role && config.headers) {
			config.headers['X-User-Id'] = userId;
			// 对用户名进行URL编码以支持中文字符
			try {
				config.headers['X-Username'] = encodeURIComponent(username);
				console.log('用户名编码:', {
					原始用户名: username,
					编码后用户名: encodeURIComponent(username)
				});
			} catch (e) {
				console.warn('用户名编码失败:', e);
				// 如果编码失败，使用Base64编码作为备选方案
				config.headers['X-Username'] = btoa(unescape(encodeURIComponent(username)));
			}
			config.headers['X-User-Role'] = role;
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
	(error: any) => {
		console.error("Request Error:", error);
		return Promise.reject(error);
	}
);

// 响应拦截器
service.interceptors.response.use(
	(response: any) => {
		console.log("Response Headers:", response.headers);
		console.log("Response Status:", response.status);
		console.log("Response Data:", response.data);
		
		// 处理响应
		const res = response.data;
		console.log("Response Code:", response.status, "Message:", res.msg);
		
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
	(error: any) => {
		console.error("Response Error:", error);
		ElMessage.error(error.message || '请求失败');
		return Promise.reject(error);
	}
);

export default service;
