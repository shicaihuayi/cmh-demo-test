// services/api.js
// 小程序专用API接口服务

const request = require('./request.js');

/**
 * 处理图片URL，确保正确的协议和路径
 * @param {string} imageUrl 
 * @returns {string}
 */
function processImageUrl(imageUrl) {
  if (!imageUrl) return '';
  
  // 如果是相对路径，添加服务器地址
  if (imageUrl.startsWith('/')) {
    // 开发环境使用HTTP，生产环境使用HTTPS
    const baseUrl = 'http://localhost:8080';
    return baseUrl + imageUrl;
  }
  
  // 如果已经是完整URL，直接返回
  return imageUrl;
}

/**
 * 处理用户信息中的图片URL
 * @param {object} userInfo 
 * @returns {object}
 */
function processUserInfo(userInfo) {
  if (userInfo && userInfo.imageUrl) {
    userInfo.imageUrl = processImageUrl(userInfo.imageUrl);
  }
  return userInfo;
}

/**
 * ============================
 * 用户相关接口
 * ============================
 */

/**
 * 微信登录
 * @param {string} code 微信登录code
 */
const wxLogin = (code, userInfo) => {
  return request({
    url: '/app/user/wxLogin',
    method: 'POST',
    data: {
      code,
      nickname: userInfo.nickName,
      avatarUrl: userInfo.avatarUrl,
      gender: userInfo.gender
    }
  });
};

/**
 * 手机号密码登录
 * @param {object} data 登录数据 {tel, pwd}
 */
const phoneLogin = (phone, password) => {
  return request({
    url: '/app/user/login',
    method: 'POST',
    data: { phone, password }
  }).then(response => {
    if (response && response.user) {
      response.user = processUserInfo(response.user);
    }
    return response;
  });
};

/**
 * 用户注册
 * @param {object} data 注册数据
 */
const userRegister = (userInfo) => {
  return request({
    url: '/app/user/register',
    method: 'POST',
    data: userInfo
  }).then(response => {
    if (response && response.user) {
      response.user = processUserInfo(response.user);
    }
    return response;
  });
};

/**
 * 获取当前用户信息
 * @param {number} id 用户ID
 */
const getUserInfo = () => {
  // 从本地存储获取用户ID
  const userInfo = wx.getStorageSync('userInfo');
  if (!userInfo || !userInfo.id) {
    return Promise.reject(new Error('用户未登录'));
  }

  return request({
    url: '/user/loadMyself',
    method: 'GET',
    data: { id: userInfo.id },
    needAuth: true
  }).then(response => {
    // 处理Map格式的返回数据
    if (response && response.isOk && response.user) {
      response.user = processUserInfo(response.user);
      return { user: response.user }; // 统一返回格式
    } else {
      throw new Error(response.msg || '获取用户信息失败');
    }
  });
};

/**
 * 更新用户信息
 * @param {object} userData 用户数据
 */
const updateUserInfo = (userInfo) => {
  // 后端接口要求所有参数都在查询字符串中，而不是请求体
  const queryString = Object.keys(userInfo)
    .map(key => {
      const value = userInfo[key];
      // 过滤掉null和undefined的值，避免出现 "key=undefined"
      if (value !== null && value !== undefined) {
        return `${encodeURIComponent(key)}=${encodeURIComponent(value)}`;
      }
      return null;
    })
    .filter(part => part !== null) // 再次过滤，移除空部分
    .join('&');

  return request({
    url: `/user/updateUser?${queryString}`,
    method: 'POST',
    data: {}, // 请求体为空
    needAuth: true
  }).then(response => {
    // 处理Map格式的返回数据
    if (response && response.isOk) {
      return { success: true, msg: response.msg };
    } else {
      throw new Error(response.msg || '更新用户信息失败');
    }
  });
};

/**
 * 发送短信验证码
 * @param {string} tel 手机号
 */
const sendSmsCode = (tel) => {
  return request({
    url: '/user/send',
    method: 'POST',
    data: { tel },
    needAuth: false // 通常发送验证码不需要登录
  });
};

/**
 * ============================
 * 动态相关接口
 * ============================
 */

/**
 * 获取动态列表（分页）
 * @param {object} params 查询参数 {pageNum, pageSize, title}
 */
