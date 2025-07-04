import axios from 'axios'

/**
 * 调试帮助工具
 */
export class DebugHelper {
  /**
   * 测试个人中心API调用
   */
  static async testMyselfAPI() {
    console.log('=== 开始测试个人中心API ===');
    
    // 检查sessionStorage中的用户信息
    const sessionData = {
      id: sessionStorage.getItem('id'),
      username: sessionStorage.getItem('username'),
      name: sessionStorage.getItem('name'),
      role: sessionStorage.getItem('role'),
      tel: sessionStorage.getItem('tel'),
      companyName: sessionStorage.getItem('companyName')
    };
    
    console.log('Session Storage数据:', sessionData);
    
    try {
      console.log('发送请求到: http://localhost:8080/user/loadMyself');
      const response = await axios.post('http://localhost:8080/user/loadMyself');
      
      console.log('完整响应对象:', response);
      console.log('响应状态码:', response.status);
      console.log('响应头:', response.headers);
      console.log('响应数据:', response.data);
      
      if (response.data) {
        console.log('数据结构分析:');
        console.log('- isOk:', response.data.isOk);
        console.log('- msg:', response.data.msg);
        console.log('- user:', response.data.user);
        console.log('- isLogin:', response.data.isLogin);
      }
      
      return {
        success: true,
        data: response.data,
        sessionData
      };
      
    } catch (error: any) {
      console.error('请求失败详情:', error);
      console.error('错误消息:', error.message);
      console.error('错误堆栈:', error.stack);
      
      if (error.response) {
        console.error('错误响应状态:', error.response.status);
        console.error('错误响应数据:', error.response.data);
        console.error('错误响应头:', error.response.headers);
      } else if (error.request) {
        console.error('请求对象:', error.request);
      }
      
      return {
        success: false,
        error: error,
        sessionData
      };
    }
  }

  /**
   * 诊断网络连接
   */
  static async diagnoseNetwork() {
    console.log('=== 开始网络诊断 ===');
    
    try {
      // 测试基础连通性
      const baseUrl = 'http://localhost:8080';
      const healthCheck = await axios.get(`${baseUrl}/health`, { timeout: 5000 });
      console.log('健康检查成功:', healthCheck.data);
    } catch (error) {
      console.error('健康检查失败:', error);
    }
    
    try {
      // 测试简单的GET请求
      const simpleTest = await axios.get('http://localhost:8080/', { timeout: 5000 });
      console.log('简单GET请求成功');
    } catch (error) {
      console.error('简单GET请求失败:', error);
    }
  }

  /**
   * 完整的问题诊断流程
   */
  static async fullDiagnosis() {
    console.log('🔍 开始完整问题诊断...');
    
    // 1. 检查浏览器环境
    console.log('浏览器信息:', {
      userAgent: navigator.userAgent,
      language: navigator.language,
      cookieEnabled: navigator.cookieEnabled
    });
    
    // 2. 检查本地存储
    console.log('LocalStorage支持:', typeof(Storage) !== "undefined");
    console.log('SessionStorage支持:', typeof(sessionStorage) !== "undefined");
    
    // 3. 网络诊断
    await this.diagnoseNetwork();
    
    // 4. API测试
    const apiResult = await this.testMyselfAPI();
    
    // 5. 生成诊断报告
    const report = {
      timestamp: new Date().toISOString(),
      sessionData: apiResult.sessionData,
      apiSuccess: apiResult.success,
      apiData: apiResult.success ? apiResult.data : null,
      apiError: !apiResult.success ? apiResult.error?.message : null,
      recommendations: this.generateRecommendations(apiResult)
    };
    
    console.log('📋 诊断报告:', report);
    return report;
  }

  /**
   * 生成修复建议
   */
  private static generateRecommendations(apiResult: any): string[] {
    const recommendations: string[] = [];
    
    if (!apiResult.success) {
      if (apiResult.error?.message?.includes('Network Error')) {
        recommendations.push('检查后端服务是否正常运行 (http://localhost:8080)');
        recommendations.push('检查防火墙设置');
        recommendations.push('确认网络连接正常');
      } else if (apiResult.error?.message?.includes('timeout')) {
        recommendations.push('服务器响应超时，检查服务器性能');
        recommendations.push('增加请求超时时间');
      } else if (apiResult.error?.message?.includes('用户未登录')) {
        recommendations.push('检查sessionStorage中的用户信息是否完整');
        recommendations.push('重新登录系统');
        recommendations.push('检查后端登录验证逻辑');
      }
    } else {
      if (!apiResult.sessionData?.id || !apiResult.sessionData?.username) {
        recommendations.push('补充完整的用户会话信息');
      }
      if (!apiResult.data?.isOk) {
        recommendations.push('检查后端业务逻辑返回的错误信息');
      }
    }
    
    return recommendations;
  }
}

// 使用示例：在浏览器控制台中运行
// import { DebugHelper } from '@/utils/debugHelper'
// DebugHelper.fullDiagnosis() 