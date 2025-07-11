const { getMyCourses, processImageUrl } = require('../../../services/api.js');
const { formatTime } = require('../../../utils/util.js');
const app = getApp();

Page({
  data: {
    courseList: [],
    isLoading: true,
    errorMessage: ''
  },

  onShow() {
    this.checkLoginAndLoadCourses();
  },

  checkLoginAndLoadCourses() {
    const userInfo = app.globalData.userInfo;
    if (userInfo && userInfo.id) {
      this.loadMyCourses(userInfo.id);
    } else {
      this.setData({ isLoading: false, courseList: [] });
      wx.showModal({
        title: '尚未登录',
        content: '请先登录以查看您报名的课程',
        confirmText: '去登录',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/profile/profile' });
          }
        }
      });
    }
  },

  async loadMyCourses(userId) {
    this.setData({ isLoading: true, errorMessage: '' });
    try {
      const response = await getMyCourses(userId);
      console.log("获取我的课程列表响应:", response);
      if (response && response.isOk) {
        const processedList = response.enrollments.map(item => ({
          ...item,
          processedCoverUrl: processImageUrl(item.courseCoverUrl),
          formattedEnrollTime: formatTime(new Date(item.enrollTime))
        }));
        this.setData({ courseList: processedList });
      } else {
        this.setData({ errorMessage: response.msg || '加载失败' });
      }
    } catch (error) {
      console.error("加载我的课程失败:", error);
      this.setData({ errorMessage: '网络错误，请稍后再试' });
    } finally {
      this.setData({ isLoading: false });
    }
  },

  continueLearning(e) {
    const courseId = e.currentTarget.dataset.id;
    if (courseId) {
      wx.navigateTo({
        url: `/pages/course/detail/detail?id=${courseId}`,
      });
    }
  },

  onPullDownRefresh() {
    this.checkLoginAndLoadCourses();
    wx.stopPullDownRefresh();
  }
}); 