import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useDashboardStore = defineStore('dashboard', () => {
  // 全局的刷新计数器，每次需要刷新时加1
  const refreshCounter = ref(0);
  
  // 最后刷新的时间戳
  const lastRefresh = ref(0);

  /**
   * 触发仪表盘数据刷新
   * 该方法会增加刷新计数器，
   * 任何监听该计数器的组件都会收到通知并刷新
   */
  function triggerRefresh() {
    refreshCounter.value++;
    lastRefresh.value = Date.now();
    console.log('触发仪表盘数据刷新, 刷新计数:', refreshCounter.value);
  }

  /**
   * 节流版本的刷新触发器
   * 短时间内只会执行一次刷新（默认1秒内）
   */
  function triggerRefreshThrottled(delay = 1000) {
    const now = Date.now();
    if (now - lastRefresh.value > delay) {
      triggerRefresh();
    } else {
      console.log('刷新请求被节流，跳过');
    }
  }

  return {
    refreshCounter,
    lastRefresh,
    triggerRefresh,
    triggerRefreshThrottled
  };
}); 