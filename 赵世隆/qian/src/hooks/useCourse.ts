import { ref, reactive, computed, onMounted, watch } from "vue";
import axios from "axios";
import {
  ElMessage,
  ElTable,
  type FormInstance,
  type ElUpload,
  type UploadFile,
  ElMessageBox,
  ElLoading,
} from "element-plus";
import * as XLSX from "xlsx";
import { useTaskStore } from '@/stores/task';
import request from "@/utils/request";
import router from "@/router";
import { 
  getCourseList, 
  addCourse as apiAddCourse, 
  updateCourse as apiUpdateCourse, 
  deleteCourse as apiDeleteCourse, 
  publishCourse as apiPublishCourse, 
  approveCourse, 
  rejectCourse 
} from '@/api/course';

export interface Course {
  id: number;
  name: string;
  coverUrl: string;
  introduction: string;
  courseOrder: string;
  videoUrl: string;
  author: string;
  pass: string;
  status?: string;
  authorId?: number;
}

export default function () {
  const task = useTaskStore();
  const isAdmin = ref(false);
  const isLookPublish = ref(false);
  
  // 新增：审核视图状态和搜索参数
  const isAuditView = ref(false);
  const searchParams = ref({
    name: '',
    author: '',
    status: '',
    pageNum: 1,
    pageSize: 6,
    authorId: '', // 新增：用于权限过滤
  });

  // 新增：状态选项
  const statusOptions = ref([
    { value: 'PUBLISHED', label: '已发布' },
    { value: 'PENDING', label: '待审核' },
    { value: 'REVIEWING', label: '审核中' },
    { value: 'REJECTED', label: '已驳回' },
  ]);

  // 新增：多选功能
  const multipleSelection = ref<Course[]>([]);
  
  // 在组件挂载时初始化isAdmin
  onMounted(() => {
    const storedUserRole = sessionStorage.getItem("role");
    if (storedUserRole == null) {
      ElMessage.error('尚未登录，请先登录');
      router.push('/');
    } else {
      // 根据新的权限体系：只有超级管理员(role=3)才是admin
      isAdmin.value = storedUserRole === '3';
      
      console.log("=== 课程管理角色权限初始化 ===");
      console.log("用户角色:", storedUserRole);
      console.log("是否为超级管理员:", isAdmin.value);
      console.log("权限说明：超级管理员(3)=审核权限，企业管理员(2)+普通管理员(1)=发布权限");
      console.log("====================");
    }
    loadCourse();
  });

  // 新增：状态转换函数
  const convertOldStatusToNew = (oldStatus: string): string => {
    const statusMap: { [key: string]: string } = {
      '待审核': 'PENDING',
      '审核中': 'REVIEWING', 
      '通过': 'PUBLISHED',
      '不通过': 'REJECTED'
    };
    return statusMap[oldStatus] || 'PENDING';
  };

  const convertNewStatusToOld = (newStatus: string): string => {
    const statusMap: { [key: string]: string } = {
      'PENDING': '待审核',
      'REVIEWING': '审核中',
      'PUBLISHED': '通过',
      'REJECTED': '不通过'
    };
    return statusMap[newStatus] || '待审核';
  };

  // 新增：格式化状态显示
  const formatStatus = (status: string): string => {
    const statusMap: { [key: string]: string } = {
      PUBLISHED: '已发布',
      PENDING: '待审核',
      REVIEWING: '审核中',
      REJECTED: '已驳回',
      // 兼容旧状态
      '通过': '已发布',
      '待审核': '待审核',
      '审核中': '审核中',
      '不通过': '已驳回',
      // 新增的审核后状态
      '已发布': '已发布',
      '已驳回': '已驳回'
    };
    return statusMap[status] || '待审核';
  };

  // 新增：获取状态标签类型
  const getStatusTagType = (status: string): string => {
    const tagMap: { [key: string]: string } = {
      PUBLISHED: 'success',
      PENDING: 'warning', 
      REVIEWING: 'info',
      REJECTED: 'danger',
      // 兼容旧状态
      '通过': 'success',
      '待审核': 'warning',
      '审核中': 'info', 
      '不通过': 'danger',
      // 新增的审核后状态
      '已发布': 'success',
      '已驳回': 'danger'
    };
    return tagMap[status] || 'warning';
  };

  // 新增：统一获取课程状态的辅助函数，确保返回标准化的英文状态
  const getNormalizedStatus = (course: Course): string => {
    const statusMap: { [key: string]: string } = {
      // 标准英文状态
      'PENDING': 'PENDING',
      'REVIEWING': 'REVIEWING',
      'PUBLISHED': 'PUBLISHED',
      'REJECTED': 'REJECTED',
      // 后端可能返回的中文状态 (来自 status 或 pass 字段)
      '待审核': 'PENDING',
      '审核中': 'REVIEWING',
      '通过': 'PUBLISHED',
      '不通过': 'REJECTED',
      '草稿': 'PENDING', // 将"草稿"统一视为待发布状态
      '未发布': 'PENDING',  // 将"未发布"统一视为待发布状态
      // 新增的审核后状态
      '已发布': 'PUBLISHED',
      '已驳回': 'REJECTED'
    };
    // 优先检查 status 字段，其次是 pass 字段
    const currentStatusKey = course.status || course.pass;
    if (currentStatusKey && statusMap[currentStatusKey]) {
      return statusMap[currentStatusKey];
    }
    // 对于任何未知或空状态，提供一个安全的默认值
    return 'PENDING';
  };

  //加载课程
  const courses = ref([]);
  async function loadCourse() {
    try {
      const params = { ...searchParams.value };
      const currentUserId = sessionStorage.getItem('id');
      const currentUserRole = sessionStorage.getItem('role');
      
      if (isAuditView.value) {
        params.status = 'REVIEWING'; // 审核列表只看审核中的课程
      }
      
      // 权限控制：超级管理员可以看到所有课程，其他管理员只能看到自己创建的课程
      if (currentUserRole !== '3' && !isAuditView.value) {
        // 非超级管理员且非审核视图时，只加载自己创建的课程
        params.authorId = currentUserId || '';
        console.log(`=== 课程权限过滤 ===`);
        console.log(`当前用户ID: ${currentUserId}, 角色: ${currentUserRole}`);
        console.log(`过滤条件: 只显示作者ID为 ${currentUserId} 的课程`);
        console.log(`=================`);
      } else if (currentUserRole === '3') {
        console.log(`=== 课程权限过滤 ===`);
        console.log(`超级管理员(${currentUserId})，显示所有课程`);
        console.log(`=================`);
      }

      const response: any = await getCourseList(params);
      
      if (response.code === 200) {
        let coursesData = response.data.courses || [];
        
        // 如果后端没有按权限过滤，前端再次过滤（双重保险）
        if (currentUserRole !== '3' && !isAuditView.value) {
          const username = sessionStorage.getItem('username');
          console.log(`=== 权限过滤调试 ===`);
          console.log(`当前用户ID: ${currentUserId}, 用户名: ${username}`);
          console.log(`过滤前课程数量: ${coursesData.length}`);
          console.log('所有课程的作者信息:', coursesData.map((c: any) => ({
            id: c.id,
            name: c.name,
            author: c.author,
            authorId: c.authorId
          })));
          
          coursesData = coursesData.filter((course: any) => {
            const isOwner = course.authorId?.toString() === currentUserId || 
                           course.author === username;
            console.log(`课程[${course.name}] - 作者ID: ${course.authorId}, 作者名: ${course.author}, 是否拥有: ${isOwner}`);
            return isOwner;
          });
          console.log(`前端二次过滤后的课程数量: ${coursesData.length}`);
          console.log(`================`);
        }
        
        courses.value = coursesData.map((course: any) => ({
          ...course,
          // 使用新的、统一的状态获取函数
          status: getNormalizedStatus(course),
          // 保持 pass 字段的兼容性
          pass: course.pass || convertNewStatusToOld(course.status)
        }));
        
        ElMessage.success(response.msg);
      } else {
        ElMessage.error(response.msg || '加载课程失败');
      }
      
      console.log("课程加载响应:", {
        总课程数: courses.value.length,
        courses: courses.value.map((item: any) => ({
          id: item.id,
          name: item.name,
          authorId: item.authorId,
          author: item.author,
          status: item.status,
          pass: item.pass
        })),
        isAdmin: isAdmin.value,
        role: currentUserRole,
        userId: currentUserId
      });
      
    } catch (err) {
      console.error("加载课程失败", err);
      ElMessage.error("加载课程失败，请检查网络连接");
    }
  }

  // 新增：搜索功能
  const handleSearch = () => {
    searchParams.value.pageNum = 1;
    loadCourse();
  };

  const handleReset = () => {
    searchParams.value = {
      name: '',
      author: '',
      status: '',
      pageNum: 1,
      pageSize: 6,
      authorId: '', // 新增：保持类型一致
    };
    loadCourse();
  };

  // 新增：审核功能
  const handleViewAudit = () => {
    isAuditView.value = true;
    isLookPublish.value = true; // 保持与原有逻辑的兼容性
    loadCourse();
  };

  const handleBackToList = () => {
    isAuditView.value = false;
    isLookPublish.value = false; // 保持与原有逻辑的兼容性
    searchParams.value.status = '';
    loadCourse();
  };

  // 新增：发布功能（按照动态管理的逻辑）
  const handlePublish = async () => {
    if (multipleSelection.value.length === 0) {
      ElMessage.error('请选择要发布的课程');
      return;
    }

    // 检查选中的课程是否都是可发布状态（待审核或已驳回）
    const invalidItems = multipleSelection.value.filter(item => {
      const status = getNormalizedStatus(item);
      // 标准化后，只允许 PENDING 或 REJECTED 状态
      return !['PENDING', 'REJECTED'].includes(status);
    });
    
    if (invalidItems.length > 0) {
      ElMessage.error('只能发布状态为"待审核"或"已驳回"的课程');
      return;
    }

    // 检查选中的课程是否都是当前用户创建的
    const currentUserId = sessionStorage.getItem('id');
    const notOwnedItems = multipleSelection.value.filter(item => 
      item.authorId && item.authorId.toString() !== currentUserId
    );
    
    if (notOwnedItems.length > 0) {
      ElMessage.error(`您只能发布自己创建的课程。以下课程不是您创建的：${notOwnedItems.map(item => item.name).join('、')}`);
      return;
    }

    const idsToPublish = multipleSelection.value.map(item => item.id);
    const itemCount = idsToPublish.length;
    
    try {
      await ElMessageBox.confirm(`确定要提交${itemCount}个课程进行审核吗？提交后状态将变为"审核中"，等待超级管理员审核。`, '提交审核确认', {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning'
      });

      console.log('开始提交课程审核，ID列表:', idsToPublish);
      const results = await apiPublishCourse(idsToPublish);
      console.log('提交课程审核结果:', results);
      
      // 统一处理返回结果，无论是单个对象还是数组
      const resultsArray = Array.isArray(results) ? results : [results];
      const failures = resultsArray.filter(r => r.code !== 200);

      if (failures.length > 0) {
        const errorMsg = failures.map(f => f.msg).join(', ');
        ElMessage.error(`发布失败: ${errorMsg}`);
      } else {
        ElMessage.success(`已成功提交${itemCount}个课程进行审核`);
        loadCourse(); // 仅在成功时刷新列表
      }
    } catch (err) {
      if (err !== 'cancel') {
        console.error('发布失败的最终错误:', err);
        const errorMessage = (err as any)?.response?.data?.msg || (err as any)?.message || '提交审核失败，请稍后重试';
        ElMessage.error(errorMessage);
      }
    }
  };

  // 新增：选择变更处理
  const handleSelectionChange = (selection: Course[]) => {
    multipleSelection.value = selection;
    console.log('选中的课程数量:', selection.length);
    if (selection.length > 0) {
      console.log('选中的课程详情:', selection.map(item => ({
        id: item.id,
        name: item.name,
        authorId: item.authorId,
        author: item.author,
        status: item.status || convertOldStatusToNew(item.pass)
      })));
    }
  };

  // 新增：一个统一的函数来通过update接口更新课程状态
  const updateCourseStatus = async (courseId: number, newStatus: 'PUBLISHED' | 'REJECTED') => {
    // 从当前课程列表中找到完整的课程对象
    const courseToUpdate = courses.value.find(c => c.id === courseId);
    if (!courseToUpdate) {
      ElMessage.error("在当前列表中找不到该课程，无法更新状态");
      return;
    }

    const updatedData = {
      ...courseToUpdate,
      status: newStatus,
      pass: newStatus === 'PUBLISHED' ? '通过' : '不通过', // 保持旧字段兼容
    };
    
    try {
      const response = await apiUpdateCourse(updatedData);
      if (response.code === 200) {
        ElMessage.success(`课程状态已更新为 "${formatStatus(newStatus)}"`);
        loadPublish(); // 成功后刷新审核列表
      } else {
        ElMessage.error(response.msg || '状态更新失败');
      }
    } catch (error) {
      console.error("更新课程状态失败:", error);
      ElMessage.error("更新课程状态时发生错误");
    }
  };

  // 新增：审核通过
  const handleApprove = async (id: number) => {
    try {
      await ElMessageBox.confirm('确定通过该课程的审核吗？通过后将变为已发布状态。', '审核通过确认', {
        confirmButtonText: '确定通过',
        cancelButtonText: '取消',
        type: 'success'
      });
      
      await updateCourseStatus(id, 'PUBLISHED');
    } catch (error) {
      if (error !== 'cancel') {
        console.error('审核通过失败:', error);
        ElMessage.error('操作失败');
      }
    }
  };

  // 新增：审核驳回
  const handleReject = async (id: number) => {
    try {
      await ElMessageBox.confirm('确定驳回该课程吗？驳回后将变为已驳回状态，作者可以修改后重新发布。', '审核驳回确认', {
        confirmButtonText: '确定驳回',
        cancelButtonText: '取消',
        type: 'warning'
      });
      
      await updateCourseStatus(id, 'REJECTED');
    } catch (error) {
      if (error !== 'cancel') {
        console.error('驳回失败:', error);
        ElMessage.error('操作失败');
      }
    }
  };

  // 新增：权限检查函数
  const canEditCourse = (course: Course): boolean => {
    const currentUserId = sessionStorage.getItem('id');
    const currentUserRole = sessionStorage.getItem('role');
    
    // 超级管理员可以编辑所有课程
    if (currentUserRole === '3') {
      return true;
    }
    
    // 企业管理员(role=2)和普通管理员(role=1)只能编辑自己创建的课程
    return course.authorId?.toString() === currentUserId;
  };

  const canDeleteCourse = (course: Course): boolean => {
    const currentUserId = sessionStorage.getItem('id');
    const currentUserRole = sessionStorage.getItem('role');
    
    // 超级管理员可以删除所有课程
    if (currentUserRole === '3') {
      return true;
    }
    
    // 企业管理员(role=2)和普通管理员(role=1)只能删除自己创建的课程
    return course.authorId?.toString() === currentUserId;
  };

  //添加课程
  const courseRef = ref<FormInstance>();
  const dialogVisible = ref(false);
  const dialogFormVisible = ref(false);
  const course = reactive({
    id: "",
    name: "",
    coverUrl: "",
    introduction: "",
    courseOrder: "",
    videoUrl: "",
    author: "",
    pass: "",
  });
  function openAdd() {
    dialogFormVisible.value = true;
    // 自动设置作者为当前用户 - 注释掉此逻辑，改为手动输入
    // const username = sessionStorage.getItem('username');
    // if (username) {
    //   course.author = username;
    // }
  }
  const rules = reactive({
    name: [{ required: true, message: "课程名称不能为空", trigger: "blur" }],
    introduction: [
      { required: true, message: "课程简介不能为空", trigger: "blur" },
    ],
    courseOrder: [
      { required: true, message: "课程排序不能为空", trigger: "blur" },
    ],
    author: [{ required: true, message: "课程作者不能为空", trigger: "blur" }],
  });
  function close() {
    reset1();
    reset2();
    course.name = "";
    course.coverUrl = "";
    course.introduction = "";
    course.courseOrder = "";
    course.videoUrl = "";
    course.author = "";
  }
  const course2 = reactive({
    id: "",
    name: "",
    coverUrl: "",
    introduction: "",
    courseOrder: "",
    videoUrl: "",
    author: "",
  });
  function close2() {
    course2.name = "";
    course2.coverUrl = "";
    course2.introduction = "";
    course2.courseOrder = "";
    course2.videoUrl = "";
    course2.author = "";
  }
  function back() {
    fileList.value=[];
    dialogFormVisible.value = false;
    dialogVisible.value = false;
    reset1();
    reset2();
    course.name = "";
    course.coverUrl = "";
    course.introduction = "";
    course.courseOrder = "";
    course.videoUrl = "";
    course.author = "";
  }
  const addCourse = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async (valid) => {
      if (valid) {
        try {
          // 验证必需字段
          if (!course.name || !course.introduction || !course.courseOrder || !course.author) {
            ElMessage.error("请填写完整的课程信息");
            return;
          }

          // 显示添加进度提示
          const loading = ElLoading.service({
            lock: true,
            text: '正在添加课程...',
            background: 'rgba(0, 0, 0, 0.7)',
          });

          console.log("=== 添加课程调试信息 ===");
          console.log("当前用户信息:", {
            username: sessionStorage.getItem('username'),
            userId: sessionStorage.getItem('id'),
            role: sessionStorage.getItem('role')
          });
          console.log("准备添加课程，数据:", {
            name: course.name,
            coverUrl: course.coverUrl,
            introduction: course.introduction,
            courseOrder: course.courseOrder,
            author: course.author,
            videoUrl: course.videoUrl
          });

          // 使用新的API，确保状态为待审核
          const courseData = {
            name: course.name,
            coverUrl: course.coverUrl || "",
            introduction: course.introduction,
            courseOrder: course.courseOrder,
            author: course.author,
            videoUrl: course.videoUrl || "",
            // 根据新需求：所有管理员创建的课程都为待审核状态
            status: 'PENDING',
            pass: '待审核'
          };
          
          console.log("发送给API的数据:", courseData);
          console.log("====================");
          
          const response: any = await apiAddCourse(courseData);

          loading.close();
          
          console.log("=== 添加课程API响应 ===");
          console.log("响应类型:", typeof response);
          console.log("完整响应:", response);
          console.log("响应的所有属性:", Object.keys(response || {}));
          if (response) {
            console.log("isOk:", response.isOk);
            console.log("msg:", response.msg);
            console.log("data:", response.data);
            console.log("code:", response.code);
          }
          console.log("===================");
          
          // 适配不同的后端响应格式
          const isSuccess = response.isOk === true || response.code === 200 || response.code === "200";
          
          if (isSuccess) {
            ElMessage.success(`课程"${course.name}"添加成功，状态为待审核！`);
            back();
            // 重新加载课程列表
            await loadCourse();
            // 重置分页到第一页以显示新添加的课程
            currentPage.value = 1;
          } else {
            // 后端返回500错误的特殊处理
            if (response.code === 500) {
              ElMessage.error("服务器内部错误，请联系管理员或稍后重试");
              console.error("后端500错误详情:", {
                url: '/course/add',
                method: 'POST',
                requestData: courseData,
                responseCode: response.code,
                responseMsg: response.msg
              });
            } else {
              ElMessage.error(response.msg || "添加课程失败");
            }
            console.error("添加课程失败，服务器响应:", response);
          }
        } catch (err: any) {
          console.error("添加课程失败，详细错误:", err);
          
          // 更详细的错误处理
          let errorMessage = "添加课程失败";
          if (err.message) {
            if (err.message.includes("系统繁忙")) {
              errorMessage = "服务器繁忙，请稍后再试";
            } else if (err.message.includes("Network Error")) {
              errorMessage = "网络连接失败，请检查网络";
            } else {
              errorMessage = `添加失败：${err.message}`;
            }
          }
          
          ElMessage.error(errorMessage);
        }
      } else {
        ElMessage.error("请填写完整的课程信息");
      }
    });
  };
  //更新课程
  const fileList = ref<Array<{ name: string; url: string }>>([])
  const courseId = ref("");
  function openForm(row: any) {
    console.log("打开修改表单，原始数据:", row);
    
    courseId.value = row.id;
    
    // 确保所有字段都正确赋值
    course.id = row.id;
    course.name = row.name || "";
    course.coverUrl = row.coverUrl || "";
    course.introduction = row.introduction || "";
    course.courseOrder = row.courseOrder || "";
    course.videoUrl = row.videoUrl || "";
    course.author = row.author || "";
    course.pass = row.pass || "";
    
    console.log("赋值后的course对象:", {
      id: course.id,
      name: course.name,
      coverUrl: course.coverUrl,
      introduction: course.introduction,
      courseOrder: course.courseOrder,
      videoUrl: course.videoUrl,
      author: course.author,
      pass: course.pass
    });
    
    // 处理文件列表
    if (course.coverUrl) {
      fileList.value = [{
        name: '课程图标',
        url: course.coverUrl,
      }];
    } else {
      fileList.value = [];
    }
    
    // 处理视频状态
    if (course.videoUrl) {
      plus.value = false;
      videoFlag.value = false;
    } else {
      plus.value = true;
      videoFlag.value = false;
    }
    
    dialogVisible.value = true;
  }
  const update = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async (valid) => {
      if (valid) {
        try {
          console.log("准备修改课程，当前用户信息:", {
            role: sessionStorage.getItem("role"),
            isAdmin: isAdmin.value
          });

          const formData = {
            id: course.id,
            name: course.name,
            coverUrl: course.coverUrl || "",
            introduction: course.introduction,
            courseOrder: course.courseOrder,
            videoUrl: course.videoUrl || "",
            author: course.author, // 不再使用sessionStorage中的用户名，而是表单中的值
            // 如果是普通管理员修改课程，修改后状态变为"待审核"
            pass: !isAdmin.value ? "待审核" : course.pass
          };

          console.log("FormData内容:", formData);

          const updateResponse: any = await request({
            url: '/course/update',
            method: 'post',
            data: formData,
            withCredentials: true,
            headers: {
              'Content-Type': 'application/json'
            }
          });

          if (updateResponse.code === 200) {
            ElMessage.success("修改成功");
            dialogVisible.value = false;
            await loadCourse();
          } else {
            ElMessage.error(updateResponse.msg || "修改失败");
          }
        } catch (err: any) {
          console.error("修改课程失败:", err);
          ElMessage.error(err.message || "修改失败，请重试");
        }
      } else {
        console.log("表单验证失败");
        ElMessage.error("请填写完整的课程信息");
      }
    });
  };
  //分页
  const currentPage = ref(1);
  const pageSize = ref(10);
  const multipleTableRef = ref<InstanceType<typeof ElTable>>();

  const handlePageChange = (page: number) => {
    currentPage.value = page;
  };
  const pagedCourse = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    return courses.value.slice(start, end);
  });

  //上传图片
  const dialogImageUrl = ref("");
  const dialogVisible1 = ref(false);
  function beforeUploadImage(file: any) {
    console.log("file.size", file.size);
    //文件大小
    const isLt5M = file.size / 1024 / 1024 < 5;
    //视频后缀检查
    if (["image/jpeg", "image/png", "image/jpg"].indexOf(file.type) === -1) {
      ElMessage.error("请上传正确的图片格式");
      return false;
    }
    if (!isLt5M) {
      ElMessage.error("上传视频大小不能超过5MB哦!");
      return false;
    }
  }

  const UploadImage = (res: any, file: any) => {
    if (res.resCode === "200") {
      course.coverUrl = res.ImageUrl;
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
  };
  const uploadImageRef = ref<InstanceType<typeof ElUpload> | null>(null);
  function reset1() {
    uploadImageRef.value!.clearFiles(); //清除文件列表
  }

  // 上传视频
  const videoFlag = ref(false);
  const plus = ref(true);
  const videoUploadPercent = ref(0);
  // 视频上传前执行
  function beforeUploadVideo(file: any) {
    console.log("videobefore");
    //文件大小
    const isLt100M = file.size / 1024 / 1024 < 100;
    //视频后缀检查
    if (["video/mp4"].indexOf(file.type) === -1) {
      ElMessage.error("请上传正确的视频格式");
      return false;
    }
    if (!isLt100M) {
      ElMessage.error("上传视频大小不能超过100MB哦!");
      return false;
    }
  }

  // 视频上传过程中执行
  function uploadVideoProcess(event: any, file: any, fileList: any) {
    plus.value = false;
    videoFlag.value = true;
    videoUploadPercent.value = +file.percentage.toFixed(0);
  }
  // 视频上传成功时执行
  function handleVideoSuccess(res: any, file: any) {
    plus.value = false;
    videoUploadPercent.value = 100;
    console.log("视频上传响应:", res);
    console.log("上传的文件信息:", file);
    
    // 如果为200代表视频保存成功
    if (res.resCode === "200") {
      // 接收视频传回来的名称和保存地址
      let videoUrl = res.VideoUrl;
      
      // 确保视频URL格式正确
      if (videoUrl) {
        // 如果是相对路径，转换为绝对路径
        if (!videoUrl.startsWith('http')) {
          // 假设后端返回的是相对路径，需要加上服务器地址
          videoUrl = `http://localhost:8080${videoUrl.startsWith('/') ? '' : '/'}${videoUrl}`;
        }
        
        console.log("处理后的视频URL:", videoUrl);
        course.videoUrl = videoUrl;
        
        ElMessage.success("视频上传成功！");
        
        // 验证视频URL是否可访问
        const testVideo = document.createElement('video');
        testVideo.onloadedmetadata = () => {
          console.log("视频URL验证成功，可以播放");
          ElMessage.success("视频文件验证成功，可以正常播放");
        };
        testVideo.onerror = (e) => {
          console.error("视频URL验证失败:", e);
          ElMessage.warning("视频上传成功，但预览可能有问题，请检查文件格式");
        };
        testVideo.src = videoUrl;
      } else {
        console.error("服务器未返回视频URL");
        ElMessage.error("视频上传成功，但未获取到文件地址");
      }
    } else {
      console.error("视频上传失败，服务器响应:", res);
      ElMessage.error(res.message || "视频上传失败，请重新上传！");
    }
  }
  const uploadVideoRef = ref<InstanceType<typeof ElUpload> | null>(null);
  function reset2() {
    videoFlag.value = false;
    plus.value = true;
    videoUploadPercent.value = 0;
    // uploadVideoRef.value!.clearFiles(); //清除文件列表
  }
  // 删除课程
  async function delCourse(item?: Course) {
    try {
      let idsToDelete: number[] = [];
      let confirmMessage = '';

      if (item && item.id) {
        // 单个删除 - 直接删除指定课程
        idsToDelete = [item.id];
        confirmMessage = `确定要删除课程 "${item.name}" 吗?`;
      } else {
        // 批量删除 - 删除选中的课程
        if (multipleSelection.value.length === 0) {
          ElMessage.warning("请先选择要删除的课程");
          return;
        }
        idsToDelete = multipleSelection.value.map(course => course.id);
        confirmMessage = `确定要删除选中的 ${idsToDelete.length} 门课程吗?`;
      }

      await ElMessageBox.confirm(confirmMessage, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      });
      
      const loading = ElLoading.service({
        lock: true,
        text: '正在删除中...',
        background: 'rgba(0, 0, 0, 0.7)',
      });

      try {
        const response = await apiDeleteCourse(idsToDelete);
        if (response.code === 200) {
          ElMessage.success("删除成功");
          await loadCourse();
        } else {
          // Promise.all 在 api/course.ts 中已经处理了部分失败的情况
          // 如果代码能到这里，通常意味着所有请求都失败了，或者有其他非200的响应
          ElMessage.error(response.msg || "删除失败");
        }
      } catch (error) {
        console.error("删除操作失败:", error);
        const errorMessage = error instanceof Error ? error.message : "删除操作失败";
        ElMessage.error(errorMessage);
      } finally {
        loading.close();
      }
    } catch (error) {
      if (error !== 'cancel') {
        // 这个catch处理MessageBox的取消操作，以及其他意外错误
        console.log("操作已取消或出现预料之外的错误");
      }
    }
  }
  //搜索课程
  const searchCourse = ref({
    name: "",
    courseOrder: "",
  });

  async function search() {
    const { name, courseOrder } = searchCourse.value;

    // 如果所有搜索条件都为空，显示全部课程数据
    if (!name && !courseOrder) {
      ElMessage.info("搜索条件为空，显示全部课程数据");
      loadCourse();
      return;
    }

    try {
      // 显示搜索进度提示
      const loading = ElLoading.service({
        lock: true,
        text: '正在搜索课程...',
        background: 'rgba(0, 0, 0, 0.7)',
      });

      // 使用统一的request工具来避免CORS问题
      const response: any = await request({
        url: '/course/search',
        method: 'post',
        data: searchCourse.value,
        headers: {
          'Content-Type': 'application/json'
        }
      });

      loading.close();

      if (response.isOk) {
        courses.value = response.courses;
        const resultCount = response.courses ? response.courses.length : 0;
        
        // 构建搜索条件描述
        const searchConditions = [];
        if (name) searchConditions.push(`课程名称包含"${name}"`);
        if (courseOrder) searchConditions.push(`课程排序为"${courseOrder}"`);
        
        const conditionText = searchConditions.join('，');
        ElMessage.success(`搜索完成！找到${resultCount}个符合条件的课程（${conditionText}）`);
        
        // 重置分页到第一页
        currentPage.value = 1;
      } else {
        ElMessage.error(response.msg || "搜索失败");
      }
    } catch (err) {
      console.error("搜索课程失败", err);
      ElMessage.error("搜索请求失败，请检查网络连接");
    }
  }
  function reset() {
    searchCourse.value = {
      name: "",
      courseOrder: "",
    };
    loadCourse();
  }
  // 结束上传视频
  // 查看详情
  const srcList = ref([]);
  const detailVisiable = ref(false);
  function lookDetail(row: any) {
    detailVisiable.value = true;
    Object.assign(course2, row);
  }
  //导出数据
  function exportData() {
    if (confirm("是否导出课程数据？")) {
      if (!isLookPublish.value) {
        const ws = XLSX.utils.json_to_sheet(courses.value);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "courses");
        XLSX.writeFile(wb, "courses.xlsx");
      } else {
        const ws = XLSX.utils.json_to_sheet(courses2.value);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "courses");
        XLSX.writeFile(wb, "courses.xlsx");
      }
    }
  }
  //修改
  const isDisabled = ref(false);
  watch(multipleSelection, (newSelection) => {
    if (newSelection.length > 1) {
      isDisabled.value = true;
    } else {
      isDisabled.value = false;
    }
  });
  function modify() {
    if (multipleSelection.value.length === 0) {
      ElMessage.error("请选择要修改的课程");
      return;
    }
    const obj = multipleSelection.value[0];

    Object.assign(course, obj);

    dialogVisible.value = true;
  }
  // 管理员审核租户发布的课程
  const courses2 = ref([]);
  async function publish() {
    if (multipleSelection.value.length === 0) {
      ElMessage.error("请选择要发布的课程");
      return;
    }
    const statues = multipleSelection.value.map((item) => item.pass);
    if (statues.includes("通过")||statues.includes("审核中")) {
      ElMessage.error("选中课程包括已通过审核或正在审核中的课程，请重新选择");
      return;
    }
    const coursess = multipleSelection.value.map((item) => ({
      id: item.id,
      name: item.name,
      introduction: item.introduction,
      author: item.author,
    }));
    
    try {
      await ElMessageBox.confirm(
        "确定要发布选中的课程吗？发布后课程状态将变为审核中，等待超级管理员审核。",
        '发布确认',
        {
          confirmButtonText: '确定发布',
          cancelButtonText: '取消',
          type: 'warning'
        }
      );
      
      const response: any = await request({
        url: '/course/publish',
        method: 'post',
        data: coursess,
        headers: {
          "Content-Type": "application/json",
        }
      });
      
      if (response.isOk) {
        ElMessage.success(`${multipleSelection.value.length}个课程已发布，等待超级管理员审核`);
        loadCourse();
      } else {
        ElMessage.error(response.msg);
      }
    } catch (err) {
      if (err !== 'cancel') {
        console.error("发布课程失败", err);
        ElMessage.error("发布课程失败，请检查网络连接");
      }
    }
  }
  async function loadPublish() {
    const loading = ElLoading.service({
      lock: true,
      text: '正在加载审核列表...',
      background: 'rgba(0, 0, 0, 0.7)',
    });
    try {
      console.log("准备加载审核列表...");
      // 不再使用专门的/audit-list接口，而是复用主列表接口，确保数据源统一
      // getCourseList 会自动处理管理员权限，获取所有课程
      const response: any = await getCourseList({ pageNum: 1, pageSize: 999 }); // 获取所有数据用于前端过滤
      
      console.log("审核列表请求响应 (来自/course/list):", response);
      
      if (response.code === 200 && response.data) {
        const list1 = response.data.courses || [];
        const list2 = response.data.courses2 || [];
        
        // 修复重复问题：优先使用courses2，如果没有则使用courses，避免重复合并
        let allCourses: any[] = [];
        if (list2.length > 0) {
          // 如果courses2有数据，说明是审核相关的数据，优先使用
          console.log("使用courses2数据源，避免重复:", list2.length);
          allCourses = list2;
        } else {
          // 否则使用courses
          console.log("使用courses数据源:", list1.length);
          allCourses = list1;
        }

        // 使用统一的状态获取函数进行精确过滤，只显示"审核中"的课程
        courses.value = allCourses.filter((course: any) => {
          const normalizedStatus = getNormalizedStatus(course);
          console.log(`课程 [${course.name}] 的标准化状态为: ${normalizedStatus}`);
          return normalizedStatus === 'REVIEWING';
        });
        task.updateNum(courses.value.length);
        ElMessage.success(`加载审核列表成功，共${courses.value.length}个待审核课程`);
      } else {
        ElMessage.error(response.msg || "加载审核列表失败");
      }
    } catch (err: any) {
      console.error("加载审核列表失败", err);
      ElMessage.error(err.message || "加载审核列表失败，请检查网络连接");
    } finally {
      loading.close();
    }
  }
  function lookPublish() {
    isLookPublish.value = true;
    isAuditView.value = true; // 进入审核模式，同步设置审核视图状态
    loadPublish();
  }
  async function passes(row: any) {
    let loading: any = null;
    let auditSuccess = false;
    
    try {
      console.log("准备审核通过课程:", row);
      
      loading = ElLoading.service({
        lock: true,
        text: '正在审核课程...',
        background: 'rgba(0, 0, 0, 0.7)',
      });

      // 使用updateCourse接口，直接修改课程状态为"通过"
      const updateData = {
        id: row.id,
        name: row.name,
        coverUrl: row.coverUrl,
        introduction: row.introduction,
        courseOrder: row.courseOrder,
        author: row.author,
        videoUrl: row.videoUrl,
        authorId: row.authorId,
        status: "已发布", // 审核通过后设置为已发布状态
        pass: "通过"      // 兼容字段
      };

      console.log("审核通过数据:", updateData);

      const response: any = await apiUpdateCourse(updateData);

      console.log("审核通过请求完成，收到响应:", response);

      if (response.code === 200) {
        ElMessage.success(`课程"${row.name}"审核通过！`);
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
          // 审核完成后，先刷新审核列表移除已审核的课程
          await loadPublish();
          
          // 延迟跳转回课程首页，确保状态更新
          setTimeout(async () => {
            backToList(); // 跳转回课程首页
            // 刷新课程首页数据，确保状态实时更新
            await loadCourse();
          }, 800);
        } catch (refreshErr) {
          console.error("刷新列表失败:", refreshErr);
          ElMessage.warning("审核成功，但刷新列表失败，请手动刷新");
          // 即使刷新失败也要跳转回首页
          setTimeout(() => {
            backToList();
          }, 1000);
        }
      }
    }
  }
  
  async function notPass(row: any) {
    let loading: any = null;
    let auditSuccess = false;
    
    try {
      console.log("准备审核不通过课程:", row);
      
      loading = ElLoading.service({
        lock: true,
        text: '正在审核课程...',
        background: 'rgba(0, 0, 0, 0.7)',
      });

      // 使用updateCourse接口，直接修改课程状态为"不通过"
      const updateData = {
        id: row.id,
        name: row.name,
        coverUrl: row.coverUrl,
        introduction: row.introduction,
        courseOrder: row.courseOrder,
        author: row.author,
        videoUrl: row.videoUrl,
        authorId: row.authorId,
        status: "已驳回", // 审核不通过后设置为已驳回状态
        pass: "不通过"    // 兼容字段
      };

      console.log("审核不通过数据:", updateData);

      const response: any = await apiUpdateCourse(updateData);

      console.log("审核不通过请求完成，收到响应:", response);

      if (response.code === 200) {
        ElMessage.success(`课程"${row.name}"审核不通过！`);
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
          // 审核完成后，先刷新审核列表移除已审核的课程
          await loadPublish();
          
          // 延迟跳转回课程首页，确保状态更新
          setTimeout(async () => {
            backToList(); // 跳转回课程首页
            // 刷新课程首页数据，确保状态实时更新
            await loadCourse();
          }, 800);
        } catch (refreshErr) {
          console.error("刷新列表失败:", refreshErr);
          ElMessage.warning("审核成功，但刷新列表失败，请手动刷新");
          // 即使刷新失败也要跳转回首页
          setTimeout(() => {
            backToList();
          }, 1000);
        }
      }
    }
  }
  function backToList() {
    isLookPublish.value = false;
    isAuditView.value = false; // 返回时，重置审核视图状态
    loadCourse();
  }
  const cellStyle = ({
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
    if (column.property == "pass") {
      // 审核状态的颜色样式
      if (row.pass == "通过") {
        return { color: "#67c23a", fontWeight: "bold" }; // 绿色
      } else if (row.pass == "不通过") {
        return { color: "#f56c6c", fontWeight: "bold" }; // 红色
      } else if (row.pass == "审核中") {
        return { color: "#e6a23c", fontWeight: "bold" }; // 橙色
      } else {
        return { color: "#909399", fontWeight: "bold" }; // 灰色（待审核）
      }
    }
    
    // 课程排序列居中显示
    if (column.property == "courseOrder") {
      return { textAlign: "center", fontWeight: "500" };
    }
  };
  // 视频处理函数
  function handleVideoError(event: Event) {
    const video = event.target as HTMLVideoElement;
    console.error("视频加载失败详细信息:", {
      videoUrl: video.src,
      error: video.error,
      networkState: video.networkState,
      readyState: video.readyState,
      event: event
    });
    
    // 更详细的错误信息
    let errorMessage = "视频加载失败";
    if (video.error) {
      switch (video.error.code) {
        case 1:
          errorMessage = "视频加载被中止";
          break;
        case 2:
          errorMessage = "网络错误导致视频加载失败";
          break;
        case 3:
          errorMessage = "视频解码失败，可能是格式不支持";
          break;
        case 4:
          errorMessage = "视频格式不支持或文件损坏";
          break;
        default:
          errorMessage = "未知的视频加载错误";
      }
    }
    
    console.log("视频URL:", video.src);
    console.log("完整视频路径检查:", {
      originalUrl: video.src,
      isAbsoluteUrl: video.src.startsWith('http'),
      isRelativeUrl: !video.src.startsWith('http') && video.src.length > 0
    });
    
    ElMessage.error(`${errorMessage}，请检查视频文件是否正确上传`);
  }

  function handleVideoLoadStart(event: Event) {
    const video = event.target as HTMLVideoElement;
    console.log("开始加载视频:", {
      url: video.src,
      networkState: video.networkState,
      readyState: video.readyState
    });
  }

  function handleVideoCanPlay(event: Event) {
    const video = event.target as HTMLVideoElement;
    console.log("视频可以播放:", {
      url: video.src,
      duration: video.duration,
      videoWidth: video.videoWidth,
      videoHeight: video.videoHeight
    });
    ElMessage.success("视频加载成功！");
  }

  function handleVideoLoadedMetadata(event: Event) {
    const video = event.target as HTMLVideoElement;
    console.log("视频元数据加载完成:", {
      url: video.src,
      duration: video.duration,
      videoWidth: video.videoWidth,
      videoHeight: video.videoHeight,
      readyState: video.readyState
    });
  }

  // 测试视频URL是否可访问
  function testVideoUrl(url: string) {
    if (!url) {
      ElMessage.warning("视频URL为空");
      return;
    }

    console.log("测试视频URL:", url);
    
    // 创建一个临时的video元素来测试
    const testVideo = document.createElement('video');
    testVideo.onloadedmetadata = () => {
      ElMessage.success("视频链接测试成功，文件可以正常访问");
      console.log("视频测试成功:", {
        url: url,
        duration: testVideo.duration,
        videoWidth: testVideo.videoWidth,
        videoHeight: testVideo.videoHeight
      });
    };
    testVideo.onerror = (e) => {
      ElMessage.error("视频链接测试失败，文件无法访问");
      console.error("视频测试失败:", e, "URL:", url);
    };
    testVideo.onloadstart = () => {
      ElMessage.info("正在测试视频链接...");
    };
    
    testVideo.src = url;
    testVideo.load();
  }

  // 在新窗口打开视频
  function openVideoInNewTab(url: string) {
    if (!url) {
      ElMessage.warning("视频URL为空");
      return;
    }
    
    console.log("在新窗口打开视频:", url);
    window.open(url, '_blank');
  }

  // 清除视频，允许重新上传
  function clearVideo() {
    course.videoUrl = "";
    plus.value = true;
    videoFlag.value = false;
    videoUploadPercent.value = 0;
    reset2();
    ElMessage.info("已清除视频，可以重新上传");
  }
  //结束
  return {
    courseRef,
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
    close2,
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
    handleVideoCanPlay,
    handleVideoLoadedMetadata,
    testVideoUrl,
    openVideoInNewTab,
    clearVideo,
    // 新增：审核相关功能
    isAuditView,
    searchParams,
    statusOptions,
    handleSearch,
    handleReset,
    handleViewAudit,
    handleBackToList,
    handlePublish,
    handleApprove,
    handleReject,
    formatStatus,
    getStatusTagType,
    canEditCourse,
    canDeleteCourse,
    convertOldStatusToNew,
    convertNewStatusToOld,
  };
}
