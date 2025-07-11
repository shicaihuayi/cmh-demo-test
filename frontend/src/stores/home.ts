import { defineStore } from 'pinia';
import { ref} from 'vue';

export const useHomeStore = defineStore('home', () => {
    const index = ref<string | null>('1');

    function setHomeInfo() {
        index.value='1'
    }

    function updateIndex(newIndex: string) {
        index.value = newIndex;
    }

    return { index,updateIndex };
});

