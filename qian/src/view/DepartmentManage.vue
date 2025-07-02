<template>
  <div style=" text-align: left;">
    <div style="display: inline-flex; color: black; align-items: center; margin: 0;">
      <h5>部门名称：</h5>
      <el-input v-model="searchName" placeholder="请输入部门名称" class="search"
                style="width: 180px; padding-left: 10px; height: 32px;"></el-input>
      <h5>状态：</h5>
      <el-select v-model="searchState" placeholder="部门状态" style="width: 100px; height: 32px;" class="search">
        <el-option v-for="item in options" :key="item.state" :label="item.label" :value="item.state"/>
      </el-select>
      <el-button type="primary" @click="searchDepartment()">搜索</el-button>
      <el-button @click="resetForm()">重置</el-button>
    </div>
    <DepartmentTree :name="childForm.searchName" :state="childForm.searchState" :search="search"/>
  </div>
</template>

<script lang="ts" setup name="DepartmentManage">
import router from '@/router';
import DepartmentTree from './DepartmentTree.vue';
import {ElMessage} from 'element-plus';
import {onMounted, reactive, ref} from 'vue';

const role = ref('');
const searchName = ref('');
const searchState = ref(-1);
const search = ref(0);

const options = [
  {
    state: 1,
    label: '正常',
  },
  {
    state: 0,
    label: '停用',
  },
]

const childForm = reactive({
  searchName: '',
  searchState: -1,
})

function resetForm() {
  searchName.value = ''
  searchState.value = null;
  search.value = 0;
};

function searchDepartment() {
  childForm.searchName = searchName.value;
  if (searchState.value != -1) {
    childForm.searchState = searchState.value;
  }
  search.value = 1;
}

onMounted(() => {
  role.value = sessionStorage.getItem('role') as string;
  if (role.value == null) {
    ElMessage.error('尚未登录，请先登录');
    router.push('./');
  }
  searchState.value = null;
})
</script>

<style scoped>
.el-button {
  padding: 0px 15px;
}

.el-input {
  display: flex;
}

:deep(.el-input__wrapper) {
  padding: 0px 10px;
}

:deep(.el-select__wrapper) {
  height: 32px;
}

.search {
  height: 40px;
  margin-right: 10px;
}

</style>

