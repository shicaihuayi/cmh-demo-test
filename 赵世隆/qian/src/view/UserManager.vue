<template>
  <div class="user-manager">
    <ModuleHeader
      title="用户管理"
      description="管理和维护所有用户信息"
      icon="User"
      color="blue"
    />
    <div class="common-layout">
      <el-container style="height: 100%; width: 100%">
        <!-- 部门树 -->
        <el-aside
          style="width: 200px;height: 100%;margin-top:18px"
        >
          <el-input
            v-model="filterText"
            style="width: 180px"
            placeholder="keyword"
          />
          <el-tree
            style="max-width: 100%"
            :data="dataSource"
            node-key="id"
            default-expand-all
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            class="filter-tree"
            ref="treeRef"
            @node-click="getCurrentNode"
          >
            <!-- 列表条目结构 -->
            <template #default="{ data }">
              <!-- 部门信息 -->
              <span class="custom-tree-node">
                <!-- 部门属性 -->
                <div style="text-align: left">
                  <span>{{ data.label }}</span>
                </div>
              </span>
            </template>
          </el-tree>
        </el-aside>
        <div>
          <div class="container">
            <h5>用户名称</h5>
            <el-input
              style="width: 150px;margin-left:10px;margin-right:10px"
              placeholder="请输入用户名称"
              v-model="searchParams.name"
              clearable
            >
            </el-input>
            <h5>手机号码</h5>
            <el-input
              style="width: 150px;margin-left:10px;margin-right:10px"
              placeholder="请输入手机号码"
              v-model="searchParams.tel"
              clearable
            >
            </el-input>
            <h5>用户昵称</h5>
            <el-input
              style="width: 150px;margin-left:10px;margin-right:10px"
              placeholder="请输入用户昵称"
              v-model="searchParams.nickname"
              clearable
            >
            </el-input>
            <h5>创建时间</h5>
            <el-date-picker
              v-model="searchParams.dates"
              type="datetimerange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              range-separator="至"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 350px; margin-left:10px; margin-right:10px"
            >
            </el-date-picker>
            <el-button type="primary" @click="search">搜索</el-button>
            <el-button type="primary" @click="reset">重置</el-button>
          </div>
          <div class="mb-4" style=" margin-top: 20px;margin-bottom:20px">
            <el-button type="primary" style="width: 100px" @click="openAdd">新增</el-button>
            <el-button type="danger" style="width: 100px" @click="delUser">删除</el-button>
            <el-button type="info" style="width: 100px" @click="exportToExcel">导出</el-button>
          </div>
          <!-- 列出租户-->
          <el-table
            ref="multipleTableRef"
            :data="pagedComp"
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55"/>
            <el-table-column type="index" width="50"/>
            <el-table-column property="name" label="用户名称"/>
            <el-table-column property="nickname" label="用户昵称"/>
            <el-table-column property="section" label="部门"/>
            <el-table-column property="tel" label="电话"/>
            <el-table-column property="state" label="状态">
              <template v-slot="scope">
                <el-switch
                  v-model="scope.row.state"
                  :active-value="1"
                  :inactive-value="0"
                  @change="change(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column property="time" label="创建时间"/>
            <el-table-column label="操作">
              <template #default="scope">
                <el-link type="primary" style="margin-right: 10px;" @click="openForm(scope.row)">修改</el-link>
                <!--                <el-link type="danger"  style="margin-right: 10px;">详情</el-link>-->
              </template>
            </el-table-column>
          </el-table>
          <!-- 分页 -->
          <div class="pagination">
            <div class="pagination-info">
              <span class="total-info">共 {{ users.length }} 条记录</span>
              <div class="page-size-selector">
                <el-select v-model="pageSize" style="width: 120px;">
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
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :background="true"
              layout="prev, pager, next"
              :total="users.length"
              :pager-count="7"
              :hide-on-single-page="false"
              @current-change="handlePageChange"
            />
          </div>
          <!--         增加用户-->
          <el-dialog title="增加用户" v-model="dialogFormVisible" width="50%" @close="back">
            <el-form label-width="80px" :model="user"
                     :rules="rules" ref="userRef">
              <el-form-item label="用户名称" prop="name">
                <el-input v-model="user.name"></el-input>
              </el-form-item>
              <el-form-item label="用户昵称" prop="nickname">
                <el-input v-model="user.nickname"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="pwd">
                <el-input v-model="user.pwd"></el-input>
              </el-form-item>
              <el-form-item label="电话" prop="tel">
                <el-input v-model="user.tel" @input="validateInput"></el-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="user.email"></el-input>
              </el-form-item>
              <el-form-item label="状态" prop="state">
                <el-radio-group v-model="user.state" class="ml-4">
                  <el-radio :label=1>正常</el-radio>
                  <el-radio :label=0>停用</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="岗位" prop="position">
                <el-select v-model="user.position" placeholder="Select" style="width: 240px">
                  <el-option
                      v-for="item in options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="性别" prop="sex">
                <el-radio-group v-model="user.sex" class="ml-4">
                  <el-radio value="1">男</el-radio>
                  <el-radio value="0">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="身份" prop="role">
                <el-radio-group v-model="user.role" class="ml-4">
                  <el-radio value="0">普通用户</el-radio>
                  <el-radio value="1">普通管理员</el-radio>
                  <el-radio value="2">企业管理员</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="所属部门" prop="section">
                <el-tree-select v-model="value" :data="dataSource" check-strictly :render-after-expand="false"
                                style="width: 240px; color: black;"/>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="addUser()" style="margin-left: 250px">提交</el-button>
                <el-button @click="back()">返回</el-button>
              </el-form-item>
            </el-form>
          </el-dialog>
          <!--    修改用户-->
          <el-dialog title="修改用户" v-model="dialogVisible" width="40%"
                     :rules="rules" @close="back">
            <el-form ref="userRef" :model="user" label-width="80px"
                     :rules="rules">
              
              <el-form-item label="用户名" prop="name">
                <el-input v-model="user.name"></el-input>
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="user.nickname"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="pwd">
                <el-input v-model="user.pwd"></el-input>
              </el-form-item>
              <el-form-item label="电话" prop="tel">
                <el-input v-model="user.tel" @input="validateInput"></el-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="user.email"></el-input>
              </el-form-item>
              <el-form-item label="岗位" prop="position">
                <el-select v-model="user.position" placeholder="Select" style="width: 240px">
                  <el-option
                      v-for="item in options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="性别" prop="sex">
                <el-radio-group v-model="user.sex" class="ml-4">
                  <el-radio value="1">男</el-radio>
                  <el-radio value="0">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="身份" prop="role">
                <el-radio-group v-model="user.role" class="ml-4">
                  <el-radio value="0">普通用户</el-radio>
                  <el-radio value="1">普通管理员</el-radio>
                  <el-radio value="2">企业管理员</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="所属部门" prop="section">
                <el-tree-select v-model="value" :data="dataSource" check-strictly :render-after-expand="false"
                                style="width: 240px; color: black;"/>
              </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                  <el-button type="primary" @click="update()">确 定</el-button>
                  <el-button type="info" @click="back()">返 回</el-button>
                </span>
          </el-dialog>
        </div>
      </el-container>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, watch } from "vue";
