const { getMeetingDetail } = require('../../../services/meeting.js');

Page({
  data: {
    meeting: {
      name: '',
      startTime: '',
      endTime: '',
      location: '',
      organizer: '',
      agenda: '',
      guests: ''
    },
    isLoading: true,
    errorMessage: ''
  },

  onLoad(options) {
    if (options.conferName) {
      // 对URL编码的会议名称进行解码
      const decodedName = decodeURIComponent(options.conferName);
      console.log('加载会议详情页，会议名称:', decodedName);
      this.fetchMeetingDetail(decodedName);
    } else {
      this.setData({ isLoading: false, errorMessage: '会议名称缺失' });
    }
  },

  async fetchMeetingDetail(conferName) {
    this.setData({ isLoading: true, errorMessage: '' });
    try {
      wx.showLoading({
        title: '加载中...',
      });
      const response = await getMeetingDetail(conferName);
      wx.hideLoading();
      
      if (response && response.isOk && response.conference) {
        const meetingData = this.mapMeetingDetail(response.conference);
        this.setData({ meeting: meetingData, isLoading: false });
      } else {
        throw new Error(response.msg || '获取会议详情失败');
      }
    } catch (error) {
      wx.hideLoading();
      console.error('获取会议详情失败:', error);
      this.setData({ 
        isLoading: false,
        errorMessage: error.message || '获取会议详情失败，请稍后重试' 
      });
    }
  },

  mapMeetingDetail(data) {
    // 格式化时间
    const formatTime = (timeStr) => {
      if (!timeStr) return '';
      // 兼容iOS: 将 'YYYY-MM-DD HH:mm:ss' 格式转换为 'YYYY/MM/DD HH:mm:ss'
      const compatibleTimeStr = timeStr.replace(/-/g, '/');
      const date = new Date(compatibleTimeStr);
      // 将时间格式化为 "YYYY.MM.DD HH:mm" 格式
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}.${month}.${day} ${hours}:${minutes}`;
    };

    // 格式化议程文本
    const formatAgenda = (agenda) => {
      if (!agenda) return '';
      try {
        if (typeof agenda === 'string') {
          // 尝试解析JSON字符串
          try {
            const agendaObj = JSON.parse(agenda);
            if (Array.isArray(agendaObj)) {
              return agendaObj.map(item => `${item.time}: ${item.content}`).join('\n');
            }
          } catch (e) {
            // 如果解析失败，说明不是JSON格式，直接返回原文本
            return agenda;
          }
        }
        return agenda;
      } catch (e) {
        console.error('格式化议程失败:', e);
        return agenda || '';
      }
    };

    // 格式化嘉宾介绍文本
    const formatGuests = (guests) => {
      if (!guests) return '';
      try {
        if (typeof guests === 'string') {
          // 尝试解析JSON字符串
          try {
            const guestsObj = JSON.parse(guests);
            if (Array.isArray(guestsObj)) {
              return guestsObj.map(guest => `${guest.name}: ${guest.introduction}`).join('\n\n');
            }
          } catch (e) {
            // 如果解析失败，说明不是JSON格式，直接返回原文本
            return guests;
          }
        }
        return guests;
      } catch (e) {
        console.error('格式化嘉宾介绍失败:', e);
        return guests || '';
      }
    };

    console.log('原始会议数据:', data); // 调试：打印原始数据

    return {
      name: data.name,
      startTime: formatTime(data.startTime),
      endTime: formatTime(data.endTime),
      location: data.location || '地点待定',
      organizer: data.organizer || '主办方待定',
      agenda: formatAgenda(data.agenda),
      guests: formatGuests(data.guests)
    };
  },
  
  // 跳转到填写回执页面
  navigateToReceipt() {
    if (!this.data.meeting || !this.data.meeting.name) {
      wx.showToast({
        title: '会议信息加载中，请稍候',
        icon: 'none'
      });
      return;
    }
    
    wx.navigateTo({
      url: `/pages/meeting/receipt/receipt?conferName=${encodeURIComponent(this.data.meeting.name)}`
    });
  },

  // 下拉刷新
  onPullDownRefresh() {
    if (this.data.isLoading) return;
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    const options = currentPage.options;
    
    if (options.conferName) {
      this.fetchMeetingDetail(options.conferName).finally(() => {
        wx.stopPullDownRefresh();
      });
    } else {
      wx.stopPullDownRefresh();
    }
  }
}); 