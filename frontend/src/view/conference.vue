<template>
  <div class="conference-manager">
    <!-- 美化头部 -->
    <ModuleHeader 
      title="会议管理" 
      description="管理系统中的所有会议信息，包括会议的创建、编辑、审核和发布"
      icon="VideoCamera"
      color="red"
    />
    
    <!-- 原有内容区域 -->
    <div class="conference-content">
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
              <!-- 根据用户角色显示不同的按钮 -->
              <el-button 
                v-if="storedUserRole === '3'" 
                type="success" 
                style="width: 120px" 
                @click="lookAudit"
                >查看审核列表</el-button
              >
              <el-button 
                v-if="storedUserRole === '1' || storedUserRole === '2'" 
                type="primary" 
                style="width: 100px" 
                @click="publish"
                >发布</el-button
              >
              <!-- 审核列表返回按钮 -->
              <el-button 
                v-if="isLookAudit && storedUserRole === '3'" 
                type="info" 
                style="width: 100px" 
                @click="backToList"
                >返回列表</el-button
              >
            </div>

            <!--会议列表表格-->
            <el-table
              v-if="!isLookAudit"
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
              <el-table-column
                property="auditStatus"
                label="审核状态"
                header-align="center"
                align="center"
                :cell-style="mainTableCellStyle"
                width="120"
              >
                <template #default="scope">
                  <div style="display: flex; align-items: center; justify-content: center;">
                    <span v-if="scope.row.auditStatus === '通过'" style="color: #67c23a; font-weight: bold;">
                      ✓ 审核通过
                    </span>
                    <span v-else-if="scope.row.auditStatus === '不通过'" style="color: #f56c6c; font-weight: bold;">
                      ✗ 审核不通过
                    </span>
                    <span v-else-if="scope.row.auditStatus === '审核中'" style="color: #e6a23c; font-weight: bold;">
                      ⏳ 审核中
                    </span>
                    <span v-else style="color: #909399; font-weight: bold;">
                      ⏸ 待审核
                    </span>
                  </div>
                </template>
              </el-table-column>
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

            <!--审核列表表格 - 仅超级管理员可见-->
            <el-table
              v-if="isLookAudit && storedUserRole === '3'"
              ref="auditTableRef"
              :data="pagedAuditConfer"
              style="width: 100%"
              @selection-change="handleAuditSelectionChange"
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
              <el-table-column
                property="auditStatus"
                label="审核状态"
                header-align="center"
                align="center"
                :cell-style="auditCellStyle"
              >
                <template #default="scope">
                  <div style="display: flex; align-items: center; justify-content: center;">
                    <span v-if="scope.row.auditStatus === '通过'" style="color: #67c23a; font-weight: bold;">
                      ✓ 审核通过
                    </span>
                    <span v-else-if="scope.row.auditStatus === '不通过'" style="color: #f56c6c; font-weight: bold;">
                      ✗ 审核不通过
                    </span>
                    <span v-else-if="scope.row.auditStatus === '审核中'" style="color: #e6a23c; font-weight: bold;">
                      ⏳ 审核中
                    </span>
                    <span v-else style="color: #909399; font-weight: bold;">
                      ⏸ 待审核
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" header-align="center" align="center">
                <template #default="scope">
                  <el-link
                    type="success"
                    style="margin-right: 10px"
                    @click="auditPass(scope.row)"
                    >审核通过</el-link
                  >
                  <el-link
                    type="danger"
                    style="margin-right: 10px"
                    @click="auditReject(scope.row)"
                    >审核不通过</el-link
                  >
                  <el-link
                    type="primary"
                    @click="openDetailForm(scope.row)"
                    >详情</el-link
                  >
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
              <div class="pagination-info">
                <span class="total-info" v-if="!isLookAudit">共 {{ confer.length }} 条记录</span>
                <span class="total-info" v-if="isLookAudit">共 {{ auditConfer.length }} 条待审核记录</span>
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
                v-if="!isLookAudit"
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :background="true"
                layout="prev, pager, next"
                :total="confer.length"
                :pager-count="7"
                :hide-on-single-page="false"
                @current-change="handlePageChange"
              />
              <el-pagination
                v-if="isLookAudit"
                v-model:current-page="auditCurrentPage"
                v-model:page-size="pageSize"
                :background="true"
                layout="prev, pager, next"
                :total="auditConfer.length"
                :pager-count="7"
                :hide-on-single-page="false"
                @current-change="handleAuditPageChange"
              />
            </div>
            <!-- 详情对话框 -->
             
            <el-dialog v-model="dialogVisible1" width="50%" title="会议详情">
              <div style="justify-content: space-between; display: flex">
                <span>会议名称：</span>
                <span>{{ con.conferName }}</span>
              </div>
              <el-divider />
              <div style="justify-content: space-between; display: flex">
                <span>会议种类：</span>
                <span>{{ con.category }}</span>
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
                <span>地点：</span>
                <span>{{ con.location }}</span>
              </div>
              <el-divider />
              <div style="justify-content: space-between; display: flex">
                <span>主办单位：</span>
                <span>{{ con.organizer }}</span>
              </div>
              <el-divider />
              <div style="justify-content: space-between; display: flex">
                <span>议程：</span>
                <span style="white-space: pre-wrap;">{{ con.agenda }}</span>
              </div>
              <el-divider />
              <div style="justify-content: space-between; display: flex">
                <span>嘉宾介绍：</span>
                <span style="white-space: pre-wrap;">{{ con.guests }}</span>
              </div>
              <el-divider />
              <div style="justify-content: space-between; display: flex">
                <span>开始时间:</span>
                <span>{{ con.stime }}</span>
              </div>
              <el-divider />
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
                <el-form-item label="会议种类" prop="category">
                  <el-select v-model="con.category" placeholder="请选择会议种类">
                    <el-option label="会议研讨" value="会议研讨"></el-option>
                    <el-option label="标准定制" value="标准定制"></el-option>
                    <el-option label="技术培训" value="技术培训"></el-option>
                    <el-option label="工具研发" value="工具研发"></el-option>
                    <el-option label="公益行动" value="公益行动"></el-option>
                  </el-select>
                </el-form-item>
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
                    readonly
                    @click="openSubDialog"
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

            <!-- 增加会议内容子模态框 -->
            <el-dialog
              title="填写会议详细内容"
              v-model="subDialogVisible"
              width="40%"
              append-to-body
            >
              <el-form label-width="100px" :model="con">
                <el-form-item label="地点：" prop="location">
                  <el-input v-model="con.location" placeholder="请输入会议地点"></el-input>
                </el-form-item>
                <el-form-item label="主办单位：" prop="organizer">
                  <el-input v-model="con.organizer" placeholder="请输入主办单位"></el-input>
                </el-form-item>
                <el-form-item label="议程：" prop="agenda">
                  <el-input type="textarea" v-model="con.agenda" placeholder="请输入会议议程"></el-input>
                </el-form-item>
                <el-form-item label="嘉宾介绍：" prop="guests">
                  <el-input type="textarea" v-model="con.guests" placeholder="请输入嘉宾介绍"></el-input>
                </el-form-item>
              </el-form>
              <template #footer>
                <span class="dialog-footer">
                  <el-button @click="subDialogVisible = false">取消</el-button>
                  <el-button type="primary" @click="confirmSubDialog">确定</el-button>
                </span>
              </template>
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
                <el-form-item label="会议种类" prop="category">
                  <el-select v-model="con.category" placeholder="请选择会议种类">
                    <el-option label="会议研讨" value="会议研讨"></el-option>
                    <el-option label="标准定制" value="标准定制"></el-option>
                    <el-option label="技术培训" value="技术培训"></el-option>
                    <el-option label="工具研发" value="工具研发"></el-option>
                    <el-option label="公益行动" value="公益行动"></el-option>
                  </el-select>
                </el-form-item>
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
                  <el-input 
                    v-model="con.content"
                    readonly
                    @click="openSubDialog"
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
    </div>
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
  ElMessageBox,
  ElLoading,
} from "element-plus";
import axios from "axios";
import * as XLSX from "xlsx";
import moment from "moment";
import router from "@/router";
import request from "@/utils/request";
import ModuleHeader from "@/components/ModuleHeader.vue";
import { uploadConferenceImage } from '@/api/conference';
import type { UploadResponse, ApiResponse } from '@/types/upload';
import { useTaskStore } from '@/stores/task';
import { getAllPendingTasks, getPendingCourses, getPendingNews, getPendingConferences } from '@/api/user';

