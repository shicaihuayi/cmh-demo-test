// pages/index/index.js
const { getBannerList, getAboutInfo, getMemberUnits } = require('../../services/api.js');

Page({
  data: {
    // 轮播图数据
    bannerList: [],
    
    // 测盟汇简介
    introduction: {
      title: '关于测盟汇',
      content: '测盟汇是一个专业的测试技术交流平台，致力于推动软件测试行业的发展。我们为测试从业者提供最新的行业动态、专业的技术培训、高质量的会议研讨，以及丰富的学习资源。通过测盟汇平台，您可以获取前沿的测试技术信息，参与行业内的深度交流，提升自己的专业技能。'
    },
    
    // 同盟会员单位
    memberUnits: [],
    
    // 加载状态
    loading: {
      banner: false,
      introduction: false,
      members: false
    }
  },

  onLoad() {
    console.log('首页加载')
    this.loadAllData()
  },

  onShow() {
    console.log('首页显示')
  },

  // 加载所有数据
  loadAllData() {
    this.loadBannerData()
    this.loadIntroduction()
    this.loadMemberUnits()
  },

  // 加载轮播图数据
  async loadBannerData() {
    try {
      this.setData({ 'loading.banner': true })
      
      // 由于后端可能没有实现首页接口，暂时使用默认数据
      console.log('加载轮播图数据')
      this.setData({ 
        bannerList: [
          {
            id: 1,
            image: '/static/images/banner1.jpg',
            title: '测盟汇轮播图1'
          },
          {
            id: 2,
            image: '/static/images/banner2.jpg',
            title: '测盟汇轮播图2'
          },
          {
            id: 3,
            image: '/static/images/banner3.jpg',
            title: '测盟汇轮播图3'
          }
        ]
      })
      
      // 尝试从后端获取，如果成功则更新数据
      try {
        const banners = await getBannerList()
        if (banners && banners.length > 0) {
          this.setData({ bannerList: banners })
        }
      } catch (apiError) {
        console.log('后端轮播图接口暂未实现，使用默认数据')
      }
      
    } catch (error) {
      console.error('加载轮播图失败:', error)
    } finally {
      this.setData({ 'loading.banner': false })
    }
  },

  // 加载简介内容
  async loadIntroduction() {
    try {
      this.setData({ 'loading.introduction': true })
      
      // 尝试从后端获取，如果失败则保持默认数据
      try {
        const aboutInfo = await getAboutInfo()
        if (aboutInfo && aboutInfo.content) {
          this.setData({ 
            introduction: {
              title: aboutInfo.title || '关于测盟汇',
              content: aboutInfo.content
            }
          })
        }
      } catch (apiError) {
        console.log('后端简介接口暂未实现，使用默认数据')
      }
      
    } catch (error) {
      console.error('加载简介失败:', error)
    } finally {
      this.setData({ 'loading.introduction': false })
    }
  },

  // 加载会员单位数据
  async loadMemberUnits() {
    try {
      this.setData({ 'loading.members': true })
      
      // 先设置默认数据
      const defaultMembers = [
        { id: 1, name: '会员单位A', logo: '/static/images/member1.png' },
        { id: 2, name: '会员单位B', logo: '/static/images/member2.png' },
        { id: 3, name: '会员单位C', logo: '/static/images/member3.png' },
        { id: 4, name: '会员单位D', logo: '/static/images/member4.png' },
        { id: 5, name: '会员单位E', logo: '/static/images/member5.png' },
        { id: 6, name: '会员单位F', logo: '/static/images/member6.png' },
        { id: 7, name: '会员单位G', logo: '/static/images/member7.png' },
        { id: 8, name: '会员单位H', logo: '/static/images/member8.png' }
      ]
      
      this.setData({ memberUnits: defaultMembers })
      
      // 尝试从后端获取，如果成功则更新数据
      try {
        const members = await getMemberUnits()
        if (members && members.length > 0) {
          this.setData({ memberUnits: members })
        }
      } catch (apiError) {
        console.log('后端会员单位接口暂未实现，使用默认数据')
      }
      
    } catch (error) {
      console.error('加载会员单位失败:', error)
    } finally {
      this.setData({ 'loading.members': false })
    }
  },

  // 轮播图切换事件
  onSwiperChange(e) {
    console.log('轮播图切换到第', e.detail.current + 1, '张')
  },

  // 轮播图点击事件
  onBannerTap(e) {
    const index = e.currentTarget.dataset.index
    const banner = this.data.bannerList[index]
    console.log('点击轮播图:', banner.title)
    
    // 如果轮播图有跳转链接，可以在这里处理
    if (banner.url) {
      wx.navigateTo({
        url: banner.url
      })
    }
  },

  // 会员单位点击事件
  onMemberTap(e) {
    const index = e.currentTarget.dataset.index
    const member = this.data.memberUnits[index]
    console.log('点击会员单位:', member.name)
    
    // 如果会员单位有详情页面，可以在这里处理跳转
    if (member.detailUrl) {
      wx.navigateTo({
        url: member.detailUrl
      })
    }
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadAllData()
    setTimeout(() => {
      wx.stopPullDownRefresh()
    }, 1500)
  }
}) 