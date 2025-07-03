<template>
  <div>
    <!-- 搜索框 -->
    <div class="search-bar">
      <div>
        <el-input v-model="searchParams.title" placeholder="按标题搜索" class="input-item" clearable/>
        <el-input v-model="searchParams.description" placeholder="按简介搜索" class="input-item" clearable/>
        <el-select v-model="searchParams.status" placeholder="按状态筛选" class="input-item" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>
      <div>
        <el-button @click="handleSearch" type="primary">搜索</el-button>
        <el-button @click="handleReset" type="warning" >重置</el-button>
        <el-button v-if="!isAuditView" type="success" @click="() => router.push({ name: 'add' })">创建动态</el-button>
        <!-- 根据用户角色和视图显示不同按钮 -->
        <el-button 
          v-if="isAdmin && !isAuditView" 
          type="info" 
          @click="handleViewAudit"
        >查看审核列表</el-button>
        <el-button 
          v-if="!isAdmin && !isAuditView" 
          type="primary" 
          @click="handlePublish"
        >发布</el-button>
        <el-button 
          v-if="isAuditView" 
          type="info" 
          @click="handleBackToList"
        >返回列表</el-button>
      </div>
    </div>

    <!-- 新闻列表 - 表格形式 -->
    <el-table
      :data="newsList"
      style="width: 100%"
      @selection-change="handleSelectionChange"
      v-loading="loading"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column label="图片" width="120">
        <template #default="scope">
          <el-image :src="scope.row.fileUrl" style="width: 100px; height: 60px" fit="cover" :preview-src-list="[scope.row.fileUrl]" preview-teleported/>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180">
      </el-table-column>
      <el-table-column prop="description" label="简介" min-width="250" show-overflow-tooltip>
      </el-table-column>
      <el-table-column prop="authorName" label="作者" width="120">
      </el-table-column>
      <el-table-column property="createTime" label="创建时间" width="180" />
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ formatStatus(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <!-- 审核视图下的操作 -->
          <div v-if="isAuditView">
            <el-button size="small" type="success" @click="handleApprove(scope.row.id)">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(scope.row.id)">驳回</el-button>
            <el-button size="small" type="primary" plain @click="(e: any) => handleView(e, scope.row)">查看</el-button>
          </div>
          <!-- 普通视图下的操作 -->
          <div v-else>
            <el-button size="small" type="warning" plain @click="(e: any) => handleEdit(e, scope.row, true)">编辑</el-button>
            <el-button size="small" type="primary" plain @click="(e: any) => handleView(e, scope.row)">查看</el-button>
            <el-button size="small" type="danger" plain @click="(e: any) => handleDelete(e, scope.row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
       <div class="pagination-info">
         <span class="total-info">共 {{ total }} 条记录</span>
         <div class="page-size-selector">
           <el-select v-model="searchParams.pageSize" @change="handleSizeChange" style="width: 120px;">
             <el-option
               v-for="size in [6, 12, 18, 24]"
               :key="size"
               :label="`${size}条/页`"
               :value="size"
             />
           </el-select>
         </div>
                  <div class="page-jumper">
           <span>前往第</span>
           <el-input
             v-model="jumpPage"
             size="small"
             :placeholder="searchParams.pageNum.toString()"
             style="width: 50px; margin: 0 8px; text-align: center;"
             @keyup.enter="handleJumpPage"
             @blur="handleJumpPage"
           />
           <span>页</span>
         </div>
       </div>
       <el-pagination
         v-model:current-page="searchParams.pageNum"
         v-model:page-size="searchParams.pageSize"
         :background="true"
         layout="prev, pager, next"
         :total="total"
         :pager-count="7"
         :hide-on-single-page="false"
         @size-change="handleSizeChange"
         @current-change="handleCurrentChange"
       />
    </div>
  </div>
</template>

<script setup lang="ts">
import { getNewsList, deleteNews, deleteNewsWithDelete, approveNews, rejectNews, publishNews } from "@/api/news";
import { ElMessage, ElMessageBox } from "element-plus";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router: any = useRouter();
const isAdmin = ref(false);
const isAuditView = ref(false);
const loading = ref(false);
const multipleSelection = ref<any[]>([]);

onMounted(() => {
  const userRole = sessionStorage.getItem('role');
  isAdmin.value = userRole === '3';
  fetchNews();
});

const getStatusTagType = (status: string) => {
  const tagMap: { [key: string]: string } = {
    PUBLISHED: 'success',
    PENDING: 'warning',
    REVIEWING: 'info',
    REJECTED: 'danger'
  };
  return tagMap[status] || 'info';
};

const fetchNews = async () => {
  loading.value = true;
  try {
    const params = { ...searchParams.value };
    if(isAuditView.value) {
      params.status = 'REVIEWING'; // 审核列表只看审核中的动态
    }
    const response: any = await getNewsList(params);
    if (response.data?.records) {
      newsList.value = response.data.records;
      total.value = response.data.total;
      
      // 调试：显示获取到的动态数据结构
      console.log('获取到的动态列表:', newsList.value.map((item: any) => ({
        id: item.id,
        title: item.title,
        authorId: item.authorId,
        authorName: item.authorName,
        status: item.status
      })));
      
      // 特别检查最新创建的动态（通常ID最大）
      if (newsList.value.length > 0) {
        const latestNews = newsList.value.reduce((latest: any, current: any) => 
          current.id > latest.id ? current : latest
        );
        console.log('=== 最新动态检查 ===');
        console.log('最新动态ID:', latestNews.id);
        console.log('最新动态标题:', latestNews.title);
        console.log('最新动态状态:', latestNews.status);
        console.log('最新动态作者ID:', latestNews.authorId);
        console.log('当前用户ID:', sessionStorage.getItem('id'));
        console.log('是否为当前用户创建:', latestNews.authorId?.toString() === sessionStorage.getItem('id'));
        console.log('==================');
      }
    } else {
      newsList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取动态列表失败:', error);
    ElMessage.error('获取动态列表失败');
  } finally {
    loading.value = false;
  }
};

const handleViewAudit = () => {
  isAuditView.value = true;
  fetchNews();
};

const handleBackToList = () => {
  isAuditView.value = false;
  searchParams.value.status = '';
  fetchNews();
};

const handlePublish = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.error('请选择要发布的动态');
    return;
  }

  // 检查选中的动态是否都是待审核状态
  const invalidItems = multipleSelection.value.filter(item => item.status !== 'PENDING');
  if (invalidItems.length > 0) {
    ElMessage.error('只能发布状态为"待审核"的动态');
    return;
  }

  // 检查选中的动态是否都是当前用户创建的
  const currentUserId = sessionStorage.getItem('id');
  const notOwnedItems = multipleSelection.value.filter(item => 
    item.authorId && item.authorId.toString() !== currentUserId
  );
  
  if (notOwnedItems.length > 0) {
    console.log('权限检查失败：');
    console.log('当前用户ID:', currentUserId);
    console.log('选中的动态:', multipleSelection.value.map(item => ({
      id: item.id,
      title: item.title,
      authorId: item.authorId,
      authorName: item.authorName
    })));
    console.log('非本人创建的动态:', notOwnedItems.map(item => ({
      id: item.id,
      title: item.title,
      authorId: item.authorId,
      authorName: item.authorName
    })));
    
    ElMessage.error(`您只能发布自己创建的动态。以下动态不是您创建的：${notOwnedItems.map(item => item.title).join('、')}`);
    return;
  }

  const idsToPublish = multipleSelection.value.map(item => item.id);
  const itemCount = idsToPublish.length;
  
  try {
    await ElMessageBox.confirm(`确定要提交${itemCount}条动态进行审核吗？提交后状态将变为"审核中"，等待超级管理员审核。`, '提交审核确认', {
      confirmButtonText: '确定提交',
      cancelButtonText: '取消',
      type: 'warning'
    });

    console.log('开始提交审核，ID列表:', idsToPublish);
    const result = await publishNews(idsToPublish);
    console.log('提交审核结果:', result);
    
    ElMessage.success(`已成功提交${itemCount}条动态进行审核，状态已变更为"审核中"`);
    fetchNews(); // 刷新列表
  } catch (err) {
    if (err !== 'cancel') {
      console.error('发布失败的最终错误:', err);
      // 从 err 对象中提取最根本的错误信息
      const errorMessage = (err as any)?.response?.data?.msg || (err as any)?.message || '提交审核失败，请稍后重试';
      ElMessage.error(errorMessage);
    }
  }
};

