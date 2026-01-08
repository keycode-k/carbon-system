<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧项目列表 -->
      <el-col :span="6">
        <el-card shadow="never" class="project-list-card">
          <template #header>
            <div class="clearfix">
              <span>进行中的项目</span>
              <el-button style="float: right; padding: 3px 0" link type="primary" @click="goToProjectCreate">新建</el-button>
            </div>
          </template>
          <div v-if="projectLoading" class="loading-container">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="projects.length === 0" class="empty-container">
            <el-empty description="暂无项目" :image-size="80" />
          </div>
          <div v-else class="project-list">
             <div 
                v-for="(item, index) in projects" 
                :key="item.id" 
                class="project-item"
                :class="{ active: currentProjectIndex === index }"
                @click="selectProject(index)"
             >
                <div class="p-title">{{ item.name }}</div>
                <div class="p-info">
                   <el-tag size="small">{{ item.projectType || '未分类' }}</el-tag>
                   <span class="p-date">{{ formatDate(item.createTime) }}</span>
                </div>
             </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧工作详情 -->
      <el-col :span="18">
        <el-card v-if="currentProject" shadow="hover">
          <template #header>
             <div class="detail-header">
                <h3>{{ currentProject.name }}</h3>
                <div>
                  <el-button type="success" size="small" @click="handleSaveProgress">保存进度</el-button>
                  <el-button type="primary" size="small" @click="handleNextStep" :disabled="currentProject.currentStep >= 5">下一阶段</el-button>
                </div>
             </div>
          </template>

          <!-- 流程步骤 -->
          <div class="step-container">
            <el-steps :active="currentProject.currentStep || 0" finish-status="success" align-center>
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
             <h4>当前阶段任务：{{ getStepText(currentProject.currentStep) }}</h4>
             <el-alert 
               v-if="currentProject.methodologyCode"
               :title="`请依据 ${currentProject.methodologyCode} 方法学进行测算`" 
               type="info" 
               show-icon 
               style="margin-bottom: 20px" 
             />
             
             <el-form label-position="top">
                <el-form-item label="项目基准线描述">
                   <el-input 
                     v-model="workData.baselineDescription"
                     type="textarea" 
                     :rows="4" 
                     placeholder="请输入项目基准线描述..." 
                   />
                </el-form-item>
                <el-form-item label="预计年减排量 (tCO2e)">
                   <el-input-number 
                     v-model="currentProject.estimatedEmissionReduction" 
                     :min="0" 
                     :precision="2"
                     style="width: 200px"
                   />
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
                       {{ getDocStatusText(scope.row.status) }}
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
        
        <!-- 无项目时显示 -->
        <el-card v-else shadow="hover">
          <el-empty description="请从左侧选择一个项目，或新建项目">
            <el-button type="primary" @click="goToProjectCreate">新建项目</el-button>
          </el-empty>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getDocumentsByProject, deleteDocument, downloadDocument, createDocumentVersion } from '@/api/document'
import { getAllProjects, getProjectById, updateProject, updateProjectStep } from '@/api/development'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 项目列表相关
const projects = ref([])
const projectLoading = ref(false)
const currentProjectIndex = ref(-1)

// 当前选中项目
const currentProject = computed(() => {
  if (currentProjectIndex.value >= 0 && currentProjectIndex.value < projects.value.length) {
    return projects.value[currentProjectIndex.value]
  }
  return null
})

// 工作数据
const workData = ref({
  baselineDescription: ''
})

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
  const stepDocTypes = ['立项申请', 'PDD设计文件', '审定报告', '备案材料', '核证报告', '签发证书']
  return {
    projectId: currentProject.value?.id || 0,
    docName: '',
    docType: stepDocTypes[currentProject.value?.currentStep || 0],
    uploadUserId: userStore.userId || 1
  }
})

// 加载项目列表
const loadProjects = async () => {
  projectLoading.value = true
  try {
    const res = await getAllProjects()
    projects.value = res || []
    
    // 如果URL带有projectId参数，选中该项目
    const projectId = route.query.projectId
    if (projectId) {
      const index = projects.value.findIndex(p => p.id === Number(projectId))
      if (index >= 0) {
        currentProjectIndex.value = index
      } else if (projects.value.length > 0) {
        currentProjectIndex.value = 0
      }
    } else if (projects.value.length > 0) {
      currentProjectIndex.value = 0
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
    ElMessage.error('加载项目列表失败')
  } finally {
    projectLoading.value = false
  }
}

// 选择项目
const selectProject = (index) => {
  currentProjectIndex.value = index
}

// 加载文档列表
const loadDocuments = async () => {
  if (!currentProject.value?.id) {
    documentList.value = []
    return
  }
  
  documentLoading.value = true
  try {
    const res = await getDocumentsByProject(currentProject.value.id)
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
      ElMessage.success(`文件路径: ${res}`)
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
  }).then(async ({ value }) => {
    try {
      await createDocumentVersion(row.id, value, userStore.userId || 1)
      ElMessage.success(`创建版本 ${value} 成功`)
      loadDocuments()
    } catch (error) {
      ElMessage.error('创建版本失败')
    }
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

// 保存进度
const handleSaveProgress = async () => {
  if (!currentProject.value) return
  
  try {
    await updateProject(currentProject.value.id, {
      estimatedEmissionReduction: currentProject.value.estimatedEmissionReduction,
      description: workData.value.baselineDescription || currentProject.value.description
    })
    ElMessage.success('进度已保存')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 下一阶段
const handleNextStep = async () => {
  if (!currentProject.value) return
  
  const nextStep = (currentProject.value.currentStep || 0) + 1
  if (nextStep > 5) {
    ElMessage.warning('已是最后阶段')
    return
  }
  
  ElMessageBox.confirm(
    `确定要将项目推进到"${getStepText(nextStep)}"阶段吗？`,
    '确认阶段推进',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await updateProjectStep(currentProject.value.id, nextStep)
      currentProject.value.currentStep = nextStep
      ElMessage.success(`已推进到${getStepText(nextStep)}阶段`)
      // 刷新项目列表以更新状态
      loadProjects()
    } catch (error) {
      ElMessage.error('阶段推进失败')
    }
  }).catch(() => {})
}

// 跳转新建项目
const goToProjectCreate = () => {
  router.push('/development/project')
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 阶段文本
const getStepText = (step) => {
  const steps = ['立项', 'PDD设计', '第三方审定', '主管部门备案', '减排量核证', '资产签发']
  return steps[step] || '未知阶段'
}

// 文档状态文本
const getDocStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'REVIEWING': '审核中',
    'APPROVED': '已批准'
  }
  return statusMap[status] || status
}

// 监听项目切换
watch(currentProjectIndex, () => {
  loadDocuments()
  fileList.value = []
  workData.value.baselineDescription = currentProject.value?.description || ''
})

// 初始加载
onMounted(() => {
  loadProjects()
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

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
}
.loading-container .el-icon {
  font-size: 24px;
  margin-bottom: 10px;
}

.empty-container {
  padding: 20px 0;
}
</style>
