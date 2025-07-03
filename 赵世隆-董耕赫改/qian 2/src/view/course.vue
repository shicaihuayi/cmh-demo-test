<template>
  <div class="container" v-if="!isLookPublish">
    <!-- 权限说明：超级管理员可以看到所有课程，普通管理员只能看到自己创建的课程 -->
    <h5>课程名称</h5>
    <el-input style="width: 250px;margin-left:10px;margin-right:10px" placeholder="请输入课程名称" v-model="searchCourse.name"
      clearable>
    </el-input>
    <h5>课程排序</h5>
    <el-input style="width: 250px;margin-left:10px;margin-right:10px" placeholder="请输入课程排序"
      v-model="searchCourse.courseOrder" clearable>
    </el-input>
    <el-button type="primary" @click="search()">搜索</el-button>
    <el-button @click="reset()">重置</el-button>
  </div>
  <div class="mb-1" style=" margin-top: 20px;margin-bottom:20px">
    <!-- 权限控制：所有角色都可以新增课程，但普通管理员创建的课程需要超级管理员审核 -->
    <el-button type="primary" style="width: 100px" @click="openAdd()" v-if="!isLookPublish">新增</el-button>
    <el-button type="danger" style="width: 100px" @click="delCourse()" v-if="!isLookPublish">删除</el-button>
    <el-button type="success" :disabled="isDisabled" style="width: 100px" @click="modify()" v-if="!isLookPublish">修改</el-button>
    <el-button type="warning" @click="exportData()" style="width: 100px">导出</el-button>
    <!-- 发布按钮：普通管理员提交课程给超级管理员审核 -->
    <el-button type="info" @click="publish()" style="width: 100px" v-if="!isAdmin">发布</el-button>
    <!-- 审核管理：只有超级管理员可以查看和处理审核列表 -->
    <el-button type="info" @click="lookPublish()" style="width: 100px"
      v-if="isAdmin && !isLookPublish">查看审核列表</el-button>
    <el-button type="info" @click="backToList()" style="width: 100px" v-if="isAdmin && isLookPublish">返回课程列表</el-button>
  </div>
  <!-- 列出课程-->
  <el-table ref="multipleTableRef" v-if="!isLookPublish" :data="pagedCourse" style="width: 100%" :cell-style="cellStyle"
    @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="50px" header-align="center"
 align="center" />
    <el-table-column type="index" width="150px" label="编号" header-align="center"
 align="center" />
    <el-table-column property="name" width="180px" label="课程名称" header-align="center"
align="center" />
    <el-table-column property="author" width="200px" label="课程作者" header-align="center"
 align="center" />
    <el-table-column property="introduction" width="230px" label="课程简介" header-align="center"
 align="center"  />
    <el-table-column property="courseOrder" width="120px" label="课程排序" header-align="center"
 align="center"  />
    <el-table-column property="pass"  width="170px" label="审核状态" v-if="!isAdmin" header-align="center"
 align="center"  >
      <template #default="scope">
        <div style="display: flex; align-items: center; justify-content: center;">
          <span v-if="scope.row.pass === '通过'" style="color: #67c23a; font-weight: bold;">
            ✓ 审核通过
          </span>
          <span v-else-if="scope.row.pass === '不通过'" style="color: #f56c6c; font-weight: bold;">
            ✗ 审核不通过
          </span>
          <span v-else-if="scope.row.pass === '审核中'" style="color: #e6a23c; font-weight: bold;">
            ⏳ 审核中
          </span>
          <span v-else style="color: #909399; font-weight: bold;">
            ⏸ 待审核
          </span>
        </div>
      </template>
    </el-table-column>
    
    <el-table-column label="操作" header-align="center"
align="center" >
      <template #default="scope">
        <div class="link-container">
          <el-link type="primary" @click="openForm(scope.row)" style="margin-right: 15px;">修改</el-link>
          <el-link @click="lookDetail(scope.row)">详情</el-link>
        </div>
      </template>
    </el-table-column>
  </el-table>
  <!-- 查看审核列表 -->
  <el-table ref="multipleTableRef" v-if="isLookPublish" :data="courses2" style="width: 100%">
    <el-table-column type="index" width="200px" label="编号"  header-align="center"
align="center" />
    <el-table-column property="name" width="250px" label="课程名称" header-align="center"
 align="center" />
    <el-table-column property="author" width="250px" label="课程作者" header-align="center"
 align="center" />
    <el-table-column property="introduction" width="250px" label="课程简介" header-align="center"
 align="center"  />
    <el-table-column label="操作" header-align="center"
