// services/request.js
// 网络请求封装

const { BASE_URL } = require('../utils/constants');

const { showError, showLoading, hideLoading } = require('../utils/util.js')

// 请求基础配置
const TIMEOUT = 10000

/**
 * 封装的网络请求方法
 * @param {Object} options 请求配置
 * @returns {Promise} 请求结果
 */
const request = (options) => {
  return new Promise((resolve, reject) => {
    // 显示加载提示
    if (options.showLoading) {
      wx.showLoading({
        title: '加载中...',
        mask: true,
      });
    }

    // 获取本地存储的认证信息
    const userInfo = wx.getStorageSync('userInfo');
    const token = wx.getStorageSync('token');
    const header = options.header || {};
    
    // 设置通用的Content-Type
    if (options.method === 'POST' || options.method === 'PUT') {
      header['Content-Type'] = 'application/json';
    }

    // 登录相关接口不需要验证登录状态
    const AUTH_EXEMPT_APIS = [
      '/app/user/login',
      '/app/user/wxLogin', 
      '/wx-login',
      '/app/user/register',
      '/app/user/send',
      '/article/list',
      '/article/detail',
      '/course/list',
      '/app/course/list',
      '/app/home/banners'
    ];
    
    const isAuthExempt = AUTH_EXEMPT_APIS.some(api => options.url.includes(api));
    
    // 如果需要认证且不在豁免列表中
    if (options.needAuth && !isAuthExempt) {
      // 优先使用Bearer token
      if (token) {
        header['Authorization'] = `Bearer ${token}`;
      }
      
      // 同时发送自定义请求头作为备用认证方式
      if (userInfo && userInfo.id && userInfo.name && userInfo.role) {
        header['X-User-Id'] = userInfo.id.toString();
        header['X-Username'] = userInfo.name;
        header['X-User-Role'] = userInfo.role;
      } else {
        // 需要认证但没有用户信息时，返回错误
        if (options.showLoading) {
          wx.hideLoading();
        }
        const error = { statusCode: 401, errMsg: '用户未登录' };
        reject(error);
        return;
      }
    } else if (!isAuthExempt && !options.needAuth) {
      // 对于非豁免的接口，但没有明确标记needAuth的，尝试添加认证信息（向后兼容）
      if (token) {
        header['Authorization'] = `Bearer ${token}`;
      }
      if (userInfo && userInfo.id && userInfo.name && userInfo.role) {
        header['X-User-Id'] = userInfo.id.toString();
        header['X-Username'] = userInfo.name;
        header['X-User-Role'] = userInfo.role;
      }
    }

    console.log(`发起请求: ${options.method || 'GET'} ${options.url}`, {
      data: options.data,
      needAuth: options.needAuth,
      hasToken: !!token,
      hasUserInfo: !!(userInfo && userInfo.id)
    });

    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      timeout: options.timeout || TIMEOUT,
      success: (res) => {
        console.log(`请求响应: ${options.url}`, res.data);
        
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const resData = res.data;
          // 兼容新版 AjaxResult 格式 { code, msg, data }
          if (resData.code !== undefined) {
            if (resData.code === 200) {
              resolve(resData.data);
            } else if (resData.code === 401) {
              // 401错误：清除登录状态
              console.log('认证失败，清除登录信息');
              wx.removeStorageSync('userInfo');
              wx.removeStorageSync('token');
              
              if (options.needAuth) {
                wx.showToast({
                  title: '登录已失效，请重新登录',
                  icon: 'none'
                });
                setTimeout(() => {
                  wx.switchTab({
                    url: '/pages/profile/profile'
                  });
                }, 1500);
              }
              reject(resData);
            } else {
              // 其他错误，根据情况决定是否显示toast
              if (options.showErrorToast !== false) {
                wx.showToast({ 
                  title: resData.msg || '操作失败', 
                  icon: 'none' 
                });
              }
              reject(resData);
            }
          } 
          // 兼容旧版 Map 格式 { isOk, msg, ... }
          else if (resData.isOk !== undefined) {
            if (resData.isOk) {
              resolve(resData); // 旧格式直接返回整个响应体
            } else {
              // 检查是否是认证相关错误
              if (resData.msg && resData.msg.includes('未登录')) {
                console.log('认证失败（Map格式），清除登录信息');
                wx.removeStorageSync('userInfo');
                wx.removeStorageSync('token');
                
                if (options.needAuth) {
                  wx.showToast({
                    title: '请先登录',
                    icon: 'none'
                  });
                  setTimeout(() => {
                    wx.switchTab({
                      url: '/pages/profile/profile'
                    });
                  }, 1500);
                }
              } else if (options.showErrorToast !== false) {
                wx.showToast({ 
                  title: resData.msg || '操作失败', 
                  icon: 'none' 
                });
              }
              reject(resData);
            }
          } 
          // 无法识别的成功响应格式
          else {
            resolve(resData);
          }
        } else {
          // HTTP状态码错误
          console.log(`HTTP错误 ${res.statusCode}:`, res);
          
          if (res.statusCode === 401) {
            // 认证失败，清除本地用户信息并跳转到登录页
            wx.removeStorageSync('userInfo');
            wx.removeStorageSync('token');
            
            if (options.needAuth) {
              wx.showToast({
                title: '请先登录',
                icon: 'none',
              });
              setTimeout(() => {
                wx.switchTab({
                  url: '/pages/profile/profile'
                });
              }, 1500);
            }
          } else {
            if (options.showErrorToast !== false) {
              wx.showToast({
                title: `请求失败 ${res.statusCode}`,
                icon: 'none',
              });
            }
          }
          reject(res);
        }
      },
      fail: (err) => {
        // 网络错误
        console.error('网络请求失败:', err);
        if (options.showErrorToast !== false) {
          wx.showToast({
            title: '网络连接异常',
            icon: 'none',
          });
        }
        reject(err);
      },
      complete: () => {
        // 请求完成，隐藏加载提示
        if (options.showLoading) {
          wx.hideLoading();
        }
      },
    });
  });
};

/**
 * GET请求
 */
const get = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'GET',
    data,
    ...options
  })
}

/**
 * POST请求
 */
const post = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

/**
 * PUT请求
 */
const put = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

/**
 * DELETE请求
 */
const del = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}

module.exports = request; 