import {
  ElMessage,
  ElMessageBox,
  ElTable,
  ElTree,
  type FormInstance,
} from "element-plus";
import type Node from 'element-plus/es/components/tree/src/model/node';
import service from "@/utils/request";
import * as XLSX from "xlsx";
import ModuleHeader from "@/components/ModuleHeader.vue";
import router from "@/router";
import { searchUsers, searchUsersByDepartment, searchUsersByCompany, updateUserState, deleteUsers, deleteUsersFormData } from '@/api/user';
import moment from "moment";

interface users {
  id: string;
  name: string;
  nickname: string;
  tel: string;
  email: string;
  pwd: string;
  section: string;
  role: string;
  state: number;
  sex: string;
  position: string;
  time: string;
}

const searchParams = ref({
  name: '',
  nickname: '',
  tel: '',
  dates: []
});
const user = ref({
  name: '',
  nickname: '',
  tel: '',
  email: '',
  pwd: '',
  secId: '',
  companyName: '',
  section: '',
  role: '0',
  state: 1,
  sex: '1',
  position: ''
});
const rules = ref({
  name: [{
    required: true,
    message: '用户名不能为空',
    trigger: 'blur'
  }],
  nickname: [{
    required: true,
    message: '昵称不能为空',
    trigger: 'blur'
  }],
  pwd: [{
    required: true,
    message: '密码不能为空',
    trigger: 'blur'
  }],
});
// const value = ref()
const options = [
  {
    value: '码农',
    label: '码农',
  },
  {
    value: '产品经理',
    label: '产品经理',
  },
  {
    value: '牢大',
    label: '牢大',
  },
  {
    value: '架构师',
    label: '架构师',
  },
];
const dialogVisible = ref(false);
const dialogFormVisible = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const multipleTableRef = ref<InstanceType<typeof ElTable>>();
const multipleSelection = ref<users[]>([]);
const userRef = ref<FormInstance | null>(null);
const users = ref<any[]>([]);
const userId = ref('');
const userState = ref('');
const pagedComp = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return users.value.slice(start, end);
});
const handlePageChange = (page: number) => {
  currentPage.value = page;
};
const handleSelectionChange = (val: users[]) => {
  multipleSelection.value = val;
};