align="center" >
      <template #default="scope">
        <div class="link-container">
          <el-link type="primary" @click="passes(scope.row)" style="margin-right: 15px;">审核通过</el-link>
          <el-link type="danger" @click="notPass(scope.row)">审核不通过</el-link>
        </div>
      </template>
    </el-table-column>
  </el-table>
  <!-- 查看详情 -->
  <el-dialog v-model="detailVisiable" style="width:40%;">
    <template #header>
      <div class="card-header" style="justify-content: center;">
        <h3>课程资料</h3>
      </div>
    </template>
    <!-- 课程名称 -->
    <div style="justify-content: space-between; display: flex">
      <span>课程名称</span>
      <span>{{ course2.name }}</span>
    </div>
    <el-divider />
    <div style="justify-content: space-between; display: flex">
      <span style="display: block">课程封面</span>
      <el-image style="width: 150px; height: 150px" :src="course2.coverUrl" :preview-src-list="[course2.coverUrl]"
        fit="contain" />
    </div>
    <el-divider />
    <div style="justify-content: space-between; display: flex">
      <span>课程排序</span>
      <span>{{ course2.courseOrder }}</span>
    </div>
    <el-divider />
    <div style="justify-content: space-between; display: flex">
      <span>课程作者</span>
      <span>{{ course2.author }}</span>
    </div>
    <el-divider />
    <div style="justify-content: space-between; display: flex">
      <span>课程简介</span>
      <span>{{ course2.introduction }}</span>
    </div>
    <el-divider />
    <div style="justify-content: space-between; display: flex; align-items: flex-start;">
      <span>课程视频</span>
      <div style="position: relative; width: 200px; min-height: 150px;">
        <div v-if="course2.videoUrl" style="width: 100%;">
          <div style="margin-bottom: 8px; font-size: 12px; color: #606266; word-break: break-all;">
            <strong>视频URL:</strong> {{ course2.videoUrl }}
          </div>
          <video 
            :src="course2.videoUrl" 
            width="180px" 
            height="135px" 
            controls 
            preload="metadata"
            style="border-radius: 4px; border: 1px solid #dcdfe6;"
            @error="handleVideoError"
            @loadstart="handleVideoLoadStart"
            @canplay="handleVideoCanPlay"
            @loadedmetadata="handleVideoLoadedMetadata"
          >
            您的浏览器不支持视频播放
          </video>
          <div style="font-size: 12px; color: #909399; margin-top: 5px; text-align: center;">
            {{ course2.videoUrl.split('/').pop() || '课程视频' }}
          </div>
          <div style="margin-top: 8px;">
            <el-button 
              type="text" 
              size="small" 
              @click="testVideoUrl(course2.videoUrl)"
              style="color: #409eff;"
            >
              测试视频链接
            </el-button>
            <el-button 
              type="text" 
              size="small" 
              @click="openVideoInNewTab(course2.videoUrl)"
              style="color: #67c23a; margin-left: 8px;"
            >
              新窗口打开
            </el-button>
          </div>
        </div>
        <div v-else style="
          width: 180px; 
          height: 135px; 
          display: flex; 
          align-items: center; 
          justify-content: center; 
          background-color: #f5f7fa; 
          border: 1px dashed #dcdfe6; 
          border-radius: 4px;
          color: #909399;
          font-size: 14px;
        ">
          暂无视频
        </div>
      </div>
    </div>
  </el-dialog>
  <!-- 结束查看详情 -->
  <!-- 分页 -->
  <div class="pagination" v-if="!isLookPublish">
    <div class="pagination-info">
      <span class="total-info">共 {{ courses.length }} 条记录</span>
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
      :total="courses.length"
      :pager-count="7"
      :hide-on-single-page="false"
      @current-change="handlePageChange"
    />
  </div>
  <!--增加课程-->
  <el-dialog title="增加课程" v-model="dialogFormVisible" @close="close()" width="50%">
    <el-form label-width="80px" :model="course" :rules="rules" ref="courseRef">
      <el-form-item label="课程名称" prop="name">
        <el-input v-model="course.name"></el-input>
      </el-form-item>
      <!-- 上传图片 -->
      <el-form-item label="课程封面">
        <el-upload ref="uploadImageRef" action="http://localhost:8080/upload/image" list-type="picture-card"
          :before-upload="beforeUploadImage" :on-preview="handlePictureCardPreview" :on-remove="handleRemove" :limit="1"
          :on-success="UploadImage" :file-list="fileList">
          <el-icon :size="30">
            <Plus />
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
        <el-dialog v-model="dialogVisible1" style="line-height: 0;">
          <img style="width: 100%;height: 100%" :src="dialogImageUrl" alt="" />
        </el-dialog>
      </el-form-item>
      <!-- //上传图片 -->
      <el-form-item label="课程简介" prop="introduction">
        <el-input v-model="course.introduction" type="textarea"></el-input>
      </el-form-item>
      <el-form-item label="课程排序" prop="courseOrder">
        <el-input v-model="course.courseOrder"></el-input>
      </el-form-item>
      <!-- 上传视频 -->
      <el-form-item label="上传视频">
        <div style="width: 100%;">
          <!-- 视频上传区域 -->
          <el-upload 
            ref="uploadVideoRef" 
            class="avatar-uploader el-upload--text" 
            :drag="plus"
            action="http://localhost:8080/upload/video" 
            multiple 
            :show-file-list="false" 
            :on-success="handleVideoSuccess"
            :before-upload="beforeUploadVideo" 
            :on-progress="uploadVideoProcess"
            style="margin-bottom: 10px;"
          >
            <el-icon v-if="plus" class="el-icon-upload" :size="20">
              <Upload />
            </el-icon>

            <div v-if="plus" class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <el-progress v-if="videoFlag == true" type="circle" :percentage="videoUploadPercent"
              style="margin-top: 30px"></el-progress>
            <template v-slot:tip>
              <div class="el-upload__tip">
                <span>请上传大小不超过</span>
                <span style="color: red;">100MB</span>
                <span>格式为</span>
                <span style="color: red;">mp4</span>
                <span>的文件</span>
              </div>
            </template>
          </el-upload>
          
          <!-- 视频预览区域 -->
          <div v-if="course.videoUrl && !plus" style="
            border: 1px solid #dcdfe6; 
            border-radius: 4px; 
            padding: 10px; 
            background-color: #fafafa;
            margin-top: 10px;
          ">
            <div style="display: flex; align-items: center; margin-bottom: 10px;">
              <span style="color: #67c23a; margin-right: 8px;">✓</span>
              <span style="color: #606266; font-size: 14px;">视频上传成功</span>
              <el-button 
                type="text" 
                size="small" 
                @click="clearVideo"
                style="margin-left: auto; color: #f56c6c;"
              >
                重新上传
              </el-button>
            </div>
            <video 
              :src="course.videoUrl" 
              width="100%" 
              height="200px" 
              controls 
              preload="metadata"
              style="border-radius: 4px;"
            >
              您的浏览器不支持视频播放
            </video>
            <div style="font-size: 12px; color: #909399; margin-top: 5px;">
              {{ course.videoUrl.split('/').pop() || '课程视频' }}
            </div>
          </div>
        </div>
      </el-form-item>
      <!-- ---- -->
      <el-form-item label="课程作者" prop="author">
        <el-input v-model="course.author"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addCourse(courseRef)" style="margin-left: 250px">确定</el-button>
        <el-button @click="back()">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>

  <!--  修改课程-->
  <el-dialog title="修改课程" v-model="dialogVisible" style="width: 50%" @close="close()">
    <el-form label-width="80px" :model="course" :rules="rules" ref="courseRef">
      <el-form-item label="课程名称" prop="name">
        <el-input v-model="course.name"></el-input>
      </el-form-item>
      <!-- //上传图片 -->
      <el-form-item label="课程封面">
        <el-upload ref="uploadImageRef" action="http://localhost:8080/upload/image" list-type="picture-card"
          :before-upload="beforeUploadImage" :on-preview="handlePictureCardPreview" :on-remove="handleRemove" :limit="1"
          :on-success="UploadImage" :file-list="fileList">
          <el-icon :size="30">
            <Plus />
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
        <el-dialog v-model="dialogVisible1" style="line-height: 0;">
          <img style="width: 100%;height: 100%" :src="dialogImageUrl" alt="" />
        </el-dialog>
      </el-form-item>
      <!-- 上传图片 -->

      <el-form-item label="课程简介" prop="introduction">
        <el-input v-model="course.introduction"></el-input>
      </el-form-item>
      <el-form-item label="课程排序" prop="order">
        <el-input v-model="course.courseOrder"></el-input>
      </el-form-item>
      <!-- 上传视频 -->
      <el-form-item label="上传视频">
        <div style="width: 100%;">
          <!-- 视频上传区域 -->
          <el-upload 
            ref="uploadVideoRef" 
            class="avatar-uploader el-upload--text" 
            :drag="plus"
            action="http://localhost:8080/upload/video" 
            multiple 
            :show-file-list="false" 
            :on-success="handleVideoSuccess"
            :before-upload="beforeUploadVideo" 
            :on-progress="uploadVideoProcess"
            style="margin-bottom: 10px;"
          >
            <el-icon v-if="plus" class="el-icon-upload" :size="20">
              <Upload />
            </el-icon>

            <div v-if="plus" class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <el-progress v-if="videoFlag == true" type="circle" :percentage="videoUploadPercent"
              style="margin-top: 30px"></el-progress>
            <template v-slot:tip>
              <div class="el-upload__tip">
                <span>请上传大小不超过</span>
                <span style="color: red;">100MB</span>
                <span>格式为</span>
                <span style="color: red;">mp4</span>
                <span>的文件</span>
              </div>
            </template>
          </el-upload>
          
          <!-- 视频预览区域 -->
          <div v-if="course.videoUrl && !plus" style="
            border: 1px solid #dcdfe6; 
            border-radius: 4px; 
            padding: 10px; 
            background-color: #fafafa;
            margin-top: 10px;
          ">
            <div style="display: flex; align-items: center; margin-bottom: 10px;">
              <span style="color: #67c23a; margin-right: 8px;">✓</span>
              <span style="color: #606266; font-size: 14px;">视频上传成功</span>
              <el-button 
                type="text" 
                size="small" 
                @click="clearVideo"
                style="margin-left: auto; color: #f56c6c;"
              >
                重新上传
              </el-button>
            </div>
            <video 
              :src="course.videoUrl" 
              width="100%" 
              height="200px" 
              controls 
              preload="metadata"
              style="border-radius: 4px;"
            >
              您的浏览器不支持视频播放
            </video>
            <div style="font-size: 12px; color: #909399; margin-top: 5px;">
              {{ course.videoUrl.split('/').pop() || '课程视频' }}
            </div>
          </div>
        </div>
      </el-form-item>
      <!-- ---- -->
      <el-form-item label="课程作者" prop="author">
        <el-input v-model="course.author"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="update(courseRef)" style="margin-left: 250px">确定</el-button>
        <el-button @click="back()">取消</el-button>
      </el-form-item>
    </el-form>

  </el-dialog>
