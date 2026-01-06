<template>
  <div class="app-container">
    <el-alert
      title="您还未完成企业实名认证，大部分交易功能受限"
      type="warning"
      show-icon
      style="margin-bottom: 20px"
    />

    <el-row :gutter="20">
       <el-col :span="16">
          <el-card shadow="hover">
             <template #header>
               <span>企业认证表单</span>
             </template>
             <el-form label-width="140px" style="max-width: 600px; padding: 20px 0;">
                <el-form-item label="企业全称">
                   <el-input placeholder="请输入营业执照上的全称" />
                </el-form-item>
                <el-form-item label="统一社会信用代码">
                   <el-input placeholder="18位信用代码" />
                </el-form-item>
                <el-form-item label="法人代表">
                   <el-input placeholder="请输入法人姓名" />
                </el-form-item>
                <el-form-item label="营业执照">
                   <el-upload action="#" class="upload-demo" drag>
                      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                      <div class="el-upload__text">
                         拖拽文件到此处或 <em>点击上传</em>
                      </div>
                   </el-upload>
                </el-form-item>
                <el-form-item>
                   <el-button type="primary" size="large">提交审核</el-button>
                   <el-button size="large">保存草稿</el-button>
                </el-form-item>
             </el-form>
          </el-card>
       </el-col>

       <el-col :span="8">
          <el-card header="认证流程说明">
             <el-steps direction="vertical" :active="1">
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
</script>

<style scoped>
.app-container { padding: 20px; }
.faq-item { font-weight: bold; font-size: 14px; margin-bottom: 5px; }
.faq-ans { font-size: 13px; color: #606266; margin-bottom: 15px; }
</style>