function validateInput(value: any) {
  user.value.tel = value.replace(/[^\d]/g, '');
}

function openAdd() {
  dialogFormVisible.value = true;
  value.value = null;
}

function openForm(row: any) {
  value.value = row.secId;
  userId.value = row.id;
  user.value = Object.assign({}, row);
  user.value = Object.assign({}, row);
  dialogVisible.value = true;
}

function back() {
  dialogFormVisible.value = false;
  dialogVisible.value = false;
  value.value = null;
  user.value = {
    name: '',
    nickname: '',
    tel: '',
    email: '',
    pwd: '',
    section: '',
    secId: '',
    companyName: '',
    role: '0',
    state: 1,
    sex: '1',
    position: ''
  };
  userRef.value!.resetFields();
}

function loadUser() {
  service.get('/user/list').then((res: any) => {
    if (res.isOK) {
      // 移除前端用户过滤，超级管理员应该看到所有用户
      users.value = res.users.map((user: users) => ({
        ...user,
        time: moment(user.time).format('YYYY-MM-DD HH:mm:ss')
      }));
    } else {
      ElMessage.error(res.msg || '加载用户列表失败');
    }
  });
}

function loadCompanyUser() {
  let fd = new FormData();
  fd.append('companyName', sessionStorage.getItem('companyName')!);
  service.post('/user/searchuserbycompanyName', fd).then((res: any) => {
    if (res.isOk) {
      users.value = res.users.map((user: users) => ({
        ...user,
        time: moment(user.time).format('YYYY-MM-DD HH:mm:ss')
      }));
    } else {
      ElMessage.error(res.msg || '加载公司用户列表失败');
    }
  });
}

function load() {
  if (sessionStorage.getItem('role') == '3') {
    loadUser();
  } else {
    loadCompanyUser();
  }
}

function addUser() {
  if (!userRef.value) return;
  if (value.value == null) {
    ElMessage.error('请选择用户所属部门');
    return;
  } else if (value.value > 0) {
    let node = ValueDFS(dataSource.value[0], value.value)[0];
    user.value.section = node.label;
    user.value.secId = node.departmentId as unknown as string;
    user.value.companyName = node.companyName;
  } else {
    ElMessage.error('请选择公司中的部门');
    return;
  }
  userRef.value.validate((valid: boolean) => {
    if (valid) {
      let fd = new FormData();
      fd.append('name', user.value.name);
      fd.append('nickname', user.value.nickname);
      fd.append('pwd', user.value.pwd);
      fd.append('tel', user.value.tel);
      fd.append('email', user.value.email);
      fd.append('section', user.value.section);
      fd.append('secId', user.value.secId);
      fd.append('companyName', user.value.companyName);
      fd.append('sex', user.value.sex);
      fd.append('role', user.value.role);
      fd.append('state', user.value.state.toString());
      fd.append('position', user.value.position);
      service.post('/user/add', (fd)).then((res: any) => {
        ElMessage.success(res.msg);
        load();
        back();
      });
    } else {
      ElMessage.error('请输入完整的信息');
    }
  });
}

