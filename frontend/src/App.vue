<!--
  测盟汇前端 - 根组件
  功能说明：
  - 应用根容器
  - 路由视图渲染
  - 全局样式设置
  
  @author Wang Shenjun
  @date 2025.07.12
  @version 1.0.0
-->
<template>
  <div id="app">
    <RouterView></RouterView>
  </div>
</template>

<script lang="ts">
import {RouterView} from 'vue-router'

/**
 * 应用根组件
 * 
 * 功能：作为整个应用的根容器，提供路由视图渲染
 * 
 * @author Wang Shenjun
 * @date 2025.07.12
 */
export default {
  name: 'App',
}
</script>

<style>
body{
  margin: 0;
}
</style>

