<template>
  <div class="common-layout">


    <!--        主界面-->

    <div class="container">
      <h5>租户名称</h5>
      <el-input v-model="searchParams.name" clearable
                placeholder="请输入租户名称" style="width: 250px;margin-left:10px;margin-right:10px">
      </el-input>
      <h5>联系人</h5>
      <el-input v-model="searchParams.linkman" clearable
                placeholder="请输入联系人" style="width: 250px;margin-left:10px;margin-right:10px">
      </el-input>
      <h5>电话</h5>
      <el-input v-model="searchParams.tel" clearable
                placeholder="请输入电话" style="width: 250px;margin-left:10px;margin-right:10px">
      </el-input>
      <h5>租户标识</h5>
      <el-input v-model="searchParams.sign" clearable
                placeholder="请输入租户标识" style="width: 250px;margin-left:10px;margin-right:10px">
      </el-input>
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button type="primary" @click="reset">重置</el-button>
    </div>

    <div class="mb-4" style=" margin-top: 20px;margin-bottom:20px">

      <el-button style="width: 100px" type="primary" @click="openAdd">新增</el-button>
      <el-button style="width: 100px" type="danger" @click="delCom">删除</el-button>
      <el-button style="width: 100px" type="warning" @click="importExcel">导入</el-button>
      <input ref="refFileInput" style="display: none;" type="file" @change="handleFileUpload">
      <el-button style="width: 100px;margin-left: 10px" type="info" @click="exportToExcel">导出</el-button>
    </div>

    <!--          列出租户-->
    <el-table
        ref="multipleTableRef"
        :data="pagedComp"
        style="width: 100%"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55"/>
      <el-table-column label="公司图标" width="100">
        <template v-slot="scope">
          <img :src="scope.row.coverUrl" alt="公司图标" style="width: 50px; height: 50px;"/>
        </template>
      </el-table-column>
      <!--      <el-table-column type="selection" width="55"/>-->
      <el-table-column label="公司名称" property="name"/>
      <el-table-column label="联系人" property="linkman"/>
      <el-table-column label="电话" property="tel"/>
      <el-table-column label="管理员" property="admin"/>
      <el-table-column label="公司标识" property="sign"/>
      <el-table-column label="操作">
        <template #default="scope">
          <el-link style="margin-right: 10px;" type="primary" @click="openForm(scope.row)">修改</el-link>
          <el-link style="margin-right: 10px;" type="danger" @click="detail(scope.row)">详情</el-link>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="comp.length"
        layout="total, prev, pager, next, jumper"
        style="margin-top: 20px; text-align: right;"
        @current-change="handlePageChange"
    />

    <!--            增加租户-->
    <el-dialog v-model="dialogFormVisible" title="增加租户" width="50%" @close="back()">

      <el-form-item label="公司图标">
        <el-upload  ref="uploadImageRef" :before-upload="beforeUploadImage" :file-list="fileList"
                   :limit="1" :on-preview="handlePictureCardPreview" :on-remove="handleRemove"
                   :on-success="UploadImage"
                   :show-file-list="showUploadButton"
                   action="http://localhost:8080/upload/image"
                   list-type="picture-card">

            <el-icon :size="30">
              <Plus/>
            </el-icon>