async function delUser() {
  if (multipleSelection.value.length === 0) {
    ElMessage.error('请选择要删除的用户');
    return;
  }

  const selectedCount = multipleSelection.value.length;
  const selectedNames = multipleSelection.value.map(item => item.name).join('、');
  
  try {
    await ElMessageBox.confirm(
      `确定要删除这 ${selectedCount} 个用户吗？\n${selectedNames}`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        icon: 'Warning',
        confirmButtonClass: 'el-button--danger',
        center: true,
        roundButton: true,
        showClose: true,
        customClass: 'delete-confirm-dialog'
      }
    );

    const ids = multipleSelection.value.map(item => item.id);

    try {
      // 首先尝试JSON格式
      let response = await deleteUsers(ids);
      
      // 如果JSON格式失败，尝试FormData格式
      if (!response || response.isOK === false) {
        console.log('JSON格式删除失败，尝试FormData格式');
        response = await deleteUsersFormData(ids);
      }
      
      if (response && response.isOK !== false) {
        ElMessage.success(response.msg || '删除成功');
        // 重新加载用户列表
        if (sessionStorage.getItem('role') == '3') {
          loadUser();
        } else {
          loadCompanyUser();
        }
      } else {
        ElMessage.error(response?.msg || '删除失败，可能无权限删除管理员');
      }
    } catch (error) {
      console.error('删除用户失败:', error);
      ElMessage.error('删除失败，请稍后重试');
    }
  } catch {
    // 用户取消删除
    ElMessage.info('已取消删除');
  }
}

function update() {
  if (!userRef.value) return;

  if (value.value == null) {
    ElMessage.error('请选择用户所属部门');
    return;
  } else if (value.value > 0) {
    let node = ValueDFS(dataSource.value[0], value.value)[0];
    user.value.section = node.label;
    user.value.secId = node.departmentId as unknown as string;
    user.value.companyName = node.companyName;
  } else {
    ElMessage.error('请选择公司中的部门');
    return;
  }
  userRef.value.validate((valid: boolean) => {
    if (valid) {
      let fd = new FormData();
      fd.append('id', userId.value);
      fd.append('name', user.value.name);
      fd.append('tel', user.value.tel);
      fd.append('nickname', user.value.nickname);
      fd.append('pwd', user.value.pwd);
      fd.append('email', user.value.email);
      fd.append('companyName', user.value.companyName);
      fd.append('secId', user.value.secId);
      fd.append('section', user.value.section);
      fd.append('role', user.value.role);
      fd.append('position', user.value.position);
      fd.append('sex', user.value.sex);

      service.post('/user/update', fd).then((res: any) => {
        ElMessage.success(res.msg);
        load();
        back();
      });
    } else {
      ElMessage.error('请输入完整的信息');
    }
  });
}

async function search() {
  const { name, nickname, tel, dates } = searchParams.value;
  if (!name && !nickname && !tel && (!dates || dates.length === 0)) {
    ElMessage.error('请输入搜索条件');
    return;
  }

  const role = sessionStorage.getItem('role');
  let params: any = {
      name: name,
      nickname: nickname,
      tel: tel,
      startDate: dates ? dates[0] : null,
      endDate: dates ? dates[1] : null,
  };

  // 如果不是超级管理员，则在搜索时限定公司范围
  if (role !== '3') {
      params.companyName = sessionStorage.getItem('companyName');
  }

  try {
    // 统一使用一个搜索接口，后端根据有无companyName来区分查询范围
    const response = await searchUsers(params);

    if (response && response.isOK) {
      users.value = response.users.map((user: users) => ({
        ...user,
        time: moment(user.time).format('YYYY-MM-DD HH:mm:ss')
      }));
      ElMessage.success(response.msg || '搜索成功');
    } else {
      users.value = [];
      ElMessage.error(response?.msg || '未找到匹配的用户');
    }
  } catch (error) {
    console.error('搜索失败:', error);
    ElMessage.error('搜索失败，请稍后重试');
  }
}

async function change(row: any) {
  userId.value = row.id;
  userState.value = row.state;
  
  try {
    const response = await updateUserState({
      id: userId.value,
      state: userState.value,
    });

    if (response && response.isOK) {
      ElMessage.success(response.msg || '状态更新成功');
    } else {
      ElMessage.error(response?.msg || '状态更新失败');
    }
  } catch (error) {
    console.error('状态更新失败:', error);
    ElMessage.error('状态更新失败，请稍后重试');
  }
}

function exportToExcel() {
  const ws = XLSX.utils.json_to_sheet(users.value);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Users');
  XLSX.writeFile(wb, 'users.xlsx');
}

function reset() {
  searchParams.value = {
    name: '',
    nickname: '',
    tel: '',
    dates: []
  };
  value.value = null;
  load();
}

//树形结构
let id = 0;
const value = ref();
const filterText = ref('');
const companyName = ref('');
const treeRef = ref<InstanceType<typeof ElTree>>();

