<template>
  <div class="common-layout">
    <el-container style="height: 100%; width: 100%">
      <!--        主界面-->
      <el-main style="padding: 0;">
        <div class="container">
          <h5>会议名称</h5>
          <el-input
            style="width: 200px; margin-left: 10px; margin-right: 10px"
            placeholder="请输入会议名称"
            v-model="searchParams.conferName"
            clearable
          >
          </el-input>
          <h5>创建人</h5>
          <el-input
            style="width: 200px; margin-left: 10px; margin-right: 10px"
            placeholder="请输入创建人"
            v-model="searchParams.creater"
            clearable
          >
          </el-input>
          <h5>会议内容</h5>
          <el-input
            style="width: 200px; margin-left: 10px; margin-right: 10px"
            placeholder="请输入会议内容关键字"
            v-model="searchParams.content"
            clearable
          >
          </el-input>
          <h5>开始时间</h5>

          <el-tooltip
            content="提示:列表中会显示开始时间在该时间之后的全部会议"
            placement="bottom"
          >
            <div>
              <el-date-picker
                style="width: 200px; margin-left: 10px; margin-right: 10px"
                v-model="searchParams.stime"
                type="datetime"
                placeholder="请选择会议开始时间"
              />
            </div>
          </el-tooltip>

          <el-button type="primary" @click="search">搜索</el-button>
          <el-button type="primary" @click="reset">重置</el-button>
        </div>
        <div class="mb-4" style="margin-top: 20px; margin-bottom: 20px">
          <el-button type="primary" style="width: 100px" @click="openAdd"
            >新增</el-button
          >
          <el-button type="danger" style="width: 100px" @click="delCom"
            >删除</el-button
          >
          <el-button type="warning" style="width: 100px" @click="handleUpdate"
            >修改</el-button
          >
          <el-button type="info" style="width: 100px" @click="Export"
            >导出</el-button
          >
        </div>

        <!--          列出租户-->
        <el-table
          ref="multipleTableRef"
          :data="pagedConfer"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column
            property="conferName"
            label="会议名称"
            header-align="center"
            align="center"
          />
          <el-table-column
            property="creater"
            label="创建人"
            header-align="center"
            align="center"
          />
          <el-table-column
            property="conferState"
            label="会议状态"
            sortable
            header-align="center"
            align="center"
          />
          <el-table-column
            property="content"
            label="会议内容"
            header-align="center"
            align="center"
          />
          <el-table-column
            property="stime"
            label="开始时间"
            sortable
            header-align="center"
            align="center"
          />
          <el-table-column label="操作" header-align="center" align="center">
            <template #default="scope">
              <el-link
                type="primary"
                style="margin-right: 10px"
                @click="openForm(scope.row)"
                >修改</el-link
              >
              <el-link
                type="danger"
                style="margin-right: 10px"
                @click="openDetailForm(scope.row)"
                >详情</el-link
              >
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="confer.length"
          @current-change="handlePageChange"
          layout="total, prev, pager, next, jumper"
          style="margin-top: 20px; text-align: right"
        />
        <!-- 详情对话框 -->
         
        <el-dialog v-model="dialogVisible1" width="50%" title="会议详情">
          
          <div style="justify-content: space-between; display: flex">
      <span>会议名称：</span>
      <span>{{ con.conferName }}</span>
    </div>
    <el-divider />
    <span>会议封面:</span>
    <div v-if="dialogImageUrl">
            <el-image :src="dialogImageUrl" style="width: 50%; height: auto" />
          </div>
          <div v-else>
            <p>该会议暂无图片</p>
          </div>
    <el-divider />
    <div style="justify-content: space-between; display: flex">
      <span>创建人:</span>
      <span>{{ con.creater }}</span>
    </div>
    <el-divider />
    <div style="justify-content: space-between; display: flex">
      <span>状态:</span>
      <span>{{ con.conferState }}</span>
    </div>
    <el-divider />
    <div style="justify-content: space-between; display: flex">
      <span>内容:</span>
      <span>{{ con.creater }}</span>
    </div>
    <div style="justify-content: space-between; display: flex">
      <span>开始时间:</span>
      <span>{{ con.stime }}</span>
    </div>
    <div style="justify-content: space-between; display: flex">
      <span>结束时间:</span>
      <span>{{ con.etime }}</span>
    </div>
        </el-dialog>
        <!--增加会议-->
        <el-dialog
          title="增加会议"
          v-model="dialogFormVisible"
          width="50%"
          @close="back"
        >
          <el-form label-width="80px" :model="con" :rules="rules" ref="comRef">
            <el-form-item label="会议名称" prop="conferName">
              <el-input
                v-model="con.conferName"
                placeholder="请输入会议名称"
              ></el-input>
            </el-form-item>
            <el-form-item label="创建人" prop="creater">
              <el-input
                v-model="con.creater"
                placeholder="请输入创建人信息"
              ></el-input>
            </el-form-item>
            <el-form-item label="会议状态" prop="conferState">
              <el-input
                v-model="con.conferState"
                disabled
                placeholder="状态根据会议的时间信息自动填写"
              ></el-input>
            </el-form-item>
            <el-form-item label="会议内容" prop="content">
              <el-input
                v-model="con.content"
                placeholder="请输入会议内容"
              ></el-input>
            </el-form-item>
            <el-form-item label="开始时间" prop="stime" required>
              <el-date-picker
                style="width: 100%"
                v-model="con.stime"
                type="datetime"
                placeholder="请选择开始时间"
              />
            </el-form-item>
            <el-form-item label="结束时间" prop="etime" required>
              <el-date-picker
                style="width: 100%"
                v-model="con.etime"
                type="datetime"
                placeholder="请选择结束时间"
              />
            </el-form-item>
            <el-form-item label="会议封面" required>
              <el-upload
                ref="uploadImageRef"
                action="http://localhost:8080/upload/image"
                list-type="picture-card"
                :before-upload="beforeUploadImage"
                :on-preview="handlePictureCardPreview"
                :on-remove="handleRemove"
                :limit="1"
                :on-success="UploadImage"
                :file-list="fileList"
              >
                <el-icon :size="30">
                  <Plus />
                </el-icon>
                <template #tip>
                  <div style="font-size: 12px; color: #919191">
                    <span>请上传大小不超过</span>
                    <span style="color: red">5MB</span>
                    <span>格式为</span>
                    <span style="color: red">png/jpg/jpeg</span>
                    <span>的图片</span>
                  </div>
                </template>
              </el-upload>
              <el-dialog v-model="dialogVisible1" style="line-height: 0">
                <img
                  style="width: 100%; height: 100%"
                  :src="dialogImageUrl"
                  alt=""
                />
              </el-dialog>
            </el-form-item>
            <!-- 上传图片 -->

            <el-form-item>
              <el-button
                type="primary"
                @click="addCom()"
                style="margin-left: 250px"
                >提交</el-button
              >
              <el-button @click="back()">返回</el-button>
            </el-form-item>
          </el-form>
        </el-dialog>

        <!--          修改会议-->
        <el-dialog
          title="修改会议"
          v-model="dialogVisible"
          width="40%"
          :rules="rules"
          @close="back"
        >
          <el-form ref="comRef" :model="con" label-width="80px" :rules="rules">
            <el-form-item label="会议名称" prop="conferName">
              <el-input v-model="con.conferName"></el-input>
            </el-form-item>
            <el-form-item label="会议封面">
              <el-upload
                ref="uploadImageRef"
                action="http://localhost:8080/upload/image"
                list-type="picture-card"
                :before-upload="beforeUploadImage"
                :on-preview="handlePictureCardPreview"
                :on-remove="handleRemove"
                :limit="1"
                :on-success="UploadImage"
                :file-list="fileList"
              >
                <el-icon :size="30">
                  <Plus />
                </el-icon>
                <template #tip>
                  <div style="font-size: 12px; color: #919191">
                    <span>请上传大小不超过</span>
                    <span style="color: red">5MB</span>
                    <span>格式为</span>
                    <span style="color: red">png/jpg/jpeg</span>
                    <span>的图片</span>
                  </div>
                </template>
              </el-upload>
              <el-dialog v-model="dialogVisible1" style="line-height: 0">
                <img
                  style="width: 100%; height: 100%"
                  :src="dialogImageUrl"
                  alt=""
                />
              </el-dialog>
            </el-form-item>
            <el-form-item label="创建人" prop="creater">
              <el-input v-model="con.creater"></el-input>
            </el-form-item>
            <el-form-item label="会议状态" prop="conferState" required>
              <el-input v-model="con.conferState" disabled></el-input>
            </el-form-item>
            <el-form-item label="会议内容" prop="content">
              <el-input v-model="con.content"></el-input>
            </el-form-item>

            <el-form-item label="开始时间" prop="stime" required>
              <el-date-picker
                style="width: 100%"
                v-model="con.stime"
                type="datetime"
                placeholder="请选择开始时间"
              />
            </el-form-item>
            <el-form-item label="结束时间" prop="etime" required>
              <el-date-picker
                style="width: 100%"
                v-model="con.etime"
                type="datetime"
                placeholder="请选择结束时间"
              />
            </el-form-item>
            <!-- <el-form-item label="描述" prop="picture">
                <el-input v-model="con.picture"></el-input>
              </el-form-item> -->
          </el-form>
          <template v-slot:footer>
            <span class="dialog-footer">
              <el-button type="primary" @click="update">确 定</el-button>
              <el-button type="info" @click="back">返 回</el-button>
            </span>
          </template>
        </el-dialog>
      </el-main>
    </el-container>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from "vue";
