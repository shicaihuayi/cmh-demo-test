<template>
  <div class="analytics-container" v-loading="loading">
    <div class="analytics-header">
      <h2>数据分析与反馈</h2>
      <div class="date-filter">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
      </div>
    </div>
    
    <div class="analytics-content">
      <!-- 概览卡片 -->
      <div class="overview-cards">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon">
              <el-icon color="#409EFF"><DataBoard /></el-icon>
            </div>
            <div class="card-info">
              <h3>{{ overview.totalConferences }}</h3>
              <p>总会议数</p>
            </div>
          </div>
        </el-card>
        
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon">
              <el-icon color="#67C23A"><User /></el-icon>
            </div>
            <div class="card-info">
              <h3>{{ overview.totalParticipants }}</h3>
              <p>会议参与人次</p>
            </div>
          </div>
        </el-card>
        
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon">
              <el-icon color="#E6A23C"><Film /></el-icon>
            </div>
            <div class="card-info">
              <h3>{{ overview.totalCourses }}</h3>
              <p>总课程数</p>
            </div>
          </div>
        </el-card>
        
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon">
              <el-icon color="#F56C6C"><UserFilled /></el-icon>
            </div>
            <div class="card-info">
              <h3>{{ overview.totalEnrollments }}</h3>
              <p>课程报名人次</p>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 图表区域 -->
      <div class="charts-area">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>会议参与趋势</span>
                </div>
              </template>
              <div ref="conferenceChart" class="chart-container"></div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>课程报名趋势</span>
                </div>
              </template>
              <div ref="courseChart" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>会议类型分布</span>
                </div>
              </template>
              <div ref="conferenceTypeChart" class="chart-container"></div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>课程分类分布</span>
                </div>
              </template>
              <div ref="courseCategoryChart" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { DataBoard, User, Film, UserFilled } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { getComprehensiveReport } from '@/api/analytics';

// 数据状态
const dateRange = ref<[string, string]>(['', '']);
const loading = ref(true);

// 概览数据
const overview = reactive({
  totalConferences: 0,
  totalParticipants: 0,
  totalCourses: 0,
  totalEnrollments: 0
});

// 图表引用
const conferenceChart = ref<HTMLElement | null>(null);
const courseChart = ref<HTMLElement | null>(null);
const conferenceTypeChart = ref<HTMLElement | null>(null);
const courseCategoryChart = ref<HTMLElement | null>(null);

// ECharts实例
let conferenceChartInstance: echarts.ECharts | null = null;
let courseChartInstance: echarts.ECharts | null = null;
let conferenceTypeChartInstance: echarts.ECharts | null = null;
let courseCategoryChartInstance: echarts.ECharts | null = null;


// 更新会议参与趋势图
const updateConferenceChart = (data: any[]) => {
  if (!conferenceChartInstance) return;
  const months = data.map(item => item.month);
  const counts = data.map(item => item.count);
  conferenceChartInstance.setOption({
    xAxis: {
      data: months
    },
    series: [{
      data: counts
    }]
  });
};

// 更新课程报名趋势图
const updateCourseChart = (data: any[]) => {
  if (!courseChartInstance) return;
  const months = data.map(item => item.month);
  const counts = data.map(item => item.count);
  courseChartInstance.setOption({
    xAxis: {
      data: months
    },
    series: [{
      data: counts
    }]
  });
};

// 更新会议类型分布图
const updateConferenceTypeChart = (data: any[]) => {
  if (!conferenceTypeChartInstance) return;
  conferenceTypeChartInstance.setOption({
    series: [{
      data: data
    }]
  });
};

// 更新课程分类分布图
const updateCourseCategoryChart = (data: any[]) => {
  if (!courseCategoryChartInstance) return;
  courseCategoryChartInstance.setOption({
    series: [{
      data: data
    }]
  });
};


