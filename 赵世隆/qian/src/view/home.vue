<template>
  <div class="home-layout">
    <el-container class="main-container">
      <el-header class="header">
        <div style="width: 100%;font-style: italic;display: flex;justify-content: space-between;align-items: center;">
          <span>测盟汇管理系统</span>
          <div style="height: 34px; display: flex;">
            <div @click="courseManage()" style="margin-right: 60px;" v-if="role == '3' ">
              <el-badge :value="task.num" style="width: 20px;" v-if=" task.num != 0 ">
                <el-icon style="width: 20px; height: 20px;">
                  <Bell />
                </el-icon>
              </el-badge>
              <el-icon style="width: 20px; height: 20px;" v-else>
                  <Bell />
                </el-icon>
            </div>
            <div style="align-items: center; display: flex; margin-right: 20px;">
              <span style="font-size: 20px; font-style: normal;">用户名：{{ name }}</span>
            </div>
            <img :src="userInf.headImg" alt="" style="border-radius: 50%; margin-right: 50px; height: 32px; width: 32px">
            <div style="background-color: #1684FC; border-radius: 10px; display: flex; justify-content: center;
                flex-direction: column; width: 80px; align-items: center;" v-if="role == '3'">
              <span style="font-size: 14px; font-style: normal; color: #E8F3FF; height: 16px;">
                超级管理员</span>
            </div>
            <div style="background-color: #47D288; border-radius: 10px; display: flex; justify-content: center;
                flex-direction: column; width: 80px; align-items: center;" v-if="role == '2'">
              <span style="font-size: 14px; font-style: normal; color: #E7FAF0; height: 16px;">
                企业管理员</span>
            </div>
            <div style="background-color: #B968FF; border-radius: 10px; display: flex; justify-content: center;
                flex-direction: column; width: 80px; align-items: center;" v-if="role == '1'">
              <span style="font-size: 14px; font-style: normal; color: #E7FAF0; height: 16px;">
                普通管理员</span>
            </div>
            <div style="align-items: center; display: flex; margin-left: 50px;" @click="logout()">
              <span style="font-size: 15px; font-style: normal; margin-right: 5px;">退出登录</span>
              <el-icon style="transform: rotate(-90deg); width: 22px; height: 22px;">
                <Download />
              </el-icon>
            </div>
          </div>
        </div>
      </el-header>
      <el-container class="content-container">
        <el-aside class="aside" width="200px">
          <!-- 设置侧边栏背景为黑色 -->
          <el-menu active-text-color="#ffd04b" background-color="#444655" text-color="#fff" @select="MenuSelect"
            :default-active="home.index">
            <el-menu-item index="1" @click="goMain()">
              <el-icon>
                <House />
              </el-icon>
              <template v-slot:title>
                <span>主页</span>
              </template>
            </el-menu-item>
            <el-menu-item index="2" @click="goUser()">

              <el-icon>
                <Service />
              </el-icon>
              <template v-slot:title>
                <span>用户管理</span>
              </template>
            </el-menu-item>

            <el-menu-item index="3" @click="judge()" v-if="role == '3'">
              <el-icon>
                <OfficeBuilding />
              </el-icon>
              <template v-slot:title>
                <span>租户管理</span>
              </template>
            </el-menu-item>

            <el-menu-item index="4" @click="goDepartment()">
              <el-icon>
                <Coin />
              </el-icon>
              <template v-slot:title>
                <span>部门管理</span>
              </template>

            </el-menu-item>

            <el-menu-item index="5" @click="gotrends()">
              <el-icon>
                <ChatLineSquare />
              </el-icon>
              <template v-slot:title>
                <span>行业动态管理</span>
              </template>

            </el-menu-item>

            <el-menu-item index="6" @click="goCourse()">
              <el-icon>
                <Film />
              </el-icon>
              <template v-slot:title>
                <span>课程管理</span>
              </template>
            </el-menu-item>
            <el-menu-item index="7" @click="goConference()">
              <el-icon>
                <DataBoard />
              </el-icon>
              <template v-slot:title>
                <span>会议管理</span>
              </template>
            </el-menu-item>

            <el-menu-item index="8" @click="goDetail()">
              <el-icon>
                <User />
              </el-icon>
              <template v-slot:title>
                <span>个人中心</span>
              </template>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main-content">
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script lang="ts">
export default {
  name: "home",
};
</script>