import {
  ElMessage,
  ElTable,
  ElUpload,
  type UploadFile,
  ElTooltip,
  type FormInstance,
} from "element-plus";
import axios from "axios"; // 引入 axios 实例
import * as XLSX from "xlsx";
import moment from "moment";
import router from "@/router";
interface confer {
  conferName: string;
  creater: string;
  conferState: string;
  picture: string;
  content: string;
  stime: string;
  etime: string;
}
const searchParams = ref({
  conferName: "",
  creater: "",
  content: "",
  stime: "",
});
const con = ref({
  conferName: "",
  creater: "",
  conferState: "",
  picture: "",
  content: "",
  stime: "",
  etime: "",
});
const validateTime = (
  rule: any,
  value: any,
  callback: (error?: Error) => void
) => {
  if (con.value.stime && con.value.etime) {
    if (moment(con.value.stime).isAfter(moment(con.value.etime))) {
      callback(new Error("开始时间不能晚于结束时间"));
    } else {
      callback();
    }
  } else {
    callback(new Error("请输入开始和结束时间"));
  }
};
const rules = ref({
  conferName: [
    {
      required: true,
      message: "会议名不能为空",
      trigger: "blur",
    },
    {
      max: 200,
      message: "会议名不能超过200个字符",
      trigger: "blur",
    },
  ],
  creater: [
    {
      required: true,
      message: "创建人不能为空",
      trigger: "blur",
    },
    {
      max: 200,
      message: "创建人不能超过200个字符",
      trigger: "blur",
    },
  ],
  content: [
    {
      required: true,
      message: "内容不能为空",
      trigger: "blur",
    },
    {
      max: 200,
      message: "内容不能超过200个字符",
      trigger: "blur",
    },
  ],
  stime: [
    {
      validator: validateTime,
      trigger: "blur",
    },
  ],
  etime: [
    {
      validator: validateTime,
      trigger: "blur",
    },
  ],
  picture: [
    {
      required: true,
      message: "封面不能为空",
      trigger: "blur",
    },
  ],
});
const oldName = ref("");
const dialogVisible = ref(false);
const dialogFormVisible = ref(false);
const multipleTableRef = ref<InstanceType<typeof ElTable>>();
const multipleSelectionGroup = ref<Array<confer>>([]);
const multipleSelection = ref<String[]>([]);
const comRef = ref<FormInstance | null>(null);
const confer = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const data = ref([]);
const storedUserRole = sessionStorage.getItem("role"); //这是登录的人的身份，管理员为1，租户为2
const storedUserName = sessionStorage.getItem("username"); //这是登录的人的名字

