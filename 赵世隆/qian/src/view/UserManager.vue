<template>

  <div class="common-layout">

    <div style="width: 200px;height: 100%;margin-top:18px">
      <el-input
          v-model="filterText"
          style="width: 180px"
          placeholder="keyword"
      />
      <el-tree style="max-width: 100%" :data="dataSource" node-key="id" default-expand-all
               :expand-on-click-node="false" :filter-node-method="filterNode" class="filter-tree" ref="treeRef"
               @node-click="getCurrentNode">
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

    </div>
    <div>

      <div class="container">

        <h5>用户名称</h5>
        <el-input style="width: 150px;margin-left:10px;margin-right:10px" placeholder="请输入用户名称"
                  v-model="searchParams.name" clearable>
        </el-input>
        <h5>手机号码</h5>
        <el-input style="width: 150px;margin-left:10px;margin-right:10px" placeholder="请输入手机号码"
                  v-model="searchParams.tel" clearable>
        </el-input>
        <h5>用户昵称</h5>
        <el-input style="width: 150px;margin-left:10px;margin-right:10px" placeholder="请输入用户昵称"
                  v-model="searchParams.nickname" clearable>
        </el-input>
        <h5>创建时间</h5>
        <el-date-picker
            v-model="searchParams.dates"
            type="datetimerange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            range-separator="至"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 350px; margin-left:10px; margin-right:10px">
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
            <el-switch v-model="scope.row.state"
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

      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="users.length"
          @current-change="handlePageChange"
          layout="total, prev, pager, next, jumper"
          style="margin-top: 20px; text-align: right;"
      />

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
              <el-radio value="1">管理员</el-radio>
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
              <el-radio value="1">管理员</el-radio>
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
  </div>

</template>


<script lang="ts" setup>
import {ElMessage, ElTable, ElTree, type FormInstance} from "element-plus";
import {computed, onMounted, ref, watch} from "vue";
import axios from "axios";
import * as XLSX from 'xlsx';
import moment from 'moment';
import router from "@/router";

interface users {
  id:string
  name: string
  nickname: string
  tel: string
  email: string
  pwd: string
  section: string
  role: string
  state: number
  sex: string
  position: string
  time: string
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
})
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

})
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
]
const dialogVisible = ref(false)
const dialogFormVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const multipleSelection = ref<users[]>([])
const userRef = ref<FormInstance | null>(null);
const users = ref<any[]>([])
const userId = ref('')
const userState = ref('')
const pagedComp = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return users.value.slice(start, end)
})
const handlePageChange = (page: number) => {
  currentPage.value = page
}

const handleSelectionChange = (val: users[]) => {
  multipleSelection.value = val
}

function validateInput(value: any) {
  user.value.tel = value.replace(/[^\d]/g, '');
}

function openAdd() {
  dialogFormVisible.value = true
  value.value = null
}

function openForm(row: any) {
  value.value=row.secId

  userId.value = row.id
  user.value = Object.assign({}, row);
  user.value = Object.assign({}, row);
  dialogVisible.value = true
}

function back() {
  dialogFormVisible.value = false
  dialogVisible.value = false
  value.value = null
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
  }
  userRef.value!.resetFields()
}

function loadUser() {
  axios.get('http://localhost:8080/user/list').then(res => {
    if (res.data.isOK) {
      // users.value = res.data.users;
      users.value = res.data.users.map((user: users) => ({
        ...user,
        time: moment(user.time).format('YYYY-MM-DD HH:mm:ss')
      }));
    }
    ElMessage.success(res.data.msg);
  })
}

function loadCompanyUser() {
  let fd = new FormData()
  fd.append('companyName', sessionStorage.getItem('companyName')!)
  axios.post('http://localhost:8080/user/searchuserbycompanyName', fd).then(res => {
    if (res.data.isOk) {
      // users.value = res.data.users;
      users.value = res.data.users.map((user: users) => ({
        ...user,
        time: moment(user.time).format('YYYY-MM-DD HH:mm:ss')
      }));
    }
    ElMessage(res.data.msg);
  })
}

function load() {
  if (sessionStorage.getItem('role') == '3') {
    loadUser()
  } else {
    loadCompanyUser()
  }

}


function addUser() {
  if (!userRef.value) return;
  if(value.value==null){
    ElMessage.error('请选择用户所属部门')
    return
  }else if( value.value > 0 ){
    let node = ValueDFS(dataSource.value[0], value.value)[0];
    user.value.section = node.label
    user.value.secId = node.departmentId as unknown as string
    user.value.companyName = node.companyName
  }else{
    ElMessage.error('请选择公司中的部门');
    return
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
      axios.post('http://localhost:8080/user/add', fd).then((res) => {
        ElMessage.success(res.data.msg);
        loadUser();
        back();
      });
    } else {
      ElMessage.error('请输入完整的信息');
    }
  });
}

