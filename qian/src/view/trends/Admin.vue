<template>
  <div>
    <!-- 搜索框 -->
    <div class="search-bar">
      <div>
        <el-input v-model="searchParams.title" placeholder="按标题搜索" class="input-item" clearable/>
        <el-input v-model="searchParams.authorName" placeholder="按作者搜索" class="input-item" clearable/>
<!--        <el-input v-model="searchParams.content" placeholder="按简介搜索" class="input-item" clearable/>-->
      </div>
      <div>
        <el-button @click="handleSearch" type="primary">搜索</el-button>
        <el-button @click="handleReset" type="warning" >重置</el-button>
        <el-button type="success" @click="() => {
          router.push({ name: 'add' })
        }">创建文章</el-button>
      </div>
    </div>

    <!-- 新闻列表 -->
    <div class="news-list">
      <div v-if="newsList.length !== 0">
        <el-row v-for="(item) in newsList" :key="item.id">
          <el-col :span="24">
            <el-card :body-style="{ padding: '20px' }">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-image :src="item.fileUrl" alt="404_not_found" class="image" @click="() => {
                    router.push({ path: '/news/preview/' + item.id })
                  }" />
                </el-col>
                <el-col :span="16">
                  <!-- <div style="padding: 14px"> -->
                  <h3 class="title">{{ item.title }}</h3>
                  <div class="bottom clearfix">
                    <p class="text">发布日期：{{ item.createTime }}</p>
                    <p class="text">
                      作者: {{ item.authorName }}
                    </p>
                  </div>
                  <p class="desc">{{ item.description }}</p>
                  <!-- </div> -->
                </el-col>
              </el-row>
              <el-button plain style="float: right;margin-left:20px;" @click="(e) => handleEdit(e, item, true)"
                type="warning">编辑</el-button>
              <el-button type="primary" plain style="float: right;margin-left:20px;"
                @click="(e) => handleEdit(e, item, false)">查看</el-button>
              <el-button type="danger" plain style="float: right; margin-bottom: 20px;"
                @click="(e) => handleDelete(e, item.id)">删除</el-button>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <el-col v-else style="text-align: center;margin-top: 40px;">
        暂无数据
      </el-col>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination v-model:current-page="searchParams.pageNum" :page-size="6" :background="true"
        layout="total, prev, pager, next, jumper" :total="total" @change="handlePaginationChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { getNewsList, deleteNews } from "@/api/news";
import { ElMessage, ElMessageBox } from "element-plus";
import { ref } from "vue";
import { useRouter } from "vue-router";
const router: any = useRouter()
const searchParams = ref<any>({
  title: '',
  authorName: '',
  fileUrl: '',
  description: '',
  pageNum: 1,
  pageSize: 6,
});

const newsList: any = ref([]);
const total = ref(0);
const handleSearch = () => {
  searchParams.value.pageNum = 1; // 重置为第一页
  fetchNews();
};

const fetchNews = async () => {
  try {
    const response: any = await getNewsList(searchParams.value);
    total.value = response.data.total
    newsList.value = response.data.records
  } catch (error) {
    console.error('Error fetching news:', error);
  }
};
const handleReset = () => {
  searchParams.value = {
    title: '',
    authorName: '',
    description: '',
    pageNum: 1,
    pageSize: 6
  };
  fetchNews();
};
const handlePaginationChange = (currentPage: any) => {
  searchParams.value.pageNum = currentPage
  fetchNews();
};
fetchNews()
const handleEdit = (e: { stopPropagation: () => void; }, newsItem: any, canEdit: any) => {
  e.stopPropagation();
  if (canEdit) {
    sessionStorage.setItem("canEdit", 'true')
  } else {
    sessionStorage.setItem("canEdit", 'false')
  }
  router.push({
    path: "/home/edit/" + newsItem.id,
  })
}
const handleDelete = (e: { stopPropagation: () => void; }, id = 1) => {
  e.stopPropagation();

  ElMessageBox.confirm('确认删除该文章吗？', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }
  ).then(() => {
    deleteNews(id).then((result: any) => {
      ElMessage({
        type: 'success',
        message: '删除成功',
      })
      //重新获取list
      fetchNews()
    }).catch((err: any) => {
      console.error(err);
    });
  })
}
</script>

<style scoped lang="less">
.textOverflow {
  display: -webkit-box;
  word-break: break-all;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-line-clamp: 2;
}

.el-row {
  margin-bottom: 20px;
}

.el-card {
  .image:hover {
    cursor: pointer;
  }
}

.search-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;

  .el-input {
    margin-right: 20px;
  }
}

.title {
  font-size: 20px;
  margin-top: 10px;
  .textOverflow;

}

.bottom {
  margin: 10px 0;

  .time {
    margin-bottom: 10px;
  }

  .text {
    .textOverflow;
    color: gray;
    font-size: 14px;
  }
}

.desc {
  font-size: 16px;
  .textOverflow;
  -webkit-line-clamp: 4;
}

.input-item {
  width: 200px;
}

.news-list {
  /* display: flex; */
  height: 75vh;
  overflow-y: scroll;
  flex-wrap: wrap;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>


