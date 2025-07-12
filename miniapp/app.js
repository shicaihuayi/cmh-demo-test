/**
 * 测盟汇微信小程序主入口文件
 * 
 * @author Wang Shenjun
 * @date 2025.07.12
 * @version 1.0.0
 * 
 * 功能说明：
 * - 小程序应用初始化
 * - 全局数据管理
 * - 用户登录状态检查
 * - 应用生命周期管理
 */

// app.js
App({
  /**
   * 小程序启动时执行
   * 检查用户登录状态，初始化全局数据
   */
  onLaunch() {
    // 小程序启动时执行
    console.log('测盟汇小程序启动')
    
    // 检查用户登录状态
    this.checkLoginStatus()
  },

  /**
   * 小程序显示时执行
   */
  onShow() {
    // 小程序显示时执行
    console.log('测盟汇小程序显示')
  },

  /**
   * 小程序隐藏时执行
   */
  onHide() {
    // 小程序隐藏时执行
    console.log('测盟汇小程序隐藏')
  },

  /**
   * 检查用户登录状态
   * 从本地存储获取用户信息，更新全局登录状态
   */
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

  /**
   * 全局数据
   * 存储用户信息、登录状态、API地址等全局数据
   */
  globalData: {
    userInfo: null,           // 用户信息
    isLoggedIn: false,        // 登录状态
    apiBaseUrl: 'http://localhost:8080' // 后端API地址
  }
}) 