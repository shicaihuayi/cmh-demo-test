// app.js
App({
  onLaunch() {
    // 小程序启动时执行
    console.log('测盟汇小程序启动')
    
    // 检查用户登录状态
    this.checkLoginStatus()
  },

  onShow() {
    // 小程序显示时执行
    console.log('测盟汇小程序显示')
  },

  onHide() {
    // 小程序隐藏时执行
    console.log('测盟汇小程序隐藏')
  },

  // 检查用户登录状态
  checkLoginStatus() {
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo && userInfo.id) {
      // 用户已登录
      this.globalData.isLoggedIn = true
      this.globalData.userInfo = userInfo
    } else {
      this.globalData.isLoggedIn = false
      this.globalData.userInfo = null
    }
  },

  // 全局数据
  globalData: {
    userInfo: null,
    isLoggedIn: false,
    apiBaseUrl: 'http://localhost:8080' // 修改为后端实际地址
  }
}) 