const { getUserInfo, updateUserInfo, sendSmsCode } = require('../../../services/api.js');
const { BASE_URL } = require('../../../utils/constants.js');

// Mappings for gender
const genderDisplayToValue = { '男': 'man', '女': 'woman', '未知': '' };
const genderValueToDisplay = { 'man': '男', 'woman': '女', '': '未知' };

Page({
  data: {
    userInfo: null,
    userInfoDisplay: {}, // For values that need mapping for display
    originalUserInfo: null, // Used to compare if phone number changed
    genderOptions: ['男', '女', '未知'],
    code: '', // 验证码
    isCountingDown: false,
    countdown: 60,
    tempAvatarPath: null,
    // 添加默认头像
    defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiByeD0iNTAiIGZpbGw9IiNGNUY1RjUiLz4KPGNpcmNsZSBjeD0iNTAiIGN5PSIzNSIgcj0iMTUiIGZpbGw9IiNDQ0NDQ0MiLz4KPHBhdGggZD0iTTMwIDc1QzMwIDY1IDM5IDU1IDUwIDU1UzcwIDY1IDcwIDc1IiBzdHJva2U9IiNDQ0NDQ0MiIHN0cm9rZS13aWR0aD0iOCIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIi8+Cjwvc3ZnPgo='
  },

  onShow() {
    this.fetchUserInfo();
  },

  // 处理图片URL，解决HTTP协议问题
  processImageUrl(imageUrl) {
    if (!imageUrl) return this.data.defaultAvatar;
    
    // 如果是临时路径（选择的新图片），直接返回
    if (imageUrl.startsWith('wxfile://') || imageUrl.startsWith('http://tmp/')) {
      return imageUrl;
    }
    
    // 如果是HTTP协议，在开发环境下直接返回（小程序会处理）
    // 在生产环境中应该换成HTTPS
    if (imageUrl.startsWith('http://')) {
      console.warn('检测到HTTP图片URL，建议升级到HTTPS:', imageUrl);
      return imageUrl; // 开发环境下保持原样
    }
    
    // 如果是相对路径，添加服务器地址
    if (imageUrl.startsWith('/')) {
      return BASE_URL + imageUrl;
    }
    
    return imageUrl;
  },

  // 从后端获取最新用户信息
  async fetchUserInfo() {
    try {
      wx.showLoading({ title: '加载中...' });
      
      // 先从本地存储获取用户信息作为默认值
      const localUserInfo = wx.getStorageSync('userInfo');
      if (localUserInfo) {
        // 处理头像URL
        localUserInfo.imageUrl = this.processImageUrl(localUserInfo.imageUrl);
        
        this.setData({ 
          userInfo: localUserInfo,
          originalUserInfo: JSON.parse(JSON.stringify(localUserInfo))
        });
        wx.hideLoading(); // 如果有本地数据，立即隐藏加载状态
      }
      
      // 尝试从服务器获取最新信息
      try {
        const response = await getUserInfo();
        console.log('获取用户信息响应:', response);
        
        if (response && response.user) {
          const userInfo = response.user;
          
          // 处理头像URL，解决HTTP协议问题
          userInfo.imageUrl = this.processImageUrl(userInfo.imageUrl);
          
          // 更新显示和本地存储
          wx.setStorageSync('userInfo', userInfo);
          this.setData({ 
            userInfo: userInfo,
            originalUserInfo: JSON.parse(JSON.stringify(userInfo))
          });
          
          console.log('用户信息加载成功');
        }
      } catch (apiError) {
        console.log('从服务器获取用户信息失败，使用本地缓存:', apiError.message || apiError);
        // 如果没有本地缓存，显示友好的错误信息
        if (!localUserInfo) {
          throw new Error('请先登录');
        }
        // 有本地缓存的情况下，不显示错误，静默使用缓存
      }
      
    } catch (error) {
      console.error('加载用户信息失败', error);
      
      // 检查是否是登录问题
      if (error.message === '用户未登录' || error.message === '请先登录') {
        wx.showModal({
          title: '提示',
          content: '请先登录',
          showCancel: false,
          success: () => {
            wx.navigateBack();
          }
        });
      } else {
        wx.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        });
      }
    } finally {
      wx.hideLoading();
    }
  },

  // 处理输入变化
  handleInputChange(e) {
    const { field } = e.currentTarget.dataset;
    const value = e.detail.value;
    if (field === 'code') {
        this.setData({
          code: value
        });
    } else {
        this.setData({
          [`userInfo.${field}`]: value
        });
    }
  },

  // 性别选择
  onGenderChange(e) {
    const selectedDisplay = this.data.genderOptions[e.detail.value];
    const genderValue = genderDisplayToValue[selectedDisplay];
    
    console.log('选择性别:', selectedDisplay, '值:', genderValue);
    
    this.setData({
      'userInfo.sex': genderValue,
      'userInfoDisplay.sex': selectedDisplay
    });
    
    // 立即提示用户选择已生效
    wx.showToast({
      title: `已选择：${selectedDisplay}`,
      icon: 'none',
      duration: 1000
    });
  },

  // 选择头像
  chooseAvatar() {
    // 先保存旧头像URL，以便用户取消选择时可以恢复
    const originalImageUrl = this.data.userInfo.imageUrl;

    // 立即清除当前显示的头像，显示默认头像，提供即时反馈
    this.setData({
      'userInfo.imageUrl': this.data.defaultAvatar,
      tempAvatarPath: null
    });

    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.setData({
          'userInfo.imageUrl': tempFilePath,
          tempAvatarPath: tempFilePath
        });
        
        wx.showToast({
          title: '头像已选择',
          icon: 'success',
          duration: 1000
        });
      },
      fail: (err) => {
        // 如果用户取消选择或选择失败，恢复原来的头像
        this.setData({
          'userInfo.imageUrl': originalImageUrl
        });
        console.error("选择图片失败", err);
        if (err.errMsg.indexOf('cancel') === -1) {
          wx.showToast({
            title: '选择图片失败',
            icon: 'none'
          });
        }
      }
    });
  },

  // 头像加载错误处理
  onAvatarError(e) {
    console.warn('头像加载失败:', e.detail);
    this.setData({
      'userInfo.imageUrl': this.data.defaultAvatar
    });
  },

  // 保存用户信息
  async saveInfo() {
    // 检查必要信息
    if (!this.data.userInfo || !this.data.userInfo.id) {
      wx.showToast({
        title: '用户信息不完整',
        icon: 'none'
      });
      return;
    }

    // 验证手机号格式
    const telPattern = /^1[3-9]\d{9}$/;
    if (this.data.userInfo.phone && !telPattern.test(this.data.userInfo.phone)) {
      wx.showToast({
        title: '请输入正确的手机号',
        icon: 'none'
      });
      return;
    }

    // 验证邮箱格式
    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    if (this.data.userInfo.email && !emailPattern.test(this.data.userInfo.email)) {
      wx.showToast({
        title: '请输入正确的邮箱地址',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '保存中...' });

    try {
      let relativeImageUrl = this.data.userInfo.imageUrl;

      // Step 1: 如果选择了新头像，先上传
      if (this.data.tempAvatarPath) {
        try {
          relativeImageUrl = await this.uploadFile(this.data.tempAvatarPath);
          console.log('头像上传成功:', relativeImageUrl);
        } catch (uploadError) {
          console.error('头像上传失败:', uploadError);
          wx.hideLoading();
          wx.showToast({
            title: '头像上传失败',
            icon: 'none'
          });
          return;
        }
      } else if (relativeImageUrl && (relativeImageUrl.startsWith(BASE_URL) || relativeImageUrl.startsWith('http'))) {
        // 转换为相对路径，去掉BASE_URL或http://localhost:8080部分
        if (relativeImageUrl.startsWith(BASE_URL)) {
          relativeImageUrl = relativeImageUrl.substring(BASE_URL.length);
        } else if (relativeImageUrl.startsWith('http://localhost:8080')) {
          relativeImageUrl = relativeImageUrl.substring('http://localhost:8080'.length);
        }
      }

      // Step 2: 准备更新数据
      const { userInfo } = this.data;
      const updateData = {
        id: userInfo.id,
        nickname: userInfo.nickname || '',
        email: userInfo.email || '',
        sex: userInfo.sex || '',
        imageUrl: relativeImageUrl || '',
        tel: userInfo.phone || userInfo.tel || '', // 统一使用tel字段
        name: userInfo.name || '',
        role: userInfo.role || 'user' // 保持原有角色
      };
      
      console.log('准备更新用户信息:', updateData);
      
      // 如果手机号被修改，则附带验证码
      if (this.data.originalUserInfo && 
          updateData.tel !== this.data.originalUserInfo.phone &&
          updateData.tel !== this.data.originalUserInfo.tel) {
        if (!this.data.code) {
          wx.showToast({ title: '请输入验证码', icon: 'none' });
          wx.hideLoading();
          return;
        }
        updateData.code = this.data.code;
      }

      // Step 3: 调用更新API
      try {
        const response = await updateUserInfo(updateData);
        console.log('更新用户信息响应:', response);
        
        if (response && response.success) {
          // 更新成功，刷新本地存储
          wx.setStorageSync('userInfo', { ...this.data.userInfo, ...updateData });
          
          wx.showToast({
            title: '保存成功',
            icon: 'success',
            duration: 1500
          });
          
          // 清除临时数据
          this.setData({
            tempAvatarPath: null,
            code: '',
            originalUserInfo: JSON.parse(JSON.stringify({ ...this.data.userInfo, ...updateData }))
          });

          // 1.5秒后返回上一页，让用户看到提示
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);

        } else {
          throw new Error(response?.msg || '保存失败');
        }
      } catch (error) {
        console.error('保存用户信息失败:', error);
        
        // 根据错误类型显示不同的提示
        let errorMessage = '系统繁忙，请稍后再试';
        if (error.message.includes('验证码')) {
          errorMessage = '验证码错误或已过期';
        } else if (error.message.includes('tel')) {
          errorMessage = '请输入正确的手机号';
        } else if (error.message.includes('email')) {
          errorMessage = '请输入正确的邮箱地址';
        } else if (error.message.includes('nickname')) {
          errorMessage = '昵称不能为空';
        } else if (error.response && error.response.status === 401) {
          errorMessage = '登录已过期，请重新登录';
        }
        
        wx.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 2000
        });
      } finally {
        wx.hideLoading();
      }

    } catch (error) {
      console.error('保存用户信息失败:', error);
      
      // 根据错误类型显示不同的提示
      let errorMessage = '系统繁忙，请稍后再试';
      if (error.message.includes('验证码')) {
        errorMessage = '验证码错误或已过期';
      } else if (error.message.includes('tel')) {
        errorMessage = '请输入正确的手机号';
      } else if (error.message.includes('email')) {
        errorMessage = '请输入正确的邮箱地址';
      } else if (error.message.includes('nickname')) {
        errorMessage = '昵称不能为空';
      } else if (error.response && error.response.status === 401) {
        errorMessage = '登录已过期，请重新登录';
      }
      
      wx.showToast({
        title: errorMessage,
        icon: 'none',
        duration: 2000
      });
    }
  },
  
  // 上传文件
  uploadFile(filePath) {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token');
      
      wx.uploadFile({
        url: `${BASE_URL}/file/upload`,
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: (res) => {
          try {
            console.log('上传响应:', res);
            if (res.statusCode === 200) {
              const data = JSON.parse(res.data);
              if (data.code === 200 || data.isOk) {
                resolve(data.data || data.url);
              } else {
                wx.showToast({
                  title: data.msg || '上传失败',
                  icon: 'none'
                });
                reject(new Error(data.msg || '上传失败'));
              }
            } else {
              wx.showToast({ title: '上传失败，请检查网络', icon: 'none' });
              reject(new Error('网络错误'));
            }
          } catch (parseError) {
            console.error('解析上传响应失败:', parseError);
            wx.showToast({ title: '上传失败', icon: 'none' });
            reject(parseError);
          }
        },
        fail: (err) => {
          console.error('上传文件失败:', err);
          wx.showToast({ title: '上传失败，请检查网络', icon: 'none' });
          reject(err);
        }
      });
    });
  },

  // 发送验证码
  async sendCode() {
    const phone = this.data.userInfo.phone || this.data.userInfo.tel;
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入有效的手机号', icon: 'none' });
      return;
    }

    if (this.data.isCountingDown) {
      return;
    }

    this.setData({ isCountingDown: true });
    let countdown = 60;
    this.setData({ countdown });

    const timer = setInterval(() => {
      countdown--;
      this.setData({ countdown });
      if (countdown === 0) {
        clearInterval(timer);
        this.setData({ isCountingDown: false, countdown: 60 });
      }
    }, 1000);

    try {
      const res = await sendSmsCode(phone);
      if (res && res.isOk) {
        wx.showToast({ title: '验证码已发送', icon: 'success' });
      } else {
        throw new Error(res.msg || '发送失败');
      }
    } catch (error) {
      console.error('发送验证码失败', error);
      wx.showToast({ title: error.message || '发送失败，请重试', icon: 'none' });
      clearInterval(timer);
      this.setData({ isCountingDown: false, countdown: 60 });
    }
  },
}); 