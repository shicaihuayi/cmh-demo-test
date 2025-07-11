import { defineStore } from 'pinia';
import { ref } from 'vue';

// 定义任务类型的接口
export interface PendingTaskSummary {
  courses: number;
  news: number;
  conferences: number;
  total: number;
}

export const useTaskStore = defineStore('task', () => {
    const num = ref<number>(-1);
    const pendingTasks = ref<PendingTaskSummary>({
      courses: 0,
      news: 0,
      conferences: 0,
      total: 0
    });

    function setTaskInfo(taskInfo: { num: number }) {
        num.value = taskInfo.num;
    }

    function updateNum(newNum: number) {
        num.value = newNum;
    }

    function updatePendingTasks(tasks: PendingTaskSummary) {
        pendingTasks.value = tasks;
        num.value = tasks.total;
    }

    // 获取优先级最高的待审核类型
    function getHighestPriorityTask(): string | null {
        if (pendingTasks.value.conferences > 0) return 'conference';
        if (pendingTasks.value.news > 0) return 'news';
        if (pendingTasks.value.courses > 0) return 'course';
        return null;
    }

    return { 
        num, 
        pendingTasks, 
        updateNum, 
        updatePendingTasks, 
        getHighestPriorityTask 
    };
});