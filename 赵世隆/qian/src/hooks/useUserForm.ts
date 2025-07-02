import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
export default function () {
    const userFormRef = ref<FormInstance>()
    const userForm = reactive({
        userName: '',
        password: ''
    })
    const rules = reactive({
        userName: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
    })
    const login = async (formEl: FormInstance | undefined) => {

        if (!formEl) return
        await formEl.validate((valid) => {
            if (valid) {
                let fd = new FormData();
                fd.append('name', userForm.userName);
                fd.append('pwd', userForm.password);
                axios.post('http://localhost:8080/user/login', fd).then(res => {
                    if (res.data.isOk) {
                        if(res.data.user.role==0){
                            ElMessage.error('该用户无管理员权限！！！')
                            return
                        }
                        sessionStorage.setItem('id', res.data.user.id);
                        sessionStorage.setItem("username",userForm.userName); //这是登录的人的用户名
                        sessionStorage.setItem('name', res.data.user.name);
                        sessionStorage.setItem('pwd', res.data.user.pwd);
                        sessionStorage.setItem('companyName', res.data.user.companyName);
                        sessionStorage.setItem('role', res.data.user.role);
                        sessionStorage.setItem('tel', res.data.user.tel);
                        sessionStorage.setItem('img', res.data.user.imageUrl);
                        ElMessage.success(res.data.msg);
                        router.push('/home/main')
                    } else {
                        ElMessage.error(res.data.msg);
                    }
                })
            } else {
                ElMessage({
                    message: '请输入完整的账号密码！',
                    type: 'error'
                })

            }
        })
    }
    function register() {
        router.push('/register')
    }
    return { userFormRef, userForm, rules, login, register }
}

