<template>
  <el-button type="primary" @click="goBack">
    <el-icon>
      <Back />
    </el-icon>
    返回</el-button>
  <div class="news-preview">
    <h1>{{ article.title }}</h1>
    <div class="meta-info">
      <span class="date">发布时间：{{ article.date }}</span>
      <span class="author">作者：{{ article.authorName }}</span>
      <span class="status" :class="getStatusClass(article.status)">
        状态：{{ formatStatus(article.status) }}
        <i :class="getStatusIcon(article.status)"></i>
      </span>
    </div>
    <el-divider></el-divider>
    <el-image :src="article.fileUrl" alt="Article Image" class="img"></el-image>
    <div class="desc">{{ article.description }}</div>
    <el-divider></el-divider>
    <div class="content" v-html="article.content"></div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNewsDetail } from '@/api/news'
import { ElMessage } from 'element-plus'
const route = useRoute()
const router = useRouter()
const article: any = ref({})

// 格式化状态显示
const formatStatus = (status: string) => {
  const statusMap: { [key: string]: string } = {
    PUBLISHED: '已发布',
    PENDING: '待审核',
    REJECTED: '已驳回',
    REVIEWING: '审核中'
  };
  return statusMap[status] || '未知状态';
};

// 获取状态对应的CSS类
const getStatusClass = (status: string) => {
  const classMap: { [key: string]: string } = {
    PUBLISHED: 'status-success',
    PENDING: 'status-warning',
    REJECTED: 'status-danger',
    REVIEWING: 'status-info'
  };
  return classMap[status] || '';
};

// 获取状态对应的图标
const getStatusIcon = (status: string) => {
  const iconMap: { [key: string]: string } = {
    PUBLISHED: 'el-icon-check',
    PENDING: 'el-icon-time',
    REJECTED: 'el-icon-close',
    REVIEWING: 'el-icon-loading'
  };
  return iconMap[status] || '';
};

const fetchArticle = async () => {
  try {
    const articleId = route.params.id || route.query.id;
    console.log('获取文章详情，ID:', articleId);
    const response = await getNewsDetail(articleId);
    
    if (response.data) {
      article.value = {
        ...response.data,
        date: response.data.createTime || response.data.date, // 兼容不同的日期字段
        authorName: response.data.authorName || '未知作者' // 确保作者字段存在
      };
      console.log('文章详情:', article.value);
    } else {
      ElMessage.error('获取文章详情失败');
      router.push('/home/trend');
    }
  } catch (error) {
    console.error('获取文章详情失败:', error);
    ElMessage.error('获取文章详情失败，请稍后重试');
    router.push('/home/trend');
  }
}

onMounted(() => {
  const articleId = route.params.id || route.query.id;
  if (!articleId) {
    ElMessage.error('无效的文章ID');
    router.push('/home/trend');
    return;
  }
  fetchArticle();
});

const goBack = () => {
  router.push('/home/trend');
};
</script>

<style scoped lang="less">
.news-preview {
  margin-top: 10px;
  text-align: center;
  padding: 20px;
  height: 80vh;
  overflow-y: scroll;

  h1 {
    font-size: 30px;
    margin-bottom: 20px;
  }

  .meta-info {
    display: flex;
    justify-content: center;
    gap: 30px;
    margin: 20px 0;
    color: #666;
    font-size: 14px;

    .date, .author {
      color: #666;
    }

    .status {
      display: inline-flex;
      align-items: center;
      font-weight: bold;
      
      i {
        margin-left: 4px;
      }
    }
  }

  .status-success {
    color: #67c23a;
  }

  .status-warning {
    color: #e6a23c;
  }

  .status-danger {
    color: #f56c6c;
  }

  .status-info {
    color: #909399;
  }

  .desc {
    text-align: justify;
    margin: 0 100px;
  }

  .content {
    text-align: justify;
    margin: 0 100px;
    line-height: 1.6;
  }
}

.img {
  max-width: 800px;
  margin: 20px auto;
  display: block;
}
</style>
