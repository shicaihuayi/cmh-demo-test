<template>
    <!-- 修改密码 -->
    <el-form ref="pwdRef" label-position="right" :model="pwd" :rules="rules" class="pwd-form">
        <!-- 旧密码-->
        <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="pwd.oldPassword" type="password" placeholder="旧密码" />
        </el-form-item>
        <!-- 新密码 -->
        <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwd.newPassword" type="text" placeholder="新密码" />
        </el-form-item>
        <!-- 确认新密码 -->
        <el-form-item label="确认新密码" prop="newPasswordRepeat">
            <el-input v-model="pwd.newPasswordRepeat" type="password" placeholder="确认新密码" />
        </el-form-item>
        <el-divider />
        <div style="justify-content: left; display: flex">
            <el-button type="primary" @click="change(pwdRef)" class="save-button">修改</el-button>
            <el-button @click="back()">关闭</el-button>
        </div>
    </el-form>

</template>

<script lang="ts">
export default {
    name: 'myself'
}
</script>

<script lang="ts" setup>
import { ref, reactive ,inject} from 'vue'
import type { FormInstance } from 'element-plus'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { FormProps } from 'element-plus'
const pwdRef = ref<FormInstance>()
const pwd = reactive({
    oldPassword: '',
    newPassword: '',
    newPasswordRepeat: ''
})
const rules = reactive({
    oldPassword: [{ required: true, message: '旧密码不能为空', trigger: 'blur' }],
    newPassword: [{ required: true, message: '新密码不能为空', trigger: 'blur' }],
    newPasswordRepeat: [{ required: true, message: '确认新密码不能为空', trigger: 'blur' }],
})
const change = async (formEl: FormInstance | undefined) => {
    if (!formEl) return
    await formEl.validate((valid) => {
        if (valid) {
            axios.get('http://localhost:8080/user/loadMyself').then(res => {
                if (res.data.isOk) {
                    if (res.data.user.pwd !== pwd.oldPassword) {
                        ElMessage({
                            message: '旧密码输入错误！',
                            type: 'error'
                        })
                        return
                    } else {
                        if (pwd.oldPassword == pwd.newPassword) {
                            ElMessage({
                                message: '新密码不能与旧密码相同！',
                                type: 'error'
                            })
                            return
                        } else {
                            if (pwd.newPassword !== pwd.newPasswordRepeat) {
                                ElMessage({
                                    message: '两次输入的新密码不一致！',
                                    type: 'error'
                                })
                                return
                            }
                            else {
                                let fd = new FormData();
                                fd.append('password', pwd.newPassword)
                                axios.post('http://localhost:8080/user/changePwd', fd).then(res => {
                                    if (res.data.isOk) {
                                        alert(res.data.msg);
                                        router.push('/')
                                    } else {
                                        ElMessage(res.data.msg);
                                    }
                                })
                            }
                        }
                    }
                } else {
                    ElMessage.error(res.data.msg);
                }
            })
        } else {
            ElMessage({
                message: '请输入完整的信息！',
                type: 'error'
            })

        }
    })
}
const act = inject<any>('action');
const back = () => {
    act.value = "1"
    console.log(act.value)
    router.push("/home/main")
};

</script>



<style scoped>
.el-form-item__label {
    margin-left: 10px
}

.el-input {
    margin-left: 20px;
}
</style>

