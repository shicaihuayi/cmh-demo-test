import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useTaskStore = defineStore('task', () => {
    const num = ref<number>(-1);

    function setTaskInfo(taskInfo: { num: number }) {
        num.value = taskInfo.num;
    }

    function updateNum(newNum: number) {
        num.value = newNum;
    }

    return { num, updateNum };
});

