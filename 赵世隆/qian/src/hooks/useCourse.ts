import { ref, reactive, computed, onMounted, watch } from "vue";
import axios from "axios";
import {
  ElMessage,
  ElTable,
  type FormInstance,
  type ElUpload,
  type UploadFile,
} from "element-plus";
import * as XLSX from "xlsx";
import { useTaskStore } from '@/stores/task';

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
  function loadCourse() {
    axios.get("http://localhost:8080/course/list").then((res) => {
      if (res.data.isOk) {
        courses.value = res.data.courses;
        ElMessage.success(res.data.msg);
      } else {
        ElMessage.error(res.data.msg);
      }
      if (res.data.isAdmin) {
        isAdmin.value = true;
      } else {
        isAdmin.value = false;
      }
    });
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
    await formEl.validate((valid) => {
      if (valid) {
        let fd = new FormData();
        fd.append("name", course.name);
        fd.append("coverUrl", course.coverUrl);
        fd.append("introduction", course.introduction);
        fd.append("courseOrder", course.courseOrder);
        fd.append("author", course.author);
        fd.append("videoUrl", course.videoUrl);
        axios.post("http://localhost:8080/course/add", fd).then((res) => {
          if (res.data.isOk) {
            ElMessage.success(res.data.msg);
            back();
            loadCourse();
          } else {
            ElMessage.error(res.data.msg);
          }
        });
      } else {
        ElMessage.error("请输入完整的信息");
      }
    });
  };
  //更新课程
  const fileList=ref([])
  const courseId = ref("");
  function openForm(row: any) {
    courseId.value = row.id;
    Object.assign(course, row);
    if (course.coverUrl) {
      fileList.value = [{
        name: '课程图标',
        url: course.coverUrl,
      }];
    } else {
      fileList.value = [];
    }
    dialogVisible.value = true;
  }
  const update = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate((valid) => {
      if (valid) {
        let fd = new FormData();
        fd.append("id", course.id);
        fd.append("name", course.name);
        fd.append("coverUrl", course.coverUrl);
        fd.append("introduction", course.introduction);
        fd.append("courseOrder", course.courseOrder);
        fd.append("videoUrl", course.videoUrl);
        fd.append("author", course.author);
        axios.post("http://localhost:8080/course/update", fd).then((res) => {
          if (res.data.isOk) {
            ElMessage.success(res.data.msg);
            back();
            loadCourse();
          } else {
            ElMessage.error(res.data.msg);
          }
        });
      } else {
        ElMessage.error("请输入完整的信息");
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
    console.log(res);
    // 如果为200代表视频保存成功
    if (res.resCode === "200") {
      // 接收视频传回来的名称和保存地址
      // 至于怎么使用看你啦~
      course.videoUrl = res.VideoUrl;

      ElMessage.success("视频上传成功！");
    } else {
      ElMessage.error("视频上传失败，请重新上传！");
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
  function delCourse() {
    if (multipleSelection.value.length === 0) {
      ElMessage.error("请选择要删除的课程");
      return;
    }
    const ids = multipleSelection.value.map((item) => item.id);
    if (!confirm(`是否删除选中的课程？`)) {
      return;
    }
    axios
      .post("http://localhost:8080/course/del", ids, {
        headers: {
          "Content-Type": "application/json",
        },
      })
      .then((res) => {
        if (res.data.isOk) {
          ElMessage.success(res.data.msg);
          loadCourse();
        } else {
          ElMessage.error(res.data.msg);
        }
      });
  }
  //搜索课程
  const searchCourse = ref({
    name: "",
    courseOrder: "",
  });

  function search() {
    const { name, courseOrder } = searchCourse.value;

    // 检查是否所有搜索条件都为空
    if (!name && !courseOrder) {
      ElMessage.error("请输入搜索条件");
      return;
    }

    axios
      .post("http://localhost:8080/course/search", searchCourse.value)
      .then((res) => {
        if (res.data.isOk) {
          ElMessage.success(res.data.msg);
          courses.value = res.data.courses;
        } else {
          ElMessage.error(res.data.msg);
        }
      });
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
  function publish() {
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
    const ides = multipleSelection.value.map((item) => item.id);
    if (!confirm("是否发布选中的课程？")) {
      return;
    }
    axios
      .post("http://localhost:8080/course/publish", coursess, {
        headers: {
          "Content-Type": "application/json",
        },
      })
      .then((res) => {
        if (res.data.isOk) {
          ElMessage.success(res.data.msg);
          loadCourse();
        } else {
          ElMessage.error(res.data.msg);
        }
      });
  }
  function loadPublish() {
    axios.get("http://localhost:8080/course/list").then((res) => {
      if (res.data.isOk) {
        ElMessage.success(res.data.msg);
        courses2.value = res.data.courses2;
        task.updateNum(res.data.courses2.length);
      } else {
        ElMessage.error(res.data.msg);
      }
    });
  }
  function lookPublish() {
    isLookPublish.value = true;
    loadPublish();
  }
  function passes(row: any) {
    let fd = new FormData();
    fd.append("id", row.id);
    fd.append("name", row.name);
    fd.append("pass", "通过");

    axios.post("http://localhost:8080/course/pass", fd).then((res) => {
      if (res.data.isOk) {
        ElMessage.success(res.data.msg);
        loadPublish();
      } else {
        ElMessage.error(res.data.msg);
        loadPublish();
      }
    });
  }
  function notPass(row: any) {
    let fd = new FormData();
    fd.append("id", row.id);
    fd.append("name", row.name);
    fd.append("pass", "不通过");

    axios.post("http://localhost:8080/course/pass", fd).then((res) => {
      if (res.data.isOk) {
        ElMessage.success(res.data.msg);
        loadPublish();
      } else {
        ElMessage.error(res.data.msg);
      }
    });
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
      // console.log(row);
      if (row.pass == "通过") {
        return { color: "dodgerblue" };
      } else if (row.pass == "不通过") {
        return { color: "red" };
      } else {
        return {};
      }
    }
  };
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
  };
}