function delUser() {
  if (multipleSelection.value.length === 0) {
    ElMessage.error('请选择要删除的用户');
    return;
  }

  if (!confirm('是否删除该用户？')) {
    return;
  }

  const ids = multipleSelection.value.map(item => item.id);

  axios.post('http://localhost:8080/user/del', ids, {
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(res => {
    if(res.data.isOK==false){
      ElMessage.error('不可删除管理员');
    }else{
      ElMessage.success(res.data.msg);
      loadUser();
    }
  }).catch(err => {
    ElMessage.error('删除失败');
  });
}

function update() {
  if (!userRef.value) return;

  if(value.value==null){
    ElMessage.error('请选择用户所属部门')
    return
  }else if( value.value > 0 ){
    let node = ValueDFS(dataSource.value[0], value.value)[0];
    user.value.section = node.label
    user.value.secId = node.departmentId as unknown as string
    user.value.companyName = node.companyName
  }else{
    ElMessage.error('请选择公司中的部门');
    return
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
      fd.append('secId',user.value.secId);
      fd.append('section', user.value.section);
      fd.append('role', user.value.role);
      fd.append('position', user.value.position);
      fd.append('sex', user.value.sex);

      axios.post('http://localhost:8080/user/update', fd).then((res) => {
        ElMessage.success(res.data.msg);
        loadUser();
        back();
      });
    } else {
      ElMessage.error('请输入完整的信息');
    }
  })
}

function search() {
  const {name,nickname,tel,dates}=searchParams.value
  if (!name && !nickname && !tel && !dates[0]&&!dates[1]) {
    ElMessage.error('请输入搜索条件');
    return;
  }

  axios.post('http://localhost:8080/user/search', {
    name: searchParams.value.name,
    nickname: searchParams.value.nickname,
    tel: searchParams.value.tel,
    startDate: searchParams.value.dates[0],
    endDate: searchParams.value.dates[1]
  }).then(res => {
    if (res.data.isOK) {
      users.value = res.data.users;
    }
    ElMessage(res.data.msg);
  })
}

function change(row: any) {
  userId.value = row.id
  userState.value = row.state
  axios.post('http://localhost:8080/user/updateState', {
    id: userId.value,
    state: userState.value,
  }).then(res => {
    if (res.data.isOK) {
      ElMessage(res.data.msg);
    } else {
      ElMessage.error(res.data.msg);
    }

  })
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
  }
  value.value = null
  load();
}

//树形结构
let id = 0;
const value = ref()
const filterText = ref('')
const companyName = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

interface Tree {
  value: number
  disabled: boolean
  id: number
  departmentId: number
  label: string
  order: number
  state: number
  director: string
  departmentLevel: number
  companyName: string
  children?: Tree[]
}

const getCurrentNode = () => {
  const currentNode: Tree = treeRef.value!.getCurrentNode() as Tree;
  users.value = [];
  if (currentNode.departmentId == -1 && sessionStorage.getItem('role') == '3') {
    loadUser();
  } else {
    displayDFS(currentNode);
  }
}

function displayUserById(departmentId: number) {
  let idfd = new FormData();
  idfd.append('secId', departmentId as unknown as string)
  axios.post('http://localhost:8080/user/search1', idfd).then(res => {
    if (res.data.isOk) {
      for (let user of res.data.users) {
        user.time=moment(user.time).format('YYYY-MM-DD HH:mm:ss')
        users.value.push(user);
      }
    }
  })
}

function displayUserByName(companyName: string) {
  let namefd = new FormData();
  namefd.append('companyName', companyName)
  axios.post('http://localhost:8080/user/searchuserbycompany', namefd).then(res => {
    if (res.data.isOk) {
      for (let user of res.data.users) {
        user.time= moment(user.time).format('YYYY-MM-DD HH:mm:ss')
        users.value.push(user);
      }
    }
  })
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
  treeRef.value!.filter(val)
})

const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.label.includes(value)
}

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
])

function loadData() {
  let role = sessionStorage.getItem('role');
  if (role == '3') {
    loadAllData();
  } else if (role == '2' || role == '1') {
    loadCompanyData();
  } else {
    ElMessage.error('尚未登录，请先登录');
    router.push('/');
  }
  value.value = null;
}

function loadAllData() {
  console.log('读取所有数据');
  axios.post('http://localhost:8080/company/list').then((result) => {
    if (result.data.isOK) {
      let companys = result.data.comps;
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
        axios.post('http://localhost:8080/department/companydata', companyname).then((result) => {
          if (result.data.isOk) {
            let departments = result.data.list;
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
        })
      }
    }
  })
}

function loadCompanyData() { // 读取企业管理员所属企业的信息
  console.log('读取公司数据');
  value.value = 0;
  companyName.value = sessionStorage.getItem('companyName')!;
  const companyname = new FormData();
  companyname.append('name', companyName.value);
  axios.post('http://localhost:8080/company/find', companyname).then((result) => {
    if (result.data.isOk) {
      let company = result.data.company;
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
      }
      append(dataSource.value[0], newChild); // 将公司读入树结点
      axios.post('http://localhost:8080/department/companydata', companyname).then((result) => {
        if (result.data.isOk) {
          console.log(result.data.list)
          let departments = result.data.list;
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
      })
    } else {
      ElMessage.error(result.data.msg);
    }
  })


}

function append(tree: Tree, data: Tree) {//添加子结点
  let newChild: Tree;
  if (tree.departmentLevel == -1) {//添加公司结点
    newChild = {
      disabled: false, value: data.value, id: id++, departmentId: data.departmentId, label: data.label,
      order: data.order, state: data.state, director: data.director, departmentLevel: 0,
      companyName: data.label, children: []
    }
  } else {//添加部门结点
    newChild = {
      disabled: false, value: data.departmentId, id: id++, departmentId: data.departmentId, label: data.label,
      order: data.order, state: data.state, director: data.director, departmentLevel: tree.departmentLevel + 1,
      companyName: tree.companyName, children: []
    }
  }
  if (!tree.children) {
    tree.children = []
  }
  tree.children.push(newChild) // 将新的树加入原结点的子结点数组中
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
  loadData();
})


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
}

</style>()

