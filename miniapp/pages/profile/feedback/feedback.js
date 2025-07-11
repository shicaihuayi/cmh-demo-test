Page({
  data: {
    form: {
      type: '',
      content: '',
      contact: ''
    },
    feedbackTypes: ['功能建议', '问题反馈', '内容错误', '其他']
  },

  onLoad() {
    console.log('意见反馈页面加载')
  },

  // 反馈类型选择
  onTypeChange(e) {
    const index = e.detail.value
    this.setData({
      'form.type': this.data.feedbackTypes[index]
    })
  },

  // 取消类型选择
  onTypeCancel() {
    // 取消选择时不做任何操作
  },

  // 输入框变化
  onInputChange(e) {
    const { field } = e.currentTarget.dataset
    const { value } = e.detail
    this.setData({
      [`form.${field}`]: value
    })
  },

  // 表单验证
  validateForm() {
    const { form } = this.data
    
    if (!form.type.trim()) {
      wx.showToast({ title: '请选择反馈类型', icon: 'none' })
      return false
    }
    
    if (!form.content.trim()) {
      wx.showToast({ title: '请输入反馈内容', icon: 'none' })
      return false
    }
    
    if (form.content.length < 10) {
      wx.showToast({ title: '反馈内容至少10个字符', icon: 'none' })
      return false
    }
    
    return true
  },

  // 提交反馈
  submitFeedback() {
    if (!this.validateForm()) {
      return
    }
    
    wx.showLoading({ title: '提交中...' })
    
    // 硬编码模拟提交成功
    setTimeout(() => {
      wx.hideLoading()
      
      wx.showModal({
        title: '提交成功',
        content: '感谢您的反馈，我们会认真处理您的建议！',
        showCancel: false,
        success: (res) => {
          if (res.confirm) {
            // 重置表单
            this.setData({
              form: {
                type: '',
                content: '',
                contact: ''
              }
            })
            
            // 返回上一页
            wx.navigateBack()
          }
        }
      })
    }, 1500)
  }
}) 