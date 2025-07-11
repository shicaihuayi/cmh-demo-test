// pages/trends/trends.js
const { getArticleList, searchArticle, processImageUrl } = require('../../services/api.js');

Page({
  data: {
    searchKeyword: '',
    trendsList: [],
    pageNum: 1,
    pageSize: 10,
    total: 0,
    isLoading: false,
    hasMore: true,
    errorMessage: ''
  },

  onLoad(options) {
    console.log('动态页面加载')
    this.fetchTrends(true)
  },

  onShow() {
    console.log('动态页面显示')
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  async onSearchConfirm() {
    const keyword = this.data.searchKeyword.trim()
    console.log('搜索动态:', keyword)
    
    if (!keyword) {
      this.fetchTrends(true)
      return
    }

    try {
      this.setData({ isLoading: true, errorMessage: '' })
      
      const response = await searchArticle(keyword, 1, this.data.pageSize)
      console.log('搜索动态响应:', response)
      
      let articles = []
      if (response && response.isOk && response.articles) {
        articles = response.articles.map(item => ({
          ...item,
          imageUrl: processImageUrl(item.imageUrl)
        }));
      }
      
      this.setData({ 
        trendsList: articles,
        total: response.total || articles.length,
        pageNum: 2,
        hasMore: false
      })
      
    } catch (error) {
      console.error('搜索动态失败:', error)
      this.setData({ errorMessage: '搜索失败，请稍后重试' })
      wx.showToast({
        title: '搜索失败',
        icon: 'none'
      })
    } finally {
      this.setData({ isLoading: false })
    }
  },

  // 获取动态列表
  async fetchTrends(isRefresh = false) {
    if (this.data.isLoading) return
    this.setData({ isLoading: true, errorMessage: '' })

    const pageNum = isRefresh ? 1 : this.data.pageNum

    try {
      let response
      
      if (this.data.searchKeyword.trim()) {
        response = await searchArticle(this.data.searchKeyword.trim(), pageNum, this.data.pageSize)
      } else {
        response = await getArticleList(pageNum, this.data.pageSize)
      }
      
      console.log('获取动态列表响应:', response)
      
      let newList = []
      let total = 0
      
      if (response && response.isOk && response.articles) {
        newList = response.articles.map(item => ({
          ...item,
          imageUrl: processImageUrl(item.imageUrl)
        }));
        total = response.total || 0
      }
      
      // 更新显示
      const currentList = isRefresh ? [] : this.data.trendsList
      
      this.setData({
        trendsList: [...currentList, ...newList],
        total: total,
        pageNum: pageNum + 1,
        hasMore: (pageNum * this.data.pageSize) < total,
      })
      
      console.log('动态列表更新成功，共', newList.length, '条新数据')

    } catch (error) {
      console.error("获取动态列表失败", error)
      this.setData({ errorMessage: '获取动态数据失败，请稍后重试' })
    } finally {
      this.setData({ isLoading: false })
      wx.stopPullDownRefresh()
    }
  },

  // 触底加载更多
  onReachBottom() {
    if (this.data.hasMore && !this.data.isLoading) {
      this.fetchTrends()
    }
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.fetchTrends(true)
  },

  navigateToDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) {
      console.error('错误：未能获取到动态ID！')
      return
    }
    wx.navigateTo({
      url: `/pages/trends/detail/detail?id=${id}`,
    })
  },

  // 格式化时间
  formatTime(timeStr) {
    if (!timeStr) return ''
    const date = new Date(timeStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
}) 