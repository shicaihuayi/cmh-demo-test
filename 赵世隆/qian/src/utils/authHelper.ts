import { ElMessage } from 'element-plus'
import { loadMyself } from '@/api/user'
import { getNewsList } from '@/api/news'
import router from '@/router'

/**
 * 认证帮助工具类
 */
export class AuthHelper {
  /**
   * 检查用户登录状态并获取新闻列表
   * 这是您提供的示例代码的完整实现
   */
  static async fetchNews(searchParams: any = {}) {
    try {
      const response = await loadMyself(); // 检查登录状态
      
      if ((response as any).isLogin) {
        // 用户已登录，可以获取新闻列表
        const newsResponse = await getNewsList(searchParams);
        
        ElMessage.success('新闻列表获取成功');
        
        return {
          success: true,
          data: newsResponse,
          userInfo: (response as any).user,
          message: '新闻列表获取成功'
        };
      } else {
        // 用户未登录，显示登录提示或跳转到登录页
        console.log('用户未登录，请先登录');
        ElMessage.warning('用户未登录，请先登录');
        
        // 可以选择跳转到登录页
        // router.push('/login');
        
        return {
          success: false,
          needLogin: true,
          message: '用户未登录，请先登录'
        };
      }
    } catch (error) {
      console.error('Error:', error);
      ElMessage.error('获取数据失败，请稍后重试');
      
      return {
        success: false,
        error: error,
        message: '获取数据失败'
      };
    }
  }

  /**
   * 通用的登录状态检查方法
   */
  static async checkLoginStatus(): Promise<{isLogin: boolean, userInfo?: any}> {
    try {
      const response = await loadMyself();
      return {
        isLogin: (response as any).isLogin || false,
        userInfo: (response as any).isLogin ? (response as any).user : null
      };
    } catch (error) {
      console.error('登录状态检查失败:', error);
      return {
        isLogin: false,
        userInfo: null
      };
    }
  }

  /**
   * 执行需要登录权限的操作
   */
  static async executeWithAuth<T>(
    operation: () => Promise<T>,
    options: {
      errorMessage?: string;
      redirectToLogin?: boolean;
      showMessage?: boolean;
    } = {}
  ): Promise<T | null> {
    const {
      errorMessage = '操作失败，请先登录',
      redirectToLogin = false,
      showMessage = true
    } = options;

    try {
      const { isLogin } = await this.checkLoginStatus();
      
      if (isLogin) {
        return await operation();
      } else {
        if (showMessage) {
          ElMessage.warning(errorMessage);
        }
        
        if (redirectToLogin) {
          router.push('/login');
        }
        
        return null;
      }
    } catch (error) {
      console.error('执行认证操作失败:', error);
      if (showMessage) {
        ElMessage.error('操作失败，请稍后重试');
      }
      return null;
    }
  }

  /**
   * 登出方法
   */
  static logout() {
    // 清除所有本地存储的用户信息
    const keysToRemove = ['id', 'username', 'name', 'pwd', 'companyName', 'role', 'tel', 'img'];
    keysToRemove.forEach(key => {
      sessionStorage.removeItem(key);
      localStorage.removeItem(key);
    });
    
    ElMessage.success('已退出登录');
    router.push('/login');
  }
}

/**
 * 使用示例
 */
export const authExamples = {
  /**
   * 示例1: 获取新闻列表（您提供的代码示例）
   */
  fetchNewsExample: async () => {
    const searchParams = { page: 1, size: 10 };
    const result = await AuthHelper.fetchNews(searchParams);
    
    if (result.success) {
      console.log('新闻数据:', result.data);
      console.log('用户信息:', result.userInfo);
      // 处理新闻数据...
    } else if (result.needLogin) {
      console.log('需要登录');
      // 显示登录页面或跳转...
    } else {
      console.log('获取失败:', result.message);
    }
  },

  /**
   * 示例2: 简单的登录状态检查
   */
  simpleLoginCheck: async () => {
    const { isLogin, userInfo } = await AuthHelper.checkLoginStatus();
    
    if (isLogin) {
      console.log('用户已登录:', userInfo);
    } else {
      console.log('用户未登录');
    }
  },

  /**
   * 示例3: 执行需要登录的操作
   */
  executeProtectedOperation: async () => {
    const result = await AuthHelper.executeWithAuth(
      async () => {
        // 这里是需要登录才能执行的操作
        const newsData = await getNewsList({ page: 1, size: 5 });
        return newsData;
      },
      {
        errorMessage: '获取新闻需要先登录',
        redirectToLogin: true,
        showMessage: true
      }
    );
    
    if (result) {
      console.log('操作成功:', result);
    } else {
      console.log('操作被取消或失败');
    }
  }
};

/**
 * Vue 组合式API 的使用示例
 */
export function useAuthExample() {
  const fetchNewsWithAuth = async (searchParams: any) => {
    return await AuthHelper.fetchNews(searchParams);
  };

  const checkLogin = async () => {
    return await AuthHelper.checkLoginStatus();
  };

  const logout = () => {
    AuthHelper.logout();
  };

  return {
    fetchNewsWithAuth,
    checkLogin,
    logout
  };
} 