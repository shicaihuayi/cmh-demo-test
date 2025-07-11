<template>
  <div class="department-manager">
    <ModuleHeader
      title="部门管理"
      description="组织和管理公司或团队的部门结构"
      icon="Tickets"
      color="green"
    />
    <div class="controls-container">
      <h5>部门名称：</h5>
      <el-input
        v-model="searchName"
        placeholder="请输入部门名称"
        class="search-input"
        clearable
        @clear="searchDepartment"
      ></el-input>
      <h5>状态：</h5>
      <el-select
        v-model="searchState"
        placeholder="部门状态"
        class="search-select"
        clearable
        @change="searchDepartment"
      >
        <el-option
          v-for="item in options"
          :key="item.state"
          :label="item.label"
          :value="item.state"
        />
      </el-select>
      <el-button type="primary" @click="searchDepartment()">搜索</el-button>
      <el-button @click="resetForm()">重置</el-button>
    </div>
    <DepartmentTree
      :name="childForm.searchName"
      :state="childForm.searchState ?? -1"
      :search="search"
    />
  </div>
</template>

<script lang="ts" setup>
import DepartmentTree from "./DepartmentTree.vue";
import { onMounted, reactive, ref } from "vue";
import ModuleHeader from "@/components/ModuleHeader.vue";
import router from "@/router";
import { ElMessage } from "element-plus";

const role = ref("");
const search = ref(0);
const searchName = ref("");
const searchState = ref<number | null>(null);

const childForm = reactive({
  searchName: "",
  searchState: -1,
});

const options = [
  { state: 1, label: "正常" },
  { state: 0, label: "禁用" },
];

function searchDepartment() {
  console.log('执行搜索 - 部门名称:', searchName.value, '状态:', searchState.value);
  childForm.searchName = searchName.value || '';
  childForm.searchState = searchState.value ?? -1;
  search.value = Date.now(); // 使用时间戳确保每次搜索都触发更新
  console.log('搜索参数已更新:', childForm);
}

function resetForm() {
  console.log('执行重置');
  searchName.value = "";
  searchState.value = null;
  childForm.searchName = "";
  childForm.searchState = -1;
  search.value = 0; // 重置为0，显示树形列表而不是搜索结果
  console.log('重置完成，搜索参数:', childForm);
}

onMounted(() => {
  const userRole = sessionStorage.getItem("role");
  if (!userRole) {
    ElMessage.error("尚未登录，请先登录");
    router.push("/");
    return;
  }
  role.value = userRole;
});
</script>

<style lang="scss" scoped>
.department-manager {
  padding: 20px;
  background-color: #f5f7fa;
}

.controls-container {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

h5 {
  margin: 0 10px 0 0;
  color: #606266;
  font-size: 14px;
}

.search-input {
  width: 180px;
  margin-right: 20px;
}

.search-select {
  width: 120px;
  margin-right: 10px;
}
</style>