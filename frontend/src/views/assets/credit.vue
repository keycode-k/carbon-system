<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <div class="stat-box bg-blue">
          <div class="title">总持有量 (tCO2e)</div>
          <div class="num">150,000</div>
          <div class="trend">较上月 +5% <el-icon><Top /></el-icon></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-box bg-green">
          <div class="title">今日新增</div>
          <div class="num">2,000</div>
          <div class="trend">-</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-box bg-purple">
          <div class="title">冻结/交易中</div>
          <div class="num">15,000</div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="hover">
      <!-- 搜索栏 -->
      <div class="filter-container">
        <el-input v-model="searchQuery" placeholder="项目名称/ID" style="width: 200px; margin-right: 10px;" prefix-icon="Search" />
        <el-select v-model="statusFilter" placeholder="状态" style="width: 120px; margin-right: 10px;">
          <el-option label="全部" value="" />
          <el-option label="持有中" value="active" />
          <el-option label="交易中" value="trading" />
          <el-option label="已注销" value="retired" />
        </el-select>
        <el-button type="primary" icon="Search">搜索</el-button>
        <el-button icon="Refresh">重置</el-button>
        <div style="float: right;">
           <el-button type="success" icon="Plus">登记资产</el-button>
           <el-button type="warning" icon="Download">导出</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" border style="width: 100%; margin-top: 20px" stripe>
        <el-table-column prop="id" label="资产ID" width="120" />
        <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="数量 (吨)" width="150" align="right" sortable />
        <el-table-column prop="vintage" label="核证年份" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusMap[scope.row.status]">{{ statusText[scope.row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small">详情</el-button>
            <el-button link type="success" size="small" v-if="scope.row.status === 'active'">挂牌</el-button>
            <el-button link type="danger" size="small" v-if="scope.row.status === 'active'">注销</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, jumper" :total="100" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const searchQuery = ref('')
const statusFilter = ref('')

const statusMap = {
  active: 'success',
  trading: 'warning',
  retired: 'info'
}

const statusText = {
  active: '持有中',
  trading: '交易中',
  retired: '已注销'
}

// 模拟数据
const tableData = [
  { id: 'ASSET001', projectName: '四川省凉山州风电项目', type: 'CCER', amount: 50000, vintage: '2023', status: 'active' },
  { id: 'ASSET002', projectName: '河北省塞罕坝林业碳汇', type: 'CCER', amount: 12000, vintage: '2022', status: 'trading' },
  { id: 'ASSET003', projectName: '某生物质发电项目一期', type: 'VCS', amount: 8000, vintage: '2021', status: 'active' },
  { id: 'ASSET004', projectName: '湖北省农村沼气项目', type: 'CCER', amount: 5000, vintage: '2023', status: 'retired' },
]
</script>

<style scoped>
.app-container { padding: 20px; }
.stat-box {
  padding: 20px;
  border-radius: 8px;
  color: white;
  margin-bottom: 0;
}
.bg-blue { background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%); }
.bg-green { background: linear-gradient(135deg, #52c41a 0%, #95de64 100%); }
.bg-purple { background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%); }

.stat-box .title { font-size: 14px; opacity: 0.9; margin-bottom: 5px; }
.stat-box .num { font-size: 28px; font-weight: bold; }
.stat-box .trend { margin-top: 10px; font-size: 12px; display: flex; align-items: center; }

.filter-container { margin-bottom: 10px; }
.pagination-container { margin-top: 20px; text-align: right; }
</style>
