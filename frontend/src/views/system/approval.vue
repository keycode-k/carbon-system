<template>
  <div class="app-container">
    <!-- 流程管理 -->
    <el-card shadow="never" class="mb20">
      <template #header>
        <div class="card-header">
          <span>审批流程管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAddFlow">新增流程</el-button>
        </div>
      </template>
      
      <el-table v-loading="flowLoading" :data="flowList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="flowName" label="流程名称" width="150" />
        <el-table-column prop="flowCode" label="流程编码" width="160">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.flowCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="130">
          <template #default="{ row }">
            <el-tag size="small">{{ getBusinessTypeName(row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Setting" @click="handleConfigNodes(row)">配置节点</el-button>
            <el-button type="primary" link :icon="Edit" @click="handleEditFlow(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDeleteFlow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审批记录 -->
    <el-card shadow="never">
      <template #header>
        <span>审批记录</span>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待我审批" name="pending">
          <el-table v-loading="recordLoading" :data="pendingList" style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="businessTitle" label="业务标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="businessType" label="业务类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getBusinessTypeName(row.businessType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicantName" label="申请人" width="100" />
            <el-table-column prop="currentNodeName" label="当前节点" width="120" />
            <el-table-column prop="applyTime" label="申请时间" width="170" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="success" link :icon="Check" @click="handleApprove(row)">通过</el-button>
                <el-button type="danger" link :icon="Close" @click="handleReject(row)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="我发起的" name="applied">
          <el-table v-loading="recordLoading" :data="appliedList" style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="businessTitle" label="业务标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="businessType" label="业务类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getBusinessTypeName(row.businessType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentNodeName" label="当前节点" width="120" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyTime" label="申请时间" width="170" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link :icon="View" @click="handleViewRecord(row)">详情</el-button>
                <el-button v-if="row.status < 2" type="warning" link @click="handleWithdraw(row)">撤回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="全部记录" name="all">
          <el-form :inline="true" :model="queryParams" class="search-form">
            <el-form-item label="业务类型">
              <el-select v-model="queryParams.businessType" placeholder="请选择" clearable style="width: 150px">
                <el-option v-for="item in businessTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="待审批" :value="0" />
                <el-option label="审批中" :value="1" />
                <el-option label="已通过" :value="2" />
                <el-option label="已驳回" :value="3" />
                <el-option label="已撤回" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="loadAllRecords">搜索</el-button>
            </el-form-item>
          </el-form>
          
          <el-table v-loading="recordLoading" :data="allRecordList" style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="businessTitle" label="业务标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="businessType" label="业务类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getBusinessTypeName(row.businessType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicantName" label="申请人" width="100" />
            <el-table-column prop="currentNodeName" label="当前节点" width="120" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyTime" label="申请时间" width="170" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link :icon="View" @click="handleViewRecord(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="queryParams.page"
              v-model:page-size="queryParams.size"
              :page-sizes="[10, 20, 50]"
              :total="recordTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="loadAllRecords"
              @current-change="loadAllRecords"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 流程编辑对话框 -->
    <el-dialog v-model="flowDialogVisible" :title="flowDialogTitle" width="500px">
      <el-form ref="flowFormRef" :model="flowForm" :rules="flowRules" label-width="100px">
        <el-form-item label="流程名称" prop="flowName">
          <el-input v-model="flowForm.flowName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="流程编码" prop="flowCode">
          <el-input v-model="flowForm.flowCode" placeholder="请输入流程编码" :disabled="!!flowForm.id" />
        </el-form-item>
        <el-form-item label="业务类型" prop="businessType">
          <el-select v-model="flowForm.businessType" placeholder="请选择业务类型" style="width: 100%">
            <el-option v-for="item in businessTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="flowForm.description" type="textarea" :rows="3" placeholder="请输入流程描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="flowForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="flowDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFlowForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 节点配置对话框 -->
    <el-dialog v-model="nodeDialogVisible" title="配置审批节点" width="700px">
      <div class="node-config">
        <div class="node-header">
          <span>流程：{{ currentFlow?.flowName }}</span>
          <el-button type="primary" size="small" :icon="Plus" @click="addNode">添加节点</el-button>
        </div>
        
        <el-table :data="nodeList" style="width: 100%">
          <el-table-column prop="nodeOrder" label="序号" width="60" />
          <el-table-column prop="nodeName" label="节点名称" width="150">
            <template #default="{ row }">
              <el-input v-model="row.nodeName" size="small" placeholder="节点名称" />
            </template>
          </el-table-column>
          <el-table-column prop="approverType" label="审批类型" width="130">
            <template #default="{ row }">
              <el-select v-model="row.approverType" size="small">
                <el-option label="角色审批" :value="1" />
                <el-option label="指定用户" :value="2" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="approverName" label="审批人/角色" width="150">
            <template #default="{ row }">
              <el-input v-model="row.approverName" size="small" placeholder="审批人名称" />
            </template>
          </el-table-column>
          <el-table-column label="允许驳回" width="90">
            <template #default="{ row }">
              <el-switch v-model="row.allowReject" :active-value="1" :inactive-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row, $index }">
              <el-button type="danger" link :icon="Delete" @click="removeNode($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="nodeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNodes">保存节点</el-button>
      </template>
    </el-dialog>

    <!-- 审批详情对话框 -->
    <el-dialog v-model="recordDetailVisible" title="审批详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="业务标题" :span="2">{{ currentRecord.businessTitle }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ getBusinessTypeName(currentRecord.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRecord.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ currentRecord.currentNodeName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord.status)" size="small">
            {{ getStatusName(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRecord.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentRecord.completeTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">审批历史</el-divider>
      <el-timeline>
        <el-timeline-item
          v-for="(history, index) in approvalHistories"
          :key="index"
          :type="history.action === 1 ? 'success' : history.action === 2 ? 'danger' : 'primary'"
          :timestamp="history.approveTime"
        >
          <div>
            <strong>{{ history.nodeName }}</strong> - {{ history.approverName }}
            <el-tag :type="history.action === 1 ? 'success' : 'danger'" size="small" style="margin-left: 10px;">
              {{ history.action === 1 ? '通过' : history.action === 2 ? '驳回' : '转交' }}
            </el-tag>
          </div>
          <p v-if="history.comment" style="color: #909399; margin-top: 5px;">{{ history.comment }}</p>
        </el-timeline-item>
      </el-timeline>
      
      <template #footer>
        <el-button @click="recordDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveDialogVisible" :title="approveDialogTitle" width="450px">
      <el-form :model="approveForm" label-width="80px">
        <el-form-item label="审批意见" :required="approveAction === 'reject'">
          <el-input v-model="approveForm.comment" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button :type="approveAction === 'approve' ? 'success' : 'danger'" @click="submitApprove">
          {{ approveAction === 'approve' ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Edit, Delete, Setting, Search, Check, Close, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 业务类型
const businessTypes = [
  { label: '资产发行', value: 'ASSET_ISSUE' },
  { label: '大额交易', value: 'TRADE_LARGE' },
  { label: '企业认证', value: 'COMPANY_VERIFY' },
  { label: '项目立项', value: 'PROJECT_CREATE' },
  { label: '减排核算', value: 'CARBON_CALCULATE' }
]

const getBusinessTypeName = (type) => {
  const item = businessTypes.find(t => t.value === type)
  return item ? item.label : type
}

const getStatusName = (status) => {
  const map = { 0: '待审批', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已撤回' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}

// 流程管理
const flowLoading = ref(false)
const flowList = ref([])
const flowDialogVisible = ref(false)
const flowDialogTitle = ref('')
const flowFormRef = ref(null)
const flowForm = reactive({
  id: null,
  flowName: '',
  flowCode: '',
  businessType: '',
  description: '',
  status: 1
})
const flowRules = {
  flowName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
  flowCode: [{ required: true, message: '请输入流程编码', trigger: 'blur' }],
  businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }]
}

// 节点配置
const nodeDialogVisible = ref(false)
const currentFlow = ref(null)
const nodeList = ref([])

// 审批记录
const activeTab = ref('pending')
const recordLoading = ref(false)
const pendingList = ref([])
const appliedList = ref([])
const allRecordList = ref([])
const recordTotal = ref(0)
const queryParams = reactive({
  businessType: '',
  status: null,
  page: 1,
  size: 10
})

// 审批详情
const recordDetailVisible = ref(false)
const currentRecord = ref({})
const approvalHistories = ref([])

// 审批操作
const approveDialogVisible = ref(false)
const approveDialogTitle = ref('')
const approveAction = ref('')
const approveRecordId = ref(null)
const approveForm = reactive({
  comment: ''
})

// 加载流程列表
const loadFlows = async () => {
  flowLoading.value = true
  try {
    const res = await request({ url: '/api/system/approval/flow/list', method: 'get', params: { page: 1, size: 100 } })
    // request.js 已经解包，返回的直接是 Page 对象
    flowList.value = res.records || res.data?.records || []
  } catch (error) {
    console.error('加载流程失败:', error)
  } finally {
    flowLoading.value = false
  }
}

// 新增流程
const handleAddFlow = () => {
  resetFlowForm()
  flowDialogTitle.value = '新增流程'
  flowDialogVisible.value = true
}

// 编辑流程
const handleEditFlow = (row) => {
  resetFlowForm()
  Object.assign(flowForm, row)
  flowDialogTitle.value = '编辑流程'
  flowDialogVisible.value = true
}

// 删除流程
const handleDeleteFlow = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除流程"${row.flowName}"吗？`, '提示', { type: 'warning' })
    await request({ url: `/api/system/approval/flow/${row.id}`, method: 'delete' })
    ElMessage.success('删除成功')
    loadFlows()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

// 提交流程表单
const submitFlowForm = async () => {
  try {
    await flowFormRef.value?.validate()
    const isEdit = !!flowForm.id
    isEdit 
      ? await request({ url: '/api/system/approval/flow', method: 'put', data: flowForm })
      : await request({ url: '/api/system/approval/flow', method: 'post', data: flowForm })
    
    ElMessage.success(isEdit ? '更新成功' : '创建成功')
    flowDialogVisible.value = false
    loadFlows()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

// 重置流程表单
const resetFlowForm = () => {
  flowForm.id = null
  flowForm.flowName = ''
  flowForm.flowCode = ''
  flowForm.businessType = ''
  flowForm.description = ''
  flowForm.status = 1
  flowFormRef.value?.resetFields()
}

// 配置节点
const handleConfigNodes = async (row) => {
  currentFlow.value = row
  try {
    const res = await request({ url: `/api/system/approval/flow/${row.id}/nodes`, method: 'get' })
    // request.js 已经解包
    nodeList.value = Array.isArray(res) ? res : (res.data || [])
  } catch (error) {
    nodeList.value = []
  }
  nodeDialogVisible.value = true
}

// 添加节点
const addNode = () => {
  nodeList.value.push({
    nodeName: '',
    nodeOrder: nodeList.value.length + 1,
    approverType: 1,
    approverId: null,
    approverName: '',
    allowTransfer: 0,
    allowReject: 1,
    status: 1
  })
}

// 删除节点
const removeNode = (index) => {
  nodeList.value.splice(index, 1)
  // 重新排序
  nodeList.value.forEach((node, i) => { node.nodeOrder = i + 1 })
}

// 保存节点
const saveNodes = async () => {
  if (!currentFlow.value) return
  try {
    await request({
      url: `/api/system/approval/flow/${currentFlow.value.id}/nodes`,
      method: 'post',
      data: nodeList.value
    })
    ElMessage.success('保存成功')
    nodeDialogVisible.value = false
  } catch (error) {
    console.error('保存节点失败:', error)
  }
}

// 加载待审批
const loadPendingRecords = async () => {
  recordLoading.value = true
  try {
    const res = await request({ url: '/api/system/approval/pending', method: 'get' })
    // request.js 已经解包，返回的直接是数据
    pendingList.value = Array.isArray(res) ? res : (res.data || [])
  } catch (error) {
    console.error('加载待审批失败:', error)
  } finally {
    recordLoading.value = false
  }
}

// 加载我发起的
const loadAppliedRecords = async () => {
  recordLoading.value = true
  try {
    const res = await request({ url: '/api/system/approval/my-applied', method: 'get', params: { page: 1, size: 50 } })
    // request.js 已经解包，返回的直接是 Page 对象
    appliedList.value = res.records || res.data?.records || []
  } catch (error) {
    console.error('加载我发起的审批失败:', error)
  } finally {
    recordLoading.value = false
  }
}

// 加载全部记录
const loadAllRecords = async () => {
  recordLoading.value = true
  try {
    const res = await request({ url: '/api/system/approval/record/list', method: 'get', params: queryParams })
    // request.js 已经解包，返回的直接是 Page 对象
    allRecordList.value = res.records || res.data?.records || []
    recordTotal.value = res.total || res.data?.total || 0
  } catch (error) {
    console.error('加载全部记录失败:', error)
  } finally {
    recordLoading.value = false
  }
}

// 查看记录详情
const handleViewRecord = async (row) => {
  currentRecord.value = row
  try {
    const res = await request({ url: `/api/system/approval/record/${row.id}`, method: 'get' })
    // request.js 已经解包
    currentRecord.value = res.record || res.data?.record || row
    approvalHistories.value = res.histories || res.data?.histories || []
  } catch (error) {
    approvalHistories.value = []
  }
  recordDetailVisible.value = true
}

// 审批通过
const handleApprove = (row) => {
  approveRecordId.value = row.id
  approveAction.value = 'approve'
  approveDialogTitle.value = '审批通过'
  approveForm.comment = ''
  approveDialogVisible.value = true
}

// 审批驳回
const handleReject = (row) => {
  approveRecordId.value = row.id
  approveAction.value = 'reject'
  approveDialogTitle.value = '审批驳回'
  approveForm.comment = ''
  approveDialogVisible.value = true
}

// 提交审批
const submitApprove = async () => {
  if (approveAction.value === 'reject' && !approveForm.comment) {
    ElMessage.warning('驳回原因不能为空')
    return
  }
  
  try {
    const url = approveAction.value === 'approve' 
      ? `/api/system/approval/approve/${approveRecordId.value}`
      : `/api/system/approval/reject/${approveRecordId.value}`
    
    await request({ url, method: 'post', data: { comment: approveForm.comment } })
    ElMessage.success(approveAction.value === 'approve' ? '审批通过' : '已驳回')
    approveDialogVisible.value = false
    loadPendingRecords()
  } catch (error) {
    console.error('审批失败:', error)
  }
}

// 撤回
const handleWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤回该审批申请吗？', '提示', { type: 'warning' })
    await request({ url: `/api/system/approval/withdraw/${row.id}`, method: 'post' })
    ElMessage.success('撤回成功')
    loadAppliedRecords()
  } catch (error) {
    if (error !== 'cancel') console.error('撤回失败:', error)
  }
}

onMounted(() => {
  loadFlows()
  loadPendingRecords()
  loadAppliedRecords()
  loadAllRecords()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.mb20 {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.node-config {
  min-height: 300px;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.search-form {
  margin-bottom: 15px;
}

.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}
</style>

