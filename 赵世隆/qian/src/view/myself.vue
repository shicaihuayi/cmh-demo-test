<template>
    <div class="myself-manager">
        <!-- 美化头部 -->
        <ModuleHeader 
            title="个人中心" 
            description="管理个人资料信息，包括头像、基本信息和密码修改"
            icon="User"
            color="purple"
        />
        
        <!-- 原有内容区域 -->
        <div class="myself-content">
            <div class="personal-center-container">
                <div class="left-panel">
                    <el-card class="profile-card" shadow="hover">
                        <template #header>
                            <div class="card-header">
                                <el-icon><User /></el-icon>
                                <span class="header-title">个人资料</span>
                            </div>
                        </template>
                        <!-- 头像区域 -->
                        <div class="avatar-section">
                            <div class="avatar-wrapper">
                                <img :src="userInfo.imageUrl" class="avatar-image">
                                <div class="avatar-overlay">
                                    <el-icon><Camera /></el-icon>
                                </div>
                            </div>
                            <el-button type="primary" link @click="openForm()" class="edit-avatar-btn">
                                <el-icon><Edit /></el-icon>
                                修改头像
                            </el-button>
                        </div>
                        
                        <!-- 用户信息列表 -->
                        <div class="info-list">
                            <div class="info-item">
                                <div class="info-label">
                                    <el-icon class="info-icon"><UserFilled /></el-icon>
                                    <span>用户名称</span>
                                </div>
                                <span class="info-value">{{ userInfo.userName }}</span>
                            </div>
                            
                            <div class="info-item">
                                <div class="info-label">
                                    <el-icon class="info-icon"><Star /></el-icon>
                                    <span>用户昵称</span>
                                </div>
                                <span class="info-value">{{ userInfo.nickName }}</span>
                            </div>
                            
                            <div class="info-item">
                                <div class="info-label">
                                    <el-icon class="info-icon"><PhoneFilled /></el-icon>
                                    <span>手机号码</span>
                                </div>
                                <span class="info-value">{{ userInfo.tel }}</span>
                            </div>
                            
                            <div class="info-item">
                                <div class="info-label">
                                    <el-icon class="info-icon"><Message /></el-icon>
                                    <span>用户邮箱</span>
                                </div>
                                <span class="info-value">{{ userInfo.email }}</span>
                            </div>
                            
                            <div class="info-item">
                                <div class="info-label">
                                    <el-icon class="info-icon"><Male /></el-icon>
                                    <span>性别</span>
                                </div>
                                <span class="info-value">{{ userInfo.gender === 'man' ? '男' : '女' }}</span>
                            </div>
                            
                            <div class="info-item">
                                <div class="info-label">
                                    <el-icon class="info-icon"><Place /></el-icon>
                                    <span>所属部门</span>
                                </div>
                                <span class="info-value">{{ userInfo.department }}</span>
                            </div>
                            
                            <div class="info-item">
                                <div class="info-label">
                                    <el-icon class="info-icon"><Stamp /></el-icon>
                                    <span>所属角色</span>
                                </div>
                                <span class="info-value">{{ userInfo.role }}</span>
                            </div>
                            
                            <div class="info-item">
                                <div class="info-label">
                                    <el-icon class="info-icon"><Calendar /></el-icon>
                                    <span>创建时间</span>
                                </div>
                                <span class="info-value">{{ userInfo.date }}</span>
                            </div>
                        </div>
                    </el-card>
                </div>

                <!-- 头像上传对话框 -->
                <el-dialog title="修改头像" v-model="dialogFormVisible" @close="close()" width="30%" class="avatar-dialog">
                    <el-upload ref="uploadImageRef" action="http://localhost:8080/upload/image" list-type="picture-card"
                        :before-upload="beforeUploadImage" :on-preview="handlePictureCardPreview" :on-remove="handleRemove"
                        :limit="1" :on-success="UploadImage" :file-list="fileList">
                        <el-icon :size="30">
                            <Plus />
                        </el-icon>
                        <template #tip>
                            <div class="upload-tip">
                                <span>请上传大小不超过</span>
                                <span class="highlight">5MB</span>
                                <span>格式为</span>
                                <span class="highlight">png/jpg/jpeg</span>
                                <span>的图片</span>
                            </div>
                        </template>
                    </el-upload>
                    <el-dialog v-model="dialogVisible1" class="image-preview">
                        <img class="preview-image" :src="dialogImageUrl" alt="" />
                    </el-dialog>

                    <el-divider />
                    <div class="dialog-actions">
                        <el-button type="primary" @click="modifyImage()">保存</el-button>
                        <el-button @click="dialogFormVisible = false">取消</el-button>
                    </div>
                </el-dialog>

                <div class="right-panel">
                    <el-card class="edit-card" shadow="hover">
                        <template #header>
                            <div class="card-header">
                                <el-icon><Setting /></el-icon>
                                <span class="header-title">基本资料</span>
                            </div>
                        </template>
                        
                        <div class="tab-buttons">
                            <el-button :type="firstButtonType" @click="lookSelfIntro()" class="tab-btn">
                                <el-icon><User /></el-icon>
                                基本资料
                            </el-button>
                            <el-button :type="secondButtonType" @click="changePassword()" class="tab-btn">
                                <el-icon><Lock /></el-icon>
                                修改密码
                            </el-button>
                        </div>

                        <!-- 基本资料表单 -->
                        <div class="form-section" v-if="show">
                            <el-form ref="userRef" :model="user" :rules="rules" class="user-form" label-position="top">
                                <div class="form-fields">
                                    <!-- 用户名称 -->
                                    <el-form-item label="用户名称" class="form-item">
                                        <el-input v-model="userInfo.userName" type="text" placeholder="用户名称" disabled class="readonly-input">
                                            <template #suffix>
                                                <el-icon class="readonly-icon"><Lock /></el-icon>
                                            </template>
                                        </el-input>
                                    </el-form-item>
                                    
                                    <!-- 用户昵称 -->
                                    <el-form-item label="用户昵称" prop="nickName" class="form-item">
                                        <el-input v-model="user.nickName" type="text" placeholder="请输入用户昵称" />
                                    </el-form-item>
                                    
                                    <!-- 手机号码 -->
                                    <el-form-item label="手机号码" prop="tel" class="form-item">
                                        <el-input v-model="user.tel" type="text" placeholder="请输入手机号码" />
                                    </el-form-item>
                                    
                                    <!-- 邮箱 -->
                                    <el-form-item label="邮箱" prop="email" class="form-item">
                                        <el-input v-model="user.email" type="text" placeholder="请输入邮箱地址" />
                                    </el-form-item>

                                    <!-- 性别 -->
                                    <el-form-item label="性别" prop="gender" class="form-item">
                                        <el-radio-group v-model="user.gender" class="gender-group">
                                            <el-radio value="man" class="gender-radio">
                                                <el-icon><Male /></el-icon>
                                                男
                                            </el-radio>
                                            <el-radio value="woman" class="gender-radio">
                                                <el-icon><Female /></el-icon>
                                                女
                                            </el-radio>
                                        </el-radio-group>
                                    </el-form-item>
                                </div>
                                
                                <div class="form-actions">
                                    <el-button type="primary" @click="save(userRef)" class="save-btn">
                                        <el-icon><Check /></el-icon>
                                        保存修改
                                    </el-button>
                                    <el-button @click="back()" class="cancel-btn">
                                        <el-icon><Close /></el-icon>
                                        返回主页
                                    </el-button>
                                </div>
                            </el-form>
                        </div>

                        <!-- 修改密码表单 -->
                        <div class="form-section" v-if="!show">
                            <router-view></router-view>
                        </div>
                    </el-card>
                </div>
            </div>
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
import ModuleHeader from "@/components/ModuleHeader.vue";
import { 
    Message, User, Setting, Lock, Check, Close, Edit, Camera, Plus,
    UserFilled, PhoneFilled, Place, Stamp, Calendar, Star, Male, Female
} from '@element-plus/icons-vue';

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
.myself-manager {
    background-color: #f5f7fa;
    min-height: 100vh;
}

