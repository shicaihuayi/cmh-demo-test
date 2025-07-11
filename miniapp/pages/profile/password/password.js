const { changePassword } = require('../../../services/user.js');

Page({
  data: {
    form: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    },
    loading: false
  },

  onLoad() {
    console.log('修改密码页面加载');
  },

  // 输入框变化
  onInputChange(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({
      [`form.${field}`]: e.detail.value
    });
  },

  // 表单验证
  validateForm() {
    const { form } = this.data;
    if (!form.oldPassword) {
      wx.showToast({ title: '请输入旧密码', icon: 'none' });
      return false;
    }
    if (!form.newPassword) {
      wx.showToast({ title: '请输入新密码', icon: 'none' });
      return false;
    }
    if (form.newPassword.length < 6) {
      wx.showToast({ title: '新密码至少为6位', icon: 'none' });
      return false;
    }
    if (form.newPassword !== form.confirmPassword) {
      wx.showToast({ title: '两次输入的密码不一致', icon: 'none' });
      return false;
    }
    if (form.oldPassword === form.newPassword) {
      wx.showToast({ title: '新密码不能与旧密码相同', icon: 'none' });
      return false;
    }
    return true;
  },

  async handleSubmit() {
    if (!this.validateForm()) {
      return;
    }

    this.setData({ loading: true });

    try {
      const userInfo = wx.getStorageSync('userInfo');
      if (!userInfo || !userInfo.id) {
        throw new Error('无法获取用户信息，请重新登录');
      }

      const { oldPassword, newPassword } = this.data.form;
      const result = await changePassword({
        id: userInfo.id,
        oldPassword,
        newPassword
      });

      if (result && result.isOk) {
        wx.showToast({
          title: '密码修改成功',
          icon: 'success',
          duration: 2000
        });

        // 清除登录信息，强制重新登录
        wx.removeStorageSync('userInfo');

        setTimeout(() => {
          wx.switchTab({
            url: '/pages/profile/profile'
          });
        }, 2000);
      } else {
        wx.showToast({
          title: result.msg || '密码修改失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('修改密码请求失败', error);
      const errMsg = error.msg || '操作失败，请重试';
      wx.showToast({ title: errMsg, icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  }
}); 