<script lang="ts" setup>
import { House, User } from "@element-plus/icons-vue";
import { RouterView } from "vue-router";
import useHome from "../hooks/useHome";
import useMyself from "../hooks/useMyself"
import { DataBoard } from "@element-plus/icons-vue";
import {onMounted, ref, watch} from "vue";
import router from "@/router";
import service from '@/utils/request';
import {ElMessage} from "element-plus";
import UseMyself from "@/hooks/useMyself";
import { useUserStore } from '@/stores/user';
import { useHomeStore } from '@/stores/home';
import { useTaskStore } from '@/stores/task';
import { getTaskList } from '@/api/user';

const { act, exit, goMain, goDetail, gotrends, goUser, goDepartment, judge, goCourse, goConference, MenuSelect } =
  useHome();

// 从 sessionStorage 直接加载用户信息，不再发起API请求
function loadInfoFromSession() {
  try {
    const imageUrl = sessionStorage.getItem('img');
    console.log('从sessionStorage加载头像URL:', imageUrl);

    if (imageUrl && imageUrl.trim() && imageUrl !== 'null') {
      const fullImageUrl = imageUrl.startsWith('http') 
        ? imageUrl 
        : `http://localhost:8080${imageUrl}`;
      console.log('设置头像URL:', fullImageUrl);
      userInf.updateHeadImg(fullImageUrl);
    } else {
      console.log('用户无头像或头像为空，使用默认头像');
      userInf.updateHeadImg('/src/img/头像.jpg');
    }
    
    const storedName = sessionStorage.getItem('name');
    if (storedName) {
      name.value = storedName;
    }

  } catch (error) {
    console.error('从sessionStorage加载信息异常:', error);
    userInf.updateHeadImg('/src/img/头像.jpg'); // 出错时使用默认头像
  }
}

// 获取待处理任务数量
async function getTask() {
  try {
    const res = await getTaskList();
    if (res && Array.isArray(res)) {
      task.updateNum(res.length);
    }
  } catch (error) {
    console.error("获取待办列表失败:", error);
  }
}

const userInf = useUserStore();
const home = useHomeStore();
const task = useTaskStore();
const name = ref(sessionStorage.getItem('name'));
const role = ref(sessionStorage.getItem('role'));

onMounted(async () => {
  loadInfoFromSession();
  // 只有超级管理员才需要获取待办数量
  if (role.value === '3') {
    await getTask();
  }
});

function logout() {
  // 清除所有会话存储的用户信息
  sessionStorage.removeItem('id');
  sessionStorage.removeItem("username");
  sessionStorage.removeItem('name');
  sessionStorage.removeItem('pwd');
  sessionStorage.removeItem('companyName');
  sessionStorage.removeItem('role');
  sessionStorage.removeItem('tel');
  sessionStorage.removeItem('token'); // 确保清除 token
  sessionStorage.removeItem('img');
  
  // 清除其他存储
  home.updateIndex('1');
  localStorage.removeItem('active');
  
  // 跳转到登录页
  router.push('/');
}

function courseManage() {
  router.push('/home/course');
  home.updateIndex('6');
}

function loadPublish() {
    service.get("/course/list").then((res:any) => {
      if (res.isOk) {
        task.updateNum(res.courses2.length)
      } else {
        ElMessage.error(res.msg);
      }
    });
  }

</script>

<style scoped>
.home-layout {
  height: 100vh;
  overflow: hidden;
}

.main-container {
  height: 100vh;
}

.content-container {
  height: calc(100vh - 50px);
}

.header {
  display: flex;
  height: 50px;
  color: #ffffff;
  font-size: 30px;
  background-color: #474953;
  flex-shrink: 0;
}

.aside {
  height: 100%;
  background-color: #474953;
  overflow-y: auto;
}

.main-content {
  height: 100%;
  overflow-y: auto;
  padding: 0;
}

:deep(.el-badge__content) {
  border: 0px;
}

:deep(.el-badge__content--danger){
  background-color: #ff0000;
}

.el-badge {
  vertical-align: top;
  margin-right: -5px;
  background-color: #ff0000;
}
</style>