const pagedConfer = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return confer.value.slice(start, end);
});

const handlePageChange = (page: number) => {
  currentPage.value = page;
};

const handleSelectionChange = (val: Array<confer>) => {
  // 使用 map 方法提取 conferName
  multipleSelectionGroup.value = val;
  const selectedConferNames = val.map((item) => item.conferName);
  // 更新 multipleSelection 为包含所有选中的 conferName
  multipleSelection.value = selectedConferNames;
};
const handleUpdate = () => {
  if (multipleSelectionGroup.value.length === 1) {
    con.value = Object.assign({}, multipleSelectionGroup.value[0]);
    if (storedUserRole == "2") {
      const allMatch = con.value.creater === storedUserName;
      if (!allMatch) {
        ElMessage.error("您不是该项目的的创建者，无法修改！");
        return;
      }
    }
    oldName.value = con.value.conferName;
    dialogVisible.value = true;
  } else if (multipleSelection.value.length === 0) {
    ElMessage.error("请选择一个会议进行修改");
  } else {
    ElMessage.error("只能选择一个会议进行修改");
  }
};
const handleOpen = (key: string, keyPath: string[]) => {
  console.log(key, keyPath);
};
const handleClose = (key: string, keyPath: string[]) => {
  console.log(key, keyPath);
};


