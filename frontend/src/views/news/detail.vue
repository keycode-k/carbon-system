<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <!-- 返回按钮 -->
      <div class="back-btn">
        <el-button @click="goBack" icon="ArrowLeft">返回列表</el-button>
      </div>

      <!-- 新闻内容 -->
      <div v-if="newsDetail" class="news-detail">
        <!-- 标题 -->
        <h1 class="news-title">{{ newsDetail.title }}</h1>
        
        <!-- 元信息 -->
        <div class="news-meta">
          <el-tag :type="getCategoryTagType(newsDetail.category)">{{ newsDetail.category }}</el-tag>
          <span><el-icon><User /></el-icon> {{ newsDetail.author }}</span>
          <span v-if="newsDetail.source"><el-icon><Reading /></el-icon> {{ newsDetail.source }}</span>
          <span><el-icon><View /></el-icon> {{ newsDetail.viewCount || 0 }}</span>
          <span><el-icon><Clock /></el-icon> {{ formatDate(newsDetail.publishTime) }}</span>
        </div>

        <!-- 封面图 -->
        <div v-if="newsDetail.coverImage" class="cover-image">
          <img :src="newsDetail.coverImage" :alt="newsDetail.title" />
        </div>

        <!-- 摘要 -->
        <div v-if="newsDetail.summary" class="news-summary">
          <el-alert :title="newsDetail.summary" type="info" :closable="false" />
        </div>

        <!-- 正文内容 -->
        <div class="news-content" v-html="newsDetail.content"></div>

        <!-- 分享操作 -->
        <div class="news-actions">
          <el-divider />
          <div class="action-buttons">
            <el-button type="primary" icon="Share">分享</el-button>
            <el-button icon="Star">收藏</el-button>
          </div>
        </div>
      </div>

      <el-empty v-else description="新闻不存在或已被删除" />
    </el-card>

    <!-- 相关推荐 -->
    <el-card v-if="relatedNews.length > 0" style="margin-top: 20px;">
      <template #header>相关推荐</template>
      <div class="related-news">
        <div 
          v-for="news in relatedNews" 
          :key="news.id" 
          class="related-item"
          @click="goToDetail(news.id)"
        >
          <div class="related-title">{{ news.title }}</div>
          <div class="related-meta">
            <span>{{ formatDate(news.publishTime) }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getNewsDetail, getNewsList } from '@/api/news'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User, Reading, View, Clock, Share, Star } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const newsDetail = ref(null)
const relatedNews = ref([])
const loading = ref(false)

// 加载新闻详情
const loadNewsDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    console.log('News Detail ID:', id, typeof id)
    const data = await getNewsDetail(id)
    newsDetail.value = data
    
    // 加载相关推荐（同分类）
    if (data && data.category) {
      loadRelatedNews(data.category, id)
    }
  } catch (error) {
    ElMessage.error('加载新闻详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载相关推荐
const loadRelatedNews = async (category, excludeId) => {
  try {
    const data = await getNewsList({ 
      page: 1, 
      size: 5, 
      category, 
      status: 1 
    })
    // 排除当前文章
    relatedNews.value = (data.records || []).filter(item => item.id !== excludeId).slice(0, 4)
  } catch (error) {
    console.error('加载相关推荐失败', error)
  }
}

// 返回列表
const goBack = () => {
  router.back()
}

// 跳转到其他详情
const goToDetail = (id) => {
  router.push(`/news/detail/${id}`)
  // 重新加载
  loadNewsDetail()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取分类标签类型
const getCategoryTagType = (category) => {
  const typeMap = {
    '政策法规': 'danger',
    '行业动态': 'primary',
    '技术创新': 'success',
    '市场分析': 'warning'
  }
  return typeMap[category] || 'info'
}

onMounted(() => {
  loadNewsDetail()
})
</script>

<style scoped>
.app-container { 
  padding: 20px; 
  max-width: 1000px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.news-detail {
  padding: 20px 0;
}

.news-title {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1.4;
  margin: 0 0 20px 0;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: #909399;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.news-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.cover-image {
  margin-bottom: 30px;
  text-align: center;
}

.cover-image img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.news-summary {
  margin-bottom: 30px;
}

.news-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}

.news-content :deep(p) {
  margin: 16px 0;
  text-indent: 2em;
}

.news-content :deep(h1),
.news-content :deep(h2),
.news-content :deep(h3) {
  margin: 24px 0 16px;
  font-weight: bold;
}

.news-content :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 20px auto;
}

.news-actions {
  margin-top: 40px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.related-news {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.related-item {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.related-item:hover {
  background: #ecf5ff;
  transform: translateX(5px);
}

.related-title {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 8px;
}

.related-meta {
  font-size: 12px;
  color: #909399;
}
</style>