const handleSelectionChange = (selection: any[]) => {
  multipleSelection.value = selection;
  
  // 调试：显示选中的动态信息
  console.log('选中的动态数量:', selection.length);
  if (selection.length > 0) {
    console.log('选中的动态详情:', selection.map(item => ({
      id: item.id,
      title: item.title,
      authorId: item.authorId,
      authorName: item.authorName,
      status: item.status
    })));
  }
};

// 查看详情的统一方法
const handleView = (e: { stopPropagation: () => void; }, newsItem: any) => {
  e.stopPropagation();
  
  // 保存查看权限和当前文章信息到会话存储
  sessionStorage.setItem("canEdit", 'false'); // 查看模式不允许编辑
  sessionStorage.setItem("currentArticle", JSON.stringify({
    id: newsItem.id,
    authorName: newsItem.authorName,
    authorId: newsItem.authorId
  }));
  
  router.push({
    name: 'preview',
    query: { id: newsItem.id }
  });
};

const searchParams = ref<any>({
  title: '',
  description: '',
  status: '', 
  pageNum: 1,
  pageSize: 6,
});

const statusOptions = ref([
  { value: 'PUBLISHED', label: '已发布' },
  { value: 'PENDING', label: '待审核' },
  { value: 'REVIEWING', label: '审核中' },
  { value: 'REJECTED', label: '已驳回' },
]);

