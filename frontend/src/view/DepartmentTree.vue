<template>
  <!-- 外部添加部门按钮 -->
  <el-button type="primary" @click="openAddForm()" :loading="isLoadingData" style="margin-left: 12px; margin-bottom: 4px">添加</el-button>
  <!-- 外部添加部门表单对话框 -->
  <el-dialog v-model="addFormDialog" title="添加部门" width="500" @close="closeAdd">
    <el-form :model="departmentForm" :rules="departmentRules" ref="departmentFormRef">
      <el-form-item label="上级部门" prop="superior">
        <el-tree-select 
          v-model="value" 
          :data="dataSource" 
          check-strictly 
          :render-after-expand="false"
          node-key="value"
          :props="{
            value: 'value',
            label: 'label',
            children: 'children',
            disabled: 'disabled'
          }"
          placeholder="请选择上级部门"
          style="width: 240px; color: black;"/>
      </el-form-item>
      <el-form-item label="部门名称" prop="departmentName">
        <el-input v-model="departmentForm.departmentName" autocomplete="off"/>
      </el-form-item>
      <el-form-item label="显示排序" prop="order">
        <el-space>
          <el-input-number v-model="departmentForm.order" controls-position="right" style="padding: 0;">
            <template #decrease-icon>
              <el-icon>
                <Minus/>
              </el-icon>
            </template>
            <template #increase-icon>
              <el-icon>
                <Plus/>
              </el-icon>
            </template>
          </el-input-number>
        </el-space>
      </el-form-item>
      <el-form-item label="负责人" prop="director">
        <el-input v-model="departmentForm.director" autocomplete="off"/>
      </el-form-item>
      <el-form-item label="联系电话" prop="tel">
        <el-input v-model="departmentForm.tel" autocomplete="off"/>
      </el-form-item>
      <el-form-item label="邮箱" prop="mail">
        <el-input v-model="departmentForm.mail" autocomplete="off"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeAdd">取消</el-button>
        <el-button type="primary" @click="add">添加</el-button>
      </div>
    </template>
  </el-dialog>
  <!-- 部门树形列表展示 -->
  <div>
    <!-- 列表标题 -->
    <div class="title">
      <span class="titleElement">部门名称</span>
      <span class="titleElement">排序</span>
      <span class="titleElement">状态</span>
      <span class="titleElement">负责人</span>
      <span class="titleElement">操作</span>
    </div>
    <!-- 列表内容 -->
    <!-- 查找列表 -->
    <div v-if="props.search > 0 && (props.name || props.state === 0 || props.state === 1)">
      <div v-if="searchTable.length === 0" style="text-align: center; margin-top: 40px;">
        <el-empty description="暂无搜索结果" />
      </div>
      <div v-else>
        <el-table :data="pagedSearchTable" style="width: 100%;" :show-header="false">
          <el-table-column prop="departmentName" style="width: 303.47px;text-align: left"/>
          <el-table-column prop="order" style="width: 303.47px;"/>
          <el-table-column prop="state" style="width: 303.47px;">
            <template #default="scope">
              <el-text type="primary" v-if="scope.row.state === 1">正常</el-text>
              <el-text type="danger" v-if="scope.row.state === 0">停用</el-text>
            </template>
          </el-table-column>
          <el-table-column prop="director" style="width: 303.47px;"/>
          <el-table-column style="width: 303.47px;">
            <template #default="scope">
              <el-link @click="openSearchInformation(scope.row)" type="primary"
                       v-if="scope.row.departmentLevel >= 0"> 详情
              </el-link>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <div class="pagination" v-if="searchTable.length > 0">
        <div class="pagination-info">
          <span class="total-info">共 {{ searchTable.length }} 条记录</span>
          <div class="page-size-selector">
            <el-select v-model="searchPageSize" @change="handleSearchSizeChange" style="width: 120px;">
              <el-option
                v-for="size in [5, 10, 15, 20]"
                :key="size"
                :label="`${size}条/页`"
                :value="size"
              />
            </el-select>
          </div>
          <div class="page-jumper">
            <span>前往第</span>
            <el-input
              v-model="jumpPage"
              size="small"
              :placeholder="searchCurrentPage.toString()"
              style="width: 50px; margin: 0 8px; text-align: center;"
              @keyup.enter="handleJumpPage"
              @blur="handleJumpPage"
            />
            <span>页</span>
          </div>
        </div>
        <el-pagination
          v-model:current-page="searchCurrentPage"
          v-model:page-size="searchPageSize"
          :background="true"
          layout="prev, pager, next"
          :total="searchTable.length"
          :pager-count="7"
          :hide-on-single-page="false"
          @size-change="handleSearchSizeChange"
          @current-change="handleSearchPageChange"
        />
      </div>
      <el-dialog v-model="searchInformationDialog" title="部门详情" width="500" @close="closeInformation">
        <div style="display: flex; margin-top: 15px;">
          <div>
            <p style="margin-left: auto">所属公司：</p>
          </div>
          <div>
            <p>{{ searchInformation.companyName }}</p>
          </div>
          <div>
            <p style="margin-left: auto">部门名称：</p>
          </div>
          <div>
            <p>{{ searchInformation.departmentName }}</p>
          </div>
          <div>
            <p style="margin-left: auto">负责人：</p>
          </div>
          <div>
            <p>{{ searchInformation.director }}</p>
          </div>
          <div>
            <p style="margin-left: auto">状态：</p>
          </div>
          <div>
            <el-text type="primary" v-if="searchInformation.state === 1">正常</el-text>
            <el-text type="danger" v-if="searchInformation.state === 0">停用</el-text>
          </div>
          <div>
            <p style="margin-left: auto">联系电话：</p>
          </div>
          <div>
            <p>{{ searchInformation.tel }}</p>
          </div>
          <div>
            <p style="margin-left: auto">邮箱：</p>
          </div>
          <div>
            <p>{{ searchInformation.mail }}</p>
          </div>
        </div>
      </el-dialog>
    </div>
    <!-- 树形列表 -->
    <div v-else>
      <el-tree style="max-width: 100%; max-height: 600px; overflow-y: auto;" 
               :data="dataSource" 
               node-key="id"
               :expand-on-click-node="false"
               :lazy="false"
               :default-expanded-keys="[0]"
               accordion>
        <!-- 列表条目结构 -->
        <template #default="{ node, data }">
          <!-- 部门信息 -->
          <span class="custom-tree-node">
                        <!-- 部门属性 -->
                        <div style="text-align: left">
                            <span>{{ data.label }}</span>
                        </div>
                        <div style="width: 1213.88px; display: flex;">
                            <div class="treeAttribute">
                                <span>{{ data.order }}</span>
                            </div>
                            <div class="treeAttribute">
                                <el-text type="primary" v-if="data.state === 1">正常</el-text>
                                <el-text type="danger" v-if="data.state === 0">停用</el-text>
                            </div>
                            <div class="treeAttribute">
                                <span>{{ data.director }}</span>
                            </div>
                            <div class="treeAttribute">
                                <!-- 部门操作 -->
                                <span>
                                    <el-link @click="openAddByNodeForm(data)" type="primary" style="margin-right: 8px"
                                             v-if="data.departmentLevel >= 0"> 添加 </el-link>
                                    <el-link style="margin-right: 8px" @click="openModifyForm(node, data)" type="primary"
                                             v-if="data.departmentLevel > 0"> 修改 </el-link>
                                    <el-link style="margin-right: 8px" @click="remove(node, data)" type="danger"
                                             v-if="data.departmentLevel > 0"> 删除 </el-link>
                                </span>
                            </div>
                        </div>
                    </span>
        </template>
      </el-tree>
      <!-- 列表内部添加部门表单对话框 -->
      <el-dialog v-model="addByNodeFormDialog" title="添加部门" width="500" @close="closeAddByNode">
        <el-form :model="departmentForm" :rules="departmentRules" ref="departmentFormRef">
          <el-form-item label="部门名称" prop="departmentName">
            <el-input v-model="departmentForm.departmentName" autocomplete="off"/>
          </el-form-item>
          <el-form-item label="显示排序" prop="order">
            <el-space>
              <el-input-number v-model="departmentForm.order" controls-position="right"
                               style="padding: 0;">
                <template #decrease-icon>
                  <el-icon>
                    <Minus/>
                  </el-icon>
                </template>
                <template #increase-icon>
                  <el-icon>
                    <Plus/>
                  </el-icon>
                </template>
              </el-input-number>
            </el-space>
          </el-form-item>
          <el-form-item label="负责人" prop="director">
            <el-input v-model="departmentForm.director" autocomplete="off"/>
          </el-form-item>
          <el-form-item label="联系电话" prop="tel">
            <el-input v-model="departmentForm.tel" autocomplete="off"/>
          </el-form-item>
          <el-form-item label="邮箱" prop="mail">
            <el-input v-model="departmentForm.mail" autocomplete="off"/>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closeAddByNode">取消</el-button>
            <el-button type="primary" @click="addByNode()">添加</el-button>
          </div>
        </template>
      </el-dialog>
      <!-- 修改部门信息 -->
      <el-dialog v-model="modifyFormDialog" title="修改部门" width="500" @close="closeModify">
        <el-form :model="departmentForm" :rules="departmentRules" ref="departmentFormRef">
          <el-form-item label="上级部门" prop="superior">
            <el-tree-select 
              v-model="value" 
              :data="dataSource" 
              check-strictly 
              :render-after-expand="false"
              node-key="value"
              :props="{
                value: 'value',
                label: 'label',
                children: 'children',
                disabled: 'disabled'
              }"
              placeholder="请选择上级部门"
              style="width: 240px; color: black;"/>
          </el-form-item>
          <el-form-item label="部门名称" prop="departmentName">
            <el-input v-model="departmentForm.departmentName" autocomplete="off"/>
          </el-form-item>
          <el-form-item label="显示排序" prop="order">
            <el-space>
              <el-input-number v-model="departmentForm.order" controls-position="right"
                               style="padding: 0;">
                <template #decrease-icon>
                  <el-icon>
                    <Minus/>
                  </el-icon>
                </template>
                <template #increase-icon>
                  <el-icon>
                    <Plus/>
                  </el-icon>
                </template>
              </el-input-number>
            </el-space>
          </el-form-item>
          <el-form-item label="负责人" prop="director">
            <el-input v-model="departmentForm.director" autocomplete="off"/>
          </el-form-item>
          <el-form-item label="联系电话" prop="tel">
            <el-input v-model="departmentForm.tel" autocomplete="off"/>
          </el-form-item>
          <el-form-item label="邮箱" prop="mail">
            <el-input v-model="departmentForm.mail" autocomplete="off"/>
          </el-form-item>
          <el-form-item label="部门状态" prop="state">
            <el-radio-group v-model="departmentForm.state">
              <el-radio :label=1 style="padding-top: 13px;">正常</el-radio>
              <el-radio :label=0 style="padding-top: 13px;">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closeModify">取消</el-button>
            <el-button type="primary" @click="modify()">修改</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script lang="ts" setup name="DepartmentTree">
