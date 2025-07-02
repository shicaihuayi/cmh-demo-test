import axios, {type AxiosResponse} from "axios";
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
	// baseURL: "/dev-api",
	baseURL: "http://localhost:8080/",
	// 超时
	timeout: 10000,
});
service.defaults.headers["Content-Type"] = "application/json;charset=utf-8";
// request拦截器
service.interceptors.request.use(
	(config: any) => {
		// console.log("Token", getToken());
		// 设置 token
		// config.headers["Authorization"] = getToken(); // 让每个请求携带自定义token 请根据实际情况自行修改
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
							// useUserStore().logOut();
							reject(new Error("无效的会话，或者会话已过期，请重新登录。"));
						})
						.catch(() => {
							isRelogin.show = false;
							resolve(res); // 假设在这种情况下仍然返回原始响应
						});
				});
			}
			// 原始代码没有返回值，这里添加了返回Promise.reject
			return Promise.reject(new Error("无效的会话，或者会话已过期，请重新登录。"));
		} else if (code === 500) {
			ElMessage({message: msg, type: "error"});
			return Promise.reject(new Error(msg));
			// useUserStore().logOut(); // 如果需要注销，可以在这里调用
		} else if (code === 601) {
			ElMessage({message: msg, type: "warning"});
			return Promise.reject(new Error(msg));
		} else if (code !== 200) {
			ElNotification.error({title: msg});
			return Promise.reject(new Error(msg));
		} else {
			return Promise.resolve(res.data);
		}
	},
	error => {
		console.error("Axioserr:\n" + error);
		let {message} = error;
		if (message == "Network Error") {
			message = "后端接口连接异常";
		} else if (message.includes("timeout")) {
			message = "系统接口请求超时";
		} else if (message.includes("Request failed with status code")) {
			message = "系统接口" + message.substr(message.length - 3) + "异常";
		}
		ElMessage({message: message, type: "error", duration: 5 * 1000});
		return Promise.reject(error);
	}
);
export default service;
