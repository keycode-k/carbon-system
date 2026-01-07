<template>
  <div class="app-container">
    <!-- 账户资产概览 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card class="asset-card">
          <div class="asset-label">账户余额</div>
          <div class="asset-value">¥{{ accountInfo.balance || 0 }}</div>
          <div class="asset-desc">可用资金</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="asset-card">
          <div class="asset-label">冻结金额</div>
          <div class="asset-value frozen">¥{{ accountInfo.frozenAmount || 0 }}</div>
          <div class="asset-desc">挂单占用</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="asset-card">
          <div class="asset-label">总资产</div>
          <div class="asset-value total">¥{{ accountInfo.totalAssets || 0 }}</div>
          <div class="asset-desc">余额+冻结</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="asset-card">
          <div class="asset-label">账户状态</div>
          <div class="asset-value">
            <el-tag v-if="accountInfo.status === 0" type="success">正常</el-tag>
            <el-tag v-else-if="accountInfo.status === 1" type="warning">冻结</el-tag>
            <el-tag v-else type="danger">注销</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 充值提现操作 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>充值</span>
          </template>
          <el-form :model="rechargeForm" label-width="80px">
            <el-form-item label="充值金额">
              <el-input v-model="rechargeForm.amount" type="number" placeholder="请输入充值金额">
                <template #prefix>¥</template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRecharge" :loading="rechargeLoading">
                确认充值
              </el-button>
              <el-button @click="rechargeForm.amount = ''">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>提现</span>
          </template>
          <el-form :model="withdrawForm" label-width="80px">
            <el-form-item label="提现金额">
              <el-input v-model="withdrawForm.amount" type="number" placeholder="请输入提现金额">
                <template #prefix>¥</template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="handleWithdraw" :loading="withdrawLoading">
                确认提现
              </el-button>
              <el-button @click="withdrawForm.amount = ''">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 账户统计信息 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <span>账户统计</span>
      </template>
      <el-descriptions border :column="3">
        <el-descriptions-item label="累计充值">¥{{ accountInfo.totalRecharge || 0 }}</el-descriptions-item>
        <el-descriptions-item label="累计提现">¥{{ accountInfo.totalWithdraw || 0 }}</el-descriptions-item>
        <el-descriptions-item label="净充值">¥{{ (accountInfo.totalRecharge || 0) - (accountInfo.totalWithdraw || 0) }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ accountInfo.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ accountInfo.updateTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ userId }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 交易记录 -->
    <el-card>
      <template #header>
        <span>交易记录</span>
        <el-button style="float: right" size="small" @click="loadAccountInfo">刷新</el-button>
      </template>
      <el-empty v-if="!accountInfo.id" description="暂无交易记录" />
      <el-table v-else :data="accountHistory" border stripe>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.type === 'recharge'" type="success">充值</el-tag>
            <el-tag v-else-if="scope.row.type === 'withdraw'" type="danger">提现</el-tag>
            <el-tag v-else type="info">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="150">
          <template #default="scope">
            <span :class="scope.row.type === 'recharge' ? 'text-success' : 'text-danger'">
              {{ scope.row.type === 'recharge' ? '+' : '-' }}¥{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="账户余额" width="150" />
        <el-table-column prop="time" label="时间" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAccountInfo, recharge, withdraw } from '@/api/trade'

// 当前用户ID（实际应从用户store获取）
const userId = ref(1)

// 账户信息
const accountInfo = ref({})

// 充值表单
const rechargeForm = ref({ amount: '' })
const rechargeLoading = ref(false)

// 提现表单
const withdrawForm = ref({ amount: '' })
const withdrawLoading = ref(false)

// 交易历史（模拟数据）
const accountHistory = ref([
  { type: 'recharge', amount: 20000, balance: 20000, time: '2024-01-15 10:30:00', remark: '初始充值' },
  { type: 'withdraw', amount: 5000, balance: 15000, time: '2024-01-20 14:20:00', remark: '提现到银行卡' },
])

// 加载账户信息
const loadAccountInfo = async () => {
  try {
    const data = await getAccountInfo(userId.value)
    accountInfo.value = data
  } catch (error) {
    console.error('加载账户信息失败:', error)
    ElMessage.error('加载账户信息失败')
  }
}

// 充值
const handleRecharge = async () => {
  if (!rechargeForm.value.amount || rechargeForm.value.amount <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  
  rechargeLoading.value = true
  try {
    await recharge(userId.value, rechargeForm.value.amount)
    ElMessage.success('充值成功')
    rechargeForm.value.amount = ''
    loadAccountInfo()
  } catch (error) {
    console.error('充值失败:', error)
    ElMessage.error('充值失败')
  } finally {
    rechargeLoading.value = false
  }
}

// 提现
const handleWithdraw = async () => {
  if (!withdrawForm.value.amount || withdrawForm.value.amount <= 0) {
    ElMessage.warning('请输入有效的提现金额')
    return
  }
  
  if (withdrawForm.value.amount > accountInfo.value.balance) {
    ElMessage.warning('提现金额不能大于账户余额')
    return
  }
  
  withdrawLoading.value = true
  try {
    await withdraw(userId.value, withdrawForm.value.amount)
    ElMessage.success('提现成功')
    withdrawForm.value.amount = ''
    loadAccountInfo()
  } catch (error) {
    console.error('提现失败:', error)
    ElMessage.error('提现失败')
  } finally {
    withdrawLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadAccountInfo()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.asset-card {
  text-align: center;
  padding: 10px;
}

.asset-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.asset-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.asset-value.frozen {
  color: #E6A23C;
}

.asset-value.total {
  color: #409EFF;
}

.asset-desc {
  font-size: 12px;
  color: #C0C4CC;
}

.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
}
</style>
