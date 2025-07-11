import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
export default function () {
    const InfoFormRef = ref<FormInstance>()
    const InfoForm = reactive({
        companyName: '',
        tel: '',
        email: '',
        userName: '',
        password: '',
        passwordRepeat: '',
        checkCode: '',
    })
    let count = ref(0);
    let show = ref(true);
    const rules = reactive({
        companyName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }],
        tel: [{ required: true, message: '手机号不能为空', trigger: 'blur' }],
        email: [{ required: true, message: '邮箱不能为空', trigger: 'blur' }],
        userName: [{ required: true, message: '登录账号不能为空', trigger: 'blur' }],
        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
        passwordRepeat: [{ required: true, message: '重复密码不能为空', trigger: 'blur' }],
        checkCode: [{ required: true, message: '验证码不能为空', trigger: 'blur' }],
    })
    function sendCode() {
        // 调发送短信接口
        axios.get('http://localhost:8080/user/send', {
            params: {
                tel: InfoForm.tel
            }
        }).then(res => {
                if (res.data.isOk) {
                    ElMessage.success(res.data.msg)
                }
                else {
                    ElMessage.error(res.data.msg)
                }

            })

        let TIME_COUNT = 60;
        count.value = TIME_COUNT;
        show.value = false;
        let timer = setInterval(() => {
            if (count.value > 0 && count.value <= TIME_COUNT) {
                count.value--;
            } else {
                show.value = true;
                clearInterval(timer);
            }
        }, 1000)


    }
    function addcom() {
        let fd = new FormData();
        const form = InfoForm;
        fd.append('name', form.companyName);
        fd.append('admin', form.userName);
        fd.append('state', "1");//这里不设成字符串会报错
        axios.post('http://localhost:8080/company/add', fd)
      }

    const register = async (formEl: FormInstance | undefined) => {
        if (!formEl) return
        await formEl.validate((valid) => {
            if (valid) {
                if (InfoForm.password !== InfoForm.passwordRepeat) {
                    ElMessage({
                        message: '两次输入的密码不一致！',
                        type: 'error'
                    })
                    return
                }
                else {
                    let fd = new FormData();
                    fd.append('companyName', InfoForm.companyName);
                    fd.append('tel', InfoForm.tel);
                    fd.append('email', InfoForm.email);
                    fd.append('name', InfoForm.userName);
                    fd.append('pwd', InfoForm.password);
                    fd.append('code', InfoForm.checkCode);
                    fd.append('role', "2");
                    fd.append('secid', "0");
                    axios.post('http://localhost:8080/user/register', fd).then(res => {
                        if (res.data.isOk) {
                            addcom() ;
                            alert(res.data.msg);
                            router.push('/')
                        } else {
                            ElMessage.error(res.data.msg);
                        }
                    })
                }
            } else {
                ElMessage({
                    message: '请输入正确的信息！',
                    type: 'error'
                })

            }
        })
    }
    function goBack() {
        router.push('/')
    }
    return {
        InfoFormRef,
        InfoForm,
        rules,
        register,
        goBack,
        sendCode,
        count,
        show
    }
}