<!--          <el-icon :size="30">-->
<!--            <Plus/>-->
<!--          </el-icon>-->
          <template #tip>
            <div style="font-size: 12px;color: #919191;">
              <span>请上传大小不超过</span>
              <span style="color: red;">5MB</span>
              <span>格式为</span>
              <span style="color: red;">png/jpg/jpeg</span>
              <span>的图片</span>
            </div>
          </template>
        </el-upload>
        <el-dialog v-model="dialogVisible1" style="line-height: 0;">
          <img :src="dialogImageUrl" alt="" style="width: 100%;height: 100%"/>
        </el-dialog>
      </el-form-item>
      <!-- //上传图片 -->

      <el-form ref="comRef" :model="com"
               :rules="rules" label-width="80px">
        <el-form-item label="公司名称" prop="name">
          <el-input v-model="com.name"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="linkman">
          <el-input v-model="com.linkman"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="tel">
          <el-input v-model="com.tel" @input="validateInput"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="mail">
          <el-input v-model="com.mail"></el-input>
        </el-form-item>
        <el-form-item label="管理员" prop="admin">
          <el-input v-model="com.admin"></el-input>
        </el-form-item>
        <el-form-item label="标识" prop="sign">
          <el-input v-model="com.sign"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="desc">
          <el-input v-model="com.desc"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button style="margin-left: 250px" type="primary" @click="addCom()">提交</el-button>
          <el-button @click="back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!--          修改租户-->
    <el-dialog v-model="dialogVisible" :rules="rules" title="修改租户"
               width="40%" @close="back()">

      <el-form-item label="公司图标">
        <el-upload ref="uploadImageRef" :before-upload="beforeUploadImage" :file-list="fileList"
                   :limit="1" :on-preview="handlePictureCardPreview" :on-remove="handleRemove"
                   :on-success="UploadImage"
                   action="http://localhost:8080/upload/image"
                   list-type="picture-card">

            <el-icon :size="30">
              <Plus/>
            </el-icon>

          <template #tip>
            <div style="font-size: 12px;color: #919191;">
              <span>请上传大小不超过</span>
              <span style="color: red;">5MB</span>
              <span>格式为</span>
              <span style="color: red;">png/jpg/jpeg</span>
              <span>的图片</span>
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form ref="comRef" :model="com" :rules="rules"
               label-width="80px">
        <
        <el-form-item label="公司名称" prop="name">
          <el-input v-model="com.name"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="linkman">
          <el-input v-model="com.linkman"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="tel">
          <el-input v-model="com.tel" @input="validateInput"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="mail">
          <el-input v-model="com.mail"></el-input>
        </el-form-item>
        <el-form-item label="管理员" prop="admin">
          <el-input v-model="com.admin"></el-input>
        </el-form-item>
        <el-form-item label="标识" prop="sign">
          <el-input v-model="com.sign"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="desc">
          <el-input v-model="com.desc"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
              <el-button type="primary" @click="update()">确 定</el-button>
              <el-button type="info" @click="back()">返 回</el-button>
            </span>
    </el-dialog>

    <!--    详情-->

    <el-dialog v-model="dialogVisible1" style="line-height: 0;">
      <img :src="dialogImageUrl" alt="公司图标" style="width: 100%; height: 100%"/>
    </el-dialog>


  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {ElMessage, ElTable, ElUpload, type FormInstance, type UploadFile} from "element-plus";
import axios from "axios";
import * as XLSX from 'xlsx';
import {Plus} from "@element-plus/icons-vue";

interface comp {
  id: number
  name: string
  linkman: string
  tel: string
  desc: string
  admin: string
  sign: string
  state: number
  mail: string
  coverUrl: string
}

const searchParams = ref({
  name: '',
  linkman: '',
  tel: '',
  sign: ''
});
const com = ref({
  name: '',
  linkman: '',
  tel: '',
  desc: '',
  admin: '',
  sign: '',
  state: 1,
  mail: '',
  coverUrl: ''
})
const rules = ref({
  name: [{
    required: true,
    message: '公司名不能为空',
    trigger: 'blur'
  }],
  linkman: [{
    required: true,
    message: '联系人不能为空',
    trigger: 'blur'
  }],
  tel: [{
    required: true,
    message: '电话不能为空',
    trigger: 'blur'
  }],
  admin: [{
    required: true,
    message: '管理员不能为空',
    trigger: 'blur'
  }],
  sign: [{
    required: true,
    message: '标识不能为空',
    trigger: 'blur'
  }],
  desc: [{
    required: true,
    message: '描述不能为空',
    trigger: 'blur'
  }],
})

const oldname = ref('');
const comId = ref('')
const oldadmin=ref('')
const dialogVisible = ref(false)
const dialogFormVisible = ref(false)
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const multipleSelection = ref<comp[]>([])
const value = ref('')
const comRef = ref<FormInstance | null>(null);
const comp = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const refFileInput = ref<HTMLInputElement | null>(null);
//上传图片
const uploadImageRef = ref<InstanceType<typeof ElUpload> | null>(null);
const fileList = ref([]);
const dialogImageUrl = ref('');
const dialogVisible1 = ref(false);

function beforeUploadImage(file: any) {
  console.log("imageBefore");
  //文件大小
  const isLt5M = file.size / 1024 / 1024 < 5
  //图片后缀检查
  if (['image/jpeg', 'image/png', 'image/jpg'].indexOf(file.type) === -1) {
    ElMessage.error('请上传正确的图片格式')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('上传视频大小不能超过5MB哦!')
    return false
  }
}

