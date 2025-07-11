const { phoneLogin, wxLogin } = require('../../../services/user.js');

Page({
  data: {
    phone: '',
    password: '',
    loading: false,
    showPassword: false
  },

  onLoad: function (options) {
    console.log('手机登录页面加载');
  },

  // 输入框事件
  onPhoneInput: function (e) {
    this.setData({
      phone: e.detail.value
    });
  },

  onPasswordInput: function (e) {
    this.setData({
      password: e.detail.value
    });
  },

  // 手机号密码登录
  onPhoneLogin: async function () {
    const { phone, password } = this.data;

    // 前端基础校验
    if (!phone) {
      return wx.showToast({ title: '请输入手机号', icon: 'none' });
    }
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(phone)) {
      return wx.showToast({ title: '手机号格式不正确', icon: 'none' });
    }
    if (!password) {
      return wx.showToast({ title: '请输入密码', icon: 'none' });
    }

    this.setData({ loading: true });

    try {
      const result = await phoneLogin({
        tel: phone,
        pwd: password
      });

      // 检查后端返回的数据格式
      if (result && result.isOk && result.user) {
        wx.setStorageSync('userInfo', result.user);
        if (result.token) {
          wx.setStorageSync('token', result.token);
          console.log('Token已保存:', result.token);
        }
        
        wx.showToast({
          title: '登录成功',
          icon: 'success'
        });

        // 登录成功后，延时1.5秒跳转到"我的"页面
        setTimeout(() => {
          wx.switchTab({
            url: '/pages/profile/profile'
          });
        }, 1500);
      } else {
        // isOk为false或缺少user对象，都视为登录失败
        wx.showToast({
          title: result.msg || '手机号或密码错误',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('登录请求失败', error);
      // 处理网络错误等异常情况
      const errMsg = error.msg || '登录失败，请检查网络';
      wx.showToast({ title: errMsg, icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  // 微信一键登录 (小程序端不再处理实际的微信登录逻辑)
  onWxLogin: function () {
    wx.showModal({
      title: '提示',
      content: '请在"我的"页面选择微信授权登录。',
      showCancel: false,
      confirmText: '知道了'
    });
  },

  // 切换密码显示
  togglePassword: function() {
    this.setData({
      showPassword: !this.data.showPassword
    });
  },

  // 忘记密码
  forgotPassword: function() {
    wx.showModal({
      title: '忘记密码',
      content: '请联系客服或通过注册时的邮箱找回密码',
      showCancel: false
    });
  },

  // 查看用户协议
  viewUserAgreement: function() {
    wx.navigateTo({
      url: '/pages/profile/user-agreement/user-agreement'
    });
  },

  // 查看隐私政策
  viewPrivacyPolicy: function() {
    wx.navigateTo({
      url: '/pages/profile/privacy-policy/privacy-policy'
    });
  },

  // 跳转到注册页面
  onGoRegister: function () {
    wx.navigateTo({
      url: '/pages/profile/register/register'
    });
  }
}); 