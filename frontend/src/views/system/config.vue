<template>
  <div class="app-container">
    <el-card>
      <template #header>基础配置管理</template>
      <el-collapse>
         <el-collapse-item title="角色权限设置">
           角色列表，权限分配...
         </el-collapse-item>
         <el-collapse-item title="数据字典">
           系统常用枚举值配置...
         </el-collapse-item>
         <el-collapse-item title="菜单配置">
           前端动态路由配置...
         </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>
<script setup></script>
<style scoped>.app-container{padding:20px;}</style>