import router from '@/router';
import service from '@/utils/request';
import {ElMessage} from 'element-plus';
import {computed, onMounted, reactive, ref, watch} from 'vue';
import type Node from 'element-plus/es/components/tree/src/model/node'
import {Minus, Plus} from "@element-plus/icons-vue";

const role = ref('');
const companyName = ref('');
const addByNodeFormDialog = ref(false);
const modifyFormDialog = ref(false);
const addFormDialog = ref(false);
const searchInformationDialog = ref(false);
const departmentFormRef = ref();
const value = ref();
const addDepartmentId = ref();
const addDepartmentLevel = ref();
const addDepartmentCompany = ref();
const modifyDepartmentId = ref();
const isLoadingData = ref(false);
let companyValueCounter = -1; // 用于为公司节点分配负数value

const departmentForm = reactive({
  superior: 0,
  departmentName: '',
  order: 0,
  director: '',
  tel: '',
  mail: '',
  state: -1,
  departmentLevel: 0,
  companyName: '',
});

const departmentRules = ref({
  superior: [
    {required: true, message: '上级部门不能为空', trigger: 'blur'}
  ],
  departmentName: [
    {required: true, message: '部门名称不能为空', trigger: 'blur'},
    {min: 1, max: 50, message: '长度在1到50个字符', trigger: 'blur'}
  ],
  order: [
    {required: true, message: '显示排序不能为空', trigger: 'blur'}
  ],
  director: [
    {min: 0, max: 20, message: '长度在0到20个字符', trigger: 'blur'}
  ],
  tel: [
    {min: 0, max: 20, message: '长度在0到20个字符', trigger: 'blur'}
  ],
  mail: [
    {min: 0, max: 50, message: '长度在0到50个字符', trigger: 'blur'}
  ],
  state: [
    {required: true, message: '部门状态不能为空', trigger: 'blur'},
  ],
})

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
  // 不要在这里重置value，以免清空用户的选择
}

