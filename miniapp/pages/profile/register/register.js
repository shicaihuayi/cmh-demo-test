const { register } = require('../../../services/user.js');

Page({
  data: {
    form: {
      nickname: '',
      tel: '',
      pwd: '',
      confirmPassword: ''
    },
    agreed: false,
    loading: false
  },

  onLoad(options) {
    console.log('注册页面加载')
    
    // 如果是从微信授权跳转过来的，可能会带有用户信息
    if (options.fromWechat) {
      const userInfo = wx.getStorageSync('tempUserInfo')
      if (userInfo) {
        this.setData({
          'form.avatarUrl': userInfo.avatarUrl,
          'form.nickName': userInfo.nickName
        })
        // 清除临时存储
        wx.removeStorageSync('tempUserInfo')
      }
    }
  },

  // 输入框变化
  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },

  // 同意协议
  onAgreeChange(e) {
    this.setData({
      agreed: e.detail.value.length > 0
    })
  },

  // 表单验证
  validateForm() {
    const { form, agreed } = this.data
    
    if (!form.nickname.trim()) {
      return wx.showToast({ title: '请输入昵称', icon: 'none' })
    }
    
    if (!form.tel.trim()) {
      return wx.showToast({ title: '请输入手机号', icon: 'none' })
    }
    
    const phoneReg = /^1[3-9]\d{9}$/
    if (!phoneReg.test(form.tel)) {
      return wx.showToast({ title: '手机号格式不正确', icon: 'none' })
    }
    
    if (!form.pwd) {
      return wx.showToast({ title: '请输入密码', icon: 'none' })
    }
    
    if (form.pwd.length < 6) {
      return wx.showToast({ title: '密码至少为6位', icon: 'none' })
    }
    
    if (form.pwd !== form.confirmPassword) {
      return wx.showToast({ title: '两次输入的密码不一致', icon: 'none' })
    }
    
    if (!agreed) {
      return wx.showToast({ title: '请阅读并同意用户协议和隐私政策', icon: 'none' })
    }
    
    return true
  },

  // 完成注册
  async handleRegister() {
    if (!this.validateForm()) {
      return
    }
    
    this.setData({ loading: true })

    try {
      const { nickname, tel, pwd } = this.data.form
      // 后端使用name作为登录名，这里我们约定使用昵称作为name
      const result = await register({
        name: nickname,
        nickname: nickname,
        tel: tel,
        pwd: pwd,
        role: "0" // 明确角色为普通用户
      })

      if (result && result.isOk) {
        wx.showToast({
          title: '注册成功',
          icon: 'success',
          duration: 2000
        })
        
        // 注册成功后，引导用户去登录
        setTimeout(() => {
          wx.redirectTo({
            url: '/pages/profile/phone-login/phone-login'
          })
        }, 2000)
      } else {
        wx.showToast({
          title: result.msg || '注册失败',
          icon: 'none'
        })
      }
    } catch (error) {
      console.error('注册请求失败', error)
      const errMsg = error.msg || '注册失败，请检查网络或联系客服'
      wx.showToast({ title: errMsg, icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  // 查看用户协议
  viewUserAgreement() {
    wx.navigateTo({
      url: '/pages/profile/user-agreement/user-agreement'
    })
  },

  // 查看隐私政策
  viewPrivacyPolicy() {
    wx.navigateTo({
      url: '/pages/profile/privacy-policy/privacy-policy'
    })
  }
}) 