import { ref, reactive, provide, onMounted, watchEffect } from 'vue'
import router from '@/router'
import { ElMessage, type FormProps } from 'element-plus'
import axios from 'axios';
import { useHomeStore } from '@/stores/home';
export default function () {
    const home = useHomeStore()

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


