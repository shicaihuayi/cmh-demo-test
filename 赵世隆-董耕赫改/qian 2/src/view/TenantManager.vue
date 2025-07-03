<template>
  <div class="tenant-manager">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon class="title-icon"><OfficeBuilding /></el-icon>
        租户管理
      </h2>
      <p class="page-description">管理系统中的所有租户信息</p>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-card class="search-card" shadow="never">
        <template #header>
          <div class="search-header">
            <el-icon><Search /></el-icon>
            <span>搜索条件</span>
          </div>
        </template>
        
        <el-form :model="searchForm" inline>
          <el-form-item label="租户名称">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入租户名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="联系人">
            <el-input
              v-model="searchForm.linkman"
              placeholder="请输入联系人"
              clearable
              style="width: 180px"
            />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input
              v-model="searchForm.tel"
              placeholder="请输入联系电话"
              clearable
              style="width: 180px"
            />
          </el-form-item>
          <el-form-item label="租户标识">
            <el-input
              v-model="searchForm.sign"
              placeholder="请输入租户标识"
              clearable
              style="width: 180px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 操作区域 -->
    <div class="mb-1" style="margin-top: 20px; margin-bottom: 20px">
      <el-button type="primary" style="width: 100px" @click="handleAdd" :loading="loading">
        <el-icon><Plus /></el-icon>
        新增租户
      </el-button>
      <el-button 
        type="danger" 
        style="width: 100px"
        @click="handleBatchDelete" 
        :disabled="!selectedTenants.length"
        :loading="loading"
      >
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button type="warning" style="width: 100px" @click="handleImport" :loading="loading">
        <el-icon><Upload /></el-icon>
        导入
      </el-button>
      <el-button type="success" style="width: 100px" @click="handleExport" :loading="loading">
        <el-icon><Download /></el-icon>
        导出
      </el-button>
      <el-tooltip content="刷新数据">
        <el-button circle @click="loadTenants" :loading="loading">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </el-tooltip>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
    <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tenantList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        row-key="id"
        stripe
        border
        :max-height="tableMaxHeight"
      >
        <el-table-column type="selection" width="50" fixed="left" header-align="center" align="center" />
        
        <el-table-column label="公司图标" width="80" fixed="left" header-align="center" align="center">
          <template #default="{ row }">
            <div class="logo-container">
              <el-image
                v-if="row.coverUrl"
                :src="getImageUrl(row.coverUrl)"
                :preview-src-list="[getImageUrl(row.coverUrl)]"
                class="tenant-logo"
                fit="cover"
                :preview-teleported="true"
                @error="handleImageError"
              />
              <div v-else class="no-image">
                <el-icon><Picture /></el-icon>
                <span>暂无图片</span>
              </div>
            </div>
        </template>
      </el-table-column>

        <el-table-column prop="name" label="公司名称" width="200" header-align="center" align="center" show-overflow-tooltip />
        
        <el-table-column prop="linkman" label="联系人" width="100" header-align="center" align="center" show-overflow-tooltip />
        
        <el-table-column prop="tel" label="联系电话" width="120" header-align="center" align="center" show-overflow-tooltip />
        
        <el-table-column prop="admin" label="管理员" width="100" header-align="center" align="center" show-overflow-tooltip />
        
        <el-table-column prop="sign" label="公司标识" width="100" header-align="center" align="center" show-overflow-tooltip />
        
        <el-table-column prop="mail" label="邮箱" width="160" header-align="center" align="center" show-overflow-tooltip />
        
        <el-table-column prop="desc" label="描述" width="230" header-align="center" align="center" show-overflow-tooltip />

        <el-table-column label="状态" width="80" fixed="right" header-align="center" align="center">
          <template #default="{ row }">
            <el-tag :type="row.state === 1 ? 'success' : 'danger'" effect="dark" size="small">
              {{ row.state === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="380" fixed="right" header-align="center" align="center">
          <template #default="{ row }">
            <div class="operation-buttons">
              <el-button type="primary" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                修改
              </el-button>
              <el-button type="info" size="small" @click="handleDetail(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-popconfirm 
                title="确定要删除这个租户吗？删除后会删除该公司所有部门和员工！"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button type="danger" size="small">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-info">
          <span class="total-info">共 {{ pagination.total }} 条记录</span>
          <div class="page-size-selector">
            <el-select v-model="pagination.size" @change="handleSizeChange" style="width: 120px;">
              <el-option
                v-for="size in [10, 20, 50, 100]"
                :key="size"
                :label="`${size}条/页`"
                :value="size"
              />
            </el-select>
          </div>
        </div>
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :background="true"
          layout="prev, pager, next"
          :total="pagination.total"
          :pager-count="7"
          :hide-on-single-page="false"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增租户' : '修改租户'"
      width="600px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="tenantForm"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="公司图标" prop="coverUrl">
          <div class="upload-container">
            <el-upload
              ref="uploadRef"
              class="logo-uploader"
              :action="uploadAction"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              accept="image/jpeg,image/jpg,image/png"
            >
              <img v-if="tenantForm.coverUrl" :src="getImageUrl(tenantForm.coverUrl)" class="logo-preview" />
              <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tips">
              <p>点击上传公司Logo</p>
              <p class="tip-text">支持 JPG、PNG 格式，文件大小不超过 5MB</p>
            </div>
          </div>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
        <el-form-item label="公司名称" prop="name">
              <el-input v-model="tenantForm.name" placeholder="请输入公司名称" />
        </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司标识" prop="sign">
              <el-input v-model="tenantForm.sign" placeholder="请输入公司标识" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
        <el-form-item label="联系人" prop="linkman">
              <el-input v-model="tenantForm.linkman" placeholder="请输入联系人" />
        </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="tel">
              <el-input 
                v-model="tenantForm.tel" 
                placeholder="请输入联系电话"
                @input="formatPhone"
              />
        </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
        <el-form-item label="管理员" prop="admin">
              <el-input v-model="tenantForm.admin" placeholder="请输入管理员用户名" />
        </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="mail">
              <el-input v-model="tenantForm.mail" placeholder="请输入邮箱地址" />
        </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="公司描述" prop="desc">
          <el-input 
            v-model="tenantForm.desc" 
            type="textarea" 
            :rows="3"
            placeholder="请输入公司描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ dialogMode === 'add' ? '确认新增' : '确认修改' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="租户详情"
      width="500px"
      destroy-on-close
    >
      <div v-if="currentTenant" class="detail-container">
        <div class="detail-logo">
          <el-image
            v-if="currentTenant.coverUrl"
            :src="getImageUrl(currentTenant.coverUrl)"
            :preview-src-list="[getImageUrl(currentTenant.coverUrl)]"
            class="detail-logo-img"
            fit="cover"
          />
          <div v-else class="no-logo">
            <el-icon><Picture /></el-icon>
            <span>暂无Logo</span>
          </div>
        </div>
        
        <div class="detail-info">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="公司名称">{{ currentTenant.name }}</el-descriptions-item>
            <el-descriptions-item label="公司标识">{{ currentTenant.sign }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ currentTenant.linkman }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentTenant.tel }}</el-descriptions-item>
            <el-descriptions-item label="管理员">{{ currentTenant.admin }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ currentTenant.mail }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentTenant.state === 1 ? 'success' : 'danger'">
                {{ currentTenant.state === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="公司描述">{{ currentTenant.desc || '暂无描述' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>

    <!-- 隐藏的文件输入框用于导入 -->
    <input 
      ref="fileInputRef" 
      type="file" 
      accept=".xlsx,.xls" 
      style="display: none" 
      @change="handleFileChange"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification, type FormInstance, type UploadInstance } from 'element-plus'
import { 
  Search, Refresh, Plus, Delete, Upload, Download, Edit, View, Picture, OfficeBuilding 
} from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import {
  getTenantList,
  searchTenants,
  addTenant,
  updateTenant,
  deleteTenant,
  createAdminUser,
  updateDepartmentName,
  updateUserName,
  updateAdmin,
  deleteDepartment,
  deleteUser,
  type TenantInfo
} from '@/api/tenant'

// 接口定义
interface Tenant {
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

interface SearchForm {
  name: string
  linkman: string
  tel: string
  sign: string
}

interface TenantForm {
  id?: number
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

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const tenantList = ref<Tenant[]>([])
const selectedTenants = ref<Tenant[]>([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const currentTenant = ref<Tenant | null>(null)
const tableMaxHeight = ref<number>(500)

// 表单引用
const formRef = ref<FormInstance>()
const tableRef = ref()
const uploadRef = ref<UploadInstance>()
const fileInputRef = ref<HTMLInputElement>()

// 搜索表单
const searchForm = reactive<SearchForm>({
  name: '',
  linkman: '',
  tel: '',
  sign: ''
})

// 租户表单
const tenantForm = reactive<TenantForm>({
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

// 保存原始数据（用于修改时的对比）
const originalTenant = ref<TenantForm | null>(null)

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 原始数据（用于搜索和分页）
const allTenants = ref<Tenant[]>([])

// 上传配置
const uploadAction = '/api/upload/image'

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入公司名称', trigger: 'blur' },
    { min: 2, max: 50, message: '公司名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  linkman: [
    { required: true, message: '请输入联系人', trigger: 'blur' },
    { min: 2, max: 20, message: '联系人长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  tel: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  admin: [
    { required: true, message: '请输入管理员用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '管理员用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  sign: [
    { required: true, message: '请输入公司标识', trigger: 'blur' },
    { min: 2, max: 20, message: '公司标识长度在 2 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '公司标识只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  mail: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  desc: [
    { max: 500, message: '描述不能超过 500 个字符', trigger: 'blur' }
  ]
}

// 计算表格最大高度
const updateTableHeight = () => {
  nextTick(() => {
    const windowHeight = window.innerHeight
    const offset = 280 // 预留空间给其他元素（头部、搜索、工具栏等）
    tableMaxHeight.value = windowHeight - offset
  })
}

// 方法定义
// 加载租户列表
const loadTenants = async () => {
  try {
    loading.value = true
    console.log('开始加载租户列表...')
    
    const response = await getTenantList()
    console.log('API响应:', response)
    
    if (response && response.isOK) {
      allTenants.value = response.comps || []
      updateTenantList()
      ElMessage.success(response.msg || '数据加载成功')
    } else {
      console.error('API返回错误:', response)
      ElMessage.error(response?.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载租户列表失败:', error)
    ElMessage.error('加载租户列表失败: ' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

// 更新租户列表（应用搜索和分页）
const updateTenantList = () => {
  let filteredData = [...allTenants.value]
  
  // 应用搜索过滤
  if (searchForm.name) {
    filteredData = filteredData.filter(item => 
      item.name.toLowerCase().includes(searchForm.name.toLowerCase())
    )
  }
  if (searchForm.linkman) {
    filteredData = filteredData.filter(item => 
      item.linkman.toLowerCase().includes(searchForm.linkman.toLowerCase())
    )
  }
  if (searchForm.tel) {
    filteredData = filteredData.filter(item => 
      item.tel.includes(searchForm.tel)
    )
  }
  if (searchForm.sign) {
    filteredData = filteredData.filter(item => 
      item.sign.toLowerCase().includes(searchForm.sign.toLowerCase())
    )
  }
  
  // 更新总数
  pagination.total = filteredData.length
  
  // 应用分页
  const start = (pagination.current - 1) * pagination.size
  const end = start + pagination.size
  tenantList.value = filteredData.slice(start, end)
}

// 搜索处理
const handleSearch = async () => {
  // 如果有搜索条件，使用服务器端搜索
  const hasSearchCondition = searchForm.name || searchForm.linkman || searchForm.tel || searchForm.sign
  
  if (hasSearchCondition) {
    try {
      loading.value = true
      const response = await searchTenants(searchForm)
      if (response.isOK) {
        allTenants.value = response.comps || []
        pagination.current = 1
        updateTenantList()
        ElMessage.success('搜索完成')
      } else {
        ElMessage.error(response.msg || '搜索失败')
      }
    } catch (error) {
      console.error('搜索失败:', error)
      ElMessage.error('搜索失败')
    } finally {
      loading.value = false
    }
  } else {
    pagination.current = 1
    updateTenantList()
  }
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    linkman: '',
    tel: '',
    sign: ''
  })
  pagination.current = 1
  loadTenants() // 重新加载所有数据
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  updateTenantList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  updateTenantList()
}

// 选择处理
const handleSelectionChange = (selection: Tenant[]) => {
  selectedTenants.value = selection
}

// 新增租户
const handleAdd = () => {
  dialogMode.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑租户
const handleEdit = (row: Tenant) => {
  dialogMode.value = 'edit'
  // 确保包含id
  Object.assign(tenantForm, {
    id: row.id,
    name: row.name,
    linkman: row.linkman,
    tel: row.tel,
    desc: row.desc,
    admin: row.admin,
    sign: row.sign,
    state: row.state,
    mail: row.mail,
    coverUrl: row.coverUrl
  })
  originalTenant.value = { ...row } // 保存原始数据
  dialogVisible.value = true
}

// 查看详情
const handleDetail = (row: Tenant) => {
  currentTenant.value = row
  detailVisible.value = true
}

// 删除单个租户
const handleDelete = async (row: Tenant) => {
  try {
    loading.value = true
    const response = await deleteTenant([row.id])
    
    if (response.isOK) {
      ElMessage.success('删除成功')
      
      // 删除相关的部门和用户
      await Promise.allSettled([
        deleteDepartment(row.name),
        deleteUser(row.name)
      ])
      
      await loadTenants()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  } finally {
    loading.value = false
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedTenants.value.length === 0) {
    ElMessage.warning('请选择要删除的租户')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedTenants.value.length} 个租户吗？删除后会删除这些公司的所有部门和员工！`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    loading.value = true
    const ids = selectedTenants.value.map(item => item.id)
    const response = await deleteTenant(ids)
    
    if (response.isOK) {
      ElMessage.success('批量删除成功')
      
      // 删除相关的部门和用户
      const deletePromises = selectedTenants.value.flatMap(tenant => [
        deleteDepartment(tenant.name),
        deleteUser(tenant.name)
      ])
      await Promise.allSettled(deletePromises)
      
      selectedTenants.value = []
      await loadTenants()
    } else {
      ElMessage.error(response.msg || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    const tenantData: TenantInfo = {
      ...tenantForm
    }

    console.log('=== 租户修改调试信息开始 ===')
    console.log('操作模式:', dialogMode.value)
    console.log('提交的数据:', tenantData)
    console.log('原始数据:', originalTenant.value)

    let response
    if (dialogMode.value === 'add') {
      response = await addTenant(tenantData)
    } else {
      console.log('准备发送修改请求...')
      response = await updateTenant(tenantData)
      console.log('修改请求响应:', response)
    }
    
    console.log('完整响应对象:', response)
    console.log('响应的isOk属性:', response?.isOk)
    console.log('响应的code属性:', response?.code)
    console.log('响应的msg属性:', response?.msg)
    console.log('响应类型:', typeof response)
    
    if (response && response.isOk) {
      console.log('✅ 判断为成功，准备处理成功逻辑')
      
      // 先关闭对话框
      dialogVisible.value = false
      
      // 显示成功消息
      ElMessage({
        type: 'success',
        message: dialogMode.value === 'add' ? '新增成功' : '修改成功'
      })
      
      // 如果是新增，创建管理员用户
      if (dialogMode.value === 'add') {
        await handleCreateAdminUser()
      } else {
        // 如果是修改，处理相关数据更新
        await handleUpdateRelatedData()
      }

      // 重新加载数据
      await loadTenants()
      
      console.log('✅ 成功处理完成')
    } else {
      console.log('❌ 判断为失败，进入失败处理逻辑')
      console.log('失败原因 - isOk:', response?.isOk)
      console.log('失败消息:', response?.msg || '操作失败')
      
      ElMessage({
        type: 'error',
        message: response?.msg || '操作失败'
      })
    }
    
    console.log('=== 租户修改调试信息结束 ===')
  } catch (error) {
    console.error('❌ 提交过程中发生异常:', error)
    
    const errorObj = error as any
    console.error('异常详情:', {
      message: errorObj?.message,
      stack: errorObj?.stack,
      response: errorObj?.response
    })
    
    ElMessage({
      type: 'error',
      message: '提交失败: ' + (errorObj?.message || '未知错误')
    })
  } finally {
    submitLoading.value = false
  }
}

// 创建管理员用户
const handleCreateAdminUser = async () => {
  try {
    await createAdminUser({
      name: tenantForm.admin,
      companyName: tenantForm.name,
      role: '2',
      pwd: tenantForm.sign
    })
  } catch (error) {
    console.error('创建管理员用户失败:', error)
    ElMessage.warning('租户创建成功，但管理员用户创建失败，请手动创建')
  }
}

// 处理修改时的相关数据更新
const handleUpdateRelatedData = async () => {
  if (!originalTenant.value) return

  try {
    const promises = []
    
    // 如果公司名称发生变化，更新部门和用户的公司名称
    if (originalTenant.value.name !== tenantForm.name) {
      promises.push(
        updateDepartmentName(originalTenant.value.name, tenantForm.name),
        updateUserName(originalTenant.value.name, tenantForm.name)
      )
    }
    
    // 如果管理员发生变化，更新管理员
    if (originalTenant.value.admin !== tenantForm.admin) {
      promises.push(
        updateAdmin(originalTenant.value.admin, tenantForm.admin)
      )
    }
    
    if (promises.length > 0) {
      await Promise.allSettled(promises)
    }
  } catch (error) {
    console.error('更新相关数据失败:', error)
    ElMessage.warning('租户修改成功，但部分相关数据更新失败')
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(tenantForm, {
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
  
  originalTenant.value = null
  
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 对话框关闭处理
const handleDialogClose = () => {
  resetForm()
}

// 格式化电话号码输入
const formatPhone = (value: string) => {
  tenantForm.tel = value.replace(/[^\d]/g, '')
}

// 图片相关方法
const getImageUrl = (url: string) => {
  if (!url) return ''
  return url.startsWith('http') ? url : `/api/${url}`
}

const handleImageError = () => {
  console.log('图片加载失败')
}

// 上传相关方法
const beforeUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response: any) => {
  if (response.resCode === '200') {
    tenantForm.coverUrl = response.ImageUrl
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

// 导入导出功能
const handleImport = () => {
  fileInputRef.value?.click()
}

const handleFileChange = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = e.target?.result
      const workbook = XLSX.read(data, { type: 'binary' })
      const firstSheetName = workbook.SheetNames[0]
      const worksheet = workbook.Sheets[firstSheetName]
      const jsonData = XLSX.utils.sheet_to_json(worksheet)

      processImportData(jsonData)
    } catch (error) {
      console.error('导入失败:', error)
      ElMessage.error('文件解析失败')
    }
  }
  reader.readAsBinaryString(file)
}

const processImportData = async (data: any[]) => {
  try {
    loading.value = true
    let successCount = 0
    let failCount = 0

    for (const item of data) {
      try {
        const tenantData: TenantInfo = {
          name: item.name || '',
          linkman: item.linkman || '',
          tel: item.tel || '',
          admin: item.admin || '',
          sign: item.sign || '',
          mail: item.mail || '',
          state: item.state || 1,
          desc: item.desc || '',
          coverUrl: ''
        }

        const response = await addTenant(tenantData)
        if (response.isOK) {
          successCount++
          // 为每个成功导入的租户创建管理员用户
          try {
            await createAdminUser({
              name: tenantData.admin,
              companyName: tenantData.name,
              role: '2',
              pwd: tenantData.sign
            })
          } catch (adminError) {
            console.error('创建管理员失败:', adminError)
          }
        } else {
          failCount++
        }
      } catch (error) {
        failCount++
      }
    }

    ElNotification.success({
      title: '导入完成',
      message: `成功导入 ${successCount} 条记录，失败 ${failCount} 条`
    })

    if (successCount > 0) {
      await loadTenants()
    }
  } catch (error) {
    console.error('批量导入失败:', error)
    ElMessage.error('批量导入失败')
  } finally {
    loading.value = false
    // 重置文件输入框
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

const handleExport = () => {
  try {
    const exportData = allTenants.value.map(item => ({
      公司名称: item.name,
      联系人: item.linkman,
      联系电话: item.tel,
      管理员: item.admin,
      公司标识: item.sign,
      邮箱: item.mail,
      状态: item.state === 1 ? '启用' : '禁用',
      描述: item.desc
    }))

    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '租户列表')
    
    const fileName = `租户列表_${new Date().getTime()}.xlsx`
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 初始化
onMounted(() => {
  console.log('组件已挂载，开始加载数据...')
  updateTableHeight()
  window.addEventListener('resize', updateTableHeight)
  loadTenants()
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('resize', updateTableHeight)
})
</script>

<style lang="scss" scoped>
.tenant-manager {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);

  .page-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 24px;
    color: white;

    .page-title {
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 12px;

      .title-icon {
        font-size: 28px;
      }
    }

    .page-description {
      margin: 0;
      opacity: 0.9;
      font-size: 14px;
    }
  }

  .search-container {
    margin-bottom: 24px;

    .search-card {
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

      :deep(.el-card__header) {
        background: #f8f9ff;
        border-bottom: 1px solid #e4e7ed;

        .search-header {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }

  .table-container {
    background: white;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    margin: 0 auto;
    width: 100%;
    max-width: 100%;
    overflow-x: auto;

    :deep(.el-table) {
      width: 100% !important;
      border-radius: 4px;
      overflow: hidden;

      th.el-table__cell {
        background-color: #f5f7fa;
        color: #606266;
        font-weight: 600;
        height: 45px;
      }

      .el-table__body-wrapper {
        overflow-x: auto;
      }
    }

    .logo-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 50px;

      .tenant-logo {
        width: 40px;
        height: 40px;
        border-radius: 4px;
        border: 1px solid #e4e7ed;
      }

      .no-image {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        color: #c0c4cc;
        font-size: 12px;

        .el-icon {
          font-size: 20px;
        }
      }
    }

    .operation-buttons {
      display: flex;
      justify-content: center;
      gap: 8px;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;

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
  }

  // 对话框样式
  :deep(.el-dialog) {
    border-radius: 12px;

    .el-dialog__header {
      background: #f8f9ff;
      border-bottom: 1px solid #e4e7ed;
      padding: 20px 24px;
    }

    .el-dialog__body {
      padding: 24px;
    }
  }

  // 图片上传样式
  .upload-container {
    display: flex;
    align-items: flex-start;
    gap: 16px;
  }

  .logo-uploader {
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    width: 150px;
    height: 150px;
    text-align: center;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &:hover {
      border-color: #409eff;
    }

    .logo-preview {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 6px;
    }

    .logo-uploader-icon {
      font-size: 32px;
      color: #8c939d;
    }
  }

  .upload-tips {
    display: flex;
    flex-direction: column;
    justify-content: center;
    color: #606266;
    margin-left: 8px;

    p {
      margin: 0;
      font-size: 14px;
      line-height: 1.6;

      &.tip-text {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }

  // 详情对话框样式
  .detail-container {
    .detail-logo {
      text-align: center;
      margin-bottom: 20px;

      .detail-logo-img {
        width: 120px;
        height: 120px;
        border-radius: 8px;
        border: 1px solid #e4e7ed;
        object-fit: cover;
      }

      .no-logo {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        width: 120px;
        height: 120px;
        margin: 0 auto;
        border: 2px dashed #d9d9d9;
        border-radius: 8px;
        color: #c0c4cc;
        font-size: 14px;

        .el-icon {
          font-size: 32px;
          margin-bottom: 8px;
        }
      }
    }

    .detail-info {
      :deep(.el-descriptions__cell) {
        padding: 12px 16px;
      }
    }
  }

  // 响应式设计
  @media (max-width: 768px) {
    padding: 12px;

    .table-container {
      padding: 12px;
      font-size: 12px;
    }

    .pagination {
      flex-direction: column;
      gap: 12px;
      align-items: center;

      .pagination-info {
        flex-direction: column;
        gap: 8px;
        align-items: center;
      }
    }

    // 移动端图片上传样式
    .upload-container {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }

    .logo-uploader {
      width: 120px;
      height: 120px;
    }

    .upload-tips {
      margin-left: 0;

      p {
        font-size: 12px;

        &.tip-text {
          font-size: 10px;
        }
      }
    }

    // 移动端详情对话框
    .detail-container .detail-logo {
      .detail-logo-img,
      .no-logo {
        width: 100px;
        height: 100px;
      }
    }
  }
}
</style>