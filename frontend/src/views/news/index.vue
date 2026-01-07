<template>
  <div class="app-container">
    <!-- 搜索筛选区 -->
    <el-card shadow="never" style="margin-bottom: 20px;">
      <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.title" placeholder="请输入新闻标题" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="全部分类" clearable style="width: 150px;">
            <el-option label="政策法规" value="政策法规" />
            <el-option label="行业动态" value="行业动态" />
            <el-option label="技术创新" value="技术创新" />
            <el-option label="市场分析" value="市场分析" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="16">
         <!-- 新闻列表 -->
         <el-card shadow="hover" v-loading="loading">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>碳资讯动态</span>
                <el-tag>共 {{ total }} 条</el-tag>
              </div>
            </template>
            <el-empty v-if="newsList.length === 0" description="暂无新闻数据" />
            <div v-else>
              <div v-for="news in newsList" :key="news.id" class="news-item" @click="goToDetail(news.id)">
                 <div class="news-content">
                    <div class="news-header">
                      <div class="news-title">
                        <el-tag v-if="news.isTop === 1" type="danger" size="small" style="margin-right: 8px;">置顶</el-tag>
                        {{ news.title }}
                      </div>
                    </div>
                    <div class="news-abstract">{{ news.summary || '暂无摘要' }}</div>
                    <div class="news-meta">
                       <el-tag size="small" :type="getCategoryTagType(news.category)">{{ news.category }}</el-tag>
                       <span><el-icon><User /></el-icon> {{ news.author }}</span>
                       <span v-if="news.source"><el-icon><Reading /></el-icon> {{ news.source }}</span>
                       <span><el-icon><View /></el-icon> {{ news.viewCount || 0 }}</span>
                       <span><el-icon><Clock /></el-icon> {{ formatDate(news.publishTime) }}</span>
                    </div>
                 </div>
                 <img v-if="news.coverImage" :src="news.coverImage" class="news-thumb" />
              </div>
            </div>
            <el-pagination 
              v-if="total > 0"
              background 
              layout="total, prev, pager, next, sizes" 
              :total="total" 
              :page-size="pageSize"
              :current-page="currentPage"
              :page-sizes="[10, 20, 50]"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
              style="margin-top: 20px; justify-content: center;" 
            />
         </el-card>
      </el-col>

      <el-col :span="8">
         <!-- 热门分类统计 -->
         <el-card header="分类浏览" style="margin-bottom: 20px;">
           <div class="category-grid">
             <div 
               v-for="cat in categories" 
               :key="cat.value" 
               class="category-item"
               :class="{ active: searchForm.category === cat.value }"
               @click="filterByCategory(cat.value)"
             >
               <div class="category-name">{{ cat.label }}</div>
               <div class="category-count">{{ cat.count }}</div>
             </div>
           </div>
         </el-card>

         <!-- 热门新闻 -->
         <el-card header="热门资讯">
            <div v-for="(hot, index) in hotNewsList" :key="hot.id" class="hot-news-item" @click="goToDetail(hot.id)">
              <div class="hot-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <div class="hot-content">
                <div class="hot-title">{{ hot.title }}</div>
                <div class="hot-meta">
                  <span><el-icon><View /></el-icon> {{ hot.viewCount }}</span>
                </div>
              </div>
            </div>
         </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNewsList } from '@/api/news'
import { ElMessage } from 'element-plus'
import { Search, User, Reading, View, Clock } from '@element-plus/icons-vue'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  title: '',
  category: ''
})

// 分页数据
const newsList = ref([])
const hotNewsList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 分类数据
const categories = ref([
  { label: '政策法规', value: '政策法规', count: 0 },
  { label: '行业动态', value: '行业动态', count: 0 },
  { label: '技术创新', value: '技术创新', count: 0 },
  { label: '市场分析', value: '市场分析', count: 0 }
])

// 加载新闻列表
const loadNewsList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      title: searchForm.value.title || undefined,
      category: searchForm.value.category || undefined,
      status: 1 // 只查询已发布的
    }
    const data = await getNewsList(params)
    console.log('News List Response:', data)
    newsList.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error('加载新闻列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载热门新闻（浏览量TOP5）
const loadHotNews = async () => {
  try {
    const data = await getNewsList({ page: 1, size: 5, status: 1 })
    // 按浏览量排序
    const sorted = (data.records || []).sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
    hotNewsList.value = sorted.slice(0, 5)
  } catch (error) {
    console.error('加载热门新闻失败', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadNewsList()
}

// 重置
const handleReset = () => {
  searchForm.value = { title: '', category: '' }
  currentPage.value = 1
  loadNewsList()
}

// 分页切换
const handlePageChange = (page) => {
  currentPage.value = page
  loadNewsList()
}

// 每页大小切换
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadNewsList()
}

// 按分类筛选
const filterByCategory = (category) => {
  if (searchForm.value.category === category) {
    searchForm.value.category = ''
  } else {
    searchForm.value.category = category
  }
  handleSearch()
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/news/detail/${id}`)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
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
  loadNewsList()
  loadHotNews()
})
</script>
<style scoped>
.app-container { padding: 20px; }

.news-item { 
  display: flex; 
  padding: 20px 0; 
  border-bottom: 1px solid #f0f0f0; 
  cursor: pointer;
  transition: background-color 0.3s;
}
.news-item:hover { background-color: #f5f7fa; }
.news-item:last-child { border-bottom: none; }

.news-content { flex: 1; }

.news-header { margin-bottom: 8px; }
.news-title { 
  font-size: 18px; 
  font-weight: bold; 
  color: #303133; 
  margin-bottom: 10px; 
  display: flex;
  align-items: center;
}
.news-title:hover { color: #409eff; }

.news-abstract { 
  font-size: 14px; 
  color: #606266; 
  margin-bottom: 15px; 
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta { 
  font-size: 12px; 
  color: #909399; 
  display: flex; 
  align-items: center; 
  gap: 15px; 
}
.news-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.news-thumb { 
  width: 120px; 
  height: 80px; 
  background: #f0f0f0; 
  margin-left: 20px; 
  object-fit: cover;
  border-radius: 4px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.category-item {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.category-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.category-item.active {
  background: #409eff;
  color: white;
}

.category-name {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
}

.category-count {
  font-size: 20px;
  font-weight: bold;
}

.hot-news-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.hot-news-item:hover {
  background-color: #f5f7fa;
}

.hot-news-item:last-child {
  border-bottom: none;
}

.hot-rank {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  background: #909399;
  color: white;
  border-radius: 4px;
  font-weight: bold;
  font-size: 14px;
  margin-right: 10px;
  flex-shrink: 0;
}

.hot-rank.rank-1 { background: #f56c6c; }
.hot-rank.rank-2 { background: #e6a23c; }
.hot-rank.rank-3 { background: #409eff; }

.hot-content { flex: 1; }

.hot-title {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 5px;
}

.hot-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