function loadAllData() {
  console.log('读取所有数据');
  service.post('http://localhost:8080/company/list').then((result: any) => {
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
          disabled: false, value: companyValueCounter--, id: id++, departmentId: 0, label: company.name, order: i,
          state: company.state, director: company.admin, departmentLevel: -1,
          companyName: company.name
        };
        append(dataSource.value[0], newChild);
        i++;
        const companyname = new FormData();
        companyname.append('name', company.name);
        service.post('http://localhost:8080/department/companydata', companyname).then((result: any) => {
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
                // 安全地查找上级部门
                const parentNodes = IdDFS(dataSource.value[0], department.superior);
                if (parentNodes && parentNodes.length > 0) {
                  append(parentNodes[0], newChild); // 将非1级部门读入树结点
                } else {
                  console.warn(`未找到上级部门 ID: ${department.superior}，部门: ${department.departmentName}`);
                  // 如果找不到上级部门，可能需要将其作为1级部门处理
                  if (dataSource.value[0].children) {
                    for (let company of dataSource.value[0].children) {
                      if (company.companyName == department.companyName) {
                        append(company, newChild);
                        break;
                      }
                    }
                  }
                }
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
  const storedCompanyName = sessionStorage.getItem('companyName');
  
  if (!storedCompanyName) {
    ElMessage.error('未找到企业信息，请重新登录');
    return;
  }
  
  companyName.value = storedCompanyName;
  console.log('企业管理员所属公司:', companyName.value);
  
  const companyname = new FormData();
  companyname.append('name', companyName.value);
  
  service.post('http://localhost:8080/company/find', companyname).then((result: any) => {
    console.log('公司查找结果:', result);
    if (result.isOk && result.company) {
      let company = result.company;
      
      // 安全地处理状态
      if (company.state == "1") {
        company.state = 1 as number;
      } else if (company.state == "0") {
        company.state = 0 as number;
      } else {
        company.state = -1 as number;
      }
      
      const newChild = {
        disabled: false, 
        value: companyValueCounter--, 
        id: id++, 
        departmentId: 0, 
        label: companyName.value, 
        order: 0,
        state: company.state, 
        director: company.admin, 
        departmentLevel: 0,
        companyName: companyName.value,
        children: [] // 确保有children数组
      }
      
      append(dataSource.value[0], newChild); // 将公司读入树结点
      console.log('公司节点添加完成:', newChild);
      
      // 加载该公司的部门数据
      service.post('http://localhost:8080/department/companydata', companyname).then((result: any) => {
        console.log('部门数据查询结果:', result);
        if (result.isOk && result.list) {
          console.log('部门列表:', result.list);
          let departments = result.list;
          
          if (departments.length === 0) {
            console.log('该公司暂无部门数据');
            ElMessage.info('该公司暂无部门数据，您可以开始创建部门');
            return;
          }
          
          // 按层级排序，确保上级部门先处理
          departments.sort((a: any, b: any) => a.departmentLevel - b.departmentLevel);
          
          for (let department of departments) {
            console.log('处理部门:', department);
            
            // 安全地处理状态
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
              companyName: department.companyName,
              children: []
            };
            
            if (department.departmentLevel == 1 && dataSource.value[0].children) { 
              // 将1级部门添加到公司节点下
              for (let company of dataSource.value[0].children) {
                if (company.companyName == department.companyName) {
                  append(company, newChild);
                  console.log('1级部门添加成功:', department.departmentName);
                  break;
                }
              }
            } else {
              // 安全地查找上级部门
              const parentNodes = IdDFS(dataSource.value[0], department.superior);
              if (parentNodes && parentNodes.length > 0) {
                append(parentNodes[0], newChild);
                console.log('非1级部门添加成功:', department.departmentName, '上级:', parentNodes[0].label);
              } else {
                console.warn(`未找到上级部门 ID: ${department.superior}，部门: ${department.departmentName}`);
                // 如果找不到上级部门，将其作为1级部门处理
                if (dataSource.value[0].children) {
                  for (let company of dataSource.value[0].children) {
                    if (company.companyName == department.companyName) {
                      newChild.departmentLevel = 1; // 修正为1级部门
                      append(company, newChild);
                      console.log('部门作为1级部门添加:', department.departmentName);
                      break;
                    }
                  }
                }
              }
            }
          }
          console.log('部门数据加载完成，最终树结构:', dataSource.value);
        } else {
          console.log('部门数据查询失败或为空');
          ElMessage.info('该公司暂无部门数据，您可以开始创建部门');
        }
      }).catch((error: any) => {
        console.error('部门数据查询错误:', error);
        ElMessage.error('加载部门数据失败，请稍后重试');
      });
    } else {
      console.error('公司查找失败:', result);
      ElMessage.error(result.msg || '未找到公司信息');
    }
  }).catch((error: any) => {
    console.error('公司查找错误:', error);
    ElMessage.error('加载公司信息失败，请稍后重试');
  });
}

// 查询参数
interface Props {
  name: string,
  state: number,
  search: number,
};

const props = defineProps<Props>();

watch([() => props.name, () => props.state, () => props.search], (newValue) => {
  searchTable.value = [];
  // 重置分页到第一页
  searchCurrentPage.value = 1;
  
  // 检查是否有搜索条件：搜索触发器有值且（有部门名称或状态为0或1）
  const hasSearchCondition = props.search > 0 && (props.name || props.state === 0 || props.state === 1);
  
  if (hasSearchCondition) {
    const searchName = newValue[0] || '';
    const searchState = newValue[1];
    
    console.log('开始搜索部门:', { departmentName: searchName, state: searchState });
    
    // 使用JSON格式发送搜索请求
    const searchData = {
      departmentName: searchName,
      state: searchState === -1 ? null : searchState
    };
    
    service.post('http://localhost:8080/department/search', searchData).then((result: any) => {
      console.log('搜索结果:', result);
      if (result.isOk) {
        let company = sessionStorage.getItem('companyName');
        
        for (let department of result.list) {
          const data: SearchData = {
            companyName: department.companyName,
            departmentId: department.departmentId,
            departmentLevel: department.departmentLevel,
            departmentName: department.departmentName,
            director: department.director,
            mail: department.mail,
            order: department.order,
            state: department.state,
            superior: department.superior,
            tel: department.tel,
          }
          if (role.value == '3') {
            searchTable.value.push(data);
          } else if (role.value == '2' || role.value == '1') {
            if (company == data.companyName) {
              searchTable.value.push(data);
            }
          }
        }
        console.log('过滤后的搜索结果:', searchTable.value);
      } else {
        ElMessage.error(result.msg || '搜索失败');
      }
    }).catch((error: any) => {
      console.error('搜索请求失败:', error);
      ElMessage.error('搜索请求失败，请稍后重试');
    });
  }
})


interface SearchData {
  companyName: string,
  departmentId: number,
  departmentLevel: number,
  departmentName: string,
  director: string,
  mail: string,
  order: number,
  state: number,
  superior: number,
  tel: string,
};

const searchTable = ref<SearchData[]>([]);
const searchInformation = ref();

// 搜索结果分页相关变量
const searchCurrentPage = ref(1);
const searchPageSize = ref(10);
const jumpPage = ref('');

// 搜索结果分页计算属性
const pagedSearchTable = computed(() => {
  const start = (searchCurrentPage.value - 1) * searchPageSize.value;
  const end = start + searchPageSize.value;
  return searchTable.value.slice(start, end);
});

// 搜索结果分页函数
const handleSearchPageChange = (page: number) => {
  searchCurrentPage.value = page;
};

const handleSearchSizeChange = (size: number) => {
  searchPageSize.value = size;
  searchCurrentPage.value = 1; // 改变每页条数时重置到第1页
};

const handleJumpPage = () => {
  if (!jumpPage.value || jumpPage.value.trim() === '') {
    return; // 空值时不处理
  }
  
  const page = parseInt(jumpPage.value.trim());
  const maxPage = Math.ceil(searchTable.value.length / searchPageSize.value);
  
  if (isNaN(page)) {
    ElMessage.warning('请输入数字');
    jumpPage.value = '';
    return;
  }
  
  if (page < 1) {
    ElMessage.warning('页码不能小于1');
    jumpPage.value = '';
    return;
  }
  
  if (page > maxPage) {
    ElMessage.warning(`页码不能大于${maxPage}`);
    jumpPage.value = '';
    return;
  }
  
  if (page === searchCurrentPage.value) {
    jumpPage.value = ''; // 如果是当前页，清空输入框
    return;
  }
  
  searchCurrentPage.value = page;
  jumpPage.value = '';
};

function openSearchInformation(data: any) {
  ElMessage.success('打开');
  searchInformation.value = data;
  searchInformationDialog.value = true;
}

function closeInformation() {
  searchInformationDialog.value = false;
  // searchInformation.value = null;
}

// 树
let id = 0;

interface Tree {
  value: number
  disabled: boolean,
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

const dataSource = ref<Tree[]>([
  {
    value: 0,
    disabled: false,
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
// 外部添加部门
// 打开外部添加部门对话框
function openAddForm() {
  console.log('打开添加部门表单');
  console.log('当前数据源:', dataSource.value);
  console.log('根节点子节点数量:', dataSource.value[0].children?.length || 0);
  
  // 检查是否有可选的部门数据
  const hasCompanyData = dataSource.value[0].children && dataSource.value[0].children.length > 0;
  
  if (!hasCompanyData) {
    console.log('数据源为空，重新加载数据');
    isLoadingData.value = true;
    ElMessage.info('正在加载部门数据...');
    
    // 清空现有数据源重新加载
    dataSource.value = [{
      value: 0,
      disabled: false,
      id: 0,
      departmentId: -1,
      label: '测盟会',
      order: 0,
      state: 1,
      director: '超级管理员',
      departmentLevel: -1,
      companyName: '',
      children: [],
    }];
    
    loadData();
    
    // 等待数据加载完成后再打开模态框
    setTimeout(() => {
      isLoadingData.value = false;
      console.log('数据加载完成，检查结果:', dataSource.value[0].children?.length || 0);
      
      if (dataSource.value[0].children && dataSource.value[0].children.length > 0) {
        console.log('有可选的部门数据，打开表单');
        value.value = null; // 重置选择
        addFormDialog.value = true;
      } else {
        console.log('仍然没有可选的部门数据');
        const userRole = sessionStorage.getItem('role');
        const companyName = sessionStorage.getItem('companyName');
        
        if (userRole === '2') {
          // 企业管理员，提示可以创建公司下的第一个部门
          ElMessage.warning(`您的公司"${companyName}"暂无部门数据。\n作为企业管理员，您需要先联系超级管理员创建公司节点，然后才能添加部门。`);
        } else {
          ElMessage.warning('暂无可选的上级部门数据，请稍后重试');
        }
      }
    }, 2000); // 增加等待时间
  } else {
    console.log('有可选的部门数据，直接打开表单');
    value.value = null; // 重置选择
    addFormDialog.value = true;
  }
}

// 添加部门
function add() {
  // 先进行表单验证
  if (!departmentFormRef.value) {
    ElMessage.error('表单引用错误');
    return;
  }
  
  departmentFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      ElMessage.error('请填写完整的部门信息');
      return;
    }
    
    if (value.value === null || value.value === undefined) {
      ElMessage.error('请选择上级部门');
      return;
    }
    
    submitDepartment();
  });
}

// 提交部门信息
function submitDepartment() {
  const departmentfd = new FormData();
  console.log('Selected value:', value.value);
  console.log('Form data:', departmentForm);
  
  // 根据选择的value查找对应的父节点
  let parentNode = ValueDFS(dataSource.value[0], value.value)[0];
  console.log('Parent node found:', parentNode);
  
  if (!parentNode) {
    ElMessage.error('未找到选择的上级部门');
    return;
  }
  
  let level = parentNode.departmentLevel + 1;
  let companyName = parentNode.companyName || parentNode.label; // 测盟会情况下使用label
  
  // 根据父节点的级别选择不同的API和参数
  if (parentNode.departmentLevel === -1) {
    // 选择了"测盟会"作为上级，应该添加公司
    departmentfd.append('name', departmentForm.departmentName);
    departmentfd.append('admin', departmentForm.director);
    departmentfd.append('tel', departmentForm.tel);
    departmentfd.append('mail', departmentForm.mail);
    departmentfd.append('order', departmentForm.order as unknown as string);
    departmentfd.append('state', '1'); // 默认状态为启用
    
    console.log('添加公司，提交数据:', {
      name: departmentForm.departmentName,
      admin: departmentForm.director,
      tel: departmentForm.tel,
      mail: departmentForm.mail,
      order: departmentForm.order
    });
    
    service.post('http://localhost:8080/company/add', departmentfd).then((result: any) => {
      if (result.isOK) {
        // 重置数据源
        dataSource.value = [{
          value: 0,
          disabled: false,
          id: 0,
          departmentId: -1,
          label: '测盟会',
          order: 0,
          state: 1,
          director: '超级管理员',
          departmentLevel: -1,
          companyName: '',
          children: [],
        }];
        
        loadAllData();
        closeAdd();
        ElMessage.success('添加公司成功');
      } else {
        ElMessage.error(result.msg);
      }
    }).catch((error: any) => {
      console.error('Submit error:', error);
      ElMessage.error('提交失败，请检查网络连接');
    });
  } else {
    // 添加部门
    departmentfd.append('superior', parentNode.departmentId as unknown as string);
    departmentfd.append('departmentName', departmentForm.departmentName);
    departmentfd.append('order', departmentForm.order as unknown as string);
    departmentfd.append('director', departmentForm.director);
    departmentfd.append('tel', departmentForm.tel);
    departmentfd.append('mail', departmentForm.mail);
    departmentfd.append('departmentLevel', level as unknown as string);
    departmentfd.append('companyName', companyName);
    
    console.log('添加部门，提交数据:', {
      superior: parentNode.departmentId,
      departmentName: departmentForm.departmentName,
      order: departmentForm.order,
      director: departmentForm.director,
      tel: departmentForm.tel,
      mail: departmentForm.mail,
      departmentLevel: level,
      companyName: companyName
    });
    
    service.post('http://localhost:8080/department/add', departmentfd).then((result: any) => {
      if (result.isOk) {
        // 重置数据源
        dataSource.value = [{
          value: 0,
          disabled: false,
          id: 0,
          departmentId: -1,
          label: '测盟会',
          order: 0,
          state: 1,
          director: '超级管理员',
          departmentLevel: -1,
          companyName: '',
          children: [],
        }];
        
        loadAllData();
        closeAdd();
        ElMessage.success('添加部门成功');
      } else {
        ElMessage.error(result.msg);
      }
    }).catch((error: any) => {
      console.error('Submit error:', error);
      ElMessage.error('提交失败，请检查网络连接');
    });
  }
}

// 关闭外部添加部门对话框
function closeAdd() {
  addFormDialog.value = false;
  if (departmentFormRef.value) {
    departmentFormRef.value.resetFields();
  }
  // 手动重置表单数据
  departmentForm.departmentName = '';
  departmentForm.order = 0;
  departmentForm.director = '';
  departmentForm.tel = '';
  departmentForm.mail = '';
  value.value = null;
}

// 列表内部添加部门
// 打开内部添加部门对话框
function openAddByNodeForm(data: Tree) {
  addDepartmentLevel.value = data.departmentLevel;
  addDepartmentId.value = data.departmentId;
  addDepartmentCompany.value = data.companyName;
  value.value = data.value;
  addByNodeFormDialog.value = true;
}

// 添加部门
function addByNode() {
  // 先进行表单验证
  if (!departmentFormRef.value) {
    ElMessage.error('表单引用错误');
    return;
  }
  
  departmentFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      ElMessage.error('请填写完整的部门信息');
      return;
    }
    
    let tree: Tree = ValueDFS(dataSource.value[0], value.value)[0];
    const departmentfd = new FormData();
    let level = tree.departmentLevel + 1;
    console.log(level);
    departmentfd.append('superior', addDepartmentId.value as unknown as string);
    departmentfd.append('departmentName', departmentForm.departmentName);
    departmentfd.append('order', departmentForm.order as unknown as string);
    departmentfd.append('director', departmentForm.director);
    departmentfd.append('tel', departmentForm.tel);
    departmentfd.append('mail', departmentForm.mail);
    departmentfd.append('departmentLevel', level as unknown as string);
    departmentfd.append('companyName', addDepartmentCompany.value);
    service.post('http://localhost:8080/department/add', departmentfd).then((result: any) => {
      if (result.isOk) {
        const newChild = {
          disabled: false,
          value: 0,
          id: 0,
          departmentId: result.departmentId,
          label: departmentForm.departmentName,
          order: departmentForm.order,
          state: 1,
          director: departmentForm.director,
          departmentLevel: tree.departmentLevel,
          companyName: departmentForm.companyName
        };
        append(tree, newChild);
        ElMessage.success(result.msg);
        
        // 重置数据源
        dataSource.value = [{
          value: 0,
          disabled: false,
          id: 0,
          departmentId: -1,
          label: '测盟会',
          order: 0,
          state: 1,
          director: '超级管理员',
          departmentLevel: -1,
          companyName: '',
          children: [],
        }];
        
        // 如果是添加公司级别的部门（level = 0），强制使用loadAllData
        if (level === 0) {
          loadAllData();
        } else {
          loadData();
        }
        
        closeAddByNode();
      } else {
        ElMessage.error(result.msg);
      }
    });
  });
}