.myself-content {
    padding: 20px;
}

.personal-center-container {
    display: flex;
    gap: 24px;
    padding: 24px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    min-height: 100vh;
    align-items: stretch;
}

.left-panel {
    flex: 0 0 350px;
    display: flex;
    flex-direction: column;
}

.right-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.profile-card, .edit-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    flex: 1;
    display: flex;
    flex-direction: column;
}

.card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #2c3e50;
}

.header-title {
    font-size: 18px;
}

.avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px 0;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 24px;
}

.avatar-wrapper {
    position: relative;
    margin-bottom: 16px;
    cursor: pointer;
}

.avatar-image {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid #409eff;
    transition: all 0.3s ease;
}

.avatar-wrapper:hover .avatar-image {
    transform: scale(1.05);
    box-shadow: 0 8px 25px rgba(64, 158, 255, 0.3);
}

.avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
    color: white;
}

.avatar-wrapper:hover .avatar-overlay {
    opacity: 1;
}

.edit-avatar-btn {
    font-size: 14px;
    padding: 8px 16px;
}

.info-list {
    display: flex;
    flex-direction: column;
    flex: 1;
}

.info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f5f5f5;
    transition: background-color 0.3s ease;
}

.info-item:hover {
    background-color: rgba(64, 158, 255, 0.05);
    border-radius: 8px;
    margin: 0 -12px;
    padding-left: 12px;
    padding-right: 12px;
}

.info-item:last-child {
    border-bottom: none;
}

.info-label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    color: #606266;
}

.info-icon {
    color: #409eff;
    font-size: 16px;
}

.info-value {
    color: #303133;
    font-weight: 500;
}

.tab-buttons {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;
}

.tab-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 12px 24px;
    border-radius: 8px;
    transition: all 0.3s ease;
}

.form-section {
    padding: 20px 0;
    flex: 1;
    display: flex;
    flex-direction: column;
}

.user-form {
    max-width: 500px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.form-fields {
    flex: 1;
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

.readonly-input :deep(.el-input__wrapper) {
    background-color: #f5f7fa;
    cursor: not-allowed;
}

.readonly-icon {
    color: #c0c4cc;
}

.gender-group {
    display: flex;
    gap: 24px;
}

.gender-radio {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    transition: all 0.3s ease;
}

.gender-radio:hover {
    border-color: #409eff;
    background-color: rgba(64, 158, 255, 0.1);
}

.form-actions {
    display: flex;
    gap: 16px;
    padding-top: 24px;
    border-top: 1px solid #ebeef5;
    margin-top: auto;
}

.save-btn, .cancel-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: 500;
    transition: all 0.3s ease;
}

.save-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    color: white;
}

.save-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.avatar-dialog :deep(.el-dialog) {
    border-radius: 16px;
}

.upload-tip {
    font-size: 12px;
    color: #919191;
    margin-top: 8px;
}

.highlight {
    color: #f56c6c;
    font-weight: 600;
}

.dialog-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 16px;
}

.image-preview :deep(.el-dialog) {
    border-radius: 12px;
}

.preview-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .personal-center-container {
        flex-direction: column;
        padding: 16px;
    }
    
    .left-panel {
        flex: none;
    }
    
    .tab-buttons {
        flex-direction: column;
    }
}
</style>