function loadCom() {
  axios.get("http://localhost:8080/confer/list").then((response) => {
    if (response.data.isOk) {
      confer.value = response.data.confers;
      data.value = pagedConfer.value;
    } else {
      ElMessage.error(response.data.msg);
    }
  });
}

function addCom() {
  if (comRef.value == null) {
    alert("没读到值");
  } else {
    if (
      !con.value.stime ||
      !con.value.etime ||
      !con.value.picture ||
      !con.value.conferName ||
      !con.value.content ||
      !con.value.creater
    ) {
      ElMessage.error(con.value.picture);
      ElMessage.error("信息填写不完整！");
      return;
    }
    if (storedUserRole == "2") {
      if (con.value.creater != storedUserName){  ElMessage.error("创建者必须为自己！");
      return;}     
    }
    con.value.stime = moment(con.value.stime).format("YYYY-MM-DD HH:mm:ss");
    con.value.etime = moment(con.value.etime).format("YYYY-MM-DD HH:mm:ss");
    comRef.value.validate((valid: boolean) => {
      if (valid) {
        axios
          .post("http://localhost:8080/confer/add", {
            conferName: con.value.conferName,
            conferState: con.value.conferState,
            creater: con.value.creater,
            content: con.value.content,
            picture: con.value.picture,
            stime: con.value.stime,
            etime: con.value.etime,
          })
          .then((res) => {
            if (res.data.isOk) {
              ElMessage.success(res.data.msg);
              loadCom();
              back();
            } else {
              ElMessage.error("添加失败，请检查该会议是否已存在！");
            }
          });
      } else {
        ElMessage.error("请输入正确的信息");
      }
    });
  }
}

function delCom() {
  if (multipleSelection.value.length === 0) {
    ElMessage.error("当前无会议被选中，删除失败！");
    return;
  }
  if (storedUserRole == "2") {
    const allMatch = multipleSelectionGroup.value.every(     
      (item) => item.creater === storedUserName
    );
    if (!allMatch) {ElMessage.error("您不是该会议的的创建者，无法删除！");
    return;}
  }
  const conferNamesToDeleteStr = multipleSelection.value.join(", ");
  if (!confirm(`是否删除以下会议:${conferNamesToDeleteStr}`)) {
    return;
  }
  const conferNamesToDelete = multipleSelection.value;
  axios
    .post("http://localhost:8080/confer/del", conferNamesToDelete, {
      headers: {
        "Content-Type": "application/json",
      },
    })
    .then((res) => {
      ElMessage.success(res.data.msg);
      loadCom();
    })
    .catch((err) => {
      ElMessage.error("删除失败");
    });
}

function update() {
  if (
    !con.value.stime ||
    !con.value.etime ||
    !con.value.picture ||
    !con.value.conferName ||
    !con.value.content ||
    !con.value.creater
  ) {
    ElMessage.error("信息填写不完整！");
    return;
  }
  if (storedUserRole == "2") {
      if (con.value.creater != storedUserName){  ElMessage.error("创建者必须为自己！");
      return;}
      
    }
  con.value.stime = moment(con.value.stime).format("YYYY-MM-DD HH:mm:ss");
  con.value.etime = moment(con.value.etime).format("YYYY-MM-DD HH:mm:ss");
  console.log(con.value.etime);
  comRef.value?.validate((valid: boolean) => {
    if (valid) {
      axios
        .post("http://localhost:8080/confer/update", {
          conferName: con.value.conferName,
          oldName: oldName.value,
          conferState: con.value.conferState,
          creater: con.value.creater,
          content: con.value.content,
          picture: con.value.picture,
          stime: con.value.stime,
          etime: con.value.etime,
        })
        .then((res) => {
          if (res.data.isOk) {
            ElMessage.success(res.data.msg);
            loadCom();
            back();
          } else {
            ElMessage.error("修改失败，该会议已存在！");
          }
        });
    } else {
      ElMessage.error("请输入正确的信息");
    }
  });
}