// 关闭列表内部添加部门对话框
function closeAddByNode() {
  addByNodeFormDialog.value = false;
  if (departmentFormRef.value) {
    departmentFormRef.value.resetFields();
  }
  // 手动重置表单数据
  departmentForm.departmentName = '';
  departmentForm.order = 0;
  departmentForm.director = '';
  departmentForm.tel = '';
  departmentForm.mail = '';
  value.value = null;
}

// 添加树结点
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

// 删除部门
function remove(node: Node, data: Tree) {
  if (!confirm('您是否要删除该部门')) {
    return;
  }
  if (!confirm('删除该部门会连带删除该部门下所有部门，是否删除')) {
    return;
  }
  DFD(data);
  const parent = node.parent;
  const children: Tree[] = parent.data.children || parent.data;
  const index = children.findIndex((d) => d.id === data.id);
  children.splice(index, 1);
  dataSource.value = [...dataSource.value];
}

function DFD(tree: Tree) {
  const departmentfd = new FormData();
  departmentfd.append('departmentId', tree.departmentId as unknown as string)
  service.post('http://localhost:8080/department/delete', departmentfd).then((result: any) => {
    if (!result.isOk) {
      ElMessage.error(result.msg);
    }
    const Id=new FormData()
    Id.append('secId',tree.departmentId as unknown as string)
    service.post('http://localhost:8080/user/delUserBySecId',Id)
  })
  if (tree.children) {
    for (const child of tree.children) {
      DFD(child);
    }
  }
}
const oldsection=ref('')
// 打开修改对话框
async function openModifyForm(node: Node, data: Tree) {
  const parent = node.parent;
  let modifyfd = new FormData();
  modifyDepartmentId.value = data.departmentId;
  modifyfd.append('departmentId', modifyDepartmentId.value as unknown as string);
  service.post('http://localhost:8080/department/departmentdata', modifyfd).then((result: any) => {
    if (result.isOk) {
      oldsection.value=result.departmentName;
      departmentForm.departmentName = result.departmentName;
      departmentForm.order = result.order;
      departmentForm.director = result.director;
      departmentForm.tel = result.tel;
      departmentForm.mail = result.mail;
      departmentForm.state = result.state;
      dataSource.value = [{
        value: 0,
        disabled: false,
        id: 0,
        departmentId: -1,
        label: '测盟会',
        order: 0,
        state: 1,
        director: '超级管理员',
        departmentLevel: -1,
        companyName: '',
        children: [],
      }]
      loadData();
      setTimeout(() => {
        value.value = parent.data.value;
        const nodesToDisable = ValueDFS(dataSource.value[0], modifyDepartmentId.value);
        if (nodesToDisable && nodesToDisable.length > 0) {
          disabledDFS(nodesToDisable[0]);
        }
        modifyFormDialog.value = true;
      }, 500);
    } else {
      ElMessage.error(result.msg);
    }
  })
}

