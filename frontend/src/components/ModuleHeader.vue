<template>
  <div class="page-header" :style="gradientBackground">
    <h2 class="page-title">
      <el-icon class="title-icon"><component :is="icon" /></el-icon>
      {{ title }}
    </h2>
    <p class="page-description">{{ description }}</p>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import {
  User,
  Tickets,
  DataAnalysis,
  MessageBox,
  Reading,
  OfficeBuilding,
} from '@element-plus/icons-vue';

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
  icon: {
    type: String,
    required: true,
  },
  color: {
    type: String,
    default: 'purple', // 'purple', 'blue', 'green', 'orange', 'red', 'teal'
  },
});

type ColorKey = 'purple' | 'blue' | 'green' | 'orange' | 'red' | 'teal';

const colorMap: Record<ColorKey, string> = {
  purple: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  blue: 'linear-gradient(135deg, #5b86e5 0%, #36d1dc 100%)',
  green: 'linear-gradient(135deg, #2af598 0%, #009efd 100%)',
  orange: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
  red: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  teal: 'linear-gradient(135deg, #90f7ec 0%, #32ccbc 100%)',
};

const gradientBackground = computed(() => {
  const colorKey = props.color as ColorKey;
  return {
    background: colorMap[colorKey] || colorMap.purple,
  };
});
</script>

<style lang="scss" scoped>
.page-header {
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);

  .page-title {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 12px;

    .title-icon {
      font-size: 28px;
    }
  }

  .page-description {
    margin: 0;
    opacity: 0.9;
    font-size: 14px;
  }
}
</style> 