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

export interface Course {
  id: number;
  name: string;
  coverUrl: string;
  introduction: string;
  courseOrder: string;
  videoUrl: string;
  author: string;
  pass: string;
}
export default function () {
  const task = useTaskStore();
  //加载课程
  const courses = ref([]);
  async function loadCourse() {
    try {
      const response: any = await request({
        url: '/course/list',
        method: 'get'
      });
      
      if (response.isOk) {
        courses.value = response.courses;
        ElMessage.success(response.msg);
      } else {
        ElMessage.error(response.msg);
      }
      
      if (response.isAdmin) {
        isAdmin.value = true;
      } else {
        isAdmin.value = false;
      }
    } catch (err) {
      console.error("加载课程失败", err);
      ElMessage.error("加载课程失败，请检查网络连接");
    }
  }
  onMounted(() => {
    const storedUserRole = sessionStorage.getItem("role");
    if (storedUserRole == null) {
      ElMessage.error('尚未登录，请先登录');
      router.push('/');
    }
    loadCourse();
  });
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

          console.log("准备添加课程，数据:", {
            name: course.name,
            coverUrl: course.coverUrl,
            introduction: course.introduction,
            courseOrder: course.courseOrder,
            author: course.author,
            videoUrl: course.videoUrl
          });

          let fd = new FormData();
          fd.append("name", course.name);
          fd.append("coverUrl", course.coverUrl || "");
          fd.append("introduction", course.introduction);
          fd.append("courseOrder", course.courseOrder);
          fd.append("author", course.author);
          fd.append("videoUrl", course.videoUrl || "");
          
          const response: any = await request({
            url: '/course/add',
            method: 'post',
            data: fd,
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });

          loading.close();
          
          console.log("添加课程响应:", response);
          
          if (response.isOk) {
            ElMessage.success(`课程"${course.name}"添加成功！`);
            back();
            // 重新加载课程列表
            await loadCourse();
            // 重置分页到第一页以显示新添加的课程
            currentPage.value = 1;
          } else {
            ElMessage.error(response.msg || "添加课程失败");
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
          // 验证必需字段
          if (!course.id) {
            ElMessage.error("课程ID缺失，无法修改");
            return;
          }

          // 显示修改进度提示
          const loading = ElLoading.service({
            lock: true,
            text: '正在修改课程...',
            background: 'rgba(0, 0, 0, 0.7)',
          });

          console.log("准备修改课程，数据:", {
            id: course.id,
            name: course.name,
            coverUrl: course.coverUrl,
            introduction: course.introduction,
            courseOrder: course.courseOrder,
            author: course.author,
            videoUrl: course.videoUrl
          });

          let fd = new FormData();
          fd.append("id", course.id);
          fd.append("name", course.name);
          fd.append("coverUrl", course.coverUrl || "");
          fd.append("introduction", course.introduction);
          fd.append("courseOrder", course.courseOrder);
          fd.append("videoUrl", course.videoUrl || "");
          fd.append("author", course.author);
          
          const response: any = await request({
            url: '/course/update',
            method: 'post',
            data: fd,
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });

          loading.close();
          
          console.log("修改课程响应:", response);
          
          if (response.isOk) {
            ElMessage.success(`课程"${course.name}"修改成功！`);
            back();
            // 重新加载课程列表
            await loadCourse();
          } else {
            ElMessage.error(response.msg || "修改课程失败");
            console.error("修改课程失败，服务器响应:", response);
          }
        } catch (err: any) {
          console.error("修改课程失败，详细错误:", err);
          
          // 更详细的错误处理
          let errorMessage = "修改课程失败";
          if (err.message) {
            if (err.message.includes("系统繁忙")) {
              errorMessage = "服务器繁忙，请稍后再试";
            } else if (err.message.includes("Network Error")) {
              errorMessage = "网络连接失败，请检查网络";
            } else {
              errorMessage = `修改失败：${err.message}`;
            }
          }
          
          ElMessage.error(errorMessage);
        }
      } else {
        ElMessage.error("请填写完整的课程信息");
      }
    });
  };
  //分页
  const currentPage = ref(1);
  const pageSize = ref(10);
  const multipleTableRef = ref<InstanceType<typeof ElTable>>();
  const multipleSelection = ref<Course[]>([]);

  const handleSelectionChange = (val: Course[]) => {
    multipleSelection.value = val;
  };
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
  async function delCourse() {
    if (multipleSelection.value.length === 0) {
      ElMessage.error("请先选择要删除的课程！");
      return;
    }
    
    const ids = multipleSelection.value.map((item) => item.id);
    const courseNames = multipleSelection.value.map((item) => item.name);
    const deleteCount = ids.length;
    const deleteMessage = deleteCount === 1 ? 
      `确定要删除课程"${courseNames[0]}"吗？` : 
      `确定要删除以下${deleteCount}个课程吗？\n${courseNames.join(", ")}`;
    
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
        text: '正在删除课程...',
        background: 'rgba(0, 0, 0, 0.7)',
      });
      
      try {
        // 使用统一的request工具来避免CORS问题
        const response: any = await request({
          url: '/course/del',
          method: 'post',
          data: ids,
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        loading.close();
        
        if (response.isOk) {
          ElMessage.success(`成功删除${deleteCount}个课程`);
          // 重新加载数据
          loadCourse();
          // 清空选择
          multipleTableRef.value?.clearSelection();
        } else {
          ElMessage.error(response.msg || "删除失败");
        }
      } catch (err) {
        loading.close();
        console.error("删除课程失败", err);
        ElMessage.error("删除请求失败，请检查网络连接");
      }
    } catch {
      // 用户取消删除
      ElMessage.info('已取消删除');
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
  const isAdmin = ref(false);
  const isLookPublish = ref(false);

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
        "确定要发布选中的课程吗？",
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
        ElMessage.success(response.msg);
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
    try {
      const response: any = await request({
        url: '/course/list',
        method: 'get'
      });
      
      if (response.isOk) {
        ElMessage.success(response.msg);
        courses2.value = response.courses2;
        task.updateNum(response.courses2.length);
      } else {
        ElMessage.error(response.msg);
      }
    } catch (err) {
      console.error("加载审核列表失败", err);
      ElMessage.error("加载审核列表失败，请检查网络连接");
    }
  }
  function lookPublish() {
    isLookPublish.value = true;
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

      const auditData = {
        courseId: row.id,
        courseName: row.name,
        status: "通过",
        remark: "超级管理员审核通过"
      };

      console.log("审核数据:", auditData);
      console.log("课程行数据:", row);

      const response: any = await request({
        url: '/course/audit',
        method: 'post',
        data: auditData,
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log("审核请求完成，收到响应:", response);
      console.log("响应结构:", {
        isOk: response.isOk,
        code: response.code,
        msg: response.msg,
        data: response.data
      });

      if (response.isOk) {
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
          await loadPublish();
        } catch (refreshErr) {
          console.error("刷新列表失败:", refreshErr);
          ElMessage.warning("审核成功，但刷新列表失败，请手动刷新");
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

      const auditData = {
        courseId: row.id,
        courseName: row.name,
        status: "不通过",
        remark: "超级管理员审核不通过"
      };

      console.log("审核数据:", auditData);
      console.log("课程行数据:", row);

      const response: any = await request({
        url: '/course/audit',
        method: 'post',
        data: auditData,
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log("审核请求完成，收到响应:", response);
      console.log("响应结构:", {
        isOk: response.isOk,
        code: response.code,
        msg: response.msg,
        data: response.data
      });

      if (response.isOk) {
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
          await loadPublish();
        } catch (refreshErr) {
          console.error("刷新列表失败:", refreshErr);
          ElMessage.warning("审核成功，但刷新列表失败，请手动刷新");
        }
      }
    }
  }
  function backToList() {
    isLookPublish.value = false;
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
  };
}