// 修改部门
function modify() {
  if (value.value === null || value.value === undefined) {
    ElMessage.error('请选择上级部门');
    return;
  }
  let superior = ValueDFS(dataSource.value[0], value.value)[0];
  if(!superior) {
    ElMessage.error('未找到指定的上级部门，请稍后重试');
    return;
  }
  let level = superior.departmentLevel + 1;
  const modifyfd = new FormData();
  modifyfd.append('departmentId', modifyDepartmentId.value as unknown as string);
  modifyfd.append('superior', superior.departmentId as unknown as string);
  modifyfd.append('departmentName', departmentForm.departmentName);
  modifyfd.append('order', departmentForm.order as unknown as string);
  modifyfd.append('director', departmentForm.director);
  modifyfd.append('tel', departmentForm.tel);
  modifyfd.append('mail', departmentForm.mail);
  modifyfd.append('state', departmentForm.state as unknown as string);
  modifyfd.append('departmentLevel', level as unknown as string);
  modifyfd.append('companyName', superior.companyName);
  service.post('http://localhost:8080/department/modify', modifyfd).then((result: any) => {
    if (result.isOk) {
      modifyUser(superior)
      closeModify();
      dataSource.value = [{
        value: 0,
        disabled: false,
        id: 0,
        departmentId: -1,
        label: '测盟会',
        order: 0,
        state: 1,
        director: '超级管理员',
        departmentLevel: -1,
        companyName: '',
        children: [],
      }]
      loadData();
      ElMessage.success(result.msg);
    } else {
      ElMessage.error(result.msg);
    }
  })
}
//修改用户
function modifyUser(superior:Tree){
  let fd=new FormData()
  fd.append('companyName',superior.companyName)
  fd.append('section',departmentForm.departmentName)
  fd.append('secId',modifyDepartmentId.value)
  service.post('http://localhost:8080/user/updateSection', fd)
}

