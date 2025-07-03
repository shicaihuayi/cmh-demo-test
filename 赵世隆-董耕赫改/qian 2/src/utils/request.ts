import axios from 'axios';
import {ElNotification, ElMessageBox, ElMessage} from "element-plus";
import {errorCode} from "@/utils/errorCode";
import {tansParams} from "@/utils/utils";
// import useUserStore from "@/stores/user";
// 是否显示重新登录
export let isRelogin = {show: false};

//axios.defaults.headers["Content-Type"] = "application/json;charset=utf-8";
// 创建axios实例
const service = axios.create({
	// axios中请求配置有baseURL选项，表示请求URL公共部分
	// 使用代理路径替代直接访问后端，解决CORS问题
	baseURL: "/api",
	// 超时
	timeout: 10000,
});
service.defaults.headers["Content-Type"] = "application/json;charset=utf-8";

// request拦截器
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
		
		// 添加用户认证信息
		const userId = sessionStorage.getItem('id');
		const username = sessionStorage.getItem('username');
		const role = sessionStorage.getItem('role');
		
		if (userId && username && role) {
			try {
				// 使用更安全的方式处理中文字符，确保HTTP头兼容性
				// 方法1：将中文转换为UTF-8字节然后Base64编码
				const utf8Username = encodeURIComponent(username);
				// 使用简单的字符替换确保ASCII兼容性
				const safeUsername = utf8Username.replace(/[^\w-]/g, '_');
				const safeRole = role;
				
				// 设置认证头信息（确保只包含ASCII字符）
				config.headers['X-User-Id'] = userId;
				config.headers['X-Username'] = safeUsername;
				config.headers['X-User-Role'] = safeRole;
				config.headers['Authorization'] = `Bearer ${userId}`;
				
				console.log("用户认证信息:", {
					原始用户名: username,
					安全用户名: safeUsername,
					原始角色: role,
					安全角色: safeRole
				});
			} catch (error) {
				console.error("处理用户信息失败:", error);
				// 如果处理失败，只设置最基本的认证信息
				config.headers['X-User-Id'] = userId;
				config.headers['X-User-Role'] = role;
				config.headers['Authorization'] = `Bearer ${userId}`;
			}
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
	error => {
		console.log(error);
		Promise.reject(error);
	}
);

// 响应拦截器
service.interceptors.response.use(
	(res: AxiosResponse<any, any>) => {
		// 未设置状态码则默认成功状态
		const code = res.data.code || "200";
		// 获取错误信息
		const msg = res.data.msg|| errorCode[code]  || errorCode["default"];
		console.log(`output->Axios Response`, res);
		console.log(`Response Code: ${code}, Message: ${msg}`);
		
		// 特殊调试：租户修改请求
		if (res.config && res.config.url && res.config.url.includes('/company/update')) {
			console.log('=== 租户修改请求响应调试 ===');
			console.log('请求URL:', res.config.url);
			console.log('HTTP状态码:', res.status);
			console.log('响应数据:', res.data);
			console.log('解析后的code:', code);
			console.log('解析后的msg:', msg);
			console.log('res.data.isOk:', res.data.isOk);
			console.log('res.data.isOK:', res.data.isOK);
			console.log('准备返回的数据:', res.data);
			console.log('========================');
		}
		
		if (code === 401) {
			if (!isRelogin.show) {
				isRelogin.show = true;
				return new Promise((resolve, reject) => {
					ElMessageBox.confirm("登录状态已过期，您可以继续留在该页面，或者重新登录", "系统提示", {
						confirmButtonText: "重新登录",
						cancelButtonText: "取消",
						type: "warning",
					})
						.then(() => {
							// 清除用户信息并跳转到登录页
							sessionStorage.clear();
							window.location.href = '/login';
							reject(new Error("无效的会话，或者会话已过期，请重新登录。"));
						})
						.catch(() => {
							isRelogin.show = false;
							resolve(res); 
						});
				});
			}
			return Promise.reject(new Error("无效的会话，或者会话已过期，请重新登录。"));
		} else if (code === 500) {
			console.error('=== 500错误详细信息 ===');
			console.error('请求URL:', res.config?.url);
			console.error('请求方法:', res.config?.method);
			console.error('响应数据:', res.data);
			console.error('错误消息:', msg);
			console.error('===================');
			ElMessage({message: msg, type: "error"});
			return Promise.reject(new Error(msg));
		} else if (code === 601) {
			ElMessage({message: msg, type: "warning"});
			return Promise.reject(new Error(msg));
		} else if (code !== 200 && code !== "200") {
			console.error(`API Error - Code: ${code}, Message: ${msg}, URL: ${res.config?.url}`);
			ElNotification.error({title: msg});
			return Promise.reject(new Error(msg));
		} else {
			return Promise.resolve(res.data);
		}
	},
	(error: AxiosError) => {
		console.error("Axios Error:\n" + error);
		
		// 如果请求配置中包含 suppressErrorHandler，则直接将错误抛出，由业务代码自行处理
		if (error.config?.suppressErrorHandler) {
			return Promise.reject(error);
		}

		let { message } = error;
		if (message == "Network Error") {
			message = "后端接口连接异常，请检查网络连接";
		} else if (message.includes("timeout")) {
			message = "系统接口请求超时";
		} else if (message.includes("Request failed with status code")) {
			message = "系统接口" + message.substr(message.length - 3) + "异常";
		}
		ElMessage({message: message, type: "error", duration: 5 * 1000});

		if (error.response) {
			if (error.response.status === 500) {
				console.error('=== 500错误详细信息 ===');
				console.error('请求URL:', error.config?.url);
				console.error('请求方法:', error.config?.method);
				console.error('响应数据:', error.response.data);
				console.error('错误消息:', message);
				console.error('===================');
				ElMessage.error(message);
			}
		}
		return Promise.reject(error);
	}
);

export default service;
