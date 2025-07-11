import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'
import { AuthHelper } from '@/utils/authHelper'

export default function useAuth() {
  const router = useRouter()
  const loading = ref(false)
  const formData = reactive({
    username: '',
    password: ''
  })

  const handleLogin = async () => {
    try {
      loading.value = true
      console.log('登录请求数据:', formData)

      const response = await login(formData)
      console.log('登录响应:', response)

      if (response.isOk) {
        console.log('login API: 登录成功, 正在存储用户信息', response.user)
        const user = response.user
        AuthHelper.setAuth(user, 'session-based') // 使用假的token，因为后端是session认证
        console.log(
          'login API: 用户信息存储完成',
          AuthHelper.getAuth(),
          '准备跳转到首页'
        )
        router.push('/')
      } else {
        console.error('login API: 登录失败', response.msg)
        ElMessage.error(response.msg || '登录失败，请检查您的用户名和密码')
      }
    } catch (error: any) {
      console.error('登录时发生异常:', error)
      ElMessage.error('登录时发生网络或服务器错误')
    } finally {
      loading.value = false
    }
  }

  const handleLogout = () => {
    // 清除所有sessionStorage数据
    sessionStorage.clear()
    router.push('/login')
    ElMessage.success('已退出登录')
  }

  return {
    loading,
    formData,
    handleLogin,
    handleLogout
  }
} 