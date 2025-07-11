// pages/meeting/receipt/receipt.js
const { submitConferenceReceipt, getUserInfo } = require('../../../services/api.js');

Page({

  /**
   * 页面的初始数据
   */
  data: {
    // 会议名称
    conferName: null,
    
    // 表单数据
    form: {
      company: '',
      name: '',
      gender: 'male', // 'male' or 'female'
      phone: '',
      email: '',
      arrivalMethod: '',
      arrivalFlightOrTrain: '',
      arrivalTime: ''
    },
    // 到达方式选项
    arrivalMethods: ['飞机', '火车', '汽车', '其他'],
    
    // 提交状态
    isSubmitting: false,
    
    // 用户信息加载状态
    isLoadingUserInfo: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  async onLoad(options) {
    // options.conferName 可以获取从详情页传来的会议名称
    console.log('加载参会回执页面，会议名称:', options.conferName);
    if (options.conferName) {
      this.setData({ conferName: decodeURIComponent(options.conferName) });
    }
    
    // 自动获取用户信息并填充表单
    await this.loadUserInfoAndFillForm();
  },

  /**
   * 获取用户信息并自动填充表单
   */
  async loadUserInfoAndFillForm() {
    try {
      this.setData({ isLoadingUserInfo: true });
      
      const response = await getUserInfo();
      console.log('获取到的用户信息:', response);
      
      if (response && response.user) {
        const user = response.user;
        
        // 自动填充表单
        const updatedForm = {
          company: user.companyName || '',
          name: user.name || '',
          gender: user.gender === '女' ? 'female' : 'male',
          phone: user.tel || '',
          email: user.email || '',
          arrivalMethod: this.data.form.arrivalMethod, // 保持原值，由用户选择
          arrivalFlightOrTrain: this.data.form.arrivalFlightOrTrain, // 保持原值，由用户填写
          arrivalTime: this.data.form.arrivalTime // 保持原值，由用户选择
        };
        
        this.setData({ form: updatedForm });
        
        console.log('已自动填充用户信息:', updatedForm);
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
      
      // 如果是认证失败，提示用户登录
      if (error.statusCode === 401 || (error.msg && error.msg.includes('未登录'))) {
        wx.showModal({
          title: '需要登录',
          content: '请先登录后再填写参会回执',
          confirmText: '去登录',
          success: (res) => {
            if (res.confirm) {
              wx.switchTab({
                url: '/pages/profile/profile'
              });
            }
          }
        });
      } else {
        wx.showToast({
          title: '获取用户信息失败',
          icon: 'none'
        });
      }
    } finally {
      this.setData({ isLoadingUserInfo: false });
    }
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  // 处理输入框变化
  onInputChange(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`form.${field}`]: e.detail.value
    });
  },
  
  // 处理性别选择
  onGenderChange(e) {
    const genders = ['male', 'female'];
    this.setData({
      'form.gender': genders[e.detail.value]
    });
  },

  // 处理到达方式选择
  onArrivalMethodChange(e) {
    this.setData({
      'form.arrivalMethod': this.data.arrivalMethods[e.detail.value]
    });
  },
  
  // 处理到达时间选择
  onArrivalTimeChange(e) {
    this.setData({
      'form.arrivalTime': e.detail.value
    });
  },

  // 表单验证
  validateForm() {
    const { form } = this.data;
    
    if (!form.company.trim()) {
      wx.showToast({
        title: '请填写单位信息',
        icon: 'none'
      });
      return false;
    }
    
    if (!form.name.trim()) {
      wx.showToast({
        title: '请填写姓名',
        icon: 'none'
      });
      return false;
    }
    
    if (!form.phone.trim()) {
      wx.showToast({
        title: '请填写手机号码',
        icon: 'none'
      });
      return false;
    }
    
    // 验证手机号格式
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(form.phone)) {
      wx.showToast({
        title: '请填写正确的手机号码',
        icon: 'none'
      });
      return false;
    }
    
    // 验证邮箱格式（如果填写了邮箱）
    if (form.email.trim()) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(form.email)) {
        wx.showToast({
          title: '请填写正确的邮箱地址',
          icon: 'none'
        });
        return false;
      }
    }
    
    return true;
  },

  // 提交表单
  async submitForm() {
    console.log('提交的表单数据:', this.data.form);
    
    // 验证表单
    if (!this.validateForm()) {
      return;
    }
    
    if (!this.data.conferName) {
      wx.showToast({
        title: '会议信息缺失',
        icon: 'none'
      });
      return;
    }
    
    if (this.data.isSubmitting) {
      return; // 防止重复提交
    }

    try {
      this.setData({ isSubmitting: true });
      
      // 构造提交数据
      const submitData = {
        conferenceName: this.data.conferName,
        company: this.data.form.company.trim(),
        name: this.data.form.name.trim(),
        gender: this.data.form.gender === 'male' ? '男' : '女',
        phone: this.data.form.phone.trim(),
        email: this.data.form.email.trim(),
        arrivalMethod: this.data.form.arrivalMethod,
        arrivalFlightOrTrain: this.data.form.arrivalFlightOrTrain.trim(),
        arrivalTime: this.data.form.arrivalTime
      };
      
      // 调用API提交数据
      await submitConferenceReceipt(submitData);
      
      wx.showToast({
        title: '提交成功',
        icon: 'success',
        duration: 2000
      });
      
      // 提交成功后返回上一页
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      
    } catch (error) {
      console.error('提交回执失败:', error);
      wx.showToast({
        title: error.message || '提交失败，请稍后重试',
        icon: 'none'
      });
    } finally {
      this.setData({ isSubmitting: false });
    }
  }
})