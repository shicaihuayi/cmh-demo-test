// pages/meeting/meeting.js
const { getMeetingList } = require('../../services/meeting.js');
const { processImageUrl } = require('../../services/api.js');

Page({
  data: {
    activeType: 0,
    searchKeyword: '',
    meetingTypes: [
      { label: '全部', value: '' },
      { label: '会议研讨', value: '会议研讨' },
      { label: '标准定制', value: '标准定制' },
      { label: '技术培训', value: '技术培训' },
      { label: '工具研发', value: '工具研发' },
      { label: '公益行动', value: '公益行动' }
    ],
    meetingList: [],
    currentPage: 1,
    pageSize: 10,
    total: 0,
    isLoading: false,
    hasMore: true,
    errorMessage: ''
  },

  onLoad() {
    console.log('会议页面加载')
    this.loadMeetings(true)
  },

  onShow() {
    console.log('会议页面显示')
  },

  onTypeChange(e) {
    const activeType = parseInt(e.currentTarget.dataset.index)
    console.log('切换会议类型:', activeType, this.data.meetingTypes[activeType])
    
    this.setData({ 
      activeType,
      searchKeyword: '',
      currentPage: 1
    })
    this.loadMeetings(true)
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  onSearchConfirm() {
    const keyword = this.data.searchKeyword.trim()
    console.log('搜索会议:', keyword)
    this.setData({ 
      currentPage: 1
    })
    this.loadMeetings(true)
  },

  // 数据映射函数
  mapConferenceData(item) {
    if (!item || !item.name) {
      console.warn('无效的会议数据:', item);
      return null;
    }
    
    return {
      conferName: item.name,
      cover: processImageUrl(item.picture) || '/static/images/default-cover.png'
    };
  },

  // 加载会议列表
  async loadMeetings(isRefresh = false) {
    if (this.data.isLoading) return;
    this.setData({ isLoading: true, errorMessage: '' });

    try {
      wx.showLoading({ title: '加载中...' });
      
      const response = await getMeetingList({
        pageNum: isRefresh ? 1 : this.data.currentPage,
        pageSize: this.data.pageSize,
        category: this.data.meetingTypes[this.data.activeType].value,
        keyword: this.data.searchKeyword.trim()
      });
      
      console.log('获取会议列表响应:', response);
      
      let newList = [];
      if (response?.conferences?.length > 0) {
        newList = response.conferences
          .map(this.mapConferenceData)
          .filter(item => item !== null); // 过滤掉无效数据
      }
      
      this.setData({
        meetingList: isRefresh ? newList : [...this.data.meetingList, ...newList],
        total: response?.total || 0,
        currentPage: (isRefresh ? 1 : this.data.currentPage) + 1,
        hasMore: newList.length >= this.data.pageSize,
        isLoading: false
      });
      
    } catch (error) {
      console.error("获取会议列表失败", error);
      this.setData({ 
        errorMessage: '获取会议数据失败，请稍后重试',
        isLoading: false
      });
    } finally {
      wx.hideLoading();
      wx.stopPullDownRefresh();
    }
  },

  // 触底加载更多
  onReachBottom() {
    if (this.data.hasMore && !this.data.isLoading) {
      this.loadMeetings();
    }
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadMeetings(true);
  },

  navigateToDetail(e) {
    const { conferName } = e.currentTarget.dataset;
    console.log('点击会议项，数据:', e.currentTarget.dataset);
    
    if (!conferName) {
      console.error('错误：未能获取到会议名称！dataset:', e.currentTarget.dataset);
      wx.showToast({
        title: '无法获取会议信息',
        icon: 'none'
      });
      return;
    }
    
    wx.navigateTo({
      url: `/pages/meeting/detail/detail?conferName=${encodeURIComponent(conferName)}`,
      fail: (err) => {
        console.error('页面跳转失败:', err);
        wx.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
      }
    });
  },

  // 格式化会议状态
  formatStatus(status) {
    const statusMap = {
      'upcoming': '即将开始',
      'ongoing': '进行中',
      'ended': '已结束',
      'cancelled': '已取消'
    }
    return statusMap[status] || '未知状态'
  },

  // 格式化会议类型
  formatType(type) {
    const typeMap = {
      '会议研讨': '会议研讨',
      '标准定制': '标准定制',
      '技术培训': '技术培训',
      '工具研发': '工具研发',
      '公益行动': '公益行动'
    }
    return typeMap[type] || '其他'
  }
}) 