<template>
  <el-button type="primary" @click="goBack">
    <el-icon>
      <Back />
    </el-icon>
    返回</el-button>
  <div class="news-preview">
    <h1>{{ article.title }}</h1>
    <div class="date">{{ article.date }}</div>
    <div>作者：{{ article.authorName }}</div>
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
const route = useRoute()
const router = useRouter()
const article: any = ref({})

const fetchArticle = async () => {
  try {
    const response = await getNewsDetail(route.params.id)
    article.value = response.data
  } catch (error) {
    console.error('Failed to fetch article:', error)
  }
}

onMounted(() => {
  fetchArticle()
})

const goBack = () => {
  router.push('/news')
}
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
  }

  .date {
    margin: 10px 0;
  }

  .desc {
    text-align: justify;
    margin: 0 100px;
  }

  .content {
    text-align: justify;
  }
}

.img {
  margin-bottom: 20px;
}
</style>


