<template>
  <div class="trends-admin">
    <ModuleHeader
      title="行业动态管理"
      description="发布、审核和管理人事行业的最新动态与资讯"
      icon="DataAnalysis"
      color="orange"
    />

    <div class="operate-part">
      <div class="search-bar">
        <div>
          <el-input
            v-model="searchParams.title"
            placeholder="按标题搜索"
            class="input-item"
            clearable
          />
          <el-select
            v-model="searchParams.status"
            placeholder="按状态筛选"
            class="input-item"
            clearable
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        <div>
          <el-button @click="handleSearch" type="primary" :icon="Search"
            >搜索</el-button
          >
          <el-button @click="handleReset" type="warning" :icon="Refresh"
            >重置</el-button
          >
        </div>
      </div>
      <div class="action-buttons">
        <el-button
          v-if="!isAuditView"
          type="success"
          @click="() => router.push({ name: 'add' })"
          :icon="Plus"
          >创建动态</el-button
        >
        <el-button
          v-if="isAdmin && !isAuditView"
          type="info"
          @click="handleViewAudit"
          :icon="View"
          >查看审核列表</el-button
        >
        <el-button
          v-if="!isAdmin && !isAuditView"
          type="primary"
          @click="handlePublish"
          :icon="Promotion"
          >发布</el-button
        >
        <el-button
          v-if="isAuditView"
          type="info"
          @click="handleBackToList"
          :icon="ArrowLeft"
          >返回列表</el-button
        >
      </div>
    </div>
    <!-- 动态列表 -->
    <el-table
      :data="newsList"
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
      v-loading="loading"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column
        prop="createTime"
        label="发布日期"
        width="180"
        sortable
      />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <div v-if="isAuditView">
            <el-button
              type="success"
              size="small"
              @click="handleApprove(row)"
              :icon="Check"
              >审核通过</el-button
            >
            <el-button
              type="danger"
              size="small"
              @click="handleReject(row)"
              :icon="Close"
              >审核不通过</el-button
            >
          </div>
          <div v-else>
            <el-button
              size="small"
              @click="() => router.push({ name: 'preview', params: { id: row.id } })"
              :icon="Reading"
              >预览</el-button
            >
            <el-button
              type="primary"
              size="small"
              @click="() => router.push({ name: 'edit', params: { id: row.id } })"
              :icon="Edit"
              >编辑</el-button
            >
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              :icon="Delete"
              >删除</el-button
            >
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <div class="pagination-info">
        <span class="total-info">共 {{ pagination.total }} 条记录</span>
        <div class="page-size-selector">
          <el-select v-model="pagination.pageSize" style="width: 120px;" @change="handleSizeChange">
            <el-option
              v-for="size in [5, 10, 15, 20]"
              :key="size"
              :label="`${size}条/页`"
              :value="size"
            />
          </el-select>
        </div>
      </div>
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :background="true"
        layout="prev, pager, next"
        :total="pagination.total"
        :pager-count="7"
        :hide-on-single-page="false"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,
  Edit,
  Delete,
  Search,
  Refresh,
  View,
  Promotion,
  ArrowLeft,
  Reading,
  Check,
  Close,
} from "@element-plus/icons-vue";
import {
  deleteNews,
  getNewsList,
  publishNews,
  approveNews,
  rejectNews,
} from "@/api/news";
import ModuleHeader from "@/components/ModuleHeader.vue";

interface News {
  id: number;
  title: string;
  authorName: string;
  createTime: string;
  status: "PENDING" | "PUBLISHED" | "REVIEWING" | "REJECTED";
}

const router = useRouter();
const newsList = ref<News[]>([]);
const loading = ref(true);
const searchParams = ref({
  title: "",
  status: "",
});

const isAdmin = ref(sessionStorage.getItem("role") === "3");
const isAuditView = ref(false);

const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

const statusOptions = [
  { value: "PUBLISHED", label: "已发布" },
  { value: "PENDING", label: "待审核" },
  { value: "REVIEWING", label: "审核中" },
  { value: "REJECTED", label: "已驳回" },
];

const selectedNews = ref<News[]>([]);

const handleSelectionChange = (selection: News[]) => {
  selectedNews.value = selection;
};

const getStatusText = (status: News["status"]) => {
  const map = {
    PENDING: "待审核",
    PUBLISHED: "已发布",
    REVIEWING: "审核中",
    REJECTED: "已驳回",
  };
  return map[status] || "未知";
};

const getStatusTag = (status: News["status"]) => {
  const map = {
    PENDING: "info",
    PUBLISHED: "success",
    REVIEWING: "warning",
    REJECTED: "danger",
  };
  return map[status] || "info";
};

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchParams.value,
      pageNum: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
    };
    if (isAuditView.value) {
      params.status = "REVIEWING";
    }

    const response = await getNewsList(params);
    if (response.data && response.data.records) {
      newsList.value = response.data.records;
      pagination.value.total = response.data.total;
    } else {
      newsList.value = [];
      pagination.value.total = 0;
    }
  } catch (error) {
    ElMessage.error("获取动态列表失败");
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.value.currentPage = 1;
  fetchData();
};

const handleReset = () => {
  searchParams.value = { title: "", status: "" };
  handleSearch();
};

const handleDelete = async (row: News) => {
  await ElMessageBox.confirm("确定要删除这篇动态吗？", "提示", {
    type: "warning",
  });
  try {
    await deleteNews(row.id);
    ElMessage.success("删除成功");
    fetchData();
  } catch (error) {
    ElMessage.error("删除失败");
  }
};

const handleViewAudit = () => {
  isAuditView.value = true;
  fetchData();
};

const handleBackToList = () => {
  isAuditView.value = false;
  fetchData();
};

const handlePublish = async () => {
  if (selectedNews.value.length === 0) {
    ElMessage.warning("请至少选择一篇动态进行发布");
    return;
  }
  await ElMessageBox.confirm(
    `确定要发布选中的 ${selectedNews.value.length} 篇动态吗？`,
    "提示",
    { type: "info" }
  );
  try {
    const ids = selectedNews.value.map((item) => item.id);
    await publishNews(ids);
    ElMessage.success("发布成功，已提交审核");
    fetchData();
  } catch (error) {
    ElMessage.error("发布失败");
  }
};

const handleApprove = async (row: News) => {
  try {
    await approveNews(row.id);
    ElMessage.success("审核通过操作成功");
    fetchData();
  } catch (error) {
    ElMessage.error("审核操作失败");
  }
};

const handleReject = async (row: News) => {
  try {
    await rejectNews(row.id);
    ElMessage.success("审核驳回操作成功");
    fetchData();
  } catch (error) {
    ElMessage.error("审核操作失败");
  }
};

const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size;
  pagination.value.currentPage = 1; // 重置到第一页
  fetchData();
};

const handleCurrentChange = (page: number) => {
  pagination.value.currentPage = page;
  fetchData();
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.trends-admin {
  padding: 20px;
  background-color: #f5f7fa;
}
.operate-part {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.input-item {
  width: 200px;
  margin-right: 10px;
}
.action-buttons {
  display: flex;
  justify-content: flex-start;
}
/* 分页样式 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-info {
  font-size: 14px;
  color: #606266;
}

.page-size-selector {
  display: flex;
  align-items: center;
}
</style>