interface confer {
  conferName: string;
  creater: string;
  conferState: string;
  picture: string;
  content: string;
  stime: string;
  etime: string;
  category?: string;
  location?: string;
  organizer?: string;
  agenda?: string;
  guests?: string;
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
  category: "",
  location: "",
  organizer: "",
  agenda: "",
  guests: "",
});
const validateStartTime = (
  rule: any,
  value: any,
  callback: (error?: Error) => void
) => {
  if (!value) {
    callback(new Error("请选择开始时间"));
    return;
  }
  
  // 如果结束时间已经填写，则验证开始时间不能晚于结束时间
  if (con.value.etime && value) {
    if (moment(value).isAfter(moment(con.value.etime))) {
      callback(new Error("开始时间不能晚于结束时间"));
      return;
    }
  }
  
  callback();
};
const validateEndTime = (
  rule: any,
  value: any,
  callback: (error?: Error) => void
) => {
  if (!value) {
    callback(new Error("请选择结束时间"));
    return;
  }
  
  // 如果开始时间已经填写，则验证结束时间不能早于开始时间
  if (con.value.stime && value) {
    if (moment(con.value.stime).isAfter(moment(value))) {
      callback(new Error("结束时间不能早于开始时间"));
      return;
    }
  }
  
  callback();
};
const rules = ref({
  conferName: [
    { required: true, message: '请输入会议名称', trigger: 'blur' },
    { min: 2, max: 50, message: '会议名称长度应在2-50个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入会议内容', trigger: 'blur' }
  ],
  stime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  etime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
});
const oldName = ref("");
const dialogVisible = ref(false);
const dialogFormVisible = ref(false);
const subDialogVisible = ref(false);
const multipleTableRef = ref<InstanceType<typeof ElTable>>();
const multipleSelectionGroup = ref<Array<confer>>([]);
const multipleSelection = ref<String[]>([]);
const comRef = ref<FormInstance | null>(null);
const confer = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const data = ref([]);
const storedUserRole = sessionStorage.getItem("role"); //这是登录的人的身份，管理员为1，租户为2，超级管理员为3
const storedUserName = sessionStorage.getItem("name"); //这是登录的人的名字

