<template>
  <div class="app-container">
    <el-card>
      <template #header>交易账户资产</template>
      <el-descriptions border :column="2">
         <el-descriptions-item label="资金余额">¥ 10,000,000.00</el-descriptions-item>
         <el-descriptions-item label="冻结资金">¥ 500,000.00</el-descriptions-item>
         <el-descriptions-item label="可用资金">¥ 9,500,000.00</el-descriptions-item>
         <el-descriptions-item label="持仓市值">¥ 2,000,000.00</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>
<script setup></script>
<style scoped>.app-container{padding:20px;}</style>
