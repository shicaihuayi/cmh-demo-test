<template>
    <div id='login' class="login-container">
        <el-form ref="userFormRef" :model="userForm" :rules="rules" class="login-form">
            <!-- 头像区域 -->
            <div class="avatar-box">
                <img src="../img/cmh.png" alt="">
            </div>

            <div class="title-container">
                <h3 class="title">测盟汇管理系统</h3>
            </div>
            <!-- 账号 -->
            <el-form-item prop="userName">
                <span class="svg-container">
                    <el-icon size="18">
                        <User />
                    </el-icon>
                </span>
                <el-input v-model="userForm.userName" placeholder="账号" />
            </el-form-item>
            <!-- 密码 -->
            <el-form-item prop="password">
                <span class="svg-container">
                    <el-icon size="18">
                        <Lock />
                    </el-icon>
                </span>
                <el-input v-model="userForm.password" type="password" placeholder="密码" />
            </el-form-item>

            <div>
                <el-button type="primary" style="width:100%;margin-bottom:20px;"
                    @click="login(userFormRef)">登录</el-button>
            </div>
            <div class="tips">
                <span style="margin-right:20px;">没有账号？ <span style="color:#409EFF;cursor:pointer"
                        @click="register()">注册</span></span>
            </div>
        </el-form>
    </div>
</template>

<script lang="ts">
export default {
    name: 'login'
}
</script>

<script lang="ts" setup>
import useUserForm from '@/hooks/useUserForm';
const { userFormRef, userForm, rules, login, register} =useUserForm()

</script>


<style scoped>
.el-alert {
    margin: 20px 0 0;
}

.el-alert:first-child {
    margin: 0;
}

.login-container {
    width: 100%;
    height: 850px;
    overflow: hidden;
    background: url("../img/bg.jpg");
    background-size: 100% 100%;
}

.login-form {
    position: relative;
    width: 450px;
    max-width: 100%;
    padding: 160px 35px 0;
    margin: 0 auto;
    overflow: hidden;
}

.avatar-box {
    margin: 0 auto;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    border: 1px solid #409eff;
    box-shadow: 0 0 10px #409eff;
    position: relative;
    bottom: 20px;
}

img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
}

.title-container {
    position: relative;

}

.title {
    font-size: 30px;
    color: #333;
    margin: 0px auto 40px auto;
    text-align: center;
    font-weight: 500;
}

.el-form-item {
    width: 100%;
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
}

.el-input {
    --el-input-border-color: transparent;
    height: 100%;
    width: 400px;
}

:deep(.el-input__wrapper) {
    background: transparent !important;
}

:deep(.el-input__inner) {
    color: #fffff2;
}


.tips {
    font-size: 18px;
    text-align: center;
    color: #000;
    margin-bottom: 10px;
}

.svg-container {
    padding: 6px 5px 6px 15px;
    color: #889aa4;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
}

.title-container {
    position: relative;
}

.title {
    font-size: 30px;
    color: #eee;
    margin: 0px auto 40px auto;
    text-align: center;
    font-weight: 500;
}
</style>

