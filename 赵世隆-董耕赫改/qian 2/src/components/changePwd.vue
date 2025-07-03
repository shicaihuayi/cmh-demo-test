<template>
    <div class="change-pwd-container">
        <div class="form-wrapper">
            <h3 class="form-title">
                <el-icon><Lock /></el-icon>
                修改密码
            </h3>
            
            <!-- 修改密码 -->
            <el-form ref="pwdRef" label-position="top" :model="pwd" :rules="rules" class="pwd-form">
                <!-- 旧密码-->
                <el-form-item label="旧密码" prop="oldPassword" class="form-item">
                    <el-input v-model="pwd.oldPassword" type="password" placeholder="请输入旧密码" show-password />
                </el-form-item>
                <!-- 新密码 -->
                <el-form-item label="新密码" prop="newPassword" class="form-item">
                    <el-input v-model="pwd.newPassword" type="password" placeholder="请输入新密码" show-password />
                </el-form-item>
                <!-- 确认新密码 -->
                <el-form-item label="确认新密码" prop="newPasswordRepeat" class="form-item">
                    <el-input v-model="pwd.newPasswordRepeat" type="password" placeholder="请再次输入新密码" show-password />
                </el-form-item>
                
                <el-divider />
                
                <div class="form-actions">
                    <el-button type="primary" @click="change(pwdRef)" class="save-button">
                        <el-icon><Check /></el-icon>
                        确认修改
                    </el-button>
                    <el-button @click="back()" class="cancel-button">
                        <el-icon><Close /></el-icon>
                        取消
                    </el-button>
                </div>
            </el-form>
        </div>
    </div>
</template>

<script lang="ts">
export default {
    name: 'changePwd'
}
</script>

<script lang="ts" setup>
import { ref, reactive ,inject} from 'vue'
import type { FormInstance } from 'element-plus'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { Lock, Check, Close } from '@element-plus/icons-vue'
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
                                        ElMessage.success(res.data.msg);
                                        // 修改密码成功后，清空表单并返回基本资料页面
                                        pwd.oldPassword = '';
                                        pwd.newPassword = '';
                                        pwd.newPasswordRepeat = '';
                                        setTimeout(() => {
                                            router.push('/home/myself')
                                        }, 1500);
                                    } else {
                                        ElMessage.error(res.data.msg);
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
    // 切换回基本资料页面
    router.push("/home/myself")
};

</script>



<style scoped>
.change-pwd-container {
    display: flex;
    justify-content: center;
    align-items: flex-start;
    min-height: 100%;
    padding: 20px 0;
}

.form-wrapper {
    width: 100%;
    max-width: 500px;
    background: white;
    border-radius: 12px;
    padding: 32px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.form-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0 0 24px 0;
    color: #2c3e50;
    font-size: 20px;
    font-weight: 600;
    border-bottom: 2px solid #409eff;
    padding-bottom: 12px;
}

.pwd-form {
    width: 100%;
}

.form-item {
    margin-bottom: 24px;
}

.form-item :deep(.el-form-item__label) {
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 8px;
}

.form-item :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border: 1px solid #e4e7ed;
    transition: all 0.3s ease;
}

.form-item :deep(.el-input__wrapper:hover) {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.form-item :deep(.el-input__wrapper.is-focus) {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.form-actions {
    display: flex;
    justify-content: flex-start;
    gap: 12px;
    margin-top: 24px;
}

.save-button {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 600;
    transition: all 0.3s ease;
}

.save-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(64, 158, 255, 0.3);
}

.cancel-button {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 600;
    transition: all 0.3s ease;
}

.cancel-button:hover {
    transform: translateY(-2px);
    background-color: #f5f7fa;
}

:deep(.el-divider) {
    margin: 24px 0;
    border-color: rgba(64, 158, 255, 0.2);
}
</style>