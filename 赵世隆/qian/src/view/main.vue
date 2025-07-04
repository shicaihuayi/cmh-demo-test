<template>
  <div style="display: flex; flex-direction: column; align-items: center; width: 100%; height: 100%;">
    <div style="width: 100%; height: 540px;">
      <div
        style="width: 100%; height:50px; background-color: white; display: flex; align-items: center; border-bottom: 2px solid #f5f5f5;">
        <span style="margin-left: 20px;">👏 欢迎回来,{{ name }}~</span>
      </div>
      <div style="width: 100%; height:180px; display: flex; border-bottom: 2px solid #f5f5f5; align-items: center;">
        <div style="height:220px; flex: 1; display: flex; align-items: center; justify-content: center;">
          <div style="height: 100px; display: flex; align-items: center;">
            <img src="../img/conference.png" alt="fill" style="height: 75px; width: 75px; border-radius: 50%; margin-right: 20px;" >
            <el-statistic :value="conferenceNum" style="height: 100px; display: flex; flex-direction: column; justify-content: center;">
              <template #title>
                <div style="display: inline-flex; align-items: center; font-size: 16px;">
                  会议数量
                </div>
              </template>
            </el-statistic>
            <span style="padding-top: 26px;">个</span>
          </div>
        </div>
        <div class="straight"></div>
        <div style="height:220px; flex: 1; display: flex; align-items: center; justify-content: center;">
          <div style="height: 100px; display: flex; align-items: center;">
            <img src="../img/user.png" alt="fill" style="height: 75px; width: 75px; border-radius: 50%; margin-right: 20px;" >
            <el-statistic :value="userNum" style="height: 100px; display: flex; flex-direction: column; justify-content: center;">
              <template #title>
                <div style="display: inline-flex; align-items: center; font-size: 16px;">
                  用户数量
                </div>
              </template>
            </el-statistic>
            <span style="padding-top: 26px;">个</span>
          </div>
        </div>
        <div class="straight"></div>
        <div style="height:220px; flex: 1; display: flex; align-items: center; justify-content: center;">
          <div style="height: 100px; display: flex; align-items: center;">
            <img src="../img/course.png" alt="fill" style="height: 75px; width: 75px; border-radius: 50%; margin-right: 20px;" >
            <el-statistic :value="courseNum" style="height: 100px; display: flex; flex-direction: column; justify-content: center;">
              <template #title>
                <div style="display: inline-flex; align-items: center; font-size: 16px;">
                  课程数量
                </div>
              </template>
            </el-statistic>
            <span style="padding-top: 26px;">个</span>
          </div>
        </div>
        <div class="straight"></div>
        <div style="height:220px; flex: 1; display: flex; align-items: center; justify-content: center;">
          <div style="height: 100px; display: flex; align-items: center;">
            <img src="../img/task.png" alt="fill" style="height: 75px; width: 75px; border-radius: 50%; margin-right: 20px;" >
            <el-statistic :value="taskNum" style="height: 100px; display: flex; flex-direction: column; justify-content: center;">
              <template #title>
                <div style="display: inline-flex; align-items: center; font-size: 16px;">
                  待处理事件
                </div>
              </template>
            </el-statistic>
            <span style="padding-top: 26px;">个</span>
          </div>
        </div>
      </div>
      <div
        style="width: 100%; height:50px; background-color: white; display: flex; align-items: center; border-bottom: 2px solid #f5f5f5;">
        <img src="../img/Enter.png" alt="fill" style="margin-left: 20px;">
        <span style="margin-left: 20px;">快捷入口</span>
      </div>
      <div style="width: 100%; height:242px; display: flex; align-items: center;">
        <div style="height:220px; flex: 1; display: flex; align-items: center; justify-content: center; flex-direction: column;"
        @click="toConference">
          <img src="../img/conferenceEnter.png" alt="fill">
          <span style="font-size: 20px;">会议管理</span>
        </div>
        <div style="height:220px; flex: 1; display: flex; align-items: center; justify-content: center; flex-direction: column;"
        @click="toUser">
          <img src="../img/userEnter.png" alt="fill">
          <span style="font-size: 20px;">用户管理</span>
        </div>
        <div style="height:220px; flex: 1; display: flex; align-items: center; justify-content: center; flex-direction: column;"
        @click="toTrend">
          <img src="../img/courseEnter.png" alt="fill">
          <span style="font-size: 20px;">动态管理</span>
        </div>
        <div style="height:220px; flex: 1; display: flex; align-items: center; justify-content: center; flex-direction: column;"
        @click="toCourse">
          <img src="../img/taskEnter.png" alt="fill">
          <span style="font-size: 20px;">待处理任务</span>
        </div>
      </div>
    </div>
    <div style="margin-top: -10px;">
      <img src="../img/测盟汇.png" alt="fill" style="width: 200px; height: 230px;">
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useHomeStore } from '@/stores/home';
import { ElMessage } from 'element-plus';
import router from "@/router";
import { getUserList } from '@/api/user';
import { getConferenceList } from '@/api/conference';
import { getCourseList, getPendingCourseList } from '@/api/course';

const conferenceNum = ref(0);
const userNum = ref(0);
const courseNum = ref(0);
const taskNum = ref(0);
const name = ref(sessionStorage.getItem('name'));

const home = useHomeStore();

function toConference(){
  router.push('/home/conference')
  home.updateIndex('7')
}

function toUser(){
  router.push('/home/user')
  home.updateIndex('2')
}

function toTrend(){
  router.push('/home/trend')
  home.updateIndex('5')
}

function toCourse(){
  router.push('/home/course')
  home.updateIndex('6')
}

async function loadDashboardData() {
  try {
    const requests = [
      getConferenceList(),
      getUserList(),
      getCourseList(),
    ];

    const isSuperAdmin = sessionStorage.getItem("role") === "3";
    if (isSuperAdmin) {
      requests.push(getPendingCourseList());
    }

    const [confRes, userRes, courseRes, taskRes] = await Promise.all(requests);

    if (confRes && confRes.isOk) {
      conferenceNum.value = confRes.confers?.length || 0;
    }
    if (userRes && userRes.isOK) {
      userNum.value = userRes.users?.length || 0;
    }
    if (courseRes && courseRes.isOk) {
      courseNum.value = courseRes.courses?.length || 0;
    }
    if (isSuperAdmin && taskRes && taskRes.isOk) {
      taskNum.value = taskRes.courses2?.length || 0;
    }

  } catch (error) {
    console.error("加载主页数据失败:", error);
    ElMessage.error("加载主页统计数据失败，请稍后重试");
  }
}

onMounted(() => {
  loadDashboardData();
})
</script>

<style scoped>
.straight {
  width: 2px;
  height: 140px;
  background-color: #f5f5f5;
}
</style>