<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 市场概况 -->
      <el-col :span="6">
         <el-card shadow="never" class="market-summary">
            <template #header>行情快览</template>
            <div class="price-item up">
              <span class="label">SHEA 2024</span>
              <span class="price">65.80 <i class="trend">↑ 1.2%</i></span>
            </div>
            <div class="price-item down">
              <span class="label">HBEA</span>
              <span class="price">42.50 <i class="trend">↓ 0.5%</i></span>
            </div>
            <div class="price-item">
              <span class="label">CCER (林业)</span>
              <span class="price">58.00 <i class="trend">-</i></span>
            </div>
         </el-card>
      </el-col>
      
      <!-- 报盘列表 -->
      <el-col :span="18">
         <el-card shadow="hover">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="买盘列表 (Bid)" name="buy">
                 <el-table :data="buyData" stripe>
                    <el-table-column prop="product" label="产品" width="120" />
                    <el-table-column prop="price" label="买入价 (元)" sortable width="150">
                       <template #default="scope"><span style="color: #f56c6c; font-weight: bold">{{ scope.row.price }}</span></template>
                    </el-table-column>
                    <el-table-column prop="amount" label="求购数量 (吨)" />
                    <el-table-column prop="trader" label="交易员" />
                    <el-table-column prop="time" label="挂单时间" />
                    <el-table-column label="操作">
                       <template #default><el-button size="small" type="primary" plain>出售</el-button></template>
                    </el-table-column>
                 </el-table>
              </el-tab-pane>
              <el-tab-pane label="卖盘列表 (Ask)" name="sell">
                 <el-table :data="sellData" stripe>
                    <el-table-column prop="product" label="产品" width="120" />
                    <el-table-column prop="price" label="卖出价 (元)" sortable width="150">
                       <template #default="scope"><span style="color: #67c23a; font-weight: bold">{{ scope.row.price }}</span></template>
                    </el-table-column>
                    <el-table-column prop="amount" label="出售数量 (吨)" />
                    <el-table-column prop="trader" label="交易员" />
                    <el-table-column prop="time" label="挂单时间" />
                    <el-table-column label="操作">
                       <template #default><el-button size="small" type="success" plain>购买</el-button></template>
                    </el-table-column>
                 </el-table>
              </el-tab-pane>
            </el-tabs>
         </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('sell')

const buyData = [
  { product: 'SHEA2024', price: 65.50, amount: 5000, trader: 'Trader_A', time: '10:45:00' },
  { product: 'CCER', price: 55.00, amount: 10000, trader: 'Carbon_Fund', time: '10:42:12' },
]

const sellData = [
  { product: 'SHEA2024', price: 66.00, amount: 2000, trader: 'Factory_X', time: '10:46:22' },
  { product: 'GDEA', price: 80.00, amount: 500, trader: 'Trader_B', time: '10:30:00' },
]
</script>

<style scoped>
.app-container { padding: 20px; }
.market-summary { height: 100%; }
.price-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 15px 0; border-bottom: 1px solid #f0f0f0;
}
.price-item:last-child { border-bottom: none; }
.price-item .label { font-weight: 500; font-size: 16px; }
.price-item .price { font-family: 'Arial'; font-weight: bold; font-size: 18px; }
.price-item.up .price { color: #f56c6c; }
.price-item.down .price { color: #67c23a; }
.trend { font-style: normal; font-size: 12px; margin-left: 5px; }
</style>
