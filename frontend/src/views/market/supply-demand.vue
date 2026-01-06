<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input placeholder="搜索关键字" style="width: 200px; margin-right: 10px;" />
      <el-select placeholder="交易方向" style="width: 120px; margin-right: 10px;">
        <el-option label="出售" value="sell" />
        <el-option label="求购" value="buy" />
      </el-select>
      <el-select placeholder="资产类型" style="width: 120px; margin-right: 10px;">
        <el-option label="配额 (SHEA)" value="SHEA" />
        <el-option label="CCER" value="CCER" />
        <el-option label="绿证" value="GEC" />
      </el-select>
      <el-button type="primary" icon="Search">筛选</el-button>
      <el-button type="success" icon="Plus" style="float: right;">发布供需</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in items" :key="index" style="margin-bottom: 20px;">
        <el-card shadow="hover" class="box-card">
          <div class="card-header">
            <el-tag :type="item.type === '出售' ? 'success' : 'warning'" effect="dark">{{ item.type }}</el-tag>
            <span class="asset-name">{{ item.asset }}</span>
          </div>
          <div class="card-body">
             <div class="info-row">
               <span class="label">数量:</span>
               <span class="value">{{ item.amount }} 吨</span>
             </div>
             <div class="info-row">
               <span class="label">价格:</span>
               <span class="value price">{{ item.price }} 元/吨</span>
             </div>
             <div class="info-row">
               <span class="label">履约年份:</span>
               <span class="value">{{ item.year }}</span>
             </div>
             <div class="info-row">
               <span class="label">截止日期:</span>
               <span class="value">{{ item.deadline }}</span>
             </div>
          </div>
          <div class="card-footer">
             <div class="user-info">
                <el-avatar :size="24" style="margin-right: 5px;">{{ item.user.charAt(0) }}</el-avatar>
                <span>{{ item.user }}</span>
             </div>
             <el-button link type="primary">联系TA</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-pagination layout="prev, pager, next" :total="50" style="justify-content: center; margin-top: 20px;" />
  </div>
</template>

<script setup>
const items = [
  { type: '出售', asset: 'SHEA 2024', amount: '5,000', price: '66.50', year: '2024', deadline: '2025-06-30', user: '上海**化工' },
  { type: '求购', asset: 'CCER 林业', amount: '10,000', price: '面议', year: '不限', deadline: '2025-05-30', user: '某投资机构' },
  { type: '出售', asset: 'GDEA', amount: '2,000', price: '78.00', year: '2024', deadline: '2025-04-15', user: '广东**电力' },
  { type: '求购', asset: 'SHEA 2023', amount: '1,000', price: '64.00', year: '2023', deadline: '2025-06-01', user: '贸易商A' },
  { type: '出售', asset: 'I-REC (风电)', amount: '50,000', price: '2.50', year: '2025', deadline: '长期有效', user: '新能源公司' },
  { type: '求购', asset: 'CCER 垃圾焚烧', amount: '5,000', price: '45.00', year: '2022+', deadline: '2025-05-20', user: '履约企业B' },
  { type: '出售', asset: 'SHEA 2024', amount: '8,000', price: '66.00', year: '2024', deadline: '2025-06-25', user: '水泥厂C' },
  { type: '出售', asset: 'SHEA 2024', amount: '3,000', price: '67.00', year: '2024', deadline: '2025-06-10', user: '钢厂D' },
]
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; background: #fff; padding: 15px; border-radius: 4px; }
.card-header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f0f0f0; padding-bottom: 10px; margin-bottom: 10px; }
.asset-name { font-weight: bold; font-size: 16px; }
.info-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
.label { color: #909399; }
.value { color: #303133; }
.price { color: #f56c6c; font-weight: bold; }
.card-footer { border-top: 1px solid #f0f0f0; padding-top: 10px; margin-top: 15px; display: flex; justify-content: space-between; align-items: center; }
.user-info { display: flex; align-items: center; font-size: 12px; color: #606266; }
</style>