function search() {
  const { conferName, creater, content, stime } = searchParams.value;
  // 检查是否所有搜索条件都为空
  if (!conferName && !creater && !content && !stime) {
    ElMessage.error("请输入搜索条件");
    return;
  }
  axios
    .post("http://localhost:8080/confer/search", searchParams.value)
    .then((res) => {
      confer.value = res.data.confers;
      data.value = pagedConfer.value;
    })
    .catch((err) => {
      console.error("搜索失败", err);
    });
}

function openAdd() {
  dialogFormVisible.value = true;
}

function openForm(row: any) {
  con.value = Object.assign({}, row);
  if (storedUserRole == "2") {
    const allMatch = con.value.creater === storedUserName;
    if (!allMatch) {
      ElMessage.error("您不是该项目的的创建者，无法修改！");
      return;
    }
  }
  if (con.value.picture) {
    fileList.value = [{
      name: '会议图标',
      url: con.value.picture,
    }];
  } else {
    fileList.value = [];
  }
  oldName.value = con.value.conferName;
  dialogVisible.value = true;
}

function openDetailForm(row: any) {
  con.value = Object.assign({}, row);
  if (con.value.picture != null) {
    console.log(con.value.picture)
    dialogImageUrl.value = con.value.picture;
  } else {
    dialogImageUrl.value = "";
  }
  dialogVisible1.value = true;
}

const clearUpload = () => {
  fileList.value = [];
  dialogImageUrl.value = "";
};

function back() {
  comRef.value?.resetFields();
  dialogFormVisible.value = false;
  dialogVisible.value = false;
  clearUpload();
  reset1();
  con.value = {
    conferName: "",
    creater: "",
    conferState: "",
    picture: "",
    content: "",
    stime: "",
    etime: "",
  };
}

function reset() {
  searchParams.value = {
    conferName: "",
    creater: "",
    content: "",
    stime: "",
  };
  loadCom();
}

//图片
//上传图片
const fileList = ref<any[]>([]);
const dialogImageUrl = ref("");
const dialogVisible1 = ref(false);
function beforeUploadImage(file: any) {
  console.log("imageBefore");
  //文件大小
  const isLt5M = file.size / 1024 / 1024 < 5;
  //视频后缀检查
  if (["image/jpeg", "image/png", "image/jpg"].indexOf(file.type) === -1) {
    ElMessage.error("请上传正确的图片格式");
    return false;
  }
  if (!isLt5M) {
    ElMessage.error("上传图片大小不能超过5MB哦!");
    return false;
  }
}

const UploadImage = (res: any, file: any) => {
  if (res.resCode === "200") {
    con.value.picture = res.ImageUrl;
    ElMessage.success("图片上传成功！");
  } else {
    ElMessage.error("图片上传失败，请重新上传！");
  }
};

const handleRemove = (file: any, fileList: any) => {
  console.log(file, fileList);
};

const handlePictureCardPreview = (file: UploadFile) => {
  console.log(file.url);
  dialogVisible1.value = true;
  dialogImageUrl.value = file.url!;
  alert("1" + file.url);
};
const uploadImageRef = ref<InstanceType<typeof ElUpload> | null>(null);

function reset1() {
  uploadImageRef.value!.clearFiles(); //清除文件列表
}

function Export() {
  if (multipleSelectionGroup.value.length === 0) {
    ElMessage.error("当前无会议被选中，请选择后再导出！");
    return;
  }
  const dataE = XLSX.utils.json_to_sheet(multipleSelectionGroup.value); //此处tableData.value为表格的数据
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, dataE, "test-data"); //test-data为自定义的sheet表名
  XLSX.writeFile(wb, "test.xlsx"); //test.xlsx为自定义的文件名
}

onMounted(() => {
  const storedUserRole = sessionStorage.getItem("role");
  if (storedUserRole == null) {
    ElMessage.error('尚未登录，请先登录');
    router.push('/');
  }
  loadCom();
});
</script>


<style scoped>
.container {
  display: flex;
  align-items: center;
}

.common-layout {
  height: 100%;
}

h5 {
  margin: 7.1px 0;
}
</style>

