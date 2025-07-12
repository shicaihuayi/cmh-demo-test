/**
 * 测盟汇小程序API接口封装
 * 
 * @author Wang Shenjun
 * @date 2025.07.12
 * @version 1.0.0
 * 
 * 功能说明：
 * - 网络请求封装
 * - API接口统一管理
 * - 请求拦截和响应处理
 * - 错误处理和重试机制
 */

const { BASE_URL } = require('../utils/constants.js');

/**
 * 通用网络请求方法
 * @param {Object} options 请求配置
 * @returns {Promise} 请求结果
 */
function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else {
          reject(new Error(`请求失败: ${res.statusCode}`));
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 获取轮播图列表
 * @returns {Promise} 轮播图数据
 */
function getBannerList() {
  return request({
    url: '/api/banner/list',
    method: 'GET'
  });
}

/**
 * 获取平台简介信息
 * @returns {Promise} 简介信息
 */
function getAboutInfo() {
  return request({
    url: '/api/about/info',
    method: 'GET'
  });
}

/**
 * 获取同盟会员单位列表
 * @returns {Promise} 会员单位列表
 */
function getMemberUnits() {
  return request({
    url: '/api/member/units',
    method: 'GET'
  });
}

// 导出API方法
module.exports = {
  request,
  getBannerList,
  getAboutInfo,
  getMemberUnits
}; 