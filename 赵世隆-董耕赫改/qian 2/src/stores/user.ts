import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
    const name = ref<string | null>(null);
    const role = ref<string | null>(null);
    const headImg = ref<string>('');

    function setUserInfo(userInfo: { name: string, role: string, headImg: string }) {
        name.value = userInfo.name;
        role.value = userInfo.role;
        headImg.value = userInfo.headImg;
    }

    function updateHeadImg(newHeadImg: string) {
        headImg.value = newHeadImg;
    }

    return { name, role, headImg, setUserInfo, updateHeadImg };
});

