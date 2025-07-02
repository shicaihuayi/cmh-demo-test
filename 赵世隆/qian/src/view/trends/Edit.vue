<template>
  <router-link to="/home/trend">
    <el-button type="info" plain style="margin-bottom: 30px;" round>
      <el-icon>
        <Back />
      </el-icon> 返回</el-button>
  </router-link>
  <div v-show="!disabledForm" style="float: right;">
    <el-button type="warning" @click="resetForm">清空</el-button>
    <el-button type="primary" @click="saveForm">保存</el-button>
  </div>
  <div class="content_area">
    <el-form ref="formRef" :model="formData" label-width="80px" :rules="formRules" :disabled="disabledForm">
      <el-form-item label="文章图片" prop="fileUrl">
        <el-upload ref="uploadRef" accept="image/jpg,image/png,iamge/jpeg"
          :class="{ 'upload_img': fileList.length != 0 }" v-model:file-list="fileList" action="http://localhost:8080/file/upload"
          list-type="picture-card" :limit="1" :auto-upload="true" :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove" :on-success="onFileUploadSuc">
          <el-icon>
            <Plus />
          </el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入文章标题"></el-input>
      </el-form-item>
      <el-form-item label="关键字" prop="keywords">
        <el-input v-model="formData.keywords" placeholder="以英文逗号,分隔"></el-input>
      </el-form-item>
      <el-form-item label="时间" prop="createTime">
        <el-date-picker v-model="formData.createTime" :disabled-date="(time) => {
          // 禁用今天以后的日期
          const today = new Date();
          const selectedDate = new Date(time);
          return selectedDate > today;

        }" :default-value="formData.createTime" type="datetime" placeholder="选择日期和时间" format="YYYY/MM/DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"></el-date-picker>
      </el-form-item>
      <el-form-item label="简介" prop="description">
        <el-input v-model="formData.description" type="textarea" placeholder="请输入文章简介"></el-input>
      </el-form-item>
      <el-form-item label="正文" prop="content">
        <el-input v-model="formData.content" type="textarea" placeholder="请输入文章正文"></el-input>
      </el-form-item>
    </el-form>
  </div>
  <el-dialog v-model="dialogVisible">
    <img w-full :src="dialogImageUrl" alt="Preview Image" class="preview_img" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onBeforeMount } from 'vue';
import { ElMessage } from 'element-plus';
import { getNewsDetail, updateNews } from '@/api/news'
import { useRoute, useRouter } from 'vue-router';
import {Back, Plus} from "@element-plus/icons-vue";
const disabledForm = ref(true)
const route = useRoute()
const router = useRouter()
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const disabled = ref(false)
const uploadRef = ref()
const fileList: any = ref([])
const onFileUploadSuc: any = (res: any, file: any, files: any) => {
  fileList.value = [{ url: res.fileUrl }]
  formData.value.fileUrl = res.fileUrl
  console.log('=====onFileUploadSuc=====');
  console.log(res, file, files);
  console.log('===========');
}
const handleRemove = (file: any, fileList: any) => {
  formData.value.fileUrl = ''
  fileList.value = []
  console.log('删除图片:', file, fileList);
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
  createTime: '',
  fileUrl: '',
  description: '',
  content: '',
});
let formRules = ref(
  {
    fileUrl: [{ required: true, trigger: "change", message: "请选择图片" }],
    keywords: [{ required: true, trigger: "blur", message: "请输入关键词" }],
    title: [{ required: true, trigger: "blur", message: "请输入标题" }],
    date: [{ required: true, trigger: "change", message: "请选择日期" }],
    description: [{ required: true, trigger: "change", message: "请输入简介" }],
    content: [{ required: true, trigger: "change", message: "请输入文章正文" }],
  })
const resetForm = () => {
  fileList.value = []
  formRef.value.resetFields();
};

const saveForm = () => {
  console.log('保存表单数据:', formData.value);
  formRef.value.validate((valid: any) => {
    console.log(valid);
    if (valid) {
      updateNews(formData.value).then((result:any) => {
        console.log(result);
        ElMessage.success(result.msg);
      }).catch((err) => {
        console.error(err);
      });
    }
  })
};

const fetchArticle = async () => {
  try {
    const response = await getNewsDetail(route.params.id)
    formData.value = response.data
    fileList.value = [{ url: formData.value.fileUrl }]
  } catch (error) {
    console.error('Failed to fetch article:', error)
  }
}
onBeforeMount(() => {
  console.log(route);
  if (sessionStorage.getItem("canEdit") === 'true') {
    disabledForm.value = false
  }
  if (route.params.id) {
    formData.value.id = route.params.id
    fetchArticle()
  }
})

</script>

<style scoped lang="less">
.el-upload {
  display: block;
  margin-bottom: 10px;
}

.content_area {
  height: 80vh;
  overflow-y: scroll
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
</style>


