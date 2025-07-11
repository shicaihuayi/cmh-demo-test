// pages/trends/detail/detail.js
const { getArticleDetail, processImageUrl } = require('../../../services/api.js');

Page({
  data: {
    // 动态详情数据
    trend: null,
    
    // 加载状态
    isLoading: false,
    
    // 错误状态
    errorMessage: ''
  },

  onLoad(options) {
    console.log('加载动态详情页, ID:', options.id);
    if (options.id) {
      this.fetchTrendDetail(options.id);
    } else {
      this.setData({ errorMessage: '动态ID缺失' });
    }
  },

  // 获取动态详情
  async fetchTrendDetail(trendId) {
    try {
      this.setData({ isLoading: true, errorMessage: '' });
      
      const response = await getArticleDetail(trendId);
      console.log('获取动态详情响应:', response);
      
      if (response && response.isOk && response.article) {
        // 映射后端数据到前端格式
        const trend = this.mapTrendDetail(response.article);
        this.setData({ trend });
      } else {
        this.setData({ errorMessage: '获取动态详情失败' });
      }
    } catch (error) {
      console.error('获取动态详情失败:', error);
      this.setData({ errorMessage: '获取动态详情失败，请稍后重试' });
      wx.showToast({
        title: '获取数据失败',
        icon: 'none'
      });
    } finally {
      this.setData({ isLoading: false });
    }
  },

  // 映射动态详情数据格式
  mapTrendDetail(data) {
    // 模拟附件数据，以备后端未提供
    const mockAttachments = [
        { name: '示例文档.pdf', url: 'mock_url_1' },
        { name: '参考资料.docx', url: 'mock_url_2' }
    ];

    return {
      id: data.id,
      title: data.title || '未命名动态',
      author: data.author || '未知作者',
      publishTime: this.formatDate(data.publishTime),
      content: data.content || '暂无内容',
      imageUrl: processImageUrl(data.imageUrl || ''),
      description: data.description || '',
      keywords: data.keywords || '',
      // 如果后端有附件信息，则使用；否则，使用模拟数据
      attachments: (data.attachments && data.attachments.length > 0) ? data.attachments : mockAttachments
    };
  },

  // 格式化日期
  formatDate(dateStr) {
    if (!dateStr) return '';
    
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hour}:${minute}`;
  },

  // 附件下载
  downloadAttachment(e) {
    const attachment = e.currentTarget.dataset.attachment;
    console.log('模拟下载附件:', attachment.name);

    // 模拟下载成功提示
    wx.showToast({
      title: '下载成功',
      icon: 'success',
      duration: 2000
    });
  },

  // 下拉刷新
  onPullDownRefresh() {
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    const options = currentPage.options;
    
    if (options.id) {
      this.fetchTrendDetail(options.id).then(() => {
        wx.stopPullDownRefresh();
      });
    } else {
      wx.stopPullDownRefresh();
    }
  }
}) 