const UploadImage = (res: any, file: any) => {
  if (res.resCode === '200') {
    com.value.coverUrl = res.ImageUrl
    ElMessage.success('图片上传成功！')
  } else {
    ElMessage.error('图片上传失败，请重新上传！')
  }
};

const handleRemove = (file: any, fileList: any) => {
  console.log(file, fileList);

};

const handlePictureCardPreview = (file: UploadFile) => {
  // console.log(file.url);
  dialogVisible1.value = true;
  dialogImageUrl.value = file.url!
};

function reset1() {
  uploadImageRef.value!.clearFiles();
  comRef.value.resetFields()
  //清除文件列表
}

//分页函数
const pagedComp = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return comp.value.slice(start, end)
})

const handlePageChange = (page: number) => {
  currentPage.value = page
}

const handleSelectionChange = (val: comp[]) => {
  multipleSelection.value = val
}

//让电话只能为数字
function validateInput(value: any) {
  com.value.tel = value.replace(/[^\d]/g, '');
}

//加载公司
function loadCom() {
  axios.get('http://localhost:8080/company/list').then(res => {
    if (res.data.isOK) {
      comp.value = res.data.comps;
      ElMessage.success(res.data.msg);
    }else
    ElMessage.error(res.data.msg);
  })
}

//增加公司
function addCom() {
  if (!comRef.value) return;

  comRef.value.validate((valid: boolean) => {
    if (valid) {
      let fd = new FormData();
      fd.append('name', com.value.name);
      fd.append('linkman', com.value.linkman);
      fd.append('tel', com.value.tel);
      fd.append('admin', com.value.admin);
      fd.append('sign', com.value.sign);
      fd.append('mail', com.value.mail);
      fd.append('state', com.value.state);
      fd.append('desc', com.value.desc);
      fd.append('coverUrl', com.value.coverUrl);
      console.log(fd);
      axios.post('http://localhost:8080/company/add', fd).then((res) => {
        if(res.data.isOK){
          ElMessage.success(res.data.msg);
          addAdmin();
          loadCom();
          back();
        }else{
          ElMessage.error(res.data.msg);
        }

      });
    } else {
      ElMessage.error('请输入完整的信息');
    }
  });
}
//增加管理员
function addAdmin(){
  let fd = new FormData();
  fd.append('name', com.value.admin);
  fd.append('companyName', com.value.name);
  fd.append('role', '2');
  fd.append('pwd',com.value.sign);
  axios.post('http://localhost:8080/user/add', fd);
}

