import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'

export default function useAuth() {
  const router = useRouter()
  const loading = ref(false)

  const handleLogin = async (formData: any) => {
    try {
      loading.value = true
      console.log('登录请求数据:', formData)

      const response = await login(formData)
      console.log('登录响应:', response)

      if (response.isOk) {
        // 保存用户信息到sessionStorage
        sessionStorage.setItem('username', formData.username)
        sessionStorage.setItem('role', response.role)
        sessionStorage.setItem('id', response.id)
        
        // 保存token
        if (response.token) {
          sessionStorage.setItem('token', response.token)
          console.log('Token已保存:', response.token)
        } else {
          console.warn('响应中没有token')
        }

        ElMessage.success('登录成功')
        router.push('/home')
      } else {
        ElMessage.error(response.msg || '登录失败')
      }
    } catch (error: any) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请重试')
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
    handleLogin,
    handleLogout
  }
} 