// 审核相关变量
const isLookAudit = ref(false); // 是否查看审核列表
const auditConfer = ref([]); // 审核会议列表
const auditCurrentPage = ref(1); // 审核列表当前页
const auditTableRef = ref<InstanceType<typeof ElTable>>(); // 审核表格引用
const auditSelectionGroup = ref<Array<confer>>([]); // 审核选中的会议组
const auditSelection = ref<String[]>([]); // 审核选中的会议名称

// 添加task store
const task = useTaskStore();

const pagedConfer = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return confer.value.slice(start, end);
});

const pagedAuditConfer = computed(() => {
  const start = (auditCurrentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return auditConfer.value.slice(start, end);
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
    const selectedConfer = multipleSelectionGroup.value[0];
    openForm(selectedConfer);
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
  // 使用统一的request工具来避免CORS问题
  request({
    url: '/confer/list',
    method: 'get'
  })
    .then((response: any) => {
      if (response.isOk) {
        confer.value = response.confers;
        // 重置分页到第一页
        currentPage.value = 1;
      } else {
        confer.value = [];
        ElMessage.error(response.msg || "加载会议列表失败");
      }
    })
    .catch((error: any) => {
      console.error("加载会议列表失败", error);
      ElMessage.error("加载请求失败，请检查网络连接");
      confer.value = [];
    });
}

function addCom() {
  if (comRef.value == null) {
    ElMessage.error("表单初始化失败，请刷新页面重试");
    return;
  }
  
  // 基本信息完整性检查
  if (
    !con.value.stime ||
    !con.value.etime ||
    !con.value.picture ||
    !con.value.conferName ||
    !con.value.content ||
    !con.value.creater
  ) {
    ElMessage.error("请完整填写所有必填信息！");
    return;
  }
  
  // 时间验证和状态更新
  const startTime = moment(con.value.stime);
  const endTime = moment(con.value.etime);
  const currentTime = moment();
  
  if (startTime.isAfter(endTime)) {
    ElMessage.error("开始时间不能晚于结束时间！");
    return;
  }
  
  // 自动计算会议状态
  if (currentTime.isBefore(startTime)) {
    con.value.conferState = "未开始";
  } else if (currentTime.isBetween(startTime, endTime)) {
    con.value.conferState = "进行中";
  } else {
    con.value.conferState = "已结束";
  }
  
  // 格式化时间
  const formattedStartTime = startTime.format("YYYY-MM-DD HH:mm:ss");
  const formattedEndTime = endTime.format("YYYY-MM-DD HH:mm:ss");
  
  // 表单验证
  comRef.value.validate((valid: boolean) => {
    if (valid) {
      // 显示加载提示
      const loading = ElLoading.service({
        lock: true,
        text: '正在创建会议...',
        background: 'rgba(0, 0, 0, 0.7)',
      });
      
      // 使用统一的request工具来避免CORS问题
      request({
        url: '/confer/add',
        method: 'post',
        data: {
          conferName: con.value.conferName,
          conferState: con.value.conferState,
          creater: con.value.creater,
          content: con.value.content,
          picture: con.value.picture,
          stime: formattedStartTime,
          etime: formattedEndTime,
          category: con.value.category,
          location: con.value.location,
          organizer: con.value.organizer,
          agenda: con.value.agenda,
          guests: con.value.guests,
        }
      })
        .then((res: any) => {
          loading.close();
          if (res.isOk) {
            ElMessage.success(`会议"${con.value.conferName}"创建成功！`);
            // 重新加载数据以显示新增的会议
            loadCom();
            // 关闭对话框并重置表单
            back();
          } else {
            ElMessage.error(res.msg || "创建失败，请检查会议名称是否已存在！");
          }
        })
        .catch((err: any) => {
          loading.close();
          console.error("添加会议失败", err);
          ElMessage.error("创建请求失败，请检查网络连接");
        });
    } else {
      ElMessage.error("请检查并完善表单信息");
    }
  });
}

async function delCom() {
  if (multipleSelection.value.length === 0) {
    ElMessage.error("请先选择要删除的会议！");
    return;
  }
  
  const conferNamesToDeleteStr = multipleSelection.value.join(", ");
  const deleteCount = multipleSelection.value.length;
  const deleteMessage = deleteCount === 1 ? 
    `确定要删除会议"${conferNamesToDeleteStr}"吗？` : 
    `确定要删除以下${deleteCount}个会议吗？\n${conferNamesToDeleteStr}`;
  
  try {
    // 使用ElementPlus的确认对话框
    await ElMessageBox.confirm(
      deleteMessage,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    );
    
    // 显示删除进度提示
    const loading = ElLoading.service({
      lock: true,
      text: '正在删除会议...',
      background: 'rgba(0, 0, 0, 0.7)',
    });
    
    try {
      // 将只读数组转换为可变数组并确保类型正确
      const conferNamesToDelete: string[] = multipleSelection.value.map(name => String(name));
      
      // 使用统一的request工具来避免CORS问题
      const response = await request({
        url: '/confer/del',
        method: 'post',
        data: conferNamesToDelete,
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      loading.close();
      
      if (response.isOk) {
        ElMessage.success(`成功删除${deleteCount}个会议`);
        // 重新加载数据
        loadCom();
        // 清空选择
        multipleTableRef.value?.clearSelection();
      } else {
        ElMessage.error(response.msg || "删除失败");
      }
    } catch (err) {
      loading.close();
      console.error("删除失败", err);
      ElMessage.error("删除请求失败，请检查网络连接");
    }
  } catch {
    // 用户取消删除
    ElMessage.info('已取消删除');
  }
}

function update() {
  // 基本信息完整性检查
  if (
    !con.value.stime ||
    !con.value.etime ||
    !con.value.picture ||
    !con.value.conferName ||
    !con.value.content ||
    !con.value.creater
  ) {
    ElMessage.error("请完整填写所有必填信息！");
    return;
  }
  
  // 时间验证和状态更新
  const startTime = moment(con.value.stime);
  const endTime = moment(con.value.etime);
  const currentTime = moment();
  
  if (startTime.isAfter(endTime)) {
    ElMessage.error("开始时间不能晚于结束时间！");
    return;
  }
  
  // 自动计算会议状态
  if (currentTime.isBefore(startTime)) {
    con.value.conferState = "未开始";
  } else if (currentTime.isBetween(startTime, endTime)) {
    con.value.conferState = "进行中";
  } else {
    con.value.conferState = "已结束";
  }
  
  // 格式化时间
  const formattedStartTime = startTime.format("YYYY-MM-DD HH:mm:ss");
  const formattedEndTime = endTime.format("YYYY-MM-DD HH:mm:ss");
  
  // 表单验证
  comRef.value?.validate((valid: boolean) => {
    if (valid) {
      // 显示加载提示
      const loading = ElLoading.service({
        lock: true,
        text: '正在更新会议信息...',
        background: 'rgba(0, 0, 0, 0.7)',
      });
      
      // 使用统一的request工具来避免CORS问题
      request({
        url: '/confer/update',
        method: 'post',
        data: {
          conferName: con.value.conferName,
          oldName: oldName.value,
          conferState: con.value.conferState,
          creater: con.value.creater,
          content: con.value.content,
          picture: con.value.picture,
          stime: formattedStartTime,
          etime: formattedEndTime,
          category: con.value.category,
          location: con.value.location,
          organizer: con.value.organizer,
          agenda: con.value.agenda,
          guests: con.value.guests,
        }
      })
        .then((res: any) => {
          loading.close();
          if (res.isOk) {
            ElMessage.success(`会议"${con.value.conferName}"更新成功！`);
            // 重新加载数据以实时更新表格
            loadCom();
            // 关闭对话框并重置表单
            back();
            // 清空选择以避免数据不一致
            multipleTableRef.value?.clearSelection();
          } else {
            ElMessage.error(res.msg || "更新失败，请检查会议名称是否与现有会议冲突！");
          }
        })
        .catch((err: any) => {
          loading.close();
          console.error("更新会议失败", err);
          ElMessage.error("更新请求失败，请检查网络连接");
        });
    } else {
      ElMessage.error("请检查并完善表单信息");
    }
  });
}

function search() {
  const { conferName, creater, content, stime } = searchParams.value;
  
  // 构建搜索条件提示
  const searchConditions = [];
  if (conferName) searchConditions.push(`会议名称包含"${conferName}"`);
  if (creater) searchConditions.push(`创建人包含"${creater}"`);
  if (content) searchConditions.push(`会议内容包含"${content}"`);
  if (stime) searchConditions.push(`开始时间在"${moment(stime).format('YYYY-MM-DD HH:mm:ss')}"之后`);
  
  // 如果没有搜索条件，显示所有数据
  if (searchConditions.length === 0) {
    ElMessage.info("未输入搜索条件，显示全部会议");
    loadCom();
    return;
  }
  
  // 显示搜索条件
  ElMessage.info(`搜索条件：${searchConditions.join('，')}`);
  
  // 格式化时间参数
  const searchData = {
    ...searchParams.value,
    stime: stime ? moment(stime).format('YYYY-MM-DD HH:mm:ss') : ''
  };
  
  // 使用统一的request工具来避免CORS问题
  request({
    url: '/confer/search',
    method: 'post',
    data: searchData
  })
    .then((res: any) => {
      if (res.isOk) {
        confer.value = res.confers;
        const resultCount = res.confers.length;
        if (resultCount === 0) {
          ElMessage.warning("未找到符合条件的会议");
        } else {
          ElMessage.success(`找到 ${resultCount} 条符合条件的会议`);
        }
        // 重置分页到第一页
        currentPage.value = 1;
      } else {
        ElMessage.error(res.msg || "搜索失败");
        // 搜索失败时保持原有数据
      }
    })
    .catch((err: any) => {
      console.error("搜索失败", err);
      ElMessage.error("搜索请求失败，请检查网络连接");
    });
}

function openAdd() {
  resetCon(); // 确保每次打开都是一个全新的表单状态
  dialogFormVisible.value = true;
}

function openSubDialog() {
  subDialogVisible.value = true;
}

function confirmSubDialog() {
  const { location, organizer, agenda, guests } = con.value;
  let content = '';
  if (location) content += `地点：${location}\\n`;
  if (organizer) content += `主办单位：${organizer}\\n`;
  if (agenda) content += `议程：${agenda}\\n`;
  if (guests) content += `嘉宾介绍：${guests}`;
  con.value.content = content;
  subDialogVisible.value = false;
}

function openForm(row: any) {
  con.value = Object.assign({}, row);
  if (con.value.picture) {
    fileList.value = [
      {
        name: "会议图标",
        url: con.value.picture,
      },
    ];
  } else {
    fileList.value = [];
  }
  oldName.value = row.conferName;
  dialogVisible.value = true;
}

function openDetailForm(row: any) {
  con.value = Object.assign({}, row);
  if (con.value.picture != null) {
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
  resetCon();
}

function resetCon() {
  con.value = {
    conferName: "",
    creater: "",
    conferState: "",
    picture: "",
    content: "",
    stime: "",
    etime: "",
    category: "",
    location: "",
    organizer: "",
    agenda: "",
    guests: "",
  };
  fileList.value = [];
  dialogImageUrl.value = "";
}

function reset() {
  searchParams.value = {
    conferName: "",
    creater: "",
    content: "",
    stime: "",
  };
  ElMessage.success("搜索条件已重置");
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

const UploadImage = (res: UploadResponse) => {
  if (res.resCode === '200' && res.ImageUrl) {
    con.value.picture = res.ImageUrl;
    // 更新 fileList 来显示新的图片
    if (fileList.value.length > 0) {
      fileList.value[0].url = res.ImageUrl;
      fileList.value[0].name = res.ImageUrl.split('/').pop() || '新图片';
    } else {
      fileList.value.push({
        name: res.ImageUrl.split('/').pop() || '新图片',
        url: res.ImageUrl,
      });
    }
    ElMessage.success('图片上传成功！');
  } else {
    ElMessage.error(res.msg || '图片上传失败，请重新上传！');
  }
};

const handleRemove = (file: any, fileList: any) => {
  console.log(file, fileList);
};

const handlePictureCardPreview = (file: UploadFile) => {
  console.log(file.url);
  dialogVisible1.value = true;
  dialogImageUrl.value = file.url!;
};
const uploadImageRef = ref<InstanceType<typeof ElUpload> | null>(null);

function reset1() {
  uploadImageRef.value!.clearFiles(); //清除文件列表
}

function Export() {
  if (multipleSelectionGroup.value.length === 0) {
    ElMessage.error("请先选择要导出的会议！");
    return;
  }
  
  try {
    const dataToExport = multipleSelectionGroup.value.map(item => ({
      '会议名称': item.conferName,
      '会议种类': item.category,
      '创建人': item.creater,
      '会议状态': item.conferState,
      '地点': item.location,
      '主办单位': item.organizer,
      '议程': item.agenda,
      '嘉宾介绍': item.guests,
      '开始时间': moment(item.stime).format('YYYY-MM-DD HH:mm:ss'),
      '结束时间': moment(item.etime).format('YYYY-MM-DD HH:mm:ss'),
    }));

    const dataE = XLSX.utils.json_to_sheet(dataToExport);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, dataE, "会议数据");
    
    // 生成带时间戳的文件名
    const timestamp = moment().format('YYYY-MM-DD-HHmmss');
    const fileName = `会议数据_${timestamp}.xlsx`;
    
    XLSX.writeFile(wb, fileName);
    ElMessage.success(`成功导出${multipleSelectionGroup.value.length}个会议到文件：${fileName}`);
  } catch (error) {
    console.error("导出失败", error);
    ElMessage.error("导出失败，请重试");
  }
}

// 审核相关函数
function lookAudit() {
  isLookAudit.value = true;
  loadAuditList();
}

function backToList() {
  isLookAudit.value = false;
  loadCom();
}

// 加载审核列表
function loadAuditList() {
  request({
    url: '/confer/auditList',
    method: 'get'
  })
    .then((response: any) => {
      if (response.isOk) {
        auditConfer.value = response.auditConfers || [];
        auditCurrentPage.value = 1;
        ElMessage.success(response.msg || "加载审核列表成功");
      } else {
        auditConfer.value = [];
        ElMessage.error(response.msg || "加载审核列表失败");
      }
    })
    .catch((error: any) => {
      console.error("加载审核列表失败", error);
      ElMessage.error("加载审核列表失败，请检查网络连接");
      auditConfer.value = [];
    });
}

// 审核列表分页处理
const handleAuditPageChange = (page: number) => {
  auditCurrentPage.value = page;
};

// 审核列表选择处理
const handleAuditSelectionChange = (val: Array<confer>) => {
  auditSelectionGroup.value = val;
  const selectedConferNames = val.map((item) => item.conferName);
  auditSelection.value = selectedConferNames;
};

// 刷新首页任务数量的函数
async function refreshHomeTaskCount() {
  try {
    // 尝试使用统一的API获取所有待审核任务
    try {
      const res = await getAllPendingTasks();
      if (res && res.code === 200 && res.data) {
        task.updatePendingTasks(res.data);
        return;
      }
    } catch (error) {
      console.log('统一API不可用，使用分别获取方式');
    }

    // 如果统一API不可用，分别获取各类型的待审核任务
    const [coursesRes, newsRes, conferencesRes] = await Promise.allSettled([
      getPendingCourses(),
      getPendingNews(), 
      getPendingConferences()
    ]);

    let coursesCount = 0;
    let newsCount = 0;
    let conferencesCount = 0;

    // 处理课程结果
    if (coursesRes.status === 'fulfilled' && coursesRes.value) {
      if (Array.isArray(coursesRes.value)) {
        coursesCount = coursesRes.value.length;
      } else if (coursesRes.value.data && Array.isArray(coursesRes.value.data)) {
        coursesCount = coursesRes.value.data.length;
      }
    }

    // 处理动态结果
    if (newsRes.status === 'fulfilled' && newsRes.value) {
      if (Array.isArray(newsRes.value)) {
        newsCount = newsRes.value.length;
      } else if (newsRes.value.data && Array.isArray(newsRes.value.data)) {
        newsCount = newsRes.value.data.length;
      }
    }

    // 处理会议结果
    if (conferencesRes.status === 'fulfilled' && conferencesRes.value) {
      if (Array.isArray(conferencesRes.value)) {
        conferencesCount = conferencesRes.value.length;
      } else if (conferencesRes.value.data && Array.isArray(conferencesRes.value.data)) {
        conferencesCount = conferencesRes.value.data.length;
      }
    }

    const totalCount = coursesCount + newsCount + conferencesCount;
    
    task.updatePendingTasks({
      courses: coursesCount,
      news: newsCount,
      conferences: conferencesCount,
      total: totalCount
    });

  } catch (error) {
    console.error("刷新首页任务数量失败:", error);
  }
}

// 审核通过
async function auditPass(row: any) {
  let loading: any = null;
  let auditSuccess = false;
  
  try {
    console.log("准备审核通过会议:", row);
    
    loading = ElLoading.service({
      lock: true,
      text: '正在审核会议...',
      background: 'rgba(0, 0, 0, 0.7)',
    });

    const auditData = {
      conferId: row.id,
      conferName: row.conferName,
      status: "通过",
      remark: "超级管理员审核通过"
    };

    console.log("审核数据:", auditData);

    const response: any = await request({
      url: '/confer/audit',
      method: 'post',
      data: auditData,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    console.log("审核请求完成，收到响应:", response);

    if (response.isOk) {
      ElMessage.success(`会议"${row.conferName}"审核通过！`);
      auditSuccess = true;
    } else {
      ElMessage.error(response.msg || "审核失败");
    }
  } catch (err: any) {
    console.error("审核失败，错误:", err);
    ElMessage.error(err.message || "审核失败，请重试");
  } finally {
    if (loading) {
      loading.close();
    }
    
    if (auditSuccess) {
      try {
        await loadAuditList();
        // 刷新首页任务数量
        await refreshHomeTaskCount();
      } catch (refreshErr) {
        console.error("刷新审核列表失败:", refreshErr);
        ElMessage.warning("审核成功，但刷新列表失败，请手动刷新");
      }
    }
  }
}

// 审核不通过
async function auditReject(row: any) {
  let loading: any = null;
  let auditSuccess = false;
  
  try {
    console.log("准备审核不通过会议:", row);
    
    loading = ElLoading.service({
      lock: true,
      text: '正在审核会议...',
      background: 'rgba(0, 0, 0, 0.7)',
    });

    const auditData = {
      conferId: row.id,
      conferName: row.conferName,
      status: "不通过",
      remark: "超级管理员审核不通过"
    };

    console.log("审核数据:", auditData);

    const response: any = await request({
      url: '/confer/audit',
      method: 'post',
      data: auditData,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    console.log("审核请求完成，收到响应:", response);

    if (response.isOk) {
      ElMessage.success(`会议"${row.conferName}"审核不通过！`);
      auditSuccess = true;
    } else {
      ElMessage.error(response.msg || "审核失败");
    }
  } catch (err: any) {
    console.error("审核失败，错误:", err);
    ElMessage.error(err.message || "审核失败，请重试");
  } finally {
    if (loading) {
      loading.close();
    }
    
    if (auditSuccess) {
      try {
        await loadAuditList();
        // 刷新首页任务数量
        await refreshHomeTaskCount();
      } catch (refreshErr) {
        console.error("刷新审核列表失败:", refreshErr);
        ElMessage.warning("审核成功，但刷新列表失败，请手动刷新");
      }
    }
  }
}

// 发布会议（普通管理员和企业管理员使用）
async function publish() {
  if (multipleSelectionGroup.value.length === 0) {
    ElMessage.error("请先选择要发布的会议！");
    return;
  }

  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在发布会议...',
      background: 'rgba(0, 0, 0, 0.7)',
    });

    const conferNames = multipleSelectionGroup.value.map(item => item.conferName);
    
    const response: any = await request({
      url: '/confer/publish',
      method: 'post',
      data: conferNames,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    loading.close();

    if (response.isOk) {
      ElMessage.success(`成功发布${conferNames.length}个会议，等待审核！`);
      // 刷新列表
      loadCom();
      // 清空选择
      multipleTableRef.value?.clearSelection();
    } else {
      ElMessage.error(response.msg || "发布失败");
    }
  } catch (err: any) {
    console.error("发布失败，错误:", err);
    ElMessage.error("发布失败，请重试");
  }
}

// 审核状态样式
const auditCellStyle = ({
  row,
  column,
  rowIndex,
  columnIndex,
}: {
  row: any;
  column: any;
  rowIndex: number;
  columnIndex: number;
}) => {
  if (column.property === "auditStatus") {
    // 审核状态的颜色样式
    if (row.auditStatus === "通过") {
      return { color: "#67c23a", fontWeight: "bold" }; // 绿色
    } else if (row.auditStatus === "不通过") {
      return { color: "#f56c6c", fontWeight: "bold" }; // 红色
    } else if (row.auditStatus === "审核中") {
      return { color: "#e6a23c", fontWeight: "bold" }; // 橙色
    } else {
      return { color: "#909399", fontWeight: "bold" }; // 灰色（待审核）
    }
  }
};

// 主表格审核状态样式（用于主会议列表）
const mainTableCellStyle = ({
  row,
  column,
  rowIndex,
  columnIndex,
}: {
  row: any;
  column: any;
  rowIndex: number;
  columnIndex: number;
}) => {
  if (column.property === "auditStatus") {
    // 审核状态的颜色样式
    if (row.auditStatus === "通过") {
      return { color: "#67c23a", fontWeight: "bold" }; // 绿色
    } else if (row.auditStatus === "不通过") {
      return { color: "#f56c6c", fontWeight: "bold" }; // 红色
    } else if (row.auditStatus === "审核中") {
      return { color: "#e6a23c", fontWeight: "bold" }; // 橙色
    } else if (row.auditStatus === "待审核") {
      return { color: "#909399", fontWeight: "bold" }; // 灰色（待审核）
    } else {
      return { color: "#909399", fontWeight: "normal" }; // 默认样式
    }
  }
};

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
.conference-manager {
  background-color: #f5f7fa;
  min-height: 100vh;
}

.conference-content {
  padding: 20px;
}

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