interface Tree {
  value: number;
  disabled: boolean;
  id: number;
  departmentId: number;
  label: string;
  order: number;
  state: number;
  director: string;
  departmentLevel: number;
  companyName: string;
  children?: Tree[];
}

const getCurrentNode = () => {
  const currentNode: Tree = treeRef.value!.getCurrentNode() as Tree;
  users.value = [];
  if (currentNode.departmentId == -1 && sessionStorage.getItem('role') == '3') {
    loadUser();
  } else {
    displayDFS(currentNode);
  }
};

async function displayUserById(departmentId: number) {
  try {
    const response = await searchUsersByDepartment(departmentId);
    if (response && response.isOk) {
      for (let user of response.users) {
        user.time = moment(user.time).format('YYYY-MM-DD HH:mm:ss');
        users.value.push(user);
      }
    }
  } catch (error) {
    console.error('根据部门ID搜索用户失败:', error);
  }
}

async function displayUserByName(companyName: string) {
  try {
    const response = await searchUsersByCompany(companyName);
    if (response && response.isOk) {
      for (let user of response.users) {
        user.time = moment(user.time).format('YYYY-MM-DD HH:mm:ss');
        users.value.push(user);
      }
    }
  } catch (error) {
    console.error('根据公司名搜索用户失败:', error);
  }
}

function displayDFS(tree: Tree) {
  if (tree.departmentId == 0) {
    displayUserByName(tree.companyName);
  } else {
    displayUserById(tree.departmentId);
  }
  if (tree.children) {
    for (const child of tree.children) {
      displayDFS(child);
    }
  }
}

function ValueDFS(tree: Tree, value: number): Tree[] {
  let result: Tree[] = [];
  if (tree.value === value) {
    result.push(tree);
  }
  if (tree.children) {
    for (const child of tree.children) {
      result = result.concat(ValueDFS(child, value));
    }
  }
  return result;
}

watch(filterText, (val) => {
  treeRef.value!.filter(val);
});

const filterNode = (value: string, data: any, node: Node) => {
  if (!value) return true;
  const treeNode = data as Tree;
  return treeNode.label.includes(value);
};

const dataSource = ref<Tree[]>([
  {
    value: 0,
    disabled: true,
    id: 0,
    departmentId: -1,
    label: '测盟会',
    order: 0,
    state: 1,
    director: '超级管理员',
    departmentLevel: -1,
    companyName: '',
    children: [],
  },
]);