const newsList: any = ref([]);
const total = ref(0);
const jumpPage = ref('');

const formatStatus = (status: string) => {
  const statusMap: { [key: string]: string } = {
    PUBLISHED: '已发布',
    PENDING: '待审核',
    REVIEWING: '审核中',
    REJECTED: '已驳回',
  };
  return statusMap[status] || '未知';
};

const handleSearch = () => {
  searchParams.value.pageNum = 1; 
  fetchNews();
};

const handleReset = () => {
  searchParams.value = {
    title: '',
    description: '',
    status: '',
    pageNum: 1,
    pageSize: 6,
  };
  fetchNews();
};

const handleSizeChange = (size: number) => {
  searchParams.value.pageSize = size;
  searchParams.value.pageNum = 1;
  fetchNews();
};

const handleCurrentChange = (page: number) => {
  searchParams.value.pageNum = page;
  fetchNews();
};

const handleJumpPage = () => {
  if (!jumpPage.value || jumpPage.value.trim() === '') {
    return; // 空值时不处理
  }
  
  const page = parseInt(jumpPage.value.trim());
  const maxPage = Math.ceil(total.value / searchParams.value.pageSize);
  
  if (isNaN(page)) {
    ElMessage.warning('请输入数字');
    jumpPage.value = '';
    return;
  }
  
  if (page < 1) {
    ElMessage.warning('页码不能小于1');
    jumpPage.value = '';
    return;
  }
  
  if (page > maxPage) {
    ElMessage.warning(`页码不能大于${maxPage}`);
    jumpPage.value = '';
    return;
  }
  
  if (page === searchParams.value.pageNum) {
    jumpPage.value = ''; // 如果是当前页，清空输入框
    return;
  }
  
  searchParams.value.pageNum = page;
  fetchNews();
  jumpPage.value = '';
};

const handleDelete = (e: { stopPropagation: () => void; }, id: number) => {
  e.stopPropagation();

  ElMessageBox.confirm('此操作将永久删除该动态, 是否继续?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    console.log('=== 删除动态调试信息 ===');
    console.log('删除ID:', id);
    console.log('用户角色:', sessionStorage.getItem('role'));
    console.log('用户ID:', sessionStorage.getItem('id'));
    console.log('========================');
    
    // 先尝试POST方法删除
    deleteNews(id).then((result: any) => {
      console.log('POST删除成功:', result);
      ElMessage.success('删除成功!');
      fetchNews(); // 重新获取列表
    }).catch((err: any) => {
      console.error('POST删除失败:', err);
      
      // 如果POST方法失败，尝试DELETE方法
      console.log('尝试DELETE方法删除...');
      deleteNewsWithDelete(id).then((result: any) => {
        console.log('DELETE删除成功:', result);
        ElMessage.success('删除成功!');
        fetchNews(); // 重新获取列表
      }).catch((err2: any) => {
        console.error('DELETE删除也失败:', err2);
        ElMessage.error('删除失败: ' + (err2?.message || '请检查网络连接或联系管理员'));
      });
    });
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

const handleEdit = (e: { stopPropagation: () => void; }, newsItem: any, canEdit: any) => {
  e.stopPropagation();
  console.log('编辑/查看文章:', newsItem);
  console.log('作者信息:', newsItem.authorName);
  
  // 保存编辑权限和当前文章信息到会话存储
  sessionStorage.setItem("canEdit", canEdit ? 'true' : 'false');
  sessionStorage.setItem("currentArticle", JSON.stringify({
    id: newsItem.id,
    authorName: newsItem.authorName,
    authorId: newsItem.authorId
  }));
  
  router.push({
    path: "/home/edit/" + newsItem.id,
  });
};

const handleApprove = async (id: number) => {
  try {
    await approveNews(id);
    ElMessage.success('审核通过成功');
    fetchNews();
  } catch (error) {
    console.error('审核通过失败:', error);
    ElMessage.error('操作失败');
  }
};

const handleReject = async (id: number) => {
  try {
    await rejectNews(id);
    ElMessage.success('驳回成功');
    fetchNews();
  } catch (error) {
    console.error('驳回失败:', error);
    ElMessage.error('操作失败');
  }
};
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
  justify-content: space-between;
  margin-bottom: 20px;

  .input-item {
    width: 200px;
    margin-right: 10px;
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
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.pagination-info {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  font-size: 14px;
  color: #606266;
  gap: 20px;
}

.total-info {
  font-weight: 500;
  color: #409eff;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-jumper {
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-jumper :deep(.el-input__wrapper) {
  justify-content: center;
}

.page-jumper :deep(.el-input__inner) {
  text-align: center !important;
}

// 添加状态样式
.status-text {
  display: inline-flex;
  align-items: center;
  font-weight: bold;
  
  i {
    margin-left: 4px;
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
</style>
