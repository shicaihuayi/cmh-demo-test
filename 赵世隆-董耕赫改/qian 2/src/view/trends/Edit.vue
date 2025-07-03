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
        <el-input v-model="formData.keywords" placeholder="以英文逗号,分隔"></el-input>
      </el-form-item>
      <el-form-item label="时间" prop="createTime">
        <el-date-picker 
          v-model="formData.createTime" 
          :disabled-date="(time: Date) => {
            // 禁用今天以后的日期
            const today = new Date();
            const selectedDate = new Date(time);
            return selectedDate > today;
          }" 
          type="datetime" 
          placeholder="选择日期和时间" 
          format="YYYY/MM/DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="简介" prop="description">
        <el-input v-model="formData.description" type="textarea" placeholder="请输入动态简介"></el-input>
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
import { ref, onBeforeMount } from 'vue';
import { ElMessage } from 'element-plus';
import { getNewsDetail, updateNews, adminUpdateNews } from '@/api/news'
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
      // 确保包含必要的用户信息和ID
      const currentUserId = sessionStorage.getItem('id');
      const currentUserRole = sessionStorage.getItem('role');
      
      const updateData = {
        ...formData.value,
        authorId: formData.value.authorId, // 保持原作者ID不变
        authorName: formData.value.authorName, // 保持原作者名称不变
        operatorId: currentUserId, // 操作者ID
        operatorRole: currentUserRole, // 操作者角色
        isAdmin: currentUserRole === '3' || currentUserRole === '1', // 明确标识是否为管理员
        permission: 'ADMIN_EDIT' // 权限标识
      };
      console.log('=== 编辑权限调试信息 ===');
      console.log('发送更新数据:', updateData);
      console.log('当前用户角色:', currentUserRole, '当前用户ID:', currentUserId);
      console.log('原文章作者ID:', formData.value.authorId);
      console.log('原文章作者名:', formData.value.authorName);
      console.log('是否为管理员:', currentUserRole === '3' || currentUserRole === '1');
      console.log('========================');
      
      // 根据用户角色选择使用不同的API
      const isAdmin = currentUserRole === '3' || currentUserRole === '1';
      const updateFunction = isAdmin && formData.value.authorId !== currentUserId 
        ? adminUpdateNews  // 管理员编辑他人文章
        : updateNews;      // 普通编辑或编辑自己的文章
      
      console.log('使用的更新函数:', isAdmin && formData.value.authorId !== currentUserId ? 'adminUpdateNews' : 'updateNews');
      
      updateFunction(updateData).then((result: any) => {
        console.log(result);
        ElMessage.success(result.msg);
        router.push('/home/trend');
      }).catch(async (err: any) => {
        console.error('管理员接口失败，尝试普通接口...', err);
        try {
          const result = await updateNews(formData.value);
          console.log('普通接口更新成功:', result);
          ElMessage.success(result.msg || '修改成功');
          router.push('/home/trend');
        } catch (finalErr: any) {
          console.error('所有更新尝试均失败:', finalErr);
          const errorMessage = (finalErr as any)?.response?.data?.msg || (finalErr as any)?.message || '修改失败，请稍后重试';
          ElMessage.error(errorMessage);
        }
      });
    }
  })
};

const fetchArticle = async () => {
  try {
    console.log('=== 获取文章详情 ===');
    console.log('文章ID:', route.params.id);
    console.log('用户角色:', sessionStorage.getItem('role'));
    console.log('用户ID:', sessionStorage.getItem('id'));
    console.log('==================');
    
    const response = await getNewsDetail(route.params.id)
    
    // 处理日期字段，确保格式正确
    const articleData = response.data;
    if (articleData.createTime) {
      // 如果createTime是字符串且为空或无效，设置为null以避免日期选择器错误
      if (typeof articleData.createTime === 'string' && articleData.createTime.trim() === '') {
        articleData.createTime = null;
      } else if (articleData.createTime) {
        // 确保日期格式正确
        articleData.createTime = new Date(articleData.createTime).toISOString().slice(0, 19).replace('T', ' ');
      }
    }
    
    formData.value = articleData;
    fileList.value = [{ url: formData.value.fileUrl }]
    
    console.log('获取文章成功:', response.data);
  } catch (error) {
    console.error('获取文章详情失败:', error);
    ElMessage.error('无法获取文章详情，可能是权限不足或文章不存在');
    
    // 如果获取文章失败，返回列表页
    setTimeout(() => {
      router.push('/home/trend');
    }, 2000);
  }
}
onBeforeMount(() => {
  console.log(route);
  console.log('=== 页面加载用户信息 ===');
  console.log('用户ID:', sessionStorage.getItem('id'));
  console.log('用户名:', sessionStorage.getItem('username'));
  console.log('用户角色:', sessionStorage.getItem('role'));
  console.log('canEdit状态:', sessionStorage.getItem("canEdit"));
  console.log('========================');
  
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
