<template>
    <div class="container" style="display: flex;">
        <div class="left">
            <el-card :model="userInfo">
                <template #header>
                    <div class="card-header">
                        <span style="color: black;">个人资料</span>
                    </div>
                </template>
                <!-- 头像区域 -->
                <div style="justify-content: center; display: flex">
                    <img :src="userInfo.imageUrl" class="avatar-box">
                </div>
                <br>
                <div style="justify-content: center; display: flex">
                    <a href="" @click.prevent="openForm()" style="color:lightblue;">修改头像</a>
                </div>
                <el-divider />
                <div style="justify-content: space-between; display: flex">
                    <div>
                        <el-icon>
                            <UserFilled />
                        </el-icon>
                        <span style="margin-left: 5px;">用户名称</span>
                    </div>
                    <span>{{ userInfo.userName }}</span>
                </div>
                <el-divider />
                <div style="justify-content: space-between; display: flex">
                    <div>
                        <el-icon>
                            <PhoneFilled />
                        </el-icon>
                        <span style="margin-left: 5px;">手机号码</span>
                    </div>
                    <span>{{ userInfo.tel }}</span>
                </div>
                <el-divider />
                <div style="justify-content: space-between; display: flex">
                    <div>
                        <el-icon>
                            <Message />
                        </el-icon>
                        <span style="margin-left: 5px;">用户邮箱 </span>
                    </div>
                    <span>{{ userInfo.email }}</span>
                </div>
                <el-divider />
                <div style="justify-content: space-between; display: flex">
                    <div>
                        <el-icon>
                            <Place />
                        </el-icon>
                        <span style="margin-left: 5px;">所属部门 </span>
                    </div>
                    <span>{{ userInfo.department }}</span>
                </div>
                <el-divider />
                <div style="justify-content: space-between; display: flex">
                    <div>
                        <el-icon>
                            <Stamp />
                        </el-icon>
                        <span style="margin-left: 5px;">所属角色 </span>
                    </div>
                    <span>{{ userInfo.role }}</span>
                </div>
                <el-divider />
                <div style="justify-content: space-between; display: flex">
                    <div>
                        <el-icon>
                            <Calendar />
                        </el-icon>
                        <span style="margin-left: 5px;">创建时间</span>
                    </div>
                    <span>{{ userInfo.date }}</span>
                </div>
            </el-card>
        </div>
        <el-dialog title="修改头像" v-model="dialogFormVisible" @close="close()" width="30%">

            <el-upload ref="uploadImageRef" action="http://localhost:8080/upload/image" list-type="picture-card"
                :before-upload="beforeUploadImage" :on-preview="handlePictureCardPreview" :on-remove="handleRemove"
                :limit="1" :on-success="UploadImage" :file-list="fileList">
                <el-icon :size="30">
                    <Plus />
                </el-icon>
                <template #tip>
                    <div style="font-size: 12px;color: #919191;">
                        <span>请上传大小不超过</span>
                        <span style="color: red;">5MB</span>
                        <span>格式为</span>
                        <span style="color: red;">png/jpg/jpeg</span>
                        <span>的图片</span>
                    </div>
                </template>
            </el-upload>
            <el-dialog v-model="dialogVisible1" style="line-height: 0;">
                <img style="width: 100%;height: 100%" :src="dialogImageUrl" alt="" />
            </el-dialog>


            <el-divider />
            <div style="display: flex;justify-content: center;">
                <el-button type="primary" @click="modifyImage()">保存</el-button>
            </div>
            <!-- //上传图片 -->

        </el-dialog>
        <div class="right">
            <el-card>
                <template #header>
                    <div class="card-header">
                        <span style="color: black;">基本资料</span>
                    </div>
                </template>
                <div style="justify-content: left; display: flex">
                    <el-button :type="firstButtonType" @click="lookSelfIntro()">基本资料</el-button>
                    <el-button :type="secondButtonType" @click="changePassword()">修改密码</el-button>
                </div>
                <el-divider />
                <!-- 我的 -->
                <div style="width: 100%; margin-top: 0;margin-left: 0;">
                    <!-- 个人信息展示与保存 -->
                    <el-form ref="userRef" :model="user" :rules="rules" class="user-form" label-position="right"
                        v-if="show">
                        <!-- 用户昵称 -->
                        <el-form-item label="用户昵称" prop="nickName">
                            <el-input v-model="user.nickName" type="text" placeholder="昵称" />
                        </el-form-item>
                        <!-- 手机号码 -->
                        <el-form-item label="手机号码" prop="tel">
                            <el-input v-model="user.tel" type="text" placeholder="手机号码" />
                        </el-form-item>
                        <!-- 邮箱 -->
                        <el-form-item label="邮箱" prop="email">
                            <el-input v-model="user.email" type="text" placeholder="邮箱" style="margin-left: 48px;" />
                        </el-form-item>

                        <el-form-item label="性别" prop="gender">
                            <el-radio-group v-model="user.gender" style="margin-left: 50px;">
                                <el-radio value="man">男</el-radio>
                                <el-radio value="woman">女</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-divider />
                        <div style="justify-content: left; display: flex">
                            <el-button type="primary" @click="save(userRef)" class="save-button">保存</el-button>
                            <el-button @click="back()">关闭</el-button>

                        </div>
                    </el-form>
                    <router-view></router-view>
                </div>

            </el-card>
        </div>
    </div>

</template>

<script lang="ts">
export default {
    name: 'myself'
}
</script>

<script lang="ts" setup>

import { ElUpload } from 'element-plus'
import router from '@/router'
import { RouterView } from 'vue-router';
import useMyself from '@/hooks/useMyself';
import { Message } from '@element-plus/icons-vue';
const {
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
} = useMyself();

</script>

<style scoped>
.avatar-box {

    width: 80px;
    height: 80px;
    border-radius: 50%;

}

.el-form-item__label {
    margin-left: 10px
}

.el-input {
    margin-left: 20px;
}

.el-card__header {
    background: lightgray;
    /* 设置背景色 */
    padding: 10px;
}

.el-card-header span {
    color: black;
    /* 设置文字颜色 */
}

.left {
    width: 400px;
    margin-left: 5px;
}

.right {
    width: 900px;
    margin-left: 10px;
}
</style>