const getArticleList = (page = 1, size = 10) => {
  return request({
    url: '/app/article/list',
    method: 'GET',
    data: { page: page, size: size }
  });
};

/**
 * 获取动态详情
 * @param {number} id 动态ID
 */
const getArticleDetail = (id) => {
  return request({
    url: '/app/article/detail',
    method: 'GET',
    data: { id }
  });
};

/**
 * 搜索动态
 * @param {object} params 搜索参数 {keyword, pageNum, pageSize}
 */
const searchArticle = (keyword, page = 1, size = 10) => {
  return request({
    url: '/app/article/search',
    method: 'GET',
    data: { keyword: keyword, page: page, size: size }
  });
};

/**
 * ============================
 * 课程相关接口
 * ============================
 */

/**
 * 获取已发布的课程列表（供小程序用户浏览）
 * @param {number} page 页码
 * @param {number} size 每页数量
 * @param {number|null} courseOrder 热门程度
 */
const getPublicCourseList = (page = 1, size = 10, courseOrder = null) => {
  const data = { page: page, size: size };
  if (courseOrder !== null) {
    data.courseOrder = courseOrder;
  }
  return request({
    url: '/app/course/list',
    method: 'GET',
    data: data
  });
};

/**
 * 获取课程详情
 * @param {number} id 课程ID
 */
const getCourseDetail = (id) => {
  return request({
    url: '/app/course/detail',
    method: 'GET',
    data: { id },
    needAuth: true,
    showLoading: true
  });
};

/**
 * 搜索课程
 * @param {string} keyword 搜索关键词
 * @param {number} page 页码
 * @param {number} size 每页数量
 */
const searchCourse = (keyword, page = 1, size = 10) => {
  return request({
    url: '/app/course/search',
    method: 'GET',
    data: { keyword: keyword, page: page, size: size }
  });
};

/**
 * 课程报名
 * @param {object} enrollData 报名数据
 */
const enrollCourse = (enrollData) => {
  return request({
    url: '/app/course/enroll',
    method: 'POST',
    data: enrollData,
    needAuth: true
  });
};

/**
 * 获取我的课程列表
 * @param {number} userId 用户ID
 */
const getMyCourses = (userId) => {
  return request({
    url: '/app/course/myEnrollments',
    method: 'GET',
    data: { userId },
    needAuth: true
  });
};

/**
 * ============================
 * 首页数据接口
 * ============================
 */

/**
 * 获取首页轮播图
 */
const getBannerList = () => {
  return request({
    url: '/app/home/banners',
    method: 'GET'
  }).catch(error => {
    console.log('轮播图接口暂未实现，返回空数组');
    return [];
  });
};

/**
 * 获取关于我们信息
 */
const getAboutInfo = () => {
  return request({
    url: '/app/home/about',
    method: 'GET'
  }).catch(error => {
    console.log('关于信息接口暂未实现，返回null');
    return null;
  });
};

/**
 * 获取会员单位列表
 */
const getMemberUnits = () => {
  return request({
    url: '/app/home/members',
    method: 'GET'
  }).catch(error => {
    console.log('会员单位接口暂未实现，返回空数组');
    return [];
  });
};

/**
 * ============================
 * 会议相关接口
 * ============================
 */

/**
 * 提交会议回执
 * @param {object} receiptData 回执数据
 */
const submitConferenceReceipt = (receiptData) => {
  return request({
    url: '/app/confer/receipt', // 后端接收回执的接口地址
    method: 'POST',
    data: receiptData,
    needAuth: true // 需要用户登录以获取用户信息
  });
};

module.exports = {
  // 用户相关
  wxLogin,
  phoneLogin,
  userRegister,
  getUserInfo,
  updateUserInfo,
  sendSmsCode,
  
  // 动态相关
  getArticleList,
  getArticleDetail,
  searchArticle,
  
  // 课程相关
  getPublicCourseList,
  getCourseDetail,
  searchCourse,
  enrollCourse,
  getMyCourses,
  
  // 首页相关
  getBannerList,
  getAboutInfo,
  getMemberUnits,
  
  // 会议相关
  submitConferenceReceipt,
  
  // 工具函数
  processImageUrl,
  processUserInfo
}; 