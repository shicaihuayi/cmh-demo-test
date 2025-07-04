<template>
  <router-link to="/home/trend">
    <el-button type="info" plain style="margin-bottom: 30px;" round>
      <el-icon>
        <Back />
      </el-icon> 返回</el-button>
  </router-link>
  <div style="float: right;">
    <el-button type="warning" @click="resetForm">清空</el-button>
    <el-button type="primary" @click="saveForm">保存</el-button>
  </div>
  <div class="content_area">

    <el-form ref="formRef" :model="formData" label-width="80px" :rules="formRules">
      <el-form-item label="动态图片" prop="fileUrl">
        <el-upload ref="uploadRef" accept="image/jpg,image/png,iamge/jpeg"
          :class="{ 'upload_img': fileList.length != 0 }" v-model:file-list="fileList" action="/api/file/upload"
          list-type="picture-card" :limit="1" :auto-upload="true" :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove" :on-success="onFileUploadSuc">
          <el-icon>
            <Plus />
          </el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入动态标题"></el-input>
      </el-form-item>
      <el-form-item label="关键字" prop="keywords">
        <el-input v-model="formData.keywords" :formatter="(value: string) => value.replace(' ', ',')"
          :parser="(value: string) => value.replace(' ', ',')" placeholder="输入一个关键字后，按空格自动分隔"></el-input>
      </el-form-item>
      <el-form-item label="时间" prop="date">
        <el-date-picker v-model="formData.date" type="datetime" placeholder="选择日期和时间" format="YYYY/MM/DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"></el-date-picker>
      </el-form-item>
      <el-form-item label="简介" prop="description">
        <el-input class="desc" v-model="formData.description" type="textarea" placeholder="请输入动态简介"></el-input>
      </el-form-item>
      <el-form-item label="作者" prop="authorName">
        <el-input v-model="formData.authorName" placeholder="请输入作者名称"></el-input>
      </el-form-item>
      <el-form-item label="正文" prop="content">
        <el-input v-model="formData.content" type="textarea" placeholder="请输入动态正文"></el-input>
      </el-form-item>
    </el-form>
  </div>
  <el-dialog v-model="dialogVisible">
    <img w-full :src="dialogImageUrl" alt="Preview Image" class="preview_img" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { addNews, updateNews } from '@/api/news'
import { useRouter } from 'vue-router';

const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const uploadRef = ref()
const fileList:any = ref([])
const router = useRouter()
const onFileUploadSuc: any = (res:any,file:any, files:any) => {
  fileList.value = [{url:res.fileUrl}]
  formData.value.fileUrl = res.fileUrl
  console.log('=====onFileUploadSuc=====');
  console.log(res,file, files);
  console.log('===========');
}
const handleRemove = (file: any, fileLists: any) => {
  console.log('删除图片:', file, fileLists);
  fileList.value = fileLists
}

const handlePictureCardPreview = (file: any) => {
  console.log('预览图片:', file);
  dialogImageUrl.value = file.url!
  dialogVisible.value = true
}

const formRef: any = ref();
let formData: any = ref({
  title: '',
  keywords: '',
  date: '',
  fileUrl: '',
  description: '',
  content: '',
  authorName: '',
});

// 在组件挂载时设置作者名称 - 注释掉此逻辑，改为手动输入
// onMounted(() => {
//   const username = sessionStorage.getItem('username');
//   if (username) {
//     formData.value.authorName = username;
//   }
// });

let formRules = ref(
  {
    fileUrl: [{ required: true, trigger: "blur", message: "请选择图片" }],
    keywords: [{ required: true, trigger: "blur", message: "请输入关键词" }],
    title: [{ required: true, trigger: "blur", message: "请输入标题" }],
    date: [{ required: true, trigger: "change", message: "请选择日期" }],
    description: [{ required: true, trigger: "change", message: "请输入简介" }],
    content: [{ required: true, trigger: "change", message: "请输入文章正文" }],
    authorName: [{ required: true, trigger: "blur", message: "请输入作者名称" }],
  });

const resetForm = () => {
  fileList.value = []
  formRef.value.resetFields();
};

const saveForm = () => {
  console.log('保存表单数据:', formData.value);

  formRef.value.validate((valid: any) => {
    if (!valid) return;

    const currentUserId = sessionStorage.getItem('id');
    const currentUserRole = sessionStorage.getItem('role');

    // 根据新的需求：所有管理员创建的动态都为待审核状态
    const status = 'PENDING';

    console.log('=== 创建动态状态设置调试 ===');
    console.log('当前用户ID:', currentUserId);
    console.log('当前用户角色:', currentUserRole);
    console.log('设置的状态:', status);
    console.log('新规则：所有管理员创建的动态都为待审核状态');
    console.log('===============================');

    // 直接使用表单中的数据，确保包含必要的用户信息
    const dataToSave = {
      ...formData.value,
      authorId: currentUserId,
      status: status,
      createTime: formData.value.date // 确保时间字段正确映射
    };

    console.log('准备保存的数据:', dataToSave);

    addNews(dataToSave).then((result:any) => {
      console.log('保存结果:', result);
      ElMessage.success(result.msg || '动态已创建，状态为待审核');
      router.push('/home/trend');
    }).catch((err) => {
      console.error('保存失败:', err);
      ElMessage.error('保存失败：' + (err.message || '未知错误'));
    });
  });
};

</script>

<style scoped lang="less">
.content_area {
  height: 80vh;
  overflow-y: scroll
}

.el-upload {
  display: block;
  margin-bottom: 10px;
}

.preview_img {
  width: 100% !important;
}

::v-deep(.upload_img) {
  .el-upload--picture-card {
    display: none;
  }
}

::v-deep(.el-textarea) {
  textarea {
    height: 200px !important;
  }
}
   ::v-deep(.desc){
    textarea{
      height: 150px !important;
    }
  }

</style>
