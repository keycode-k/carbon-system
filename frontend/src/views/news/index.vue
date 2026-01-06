<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="16">
         <!-- 轮播图/焦点新闻 -->
         <el-carousel height="300px" style="margin-bottom: 20px;">
            <el-carousel-item v-for="item in 3" :key="item" class="news-banner">
               <h3>全国碳市场启动{{ item }}周年回顾：成就与展望</h3>
            </el-carousel-item>
         </el-carousel>

         <!-- 新闻列表 -->
         <el-card shadow="hover">
            <template #header>行业动态</template>
            <div v-for="(news, index) in newsList" :key="index" class="news-item">
               <div class="news-content">
                  <div class="news-title">{{ news.title }}</div>
                  <div class="news-abstract">{{ news.abstract }}</div>
                  <div class="news-meta">
                     <el-tag size="small" :type="news.tagType">{{ news.category }}</el-tag>
                     <span>{{ news.source }}</span>
                     <span>{{ news.date }}</span>
                  </div>
               </div>
               <img v-if="news.img" :src="news.img" class="news-thumb" />
            </div>
            <el-pagination background layout="prev, pager, next" :total="100" style="margin-top: 20px; justify-content: center;" />
         </el-card>
      </el-col>

      <el-col :span="8">
         <el-card header="政策法规" style="margin-bottom: 20px;">
           <ul class="policy-list">
             <li><a href="#">关于做好2025年度碳排放报告管理的通知</a></li>
             <li><a href="#">温室气体自愿减排交易管理办法（试行）</a></li>
             <li><a href="#">碳排放权交易管理暂行条例</a></li>
             <li><a href="#">企业温室气体排放核算与报告指南</a></li>
           </ul>
         </el-card>

         <el-card header="即将发生">
            <el-timeline>
               <el-timeline-item timestamp="2025-06-30" placement="top" type="danger">
                  <el-card>
                     <h4>履约截止日期</h4>
                     <p>2024年度配额清缴截止</p>
                  </el-card>
               </el-timeline-item>
               <el-timeline-item timestamp="2025-05-01" placement="top">
                  <el-card>
                     <h4>碳排放报告报送</h4>
                     <p>重点排放单位完成系统填报</p>
                  </el-card>
               </el-timeline-item>
            </el-timeline>
         </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const newsList = [
  {
    title: '欧盟碳边境调节机制(CBAM)正式进入过渡期，出口企业如何应对？',
    abstract: '欧盟CBAM法案已正式生效，标志着全球首个"碳关税"制度落地。对于我国钢铁、铝、水泥等行业出口将产生深远影响...',
    category: '国际',
    tagType: 'warning',
    source: '碳道',
    date: '2025-01-05',
    img: '' // 占位
  },
  {
    title: '生态环境部：加快建设更加有效、更有活力的全国碳市场',
    abstract: '近日，生态环境部召开新闻发布会，重点介绍了在扩容行业、优化配额分配机制等方面的新举措...',
    category: '政策',
    tagType: 'success',
    source: '新华网',
    date: '2025-01-02',
    img: ''
  },
  {
    title: 'CCER 重启后首批项目完成备案，林业碳汇成热门',
    abstract: '随着自愿减排交易系统的上线，首批四个造林碳汇项目今日完成主管部门备案...',
    category: 'CCER',
    tagType: 'primary',
    source: '绿色交易所',
    date: '2024-12-28',
    img: ''
  }
]
</script>

<style scoped>
.app-container { padding: 20px; }
.news-banner { background-color: #3b4256; color: white; display: flex; align-items: flex-end; padding: 20px; box-sizing: border-box; }
.news-banner h3 { margin: 0; font-size: 24px; text-shadow: 0 2px 4px rgba(0,0,0,0.5); }
.news-item { display: flex; padding: 20px 0; border-bottom: 1px solid #f0f0f0; }
.news-item:last-child { border-bottom: none; }
.news-content { flex: 1; }
.news-title { font-size: 18px; font-weight: bold; color: #303133; margin-bottom: 10px; cursor: pointer; }
.news-title:hover { color: #409eff; }
.news-abstract { font-size: 14px; color: #606266; margin-bottom: 15px; line-height: 1.6; }
.news-meta { font-size: 12px; color: #909399; display: flex; align-items: center; gap: 15px; }
.news-thumb { width: 120px; height: 80px; background: #f0f0f0; margin-left: 20px; object-fit: cover; }

.policy-list { list-style: none; padding: 0; margin: 0; }
.policy-list li { margin-bottom: 10px; }
.policy-list a { color: #303133; text-decoration: none; font-size: 14px; }
.policy-list a:hover { color: #409eff; text-decoration: underline; }
</style>
