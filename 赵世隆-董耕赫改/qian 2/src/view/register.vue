<template>
    <div class="register-container">
        <div id="register">
            <el-form ref="InfoFormRef" :model="InfoForm" :rules="rules" class="register-form" label-position="right">
                <!-- 企业名称 -->
                <el-form-item label="企业名称" prop="companyName" label-width="108px">
                    <el-input v-model.number="InfoForm.companyName" type="text" placeholder="企业名称" />
                </el-form-item>
                <!-- 账号 -->
                <el-form-item label="登录账号" prop="userName" label-width="108px">
                    <el-input v-model="InfoForm.userName" type="text" placeholder="账号" />
                </el-form-item>
                <!-- 密码 -->
                <el-form-item label="密码" prop="password" label-width="108px">
                    <el-input v-model="InfoForm.password" type="text" placeholder="密码" />
                </el-form-item>
                <!-- 再次确认密码 -->
                <el-form-item label="确认密码" prop="passwordRepeat" label-width="108px">
                    <el-input v-model="InfoForm.passwordRepeat" type="password" placeholder="密码" />
                </el-form-item>
                <!-- 邮箱 -->
                <el-form-item label="邮箱" prop="email" label-width="108px">
                    <el-input v-model="InfoForm.email" type="text" placeholder="邮箱" />
                </el-form-item>
                <!-- 手机号 -->
                <el-form-item label="手机号" prop="tel" label-width="108px">
                    <el-input v-model.number="InfoForm.tel" type="text" placeholder="手机号" />
                </el-form-item>
                <!-- 验证码 -->
                <el-form-item label="验证码" prop="checkCode" label-width="108px">
                    <el-input class="codeInput" v-model="InfoForm.checkCode" type="text" placeholder="验证码"
                        style="width: 190px;" />
                    <el-button type="primary" @click="sendCode()" v-if="show"
                        style="margin-left: 10px;">发送验证码</el-button>
                    <el-button type="primary" v-if="!show" disabled style="margin-left: 10px;">{{ count }}秒后重发</el-button>
                </el-form-item>


                <div style="justify-content: center; display: flex">
                    <el-button type="primary" @click="register(InfoFormRef)" class="register-button">注册</el-button>
                    <el-button @click="goBack()">返回</el-button>
                </div>
            </el-form>
        </div>
    </div>
</template>
<script lang="ts">
export default {
    name: 'register'
}
</script>

<script lang="ts" setup>
import useRegisterForm from '@/hooks/useRegisterForm';
const { InfoFormRef, InfoForm, rules, register, goBack, sendCode, count, show } = useRegisterForm()

</script>

<style scoped>
.el-input {
    width: 300px;
}

.register-container {
    width: 100%;
    height: 850px;
    overflow: hidden;
    background: url("../img/背景.jpg");
    background-size: 100% 100%;
}

#register {
    background-color: rgba(255, 255, 255, 0.5);
    /* 背景颜色为半透明的白色 */
    display: flex;
    justify-content: center;
    align-items: center;
    position: absolute;
    top: 50%;
    left: 50%;
    margin-top: -250px;
    margin-left: -250px;
    width: 500px;
    height: 500px;
    background: #fff;
    border-radius: 5%;
}
</style>