// 关闭修改对话框
function closeModify() {
  modifyFormDialog.value = false;
  departmentFormRef.value.resetFields();
  departmentForm.departmentName = '';
  departmentForm.order = 0;
}

// 通过部门id寻找树结点
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

// 通过value寻找树结点
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

function disabledDFS(tree: Tree) {
  tree.disabled = true;
  if (tree.children) {
    for (const child of tree.children) {
      disabledDFS(child);
    }
  }
}

onMounted(() => {
  role.value = sessionStorage.getItem('role') as string;
  loadData();
  value.value = null;
})

</script>

<style scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  justify-content: space-between;
  text-align: center;
  font-size: 14px;
  padding-right: 8px;
  padding: 0;
}

.el-button {
  padding: 0px 15px;
}

:deep(.el-input-number.is-controls-right .el-input__wrapper) {
  padding: 0px;
  display: flex;
}

.title {
  color: gray;
  display: flex;
  background-color: #ffffff;
  height: 39.67px;
  border-bottom-style: solid;
  border-bottom-color: #f5f5f5;
}

.titleElement {
  flex: 1;
  text-align: left;
  padding: 8px 0;
  line-height: 23px;
}

.treeAttribute {
  flex: 1;
  text-align: left;
}

:deep(.el-table__header-wrapper) {
  height: 0;
}

:deep(* .el-table__cell) {
  text-align: left;
  padding: 0;
}

:deep(.el-tree-node__content) {
  height: 30px;
  border-bottom-style: solid;
  border-bottom-color: #f5f5f5;
}

/* 分页样式 */
.pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
}

.pagination-info {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  font-size: 14px;
  color: #606266;
  gap: 20px;
}

.total-info {
  font-weight: 500;
  color: #409eff;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-jumper {
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-jumper :deep(.el-input__wrapper) {
  justify-content: center;
}

.page-jumper :deep(.el-input__inner) {
  text-align: center !important;
}
</style>