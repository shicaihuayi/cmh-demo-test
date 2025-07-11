// utils/constants.js
// 项目常量配置

// API根路径
// 区分开发环境和生产环境
const env = 'dev'; // 'dev' or 'prod'

const baseURLMap = {
  dev: 'http://localhost:8080', // 修改：去掉/api前缀，直接对接后端
  prod: 'https://api.yourdomain.com', // 修改：去掉/api前缀
};

const BASE_URL = 'http://localhost:8080';

// 其他常量
const APP_NAME = '测盟汇';

// API相关配置
const API_CONFIG = {
  TIMEOUT: 10000, // 请求超时时间(毫秒)
  
  // HTTP状态码
  STATUS_CODES: {
    SUCCESS: 200,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    SERVER_ERROR: 500
  },
  
  // 业务状态码
  BUSINESS_CODES: {
    SUCCESS: 200,
    ERROR: 500,
    TOKEN_EXPIRED: 401
  },
  
  // API接口路径
  ENDPOINTS: {
    // 用户相关
    WX_LOGIN: '/api/wx-login',
    USER_INFO: '/api/user/info',
    UPDATE_USER: '/api/user/update',
    CHANGE_PASSWORD: '/api/user/change-password',
    
    // 会议相关
    CONFER_LIST: '/api/confer/page',
    CONFER_DETAIL: '/api/confer',
    CONFER_RECEIPT: '/api/confer/receipt',
    
    // 动态相关
    ARTICLE_LIST: '/api/article/page',
    ARTICLE_DETAIL: '/api/article',
    
    // 课程相关
    COURSE_LIST: '/api/course/page',
    COURSE_DETAIL: '/api/course',
    COURSE_ENROLL: '/api/course/enroll',
    MY_COURSES: '/api/course/my'
  }
};

// 存储相关常量
const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'userInfo',
  LOGIN_STATUS: 'isLoggedIn'
};

// 页面路径常量
const PAGE_PATHS = {
  INDEX: '/pages/index/index',
  MEETING_LIST: '/pages/meeting/meeting-list',
  MEETING_DETAIL: '/pages/meeting/meeting-detail',
  TRENDS_LIST: '/pages/trends/trends-list',
  TRENDS_DETAIL: '/pages/trends/trends-detail',
  COURSE_LIST: '/pages/course/course-list',
  COURSE_DETAIL: '/pages/course/course-detail',
  PROFILE: '/pages/profile/profile',
  LOGIN: '/pages/profile/login',
  EDIT_INFO: '/pages/profile/edit-info'
};

// 会议类型
const MEETING_TYPES = {
  CONFERENCE: 'conference', // 会议研讨
  STANDARD: 'standard',     // 标准定制
  TRAINING: 'training',     // 技术培训
  DEVELOPMENT: 'development', // 工具研发
  CHARITY: 'charity'        // 公益行动
};

// 会议类型显示名称
const MEETING_TYPE_NAMES = {
  [MEETING_TYPES.CONFERENCE]: '会议研讨',
  [MEETING_TYPES.STANDARD]: '标准定制',
  [MEETING_TYPES.TRAINING]: '技术培训',
  [MEETING_TYPES.DEVELOPMENT]: '工具研发',
  [MEETING_TYPES.CHARITY]: '公益行动'
};

module.exports = {
  BASE_URL,
  APP_NAME,
  API_CONFIG,
  STORAGE_KEYS,
  PAGE_PATHS,
  MEETING_TYPES,
  MEETING_TYPE_NAMES
}; 