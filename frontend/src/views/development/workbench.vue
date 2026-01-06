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
                <el-form-item label="附件上传">
                   <el-upload
                      action="#"
                      multiple
                      :limit="3">
                      <el-button type="primary">点击上传 PDD 初稿</el-button>
                   </el-upload>
                </el-form-item>
             </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const currentProject = ref(0)
const num = ref(50000)

const projects = [
  { name: '内蒙古 100MW 风电并网项目', type: '风电', date: '2025-10-01', step: 1 },
  { name: '广东某造纸厂生物质锅炉改造', type: '节能提效', date: '2025-09-15', step: 3 },
  { name: '福建沿海红树林修复工程', type: '林业碳汇', date: '2025-08-20', step: 2 },
]
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
</style>