function loadDepartment() {
  const role = sessionStorage.getItem('role');
  const companyNameFromSession = sessionStorage.getItem('companyName');

  if (role === '3') {
    // 超级管理员加载所有公司和部门
    service.post('/company/list').then((result: any) => {
      if (result.isOK) {
        let companys = result.comps;
        let i = 0;
        for (let company of companys) { // 将所有公司读入树结点
          if (company.state == "1") {
            company.state = 1 as number;
          } else if (company.state == "0") {
            company.state = 0 as number;
          } else {
            company.state = -1 as number;
          }
          const newChild = {
            disabled: false, value: --value.value, id: id++, departmentId: 0, label: company.name, order: i,
            state: company.state, director: company.admin, departmentLevel: 0,
            companyName: company.name
          };
          append(dataSource.value[0], newChild);
          i++;
          const companyname = new FormData();
          companyname.append('name', company.name);
          service.post('/department/companydata', companyname).then((result: any) => {
            if (result.isOk) {
              let departments = result.list;
              for (let department of departments) {
                if (department.state == "1") {
                  department.state = 1 as number;
                } else if (department.state == "0") {
                  department.state = 0 as number;
                } else {
                  department.state = -1 as number;
                }
                const newChild = {
                  disabled: false,
                  value: department.departmentId,
                  id: 0,
                  departmentId: department.departmentId,
                  label: department.departmentName,
                  order: department.order,
                  state: department.state,
                  director: department.director,
                  departmentLevel: department.departmentLevel,
                  companyName: department.companyName
                };
                if (department.departmentLevel == 1 && dataSource.value[0].children) { // 将1级部门读入树结点
                  for (let company of dataSource.value[0].children) {
                    if (company.companyName == department.companyName) {
                      append(company, newChild);
                    }
                  }
                } else {
                  append(IdDFS(dataSource.value[0], department.superior)[0], newChild); // 将非1级部门读入树结点
                }
              }
            }
          });
        }
      }
    });
  } else if (role === '2' || role === '1') {
    if (!companyNameFromSession) {
        ElMessage.error('无法获取公司信息，请重新登录。');
        return;
    }
    // 企业管理员或普通管理员加载自己公司的部门
    const companyname = new FormData();
    companyname.append('name', companyNameFromSession);
    service.post('/company/find', companyname).then((result: any) => {
      if (result.isOk && result.company) {
        let company = result.company;
        companyName.value = company.name; // 正确给 ref 赋值
        if (company.state == "1") {
          company.state = 1 as number;
        } else if (company.state == "0") {
          company.state = 0 as number;
        } else {
          company.state = -1 as number;
        }
        const newChild = {
          disabled: false, value: --value.value, id: id++, departmentId: 0, label: companyName.value, order: 0,
          state: company.state, director: company.admin, departmentLevel: 0,
          companyName: companyName.value
        };
        append(dataSource.value[0], newChild);

        service.post('/department/companydata', companyname).then((result: any) => {
          if (result.isOk) {
            let departments = result.list;
            departments.sort((a: any, b: any) => a.departmentLevel - b.departmentLevel);
            for (let department of departments) {
              if (department.state == "1") {
                department.state = 1 as number;
              } else if (department.state == "0") {
                department.state = 0 as number;
              } else {
                department.state = -1 as number;
              }
              const newChild = {
                disabled: false,
                value: department.departmentId,
                id: 0,
                departmentId: department.departmentId,
                label: department.departmentName,
                order: department.order,
                state: department.state,
                director: department.director,
                departmentLevel: department.departmentLevel,
                companyName: department.companyName
              };
              if (department.departmentLevel == 1 && dataSource.value[0].children) { // 将1级部门读入树结点
                for (let company of dataSource.value[0].children) {
                  if (company.companyName == department.companyName) {
                    append(company, newChild);
                  }
                }
              } else {
                append(IdDFS(dataSource.value[0], department.superior)[0], newChild); // 将非1级部门读入树结点
              }
            }
          }
        });
      } else {
        ElMessage.error(result.msg || '加载公司部门失败');
      }
    });
  } else {
    ElMessage.error('尚未登录，请先登录');
    router.push('/');
  }
  value.value = null;
}

function append(tree: Tree, data: Tree) {//添加子结点
  let newChild: Tree;
  if (tree.departmentLevel == -1) {//添加公司结点
    newChild = {
      disabled: false, value: data.value, id: id++, departmentId: data.departmentId, label: data.label,
      order: data.order, state: data.state, director: data.director, departmentLevel: 0,
      companyName: data.label, children: []
    };
  } else {//添加部门结点
    newChild = {
      disabled: false, value: data.departmentId, id: id++, departmentId: data.departmentId, label: data.label,
      order: data.order, state: data.state, director: data.director, departmentLevel: tree.departmentLevel + 1,
      companyName: tree.companyName, children: []
    };
  }
  if (!tree.children) {
    tree.children = [];
  }
  tree.children.push(newChild); // 将新的树加入原结点的子结点数组中
}

function IdDFS(tree: Tree, departmentId: number): Tree[] {
  let result: Tree[] = [];
  if (tree.departmentId === departmentId) {
    result.push(tree);
  }
  if (tree.children) {
    for (const child of tree.children) {
      result = result.concat(IdDFS(child, departmentId));
    }
  }
  return result;
}

onMounted(() => {
  load();
  loadDepartment();
});
</script>

<style lang="scss" scoped>
.common-layout {
  height: 100%;
  width: 100%;
  display: flex;
}

.container {
  display: flex;
  align-items: center;
  margin-top: 18px;
  background-color: #ffffff;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.container h5 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 分页样式 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  padding: 10px 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #606266;
}

.total-info {
  font-size: 14px;
  color: #606266;
}

.page-size-selector {
  display: flex;
  align-items: center;
}

.page-size-selector .el-select {
  width: 120px;
}

:deep(.el-input__inner) {
  text-align: center;
}

/* 删除确认弹窗样式 */
.delete-confirm-dialog {
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.delete-confirm-dialog .el-message-box__title {
  font-size: 18px;
  font-weight: bold;
}

.delete-confirm-dialog .el-message-box__content {
  font-size: 16px;
  line-height: 1.6;
}

.delete-confirm-dialog .el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
}

.delete-confirm-dialog .el-button--danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.user-manager {
  padding: 20px;
  background-color: #f5f7fa;
}
.search-container {
  display: flex;
  justify-content: space-between;
}
</style>