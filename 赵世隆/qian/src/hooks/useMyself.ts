import { ref, reactive, onMounted, inject, onUnmounted } from 'vue'
import type { FormInstance } from 'element-plus'
import service from '@/utils/request';
import { ElMessage } from 'element-plus'
import router from '@/router'
import { type ElUpload, type UploadFile } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { loadMyself as loadMyselfApi } from '@/api/user'
import { DebugHelper } from '@/utils/debugHelper'
export default function () {
    let show = ref(true);
    const userInfo = reactive({
        userName: '',
        nickName: '',
        tel: '',
        email: '',
        gender: '',
        department: '',
        role: '',
        date: '',
        imageUrl: ''
    })
    const act = inject<any>('action');
    const userRef = ref<FormInstance>()
    const user = reactive({
        id: '',
        nickName: '',
        imgUrl: '',
        tel: '',
        email: '',
        gender: ''
    })
    const rules = reactive({
        nickName: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
        tel: [{ required: true, message: '手机号码不能为空', trigger: 'blur' }],
        email: [{ required: true, message: '邮箱不能为空', trigger: 'blur' }],
        gender: [{ required: true, message: '性别不能为空', trigger: 'change' }],
    })
    async function load() {
        const userId = sessionStorage.getItem('id');
        if (!userId) {
            ElMessage.error("无法获取用户ID，请重新登录。");
            router.push('/');
            return;
        }

        try {
            const res: any = await loadMyselfApi();
            if (res.isOk) {
                const userData = res.user;
                
                // 统一处理昵称字段，确保左右两侧显示一致
                const actualNickName = userData.nickName || userData.nickname || userData.name;
                // 修复性别字段统一性问题 - 优先使用数据库中的性别字段
                const actualGender = userData.gender || userData.sex || 'man';
                
                // 改进头像URL处理逻辑
                try {
                    // 检查imageUrl是否为有效URL
                    if (userData.imageUrl && userData.imageUrl.trim()) {
                        userInfo.imageUrl = userData.imageUrl.startsWith('http') 
                            ? userData.imageUrl 
                            : `http://localhost:8080${userData.imageUrl}`;
                    } else {
                        // 使用项目中已存在的默认头像
                        userInfo.imageUrl = '/src/img/头像.jpg';
                    }
                } catch (error) {
                    console.error('头像URL处理失败:', error);
                    // 发生错误时使用项目中已存在的默认头像
                    userInfo.imageUrl = '/src/img/头像.jpg';
                }
                
                userInfo.userName = userData.name;
                userInfo.nickName = actualNickName;
                userInfo.tel = userData.tel;
                userInfo.email = userData.email;
                userInfo.gender = actualGender;
                userInfo.department = userData.section;
                if (userData.role === "3") {
                    userInfo.role = "超级管理员"
                } else if (userData.role === "2") {
                    userInfo.role = "企业管理员"
                } else if (userData.role === "1") {
                    userInfo.role = "普通管理员"
                }
                userInfo.date = userData.time;
                
                //表单 - 使用相同的逻辑确保一致性
                user.id = userData.id;
                user.nickName = actualNickName;
                user.tel = userData.tel;
                user.email = userData.email;
                user.gender = actualGender;
                
                ElMessage.success(res.msg);
            } else {
                ElMessage.error(res.msg);
            }
        } catch (error) {
            console.error('加载用户信息失败:', error);
            ElMessage.error('加载用户信息失败');
        }
    }
    onMounted(async () => {
        const storedUserRole = sessionStorage.getItem("role");
        const storedUserId = sessionStorage.getItem("id");
        const storedUsername = sessionStorage.getItem("username");
        
        console.log('Session Storage Check:', {
            role: storedUserRole,
            id: storedUserId,
            username: storedUsername
        });
        
        if (!storedUserRole || !storedUserId || !storedUsername) {
          ElMessage.error('登录信息不完整，请重新登录');
          router.push('/');
          return;
        }
        
        console.log(inject<any>('action'))
        
        // 统一调用load函数，确保数据加载的一致性
        await load();
    })

    const save = async (formEl: FormInstance | undefined) => {
        if (!formEl) return
        
        try {
            const isValid = await formEl.validate();
            if (isValid) {
                console.log('=== 开始保存用户信息 ===');
                console.log('要保存的数据:', {
                    id: user.id,
                    nickname: user.nickName,
                    tel: user.tel,
                    email: user.email,
                    sex: user.gender
                });
                
                let fd = new FormData();
                fd.append('id', user.id);
                fd.append('nickname', user.nickName);
                fd.append('tel', user.tel);
                fd.append('email', user.email);
                fd.append('sex', user.gender);
                
                console.log('FormData内容:');
                for (let [key, value] of fd.entries()) {
                    console.log(`${key}: ${value}`);
                }
                
                const response = await service.post('/user/updateUser', fd);
                console.log('更新用户信息响应:', response);
                
                if (response.isOk) {
                    ElMessage.success(response.msg);
                    // 重新加载数据以确保左右两侧同步更新
                    await load();
                } else {
                    console.error('更新失败:', response);
                    ElMessage.error(response.msg || '更新用户信息失败');
                }
            }
        } catch (error: any) {
            console.error('保存用户信息失败:', error);
            
            if (error.response) {
                console.error('后端响应错误:', error.response.status, error.response.data);
                ElMessage.error(`保存失败：${error.response.data?.msg || '服务器错误'}`);
            } else if (error.request) {
                console.error('网络请求失败:', error.request);
                ElMessage.error('网络连接失败，请检查网络设置');
            } else if (error.message?.includes('Required field')) {
                ElMessage.error('请输入完整的信息！');
            } else {
                ElMessage.error('保存失败，请稍后重试');
            }
        }
    }
    let firstButtonType = ref<string>("primary")
    let secondButtonType = ref<string>("")

    function lookSelfIntro() {
        show.value = true;
        router.push("/home/myself")
        firstButtonType.value = "primary"
        secondButtonType.value = ""
    }
    function changePassword() {
        show.value = false;

        router.push("/home/myself/change")
        firstButtonType.value = ""
        secondButtonType.value = "primary"
    }
    //修改头像
    const dialogFormVisible = ref(false)
    const fileList = ref([]);
    const dialogImageUrl = ref('');
    const dialogVisible1 = ref(false);
    function openForm() {
        dialogFormVisible.value = true
    }
    function beforeUploadImage(file: any) {
        console.log("file.size", file.size);
        //文件大小
        const isLt5M = file.size / 1024 / 1024 < 5
        //视频后缀检查
        if (['image/jpeg', 'image/png', 'image/jpg'].indexOf(file.type) === -1) {
            ElMessage.error('请上传正确的图片格式')
            return false
        }
        if (!isLt5M) {
            ElMessage.error('上传视频大小不能超过5MB哦!')
            return false
        }
    }
    const UploadImage = (res: any, file: any) => {
        if (res.resCode === '200') {
            console.log(res.ImageUrl)
            user.imgUrl = res.ImageUrl
            ElMessage.success('图片上传成功！')
        } else {
            ElMessage.error('图片上传失败，请重新上传！')
        }
    };
    const handleRemove = (file: any, fileList: any) => {
        console.log(file, fileList);
    };

    const handlePictureCardPreview = (file: UploadFile) => {
        console.log(file.url);
        dialogVisible1.value = true;
        dialogImageUrl.value = file.url!
    };
    const uploadImageRef = ref<InstanceType<typeof ElUpload> | null>(null);
    function reset1() {
        uploadImageRef.value!.clearFiles(); //清除文件列表
    }
    function close() {
        reset1()
    }
    //结束修改头像
    const userStore = useUserStore();
    const back = () => {
        act.value = "1"
        console.log(act.value)
        router.push("/home/main")
    };
    function modifyImage() {
        console.log(user.imgUrl)
        let fd = new FormData();
        fd.append('id', user.id);
        fd.append('img', user.imgUrl)
        service.post('/user/updateImg', fd).then((res:any) => {
            if (res.isOk) {
                ElMessage.success(res.msg);
                load();
            } else {
                ElMessage.error(res.msg);
            }
        });
        dialogFormVisible.value = false;
        reset1()
    }
    return {
            firstButtonType,
            secondButtonType,
            lookSelfIntro,
            changePassword,
            openForm,
            save,
            user,
            rules,
            userInfo,
            userRef,
            show,
            back,
            dialogFormVisible,
            fileList,
            dialogImageUrl,
            dialogVisible1,
            beforeUploadImage,
            UploadImage,
            handleRemove,
            handlePictureCardPreview,
            uploadImageRef,
            close,
            modifyImage
        }
    }