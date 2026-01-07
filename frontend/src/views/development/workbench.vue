<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧项目列表 -->
      <el-col :span="6">
        <el-card shadow="never" class="project-list-card">
          <template #header>
            <div class="clearfix">
              <span>进行中的项目</span>
              <el-button style="float: right; padding: 3px 0" link type="primary">新建</el-button>
            </div>
          </template>
          <div class="project-list">
             <div 
                v-for="(item, index) in projects" 
                :key="index" 
                class="project-item"
                :class="{ active: currentProject === index }"
                @click="currentProject = index"
             >
                <div class="p-title">{{ item.name }}</div>
                <div class="p-info">
                   <el-tag size="small">{{ item.type }}</el-tag>
                   <span class="p-date">{{ item.date }}</span>
                </div>
             </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧工作详情 -->
      <el-col :span="18">
        <el-card shadow="hover">
          <template #header>
             <div class="detail-header">
                <h3>{{ projects[currentProject].name }}</h3>
                <el-button type="primary" size="small">保存进度</el-button>
             </div>
          </template>

          <!-- 流程步骤 -->
          <div class="step-container">
            <el-steps :active="projects[currentProject].step" finish-status="success" align-center>
              <el-step title="立项" description="提交项目建议书"></el-step>
              <el-step title="PDD设计" description="编写项目设计文件"></el-step>
              <el-step title="第三方审定" description="DOE 现场审定"></el-step>
              <el-step title="主管部门备案" description="发改委/生态部备案"></el-step>
              <el-step title="减排量核证" description="监测与核证"></el-step>
              <el-step title="资产签发" description="CCER 上市"></el-step>
            </el-steps>
          </div>

          <el-divider />

          <!-- 当前任务内容 -->
          <div class="task-content">
             <h4>当前阶段任务：PDD 文件编写</h4>
             <el-alert title="请依据 CM-001-V01 方法学进行测算" type="info" show-icon style="margin-bottom: 20px" />
             
             <el-form label-position="top">
                <el-form-item label="项目基准线描述">
                   <el-input type="textarea" :rows="4" placeholder="请输入..." />
                </el-form-item>
                <el-form-item label="预计年减排量 (tCO2e)">
                   <el-input-number v-model="num" :min="0" />
                </el-form-item>
                <el-form-item label="文档上传">
                   <el-upload
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :data="uploadData"
                      :before-upload="beforeUpload"
                      :on-success="handleUploadSuccess"
                      :on-error="handleUploadError"
                      multiple
                      :limit="10"
                      :file-list="fileList">
                      <el-button type="primary">点击上传文档</el-button>
                      <template #tip>
                        <div class="el-upload__tip">
                          支持 pdf、doc、docx、xls、xlsx 等格式，单个文件不超过50MB
                        </div>
                      </template>
                   </el-upload>
                </el-form-item>
             </el-form>

             <el-divider />

             <!-- 文档列表 -->
             <div class="document-list">
               <h4>项目文档列表</h4>
               <el-table :data="documentList" border stripe v-loading="documentLoading">
                 <el-table-column prop="docName" label="文档名称" min-width="200" />
                 <el-table-column prop="docType" label="文档类型" width="120">
                   <template #default="scope">
                     <el-tag v-if="scope.row.docType" size="small">{{ scope.row.docType }}</el-tag>
                     <span v-else>-</span>
                   </template>
                 </el-table-column>
                 <el-table-column prop="version" label="版本" width="100" />
                 <el-table-column prop="status" label="状态" width="100">
                   <template #default="scope">
                     <el-tag 
                       :type="scope.row.status === 'APPROVED' ? 'success' : scope.row.status === 'REVIEWING' ? 'warning' : 'info'"
                       size="small">
                       {{ getStatusText(scope.row.status) }}
                     </el-tag>
                   </template>
                 </el-table-column>
                 <el-table-column prop="fileSize" label="文件大小" width="120">
                   <template #default="scope">
                     {{ formatFileSize(scope.row.fileSize) }}
                   </template>
                 </el-table-column>
                 <el-table-column prop="createTime" label="上传时间" width="160" />
                 <el-table-column label="操作" width="200" fixed="right">
                   <template #default="scope">
                     <el-button link type="primary" size="small" @click="handleDownload(scope.row)">下载</el-button>
                     <el-button link type="primary" size="small" @click="handleVersion(scope.row)">版本</el-button>
                     <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
                   </template>
                 </el-table-column>
               </el-table>
             </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getDocumentsByProject, deleteDocument, downloadDocument } from '@/api/document'

