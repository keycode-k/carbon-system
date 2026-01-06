<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="search-wrapper">
         <el-form :inline="true" :model="formInline" class="demo-form-inline">
            <el-form-item label="申请人">
              <el-input v-model="formInline.user" placeholder="审批人姓名" />
            </el-form-item>
            <el-form-item label="审批类型">
              <el-select v-model="formInline.region" placeholder="全部类型">
                <el-option label="资产登记" value="register" />
                <el-option label="交易申请" value="trade" />
                <el-option label="账户变更" value="account" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search">查询</el-button>
            </el-form-item>
         </el-form>
      </div>

      <el-tabs v-model="activeName" class="demo-tabs">
        <el-tab-pane label="待审批" name="pending">
           <el-table :data="pendingData" style="width: 100%" stripe>
              <el-table-column prop="id" label="单号" width="140" />
              <el-table-column prop="title" label="审批标题" min-width="200" />
              <el-table-column prop="applicant" label="申请人" width="120" />
              <el-table-column prop="time" label="提交时间" width="180" />
              <el-table-column label="操作" width="150" fixed="right">
                 <template #default>
                    <el-button type="primary" link icon="Check">通过</el-button>
                    <el-button type="danger" link icon="Close">驳回</el-button>
                 </template>
              </el-table-column>
           </el-table>
        </el-tab-pane>
        <el-tab-pane label="已处理" name="processed">
           <el-empty description="暂无历史记录" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const activeName = ref('pending')
const formInline = reactive({
  user: '',
  region: ''
})

const pendingData = [
  { id: 'AP20260106001', title: '关于 CCER 项目(ID:882) 的减排量签发申请', applicant: '张三', time: '2026-01-06 09:30:00' },
  { id: 'AP20260106002', title: '企业账户信息变更申请', applicant: '李四', time: '2026-01-05 14:20:00' },
]
</script>

<style scoped>
.app-container { padding: 20px; }
.search-wrapper { margin-bottom: 20px; }
</style>