</template>

<script lang="ts">
export default {
  name: 'course',
}
</script>

<script lang="ts" setup>
import useCourse from '@/hooks/useCourse'
import { Plus } from '@element-plus/icons-vue'; // 导入Element Plus的图标组件

import { Upload } from '@element-plus/icons-vue'
import { ElUpload } from 'element-plus'

const { courseRef,
  course,
  course2,
  dialogVisible,
  dialogFormVisible,
  courses,
  rules,
  addCourse,
  update,
  back,
  currentPage,
  pageSize,
  handlePageChange,
  pagedCourse,
  videoFlag,
  plus,
  videoUploadPercent,
  beforeUploadImage,
  beforeUploadVideo,
  openAdd,
  uploadVideoProcess,
  handleVideoSuccess,
  fileList,
  dialogImageUrl,
  dialogVisible1,
  UploadImage,
  handleRemove,
  handlePictureCardPreview,
  uploadImageRef,
  uploadVideoRef,
  searchCourse,
  multipleTableRef,
  handleSelectionChange,
  search,
  openForm,
  reset,
  close,
  delCourse,
  lookDetail,
  exportData,
  modify,
  isDisabled,
  detailVisiable,
  isAdmin,
  isLookPublish,
  publish,
  courses2,
  lookPublish,
  passes,
  notPass,
  backToList,
  cellStyle,
  handleVideoError,
  handleVideoLoadStart,
  handleVideoCanPlay
} = useCourse()
</script>

<style scoped>

.common-layout {
  height: 100%;
  width: 100%;
}

.container {
  display: flex;
  align-items: center;
}

.el-card__body {
  padding: 0;
}

h5 {
  margin: 7.1px 0;
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

:deep(.el-input__inner) {
  text-align: center;
}
</style>