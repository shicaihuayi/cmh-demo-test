import { ref, reactive, provide, onMounted, watchEffect } from 'vue'
import router from '@/router'
import { ElMessage, type FormProps } from 'element-plus'
import axios from 'axios';
import { useHomeStore } from '@/stores/home';
import { useRouter } from 'vue-router';
import request from '@/utils/request';

export default function () {
    const home = useHomeStore()
    const router = useRouter()
    const user = ref({}) // 声明 user 变量

    const act = ref(localStorage.getItem('active') || '1'); // 初始化为localStorage中的值或默认为1
    function MenuSelect(index: any, path: any) {
        console.log(index, path);
        act.value = index;
        localStorage.setItem('active', index);
    }
    watchEffect(() => {
        const storedActive = localStorage.getItem('active');
        if (storedActive !== null) {
            // act.value = storedActive;
            home.updateIndex(storedActive)
        }
    });
    provide('action', act);
    function exit() {
        if (confirm("确定退出吗？")) {
            localStorage.clear();
            sessionStorage.setItem("role",'');
            sessionStorage.clear();
            axios.post("http://localhost:8080/user/exit").then(res => {
                if (res.data.isOk) {
                    ElMessage.success(res.data.msg);
                }
            }
            )
            router.push('/');
        }
    }

    function goMain() {
        act.value = '1';
        router.push({
            name: 'main'
        });
        home.updateIndex('1');
    }
    function goUser() {
        act.value = '2';
        router.push({
            name: 'user'
        });
    }



    function judge() {
        if(sessionStorage.getItem('role')!='3'){
            alert("您不是超级管理员，无权限访问该页面！！！")
          }else{
            act.value = '3';
            router.push({
                name: 'tenant'
            });
          }

    }
    function goDepartment() {
        act.value = '4';
        router.push({
            name: 'department'
        });
    }
    function gotrends() {
        act.value = '5';
        router.push({
            name: 'trend'
        });
    }
    function goCourse() {
        act.value = '6';
        router.push({
            name: 'course'

        });
    }
    function goConference() {
        act.value = '7';
        router.push({
            name: 'conference'

        });
    }
    function goDetail() {
        act.value = '8';
        router.push({
            name: 'myself'

        });
    }

    // 获取用户数据
    const loadUser = async () => {
        try {
            const res: any = await request({ 
                url: '/user/info',
                method: 'get'
            });
            if (res && res.data) { // 增加安全校验
                user.value = res.data;
                sessionStorage.setItem('user', JSON.stringify(res.data));
                sessionStorage.setItem('role', res.data.role);
                sessionStorage.setItem('username', res.data.name);
                sessionStorage.setItem('id', res.data.id);
            } else {
                // 如果获取数据失败，给出提示，并使用 sessionStorage 的缓存
                console.warn("未能从后端获取用户信息，尝试使用缓存。");
                const cachedUser = sessionStorage.getItem('user');
                if (cachedUser) {
                    user.value = JSON.parse(cachedUser);
                } else {
                    ElMessage.error("用户信息加载失败，请重新登录。");
                    router.push('/');
                }
            }
        } catch (error) {
            console.error("加载用户信息时发生错误:", error);
            ElMessage.error("网络错误，用户信息加载失败。");
        }
    }

    return {
        act,
        exit,
        goMain,
        goDepartment,
        goUser,
        gotrends,
        judge,
        goDetail,
        goCourse,
        goConference,
        MenuSelect,

    }
}
