const { getCourseDetail, enrollCourse, processImageUrl } = require('../../../services/api.js');
const app = getApp();

Page({
  data: {
    // 课程详情数据
    courseId: null,
    course: {
      id: '',
      name: '',
      author: '',
      introduction: '',
      videoUrl: '',
      coverUrl: ''
    },
    
    // 加载状态
    isLoading: false,
    
    // 错误状态
    errorMessage: '',

    // 报名状态
    isEnrolled: false,
    
    // 报名处理中状态
    isEnrolling: false
  },

  onLoad(options) {
    console.log('课程详情页面加载，参数:', options)
    const courseId = options.id
    if (courseId) {
      this.setData({ courseId })
      this.loadCourseDetail(courseId)
    } else {
      console.error('错误：未获取到课程ID！')
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      })
    }
  },

  // 获取课程详情
  async loadCourseDetail(courseId) {
    if (this.data.isLoading) return
    this.setData({ isLoading: true, errorMessage: '' })

    try {
      const response = await getCourseDetail(courseId)
      console.log('获取课程详情响应:', response)
      
      if (response && response.isOk && response.course) {
        const course = {
          ...response.course,
          coverUrl: processImageUrl(response.course.coverUrl)
        }
        this.setData({ 
          course,
          isEnrolled: response.course.isEnrolled || false 
        })
      } else {
        this.setData({ errorMessage: '课程不存在或已下架' })
        wx.showToast({
          title: '课程不存在',
          icon: 'none'
        })
      }
      
    } catch (error) {
      console.error('获取课程详情失败:', error)
      this.setData({ errorMessage: '获取课程详情失败，请稍后重试' })
      wx.showToast({
        title: '获取失败',
        icon: 'none'
      })
    } finally {
      this.setData({ isLoading: false })
    }
  },

  // 立即报名
  async enrollNow() {
    // 如果已报名，则不执行任何操作
    if (this.data.isEnrolled) {
      return;
    }
    
    if (!this.data.courseId) {
      wx.showToast({
        title: '课程信息错误',
        icon: 'none'
      })
      return
    }

    // 检查登录状态
    if (app.globalData.userInfo && app.globalData.userInfo.id) {
        this.performEnroll(app.globalData.userInfo);
    } else {
        // 用户信息可能仍在加载中，设置一个回调来处理
        app.userInfoReadyCallback = res => {
            console.log('在详情页通过回调获取到用户信息:', res);
            if (res && res.id) {
                this.performEnroll(res);
            } else {
                this.showLoginModal();
            }
        };
        // 如果回调长时间未执行，可能用户根本没登录
        setTimeout(() => {
            if (!app.globalData.userInfo) {
                this.showLoginModal();
            }
        }, 1500); // 等待1.5秒
    }
  },

  async performEnroll(userInfo) {
      try {
          wx.showLoading({
              title: '报名中...'
          });

          const enrollData = {
              courseId: this.data.courseId,
              userId: userInfo.id
          };

          const response = await enrollCourse(enrollData);
          console.log('课程报名响应:', response);

          if (response && response.isOk) {
              wx.showToast({
                  title: '报名成功',
                  icon: 'success',
                  duration: 2000
              });
              // 报名成功后，更新页面状态
              this.setData({ isEnrolled: true });
          } else {
              wx.showToast({
                  title: response.msg || '报名失败',
                  icon: 'none'
              });
              // 如果提示已报名，同样更新状态
              if (response.msg && response.msg.includes('已报名')) {
                this.setData({ isEnrolled: true });
              }
          }

      } catch (error) {
          console.error('课程报名失败:', error);
          wx.showToast({
              title: '报名失败',
              icon: 'none'
          });
      } finally {
          wx.hideLoading();
      }
  },

  showLoginModal() {
      wx.showModal({
          title: '提示',
          content: '请先登录后再进行报名',
          confirmText: '去登录',
          success: (res) => {
              if (res.confirm) {
                  wx.switchTab({
                      url: '/pages/profile/profile'
                  });
              }
          }
      });
  },

  // 播放视频（如果有视频）
  playVideo() {
    if (!this.data.course || !this.data.course.videoUrl) {
      wx.showToast({
        title: '暂无视频资源',
        icon: 'none'
      });
      return;
    }

    // 这里可以实现视频播放逻辑
    // 可以跳转到专门的视频播放页面，或者使用视频播放组件
    console.log('播放视频:', this.data.course.videoUrl);
  },

  // 下拉刷新
  onPullDownRefresh() {
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    const options = currentPage.options;
    
    if (options.id) {
      this.loadCourseDetail(options.id).then(() => {
        wx.stopPullDownRefresh();
      });
    } else {
      wx.stopPullDownRefresh();
    }
  }
}) 