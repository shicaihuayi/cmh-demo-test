// pages/profile/profile.js
const { getUserInfo } = require('../../services/api.js');

Page({
  data: {
    userInfo: null,
    isLoggedIn: false,
    // 使用有效的默认头像 - 使用微信小程序默认头像
    defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiByeD0iNTAiIGZpbGw9IiNGNUY1RjUiLz4KPGNpcmNsZSBjeD0iNTAiIGN5PSIzNSIgcj0iMTUiIGZpbGw9IiNDQ0NDQ0MiLz4KPHBhdGggZD0iTTMwIDc1QzMwIDY1IDM5IDU1IDUwIDU1UzcwIDY1IDcwIDc1IiBzdHJva2U9IiNDQ0NDQ0MiIHN0cm9rZS13aWR0aD0iOCIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIi8+Cjwvc3ZnPgo='
  },

  onLoad() {
    console.log('我的页面加载')
  },

  onShow() {
    console.log('我的页面显示')
    this.checkLoginStatus()
  },

  // 检查登录状态
  checkLoginStatus() {
    const userInfo = wx.getStorageSync('userInfo')
    console.log('当前存储的用户信息:', userInfo)
    
    const isLoggedIn = !!(userInfo && userInfo.id)
    console.log('是否已登录:', isLoggedIn)
    
    // 处理用户头像URL
    if (userInfo && userInfo.imageUrl) {
      userInfo.imageUrl = this.processImageUrl(userInfo.imageUrl)
    }
    
    this.setData({
      userInfo: userInfo || null,
      isLoggedIn: isLoggedIn
    })

    // 如果已登录，从服务器获取最新用户信息
    if (isLoggedIn) {
      this.getUserInfoFromServer()
    }
  },

  // 处理图片URL
  processImageUrl(imageUrl) {
    if (!imageUrl) return this.data.defaultAvatar
    
    // 如果是相对路径，添加服务器地址
    if (imageUrl.startsWith('/')) {
      return 'http://localhost:8080' + imageUrl
    }
    
    // 如果已经是完整URL，直接返回
    return imageUrl
  },

  // 从服务器获取用户信息
  async getUserInfoFromServer() {
    try {
      const response = await getUserInfo()
      console.log('从服务器获取的用户信息:', response)
      
      if (response && response.user) {
        let userInfo = response.user
        
        // 处理图片URL
        if (userInfo.imageUrl) {
          userInfo.imageUrl = this.processImageUrl(userInfo.imageUrl)
        }
        
        // 保存到本地存储
        wx.setStorageSync('userInfo', userInfo)
        console.log('用户信息已更新:', userInfo)
        
        this.setData({
          userInfo: userInfo,
          isLoggedIn: true
        })
      }
    } catch (error) {
      console.log('获取用户信息失败，使用本地缓存:', error.message || error)
      // 不显示错误提示，静默处理
    }
  },

  // 处理头像图片加载错误
  onAvatarError(e) {
    console.log('头像加载失败，使用默认头像')
    this.setData({
      'userInfo.imageUrl': this.data.defaultAvatar
    })
  },

  // 登录
  goToLogin() {
    wx.navigateTo({
      url: '/pages/profile/phone-login/phone-login'
    })
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '您确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除本地存储的用户信息
          wx.removeStorageSync('userInfo')
          wx.removeStorageSync('token')
          
          // 更新页面状态
          this.setData({
            userInfo: null,
            isLoggedIn: false
          })
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          })
        }
      }
    })
  },

  // 跳转到个人信息页面
  goToInfo() {
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }
    
    wx.navigateTo({
      url: '/pages/profile/info/info'
    })
  },

  // 跳转到我的课程页面
  goToMyCourses() {
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }
    
    wx.navigateTo({
      url: '/pages/profile/my-courses/my-courses'
    })
  },

  // 跳转到修改密码页面
  goToPassword() {
    if (!this.data.isLoggedIn) {
      this.goToLogin()
      return
    }
    
    wx.navigateTo({
      url: '/pages/profile/password/password'
    })
  },

  // 跳转到意见反馈页面
  goToFeedback() {
    wx.navigateTo({
      url: '/pages/profile/feedback/feedback'
    })
  },

  // 跳转到隐私政策页面
  goToPrivacyPolicy() {
    wx.navigateTo({
      url: '/pages/profile/privacy-policy/privacy-policy'
    })
  },

  // 跳转到用户协议页面
  goToUserAgreement() {
    wx.navigateTo({
      url: '/pages/profile/user-agreement/user-agreement'
    })
  },

  // 微信快速登录
  async quickLogin() {
    try {
      wx.showLoading({
        title: '授权中...'
      });

      // 1. 先获取用户微信资料，这必须由用户点击直接触发
      const userProfileRes = await new Promise((resolve, reject) => {
        wx.getUserProfile({
          desc: '用于完善会员资料', // 声明获取用户个人信息后的用途
          success: resolve,
          fail: reject,
        });
      });

      if (!userProfileRes.userInfo) {
        throw new Error('获取微信用户信息失败');
      }

      wx.showLoading({
        title: '登录中...'
      });

      // 2. 获取到用户信息后，再获取登录code
      const loginRes = await new Promise((resolve, reject) => {
        wx.login({
          success: resolve,
          fail: reject,
        });
      });

      if (!loginRes.code) {
        throw new Error('获取微信登录code失败');
      }

      // 3. 调用后端微信登录接口
      const { wxLogin } = require('../../services/api.js');
      const response = await wxLogin(loginRes.code, userProfileRes.userInfo);

      if (response && response.user) {
        let userInfo = response.user;
        
        // 处理图片URL
        if (userInfo.imageUrl) {
          userInfo.imageUrl = this.processImageUrl(userInfo.imageUrl);
        }
        
        // 保存用户信息和token
        wx.setStorageSync('userInfo', userInfo);
        if (response.token) {
          wx.setStorageSync('token', response.token);
          console.log('Token已保存:', response.token);
        }

        this.setData({
          userInfo: userInfo,
          isLoggedIn: true
        })

        wx.showToast({
          title: '登录成功',
          icon: 'success'
        })
      } else {
        throw new Error(response?.msg || '登录失败');
      }
    } catch (error) {
      console.error('微信登录失败:', error);
      // 对用户取消授权操作进行静默处理
      if (error.errMsg && error.errMsg.includes('getUserProfile:fail auth deny')) {
        // 不显示toast
      } else {
        wx.showToast({
          title: error.message || '登录失败',
          icon: 'none',
        });
      }
    } finally {
      wx.hideLoading();
    }
  }
}) 