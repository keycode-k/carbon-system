<template>
  <div class="app-container">
    <el-card>
      <template #header>方法学管理</template>
      <el-form :inline="true">
         <el-form-item label="名称">
           <el-input placeholder="请输入方法学名称" />
         </el-form-item>
         <el-form-item>
           <el-button type="primary">查询</el-button>
         </el-form-item>
      </el-form>
      <el-empty description="暂无方法学数据" />
    </el-card>
  </div>
</template>
<script setup></script>
<style scoped>.app-container{padding:20px;}</style>