// 初始化图表
const initCharts = () => {
  nextTick(() => {
    // 会议参与趋势图
    if (conferenceChart.value) {
      conferenceChartInstance = echarts.init(conferenceChart.value);
      const conferenceOption = {
        title: {
          text: '会议参与趋势',
          textStyle: { fontSize: 14 }
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '参与人次',
          type: 'line',
          data: [], // 初始为空
          smooth: true,
          itemStyle: { color: '#409EFF' }
        }]
      };
      conferenceChartInstance.setOption(conferenceOption);
    }

    // 课程报名趋势图
    if (courseChart.value) {
      courseChartInstance = echarts.init(courseChart.value);
      const courseOption = {
        title: {
          text: '课程报名趋势',
          textStyle: { fontSize: 14 }
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '报名人次',
          type: 'bar',
          data: [], // 初始为空
          itemStyle: { color: '#67C23A' }
        }]
      };
      courseChartInstance.setOption(courseOption);
    }

    // 会议类型分布饼图
    if (conferenceTypeChart.value) {
      conferenceTypeChartInstance = echarts.init(conferenceTypeChart.value);
      const conferenceTypeOption = {
        title: {
          text: '会议类型分布',
          textStyle: { fontSize: 14 }
        },
        tooltip: {
          trigger: 'item'
        },
        series: [{
          name: '会议类型',
          type: 'pie',
          radius: '80%',
          data: [], // 初始为空
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      };
      conferenceTypeChartInstance.setOption(conferenceTypeOption);
    }

    // 课程分类分布饼图
    if (courseCategoryChart.value) {
      courseCategoryChartInstance = echarts.init(courseCategoryChart.value);
      const courseCategoryOption = {
        title: {
          text: '课程分类分布',
          textStyle: { fontSize: 14 }
        },
        tooltip: {
          trigger: 'item'
        },
        series: [{
          name: '课程分类',
          type: 'pie',
          radius: '80%',
          data: [], // 初始为空
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      };
      courseCategoryChartInstance.setOption(courseCategoryOption);
    }
  });
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getComprehensiveReport();
    console.log('API响应:', res);
    
    if (res && res.code === 200 && res.data) {
        const data = res.data;
        // 更新概览数据
        overview.totalConferences = data.totalConferences || 0;
        overview.totalParticipants = data.totalParticipants || 0;
        overview.totalCourses = data.totalCourses || 0;
        overview.totalEnrollments = data.totalEnrollments || 0;

        // 更新图表
        updateConferenceChart(data.conferenceTrend || []);
        updateCourseChart(data.courseTrend || []);
        updateConferenceTypeChart(data.conferenceTypeDistribution || []);
        updateCourseCategoryChart(data.courseCategoryDistribution || []);

        ElMessage.success('数据加载完成');
    } else {
        ElMessage.error(res?.msg || '数据加载失败');
    }

  } catch (error) {
    console.error('数据加载失败:', error);
    ElMessage.error('数据加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 日期范围改变处理
const handleDateChange = (dates: [string, string]) => {
  if (dates && dates.length === 2) {
    // 如果需要按日期范围加载数据，这里可以调用相应的API
    // 例如：loadDataByDateRange(dates[0], dates[1]);
    console.log('日期范围改变:', dates);
  }
};

// 按日期范围加载数据
const loadDataByDateRange = async (startDate: string, endDate: string) => {
  try {
    // 调用API获取指定日期范围的数据
    // const [conferenceData, courseData] = await Promise.all([
    //   getConferenceDataByDateRange(startDate, endDate),
    //   getCourseDataByDateRange(startDate, endDate)
    // ]);
    
    // 更新图表数据
    ElMessage.success(`已加载 ${startDate} 至 ${endDate} 的数据`);
  } catch (error) {
    console.error('按日期加载数据失败:', error);
    ElMessage.error('数据加载失败');
  }
};

// 刷新数据
const refreshData = () => {
  loadData();
  // 如果有日期范围，按日期范围刷新
  if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
    loadDataByDateRange(dateRange.value[0], dateRange.value[1]);
  }
};

// 响应式调整图表大小
const resizeCharts = () => {
  conferenceChartInstance?.resize();
  courseChartInstance?.resize();
  conferenceTypeChartInstance?.resize();
  courseCategoryChartInstance?.resize();
};

onMounted(() => {
  // 设置默认日期范围为最近30天
  const endDate = new Date();
  const startDate = new Date();
  startDate.setDate(endDate.getDate() - 30);
  
  dateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ];

  loadData();
  initCharts();
  
  // 监听窗口大小变化
  window.addEventListener('resize', resizeCharts);
});

// 清理
const cleanup = () => {
  window.removeEventListener('resize', resizeCharts);
  conferenceChartInstance?.dispose();
  courseChartInstance?.dispose();
  conferenceTypeChartInstance?.dispose();
  courseCategoryChartInstance?.dispose();
};

// 在组件卸载时清理
import { onBeforeUnmount } from 'vue';
onBeforeUnmount(cleanup);
</script>

<style scoped>
.analytics-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.analytics-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.analytics-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.date-filter {
  display: flex;
  gap: 10px;
  align-items: center;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.overview-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.card-icon {
  font-size: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #f0f2f5;
}

.card-info h3 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.card-info p {
  margin: 5px 0 0 0;
  color: #666;
  font-size: 14px;
}

.charts-area {
  margin-top: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-weight: 600;
  color: #333;
}

.chart-container {
  width: 100%;
  height: 350px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}
</style> 