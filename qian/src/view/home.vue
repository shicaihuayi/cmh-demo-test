<template>
  <div id="myself">
    <el-container>
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
                flex-direction: column; width: 80px; align-items: center;" v-if="role == 3">
              <span style="font-size: 14px; font-style: normal; color: #E8F3FF; height: 16px;">
                超级管理员</span>
            </div>
            <div style="background-color: #47D288; border-radius: 10px; display: flex; justify-content: center;
                flex-direction: column; width: 80px; align-items: center;" v-if="role == 2">
              <span style="font-size: 14px; font-style: normal; color: #E7FAF0; height: 16px;">
                企业租户</span>
            </div>
            <div style="background-color: #B968FF; border-radius: 10px; display: flex; justify-content: center;
                flex-direction: column; width: 80px; align-items: center;" v-if="role == 1">
              <span style="font-size: 14px; font-style: normal; color: #E7FAF0; height: 16px;">
                企业管理员</span>
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
      <el-container>
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
        <el-main>
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
import axios from "axios";
import {ElMessage} from "element-plus";
import UseMyself from "@/hooks/useMyself";
import { useUserStore } from '@/stores/user';
import { useHomeStore } from '@/stores/home';
import { useTaskStore } from '@/stores/task';

const { act, exit, goMain, goDetail, gotrends, goUser, goDepartment, judge, goCourse, goConference, MenuSelect } =
  useHome();

function getImg() {
  axios.post("http://localhost:8080/user/loadMyself").then(res => {
    if (res.data.isOk) {
      userInf.updateHeadImg(res.data.user.imageUrl);
      if(!userInf.headImg){
  userInf.updateHeadImg('http://localhost:8080/upload/271b0163fee84a25ba8167b5cd243e9e.png')
}
    } else {
      ElMessage.error('加载头像失败');
    }
  }).catch(err => {
    ElMessage.error('加载头像失败');
  });
}
const userInf=useUserStore()
const home = useHomeStore()
const img = ref('')
const role = ref();
const name = ref();
const task = useTaskStore();

function courseManage() {
  router.push('/home/course');
  home.updateIndex('6');
}

function loadPublish() {
    axios.get("http://localhost:8080/course/list").then((res) => {
      if (res.data.isOk) {
        task.updateNum(res.data.courses2.length)
      } else {
        ElMessage.error(res.data.msg);
      }
    });
  }

function logout() {
  sessionStorage.removeItem('id');
  sessionStorage.removeItem("username"); //这是登录的人的用户名
  sessionStorage.removeItem('name');
  sessionStorage.removeItem('pwd');
  sessionStorage.removeItem('companyName');
  sessionStorage.removeItem('role');
  sessionStorage.removeItem('tel');
  home.updateIndex('1');
  localStorage.removeItem('active');
  router.push('/');
}

onMounted(() => {
  role.value = sessionStorage.getItem('role') as string;
  name.value = sessionStorage.getItem('name') as string;
  getImg()
  if(sessionStorage.getItem("role")=="3")
  loadPublish()
})

</script>

<style scoped>
.header {
  display: flex;
  height: 50px;
  color: #ffffff;
  font-size: 30px;
  background-color: #474953;
}

.aside {
  height: 898px;
  background-color: #474953;
}

:deep(.el-badge__content) {
  border: 0px;
}

:deep(.el-badge__content--danger){
  background-color: #ff0000;
}
</style>