//删除公司
function delCom() {
  if (multipleSelection.value.length === 0) {
    ElMessage.error('请选择要删除的公司');
    return;
  }

  if (!confirm('删除后会删除该公司所有部门和员工，是否删除？')) {
    return;
  }

  const ids = multipleSelection.value.map(item => item.id);

  axios.post('http://localhost:8080/company/del', ids, {
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(res => {
    delSection()
    delUser()
    ElMessage.success(res.data.msg);
    loadCom();
  }).catch(err => {
    ElMessage.error('删除失败');
  });
}

//删除部门
function delSection() {
  const names = multipleSelection.value.map(item => item.name);
  for (let name of names) {
    let namefd = new FormData();
    namefd.append('companyName', name)
    axios.post('http://localhost:8080/department/del', namefd)
  }

}

//删除用户
function delUser() {

  const names = multipleSelection.value.map(item => item.name);
  for (let name of names) {
    let namefd = new FormData();
    namefd.append('companyName', name)
    axios.post('http://localhost:8080/user/delUser', namefd)
  }
}

//修改公司
function update() {
  if (!comRef.value) return;

  comRef.value.validate((valid: boolean) => {
    if (valid) {
      let fd = new FormData();
      fd.append('oldname', oldname.value);
      fd.append('id', comId.value);
      fd.append('name', com.value.name);
      fd.append('tel', com.value.tel);
      fd.append('linkman', com.value.linkman);
      fd.append('admin', com.value.admin);
      fd.append('desc', com.value.desc);
      fd.append('mail', com.value.mail);
      fd.append('sign', com.value.sign);
      fd.append('coverUrl', com.value.coverUrl);
      axios.post('http://localhost:8080/company/update', fd).then((res) => {
        if (res.data.isOk) {
          ElMessage.success(res.data.msg);
          // if (oldname.value != com.value.name) {
            updateSection()
            updateUser()
          //   console.log('wqeqew')
          // }
          loadCom();
          back();
        } else {
          ElMessage.error(res.data.msg);
        }
      });

    } else {
      ElMessage.error('请输入完整的信息');
    }
  })
}

//修改部门
function updateSection(){
  // console.log('1111')
  const namefd = new FormData();
  namefd.append('oldName', oldname.value);
  namefd.append('newName', com.value.name);
  axios.post('http://localhost:8080/department/updateName', namefd)
}

//修改用户
function updateUser(){
  if(oldadmin.value==com.value.admin){
    updateName()
  }else{
    updateName()
    updateAdmin()
  }
}

function updateAdmin(){
  const namefd = new FormData();
  namefd.append('oldadmin', oldadmin.value);
  namefd.append('name', com.value.admin);
  axios.post('http://localhost:8080/user/updateAdmin', namefd)
}

function updateName(){
  const namefd = new FormData();
  namefd.append('oldName', oldname.value);
  namefd.append('newName', com.value.name);
  axios.post('http://localhost:8080/user/updateName', namefd)
}




//查询公司
function search() {
  const {name, linkman, tel, sign} = searchParams.value;

  // 检查是否所有搜索条件都为空
  if (!name && !linkman && !tel && !sign) {
    ElMessage.error('请输入搜索条件');
    return;
  }

  axios.post('http://localhost:8080/company/search', searchParams.value).then(res => {
    comp.value = res.data.comps;

  }).catch(err => {
    console.error('搜索失败', err);
  });
}

//导出表格
function exportToExcel() {
  const ws = XLSX.utils.json_to_sheet(comp.value);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Companys');
  XLSX.writeFile(wb, 'companys.xlsx');
}

//导入表格
function importExcel() {
  const fileInput = refFileInput.value;
  if (fileInput) {
    (fileInput as HTMLInputElement).click();
  }
}

function handleFileUpload(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = (e) => {
    const data = e.target?.result;
    const workbook = XLSX.read(data, {type: 'binary'});

    const firstSheetName = workbook.SheetNames[0];
    const worksheet = workbook.Sheets[firstSheetName];
    const jsonData = XLSX.utils.sheet_to_json(worksheet);

    // 处理导入的数据
    processImportedData(jsonData);
  };
  reader.readAsBinaryString(file);
}

function processImportedData(data: any) {
  // 将导入的数据转换为适合的格式并保存到服务器
  data.forEach((item: any) => {
    let fd = new FormData();
    fd.append('name', item.name);
    fd.append('linkman', item.linkman);
    fd.append('tel', item.tel);
    fd.append('admin', item.admin);
    fd.append('sign', item.sign);
    fd.append('mail', item.mail);
    fd.append('state', item.state.toString());
    fd.append('desc', item.desc);

    axios.post('http://localhost:8080/company/add', fd).then((res) => {
      ElMessage.success(`导入公司 ${item.name} 成功`);
      loadCom();
    });
  });
}

//打开增加表单
function openAdd() {
  dialogFormVisible.value = true
}

//打开修改表单
function openForm(row: any) {
  comId.value = row.id
  com.value = Object.assign({}, row);
  oldname.value = com.value.name
  oldadmin.value=com.value.admin
  if (com.value.coverUrl) {
    fileList.value = [{
      name: '公司图标',
      url: com.value.coverUrl,
    }];
  } else {
    fileList.value = [];
  }
  dialogVisible.value = true
}

// 详情
function detail(row: any) {
  dialogImageUrl.value = row.coverUrl
  if(!dialogImageUrl.value){
    ElMessage.error('该公司还没有注册图标哦')
  }else{
    dialogVisible1.value = true
  }

}

//关闭表单
function back() {
  dialogFormVisible.value = false
  dialogVisible.value = false
  com.value = {
    name: '',
    linkman: '',
    tel: '',
    desc: '',
    admin: '',
    sign: '',
    state: 1,
    mail: '',
    coverUrl: ''
  };
  reset1()
}

//重置
function reset() {
  searchParams.value = {
    name: '',
    linkman: '',
    tel: '',
    sign: ''
  }
  loadCom();
}

//钩子
onMounted(() => {
  loadCom();
})

</script>


<style>
.common-layout {
  height: 100%;
  width: 100%;
}

.container {
  display: flex;
  align-items: center;
}

</style>