const userStore = useUserStore()
const currentProject = ref(0)
const num = ref(50000)

const projects = [
  { id: 1, name: '内蒙古 100MW 风电并网项目', type: '风电', date: '2025-10-01', step: 1 },
  { id: 2, name: '广东某造纸厂生物质锅炉改造', type: '节能提效', date: '2025-09-15', step: 3 },
  { id: 3, name: '福建沿海红树林修复工程', type: '林业碳汇', date: '2025-08-20', step: 2 },
]

// 文档管理
const documentList = ref([])
const documentLoading = ref(false)
const fileList = ref([])

// 上传配置
const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083'}/api/development/document/upload`
})

const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
})

const uploadData = computed(() => {
  return {
    projectId: projects[currentProject.value].id,
    docName: '', // 会在beforeUpload中设置
    docType: 'PDD设计文件',
    uploadUserId: userStore.userId || 1
  }
})

// 加载文档列表
const loadDocuments = async () => {
  documentLoading.value = true
  try {
    const projectId = projects[currentProject.value].id
    const res = await getDocumentsByProject(projectId)
    if (res) {
      documentList.value = res
    }
  } catch (error) {
    console.error('加载文档列表失败:', error)
  } finally {
    documentLoading.value = false
  }
}

// 上传前检查
const beforeUpload = (file) => {
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('上传文件大小不能超过 50MB!')
    return false
  }
  // 设置文档名称为文件名
  uploadData.value.docName = file.name
  return true
}

// 上传成功
const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200 || response.success) {
    ElMessage.success('文档上传成功')
    loadDocuments()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = (error, file, fileList) => {
  ElMessage.error('文档上传失败')
  console.error(error)
}

// 下载文档
const handleDownload = async (row) => {
  try {
    const res = await downloadDocument(row.id)
    if (res) {
      // 简化处理：提示文件路径
      ElMessage.success(`文件路径: ${res}`)
      // 实际应该创建下载链接
      // window.open(res, '_blank')
    }
  } catch (error) {
    console.error('下载失败:', error)
  }
}

// 创建新版本
const handleVersion = (row) => {
  ElMessageBox.prompt('请输入新版本号', '创建版本', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^v\d+\.\d+$/,
    inputErrorMessage: '版本号格式错误，如: v1.1'
  }).then(({ value }) => {
    // TODO: 调用创建版本接口
    ElMessage.success(`创建版本 ${value} 成功`)
    loadDocuments()
  }).catch(() => {})
}

// 删除文档
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该文档吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDocument(row.id)
      ElMessage.success('删除成功')
      loadDocuments()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

// 状态文本
const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'REVIEWING': '审核中',
    'APPROVED': '已批准'
  }
  return statusMap[status] || status
}

// 监听项目切换
watch(currentProject, () => {
  loadDocuments()
})

// 初始加载
onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.project-list-card { height: 600px; overflow-y: auto; }
.project-item {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}
.project-item:hover { background-color: #f5f7fa; }
.project-item.active { background-color: #ecf5ff; border-right: 3px solid #409eff; }
.p-title { font-weight: 500; font-size: 15px; margin-bottom: 8px; color: #303133; }
.p-info { display: flex; justify-content: space-between; align-items: center; }
.p-date { font-size: 12px; color: #909399; }

.detail-header { display: flex; justify-content: space-between; align-items: center; }
.step-container { padding: 30px 0; background-color: #fafafa; margin-bottom: 20px; }
.task-content { padding: 0 20px; }
.document-list { margin-top: 30px; }
.el-upload__tip { color: #909399; font-size: 12px; margin-top: 5px; }
</style>
