<template>
  <div class="app-container">
    <el-alert
      v-if="companyInfo && companyInfo.status !== 2"
      :title="getAlertTitle()"
      :type="getAlertType()"
      show-icon
      style="margin-bottom: 20px"
    />

    <el-row :gutter="20">
       <el-col :span="16">
          <el-card shadow="hover">
             <template #header>
               <span>{{ isEdit ? '企业信息编辑' : '企业认证表单' }}</span>
             </template>
             <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" style="max-width: 600px; padding: 20px 0;">
                <el-form-item label="企业全称" prop="companyName">
                   <el-input v-model="form.companyName" placeholder="请输入营业执照上的全称" />
                </el-form-item>
                <el-form-item label="统一社会信用代码" prop="creditCode">
                   <el-input v-model="form.creditCode" placeholder="18位信用代码" maxlength="18" />
                </el-form-item>
                <el-form-item label="法人代表" prop="legalPerson">
                   <el-input v-model="form.legalPerson" placeholder="请输入法人姓名" />
                </el-form-item>
                <el-form-item label="企业地址" prop="address">
                   <el-input v-model="form.address" placeholder="请输入企业注册地址" />
                </el-form-item>
                <el-form-item label="联系电话" prop="phone">
                   <el-input v-model="form.phone" placeholder="请输入联系电话" />
                </el-form-item>
                <el-form-item label="注册资本">
                   <el-input v-model="form.registeredCapital" placeholder="例如：5000万元" />
                </el-form-item>
                <el-form-item label="成立日期">
                   <el-date-picker
                     v-model="form.establishDate"
                     type="date"
                     placeholder="选择成立日期"
                     style="width: 100%"
                   />
                </el-form-item>
                <el-form-item label="经营范围">
                   <el-input
                     v-model="form.businessScope"
                     type="textarea"
                     :rows="3"
                     placeholder="请输入经营范围"
                   />
                </el-form-item>
                <el-form-item label="营业执照">
                   <el-input v-model="form.licenseUrl" placeholder="营业执照图片URL（暂不支持上传）" />
                </el-form-item>
                
                <el-form-item v-if="companyInfo && companyInfo.status === 3">
                  <el-alert
                    title="驳回原因"
                    :description="companyInfo.rejectReason"
                    type="error"
                    show-icon
                    :closable="false"
                  />
                </el-form-item>

                <el-form-item>
                   <el-button type="primary" size="large" @click="submitForm" :loading="submitting">提交审核</el-button>
                   <el-button size="large" @click="saveDraft" :loading="saving">保存草稿</el-button>
                   <el-button size="large" @click="resetForm">重置</el-button>
                </el-form-item>
             </el-form>
          </el-card>
       </el-col>

       <el-col :span="8">
          <el-card header="认证状态">
            <div v-if="companyInfo">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="认证状态">
                  <el-tag :type="getStatusType(companyInfo.status)">
                    {{ getStatusText(companyInfo.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="提交时间" v-if="companyInfo.createTime">
                  {{ formatDate(companyInfo.createTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="审核时间" v-if="companyInfo.reviewTime">
                  {{ formatDate(companyInfo.reviewTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="审核人" v-if="companyInfo.reviewer">
                  {{ companyInfo.reviewer }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <div v-else>
              <el-empty description="尚未提交企业认证" :image-size="80" />
            </div>
          </el-card>

          <el-card header="认证流程说明" style="margin-top: 20px;">
             <el-steps direction="vertical" :active="getActiveStep()" finish-status="success">
                <el-step title="提交资料" description="填写企业基本信息并上传执照"></el-step>
                <el-step title="平台初审" description="预计 1-2 个工作日"></el-step>
                <el-step title="打款验证" description="对公账户小额打款验证"></el-step>
                <el-step title="认证通过" description="解锁所有交易权限"></el-step>
             </el-steps>
          </el-card>

          <el-card header="常见问题" style="margin-top: 20px;">
             <p class="faq-item">Q: 认证需要多长时间？</p>
             <p class="faq-ans">A: 通常在提交后 3 个工作日内完成。</p>
             <p class="faq-item">Q: 必须对公打款吗？</p>
             <p class="faq-ans">A: 是的，这是为了验证企业银行账户的有效性。</p>
          </el-card>
       </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCompanyInfo, submitCertification, saveDraft as saveDraftApi } from '@/api/company'

const formRef = ref(null)
const companyInfo = ref(null)
const isEdit = ref(false)
const submitting = ref(false)
const saving = ref(false)

const form = reactive({
  userId: 1, // TODO: 从登录信息获取
  companyName: '',
  creditCode: '',
  legalPerson: '',
  address: '',
  phone: '',
  registeredCapital: '',
  establishDate: null,
  businessScope: '',
  licenseUrl: ''
})

const rules = {
  companyName: [{ required: true, message: '请输入企业全称', trigger: 'blur' }],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { len: 18, message: '信用代码必须为18位', trigger: 'blur' }
  ],
  legalPerson: [{ required: true, message: '请输入法人代表', trigger: 'blur' }],
  address: [{ required: true, message: '请输入企业地址', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/, message: '请输入有效的电话号码', trigger: 'blur' }
  ]
}

const fetchCompanyInfo = async () => {
  try {
    const userId = 1 // TODO: 从登录信息获取
    const res = await getCompanyInfo(userId)
    if (res) {
      companyInfo.value = res
      isEdit.value = true
      // 填充表单
      Object.assign(form, {
        userId: res.userId,
        companyName: res.companyName,
        creditCode: res.creditCode,
        legalPerson: res.legalPerson,
        address: res.address || '',
        phone: res.phone || '',
        registeredCapital: res.registeredCapital || '',
        establishDate: res.establishDate || null,
        businessScope: res.businessScope || '',
        licenseUrl: res.licenseUrl || ''
      })
    }
  } catch (e) {
    // 没有企业信息，显示空表单
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await submitCertification(form)
      ElMessage.success('提交成功，等待审核')
      await fetchCompanyInfo()
    } catch (e) {
      // request.js handles error
    } finally {
      submitting.value = false
    }
  })
}

const saveDraft = async () => {
  saving.value = true
  try {
    await saveDraftApi(form)
    ElMessage.success('草稿保存成功')
    await fetchCompanyInfo()
  } catch (e) {
    // request.js handles error
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const getAlertTitle = () => {
  if (!companyInfo.value) return '您还未完成企业实名认证，大部分交易功能受限'
  
  switch (companyInfo.value.status) {
    case 0: return '企业信息已保存为草稿，请完善信息后提交审核'
    case 1: return '您的企业认证正在审核中，请耐心等待'
    case 3: return '您的企业认证已被驳回，请修改后重新提交'
    default: return ''
  }
}

const getAlertType = () => {
  if (!companyInfo.value || companyInfo.value.status === 0) return 'warning'
  if (companyInfo.value.status === 1) return 'info'
  if (companyInfo.value.status === 3) return 'error'
  return 'success'
}

const getStatusText = (status) => {
  const map = {
    0: '未认证',
    1: '审核中',
    2: '已认证',
    3: '已驳回'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getActiveStep = () => {
  if (!companyInfo.value) return 0
  if (companyInfo.value.status === 0) return 0
  if (companyInfo.value.status === 1) return 1
  if (companyInfo.value.status === 2) return 4
  if (companyInfo.value.status === 3) return 1
  return 0
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchCompanyInfo()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.faq-item { font-weight: bold; font-size: 14px; margin-bottom: 5px; }
.faq-ans { font-size: 13px; color: #606266; margin-bottom: 15px; }
</style>

