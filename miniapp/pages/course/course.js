// pages/course/course.js
const { getPublicCourseList, searchCourse, processImageUrl } = require('../../services/api.js');

Page({
  data: {
    searchKeyword: '',
    popularityLevels: ['全部', '热门', '较为热门', '中等', '冷门'],
    popularityMapping: [null, 4, 3, 2, 1], // 索引0对应'全部'，不传任何值
    selectedPopularityIndex: 0,
    courseList: [],
    currentPage: 1,
    pageSize: 10,
    total: 0,
    isLoading: false,
    hasMore: true,
    errorMessage: ''
  },

  onLoad() {
    console.log('课程页面加载')
    this.loadCourses(true)
  },

  onShow() {
    console.log('课程页面显示')
  },

  onPopularityChange(e) {
    const selectedIndex = e.detail.value;
    console.log(`热门程度筛选改变，选择索引: ${selectedIndex}`);
    this.setData({
      selectedPopularityIndex: selectedIndex,
      currentPage: 1, // 重置页码
    });
    this.loadCourses(true); // 重新加载课程
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  async onSearchConfirm() {
    const keyword = this.data.searchKeyword.trim()
    console.log('搜索课程:', keyword)
    
    if (!keyword) {
      this.loadCourses(true)
      return
    }

    try {
      this.setData({ isLoading: true, errorMessage: '' })
      
      const response = await searchCourse(keyword, 1, this.data.pageSize)
      console.log('搜索课程响应:', response)
      
      let courses = []
      if (response && response.isOk && response.courses) {
        courses = response.courses.map(item => ({
          ...item,
          coverUrl: processImageUrl(item.coverUrl)
        }));
      }
      
      this.setData({ 
        courseList: courses,
        total: response.total || courses.length,
        currentPage: 2,
        hasMore: false
      })
      
    } catch (error) {
      console.error('搜索课程失败:', error)
      this.setData({ errorMessage: '搜索失败，请稍后重试' })
      wx.showToast({
        title: '搜索失败',
        icon: 'none'
      })
    } finally {
      this.setData({ isLoading: false })
    }
  },

  // 数据映射函数
  mapCourseData(item) {
    return {
      id: item.id || Math.random().toString(36).substr(2, 9),
      title: item.title || item.courseName || '课程标题',
      instructor: item.instructor || item.teacher || '讲师',
      description: item.description || item.summary || item.content || '课程描述',
      price: item.price || 0,
      originalPrice: item.originalPrice || item.price || 0,
      duration: item.duration || '待定',
      level: item.level || 'beginner',
      enrollCount: item.enrollCount || item.students || 0,
      rating: item.rating || 4.5,
      cover: item.cover || item.imageUrl || '',
      status: item.status || 'published',
      publishTime: item.publishTime || item.createTime || '2024-01-01',
      category: item.category || 'testing'
    }
  },

  // 加载课程列表
  async loadCourses(isRefresh = false) {
    if (this.data.isLoading) return
    this.setData({ isLoading: true, errorMessage: '' })

    const currentPage = isRefresh ? 1 : this.data.currentPage
    const courseOrder = this.data.popularityMapping[this.data.selectedPopularityIndex];

    try {
      let response
      
      if (this.data.searchKeyword.trim()) {
        response = await searchCourse(this.data.searchKeyword.trim(), currentPage, this.data.pageSize)
      } else {
        response = await getPublicCourseList(currentPage, this.data.pageSize, courseOrder)
      }
      
      console.log('获取课程列表响应:', response)
      
      let newList = []
      let total = 0
      
      if (response && response.isOk && response.courses) {
        newList = response.courses.map(item => ({
          ...item,
          coverUrl: processImageUrl(item.coverUrl)
        }));
        total = response.total || 0
      }
      
      // 更新显示
      const currentList = isRefresh ? [] : this.data.courseList
      
      this.setData({
        courseList: [...currentList, ...newList],
        total: total,
        currentPage: currentPage + 1,
        hasMore: (currentPage * this.data.pageSize) < total,
      })
      
      console.log('课程列表更新成功，共', newList.length, '条新数据')

    } catch (error) {
      console.error("获取课程列表失败", error)
      this.setData({ errorMessage: '获取课程数据失败，请稍后重试' })
    } finally {
      this.setData({ isLoading: false })
      wx.stopPullDownRefresh()
    }
  },

  // 触底加载更多
  onReachBottom() {
    if (this.data.hasMore && !this.data.isLoading) {
      this.loadCourses()
    }
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadCourses(true)
  },

  navigateToDetail(e) {
    const { id } = e.currentTarget.dataset
    if (!id) {
      console.error('错误：未能获取到课程ID！')
      return
    }
    wx.navigateTo({
      url: `/pages/course/detail/detail?id=${id}`,
    })
  },

  // 格式化课程级别
  formatLevel(level) {
    const levelMap = {
      'beginner': '初级',
      'intermediate': '中级',
      'advanced': '高级',
      'expert': '专家级'
    }
    return levelMap[level] || '初级'
  },

  // 格式化价格显示
  formatPrice(price) {
    if (price === 0) return '免费'
    return `¥${price}`
  },

  // 格式化报名人数
  formatEnrollCount(count) {
    if (count > 10000) {
      return `${Math.floor(count / 1000) / 10}万人`
    } else if (count > 1000) {
      return `${Math.floor(count / 100) / 10}k人`
    } else {
      return `${count}人`
    }
  }
}) 