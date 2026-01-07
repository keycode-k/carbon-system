# 碳资讯模块部署指南

## 模块概述

**service-news** 是独立的碳资讯服务，提供新闻资讯的发布、查询、分类检索等功能。

- **端口**: 8085
- **数据库**: carbon_system（共享）
- **服务名**: service-news
- **注册中心**: Nacos (localhost:8848)

## 快速启动

### 1. 数据库初始化

在MySQL中执行初始化脚本：

```sql
-- 位置: service-news/src/main/resources/init.sql
-- 创建news表并插入8条测试数据
```

或直接在MySQL客户端执行：

```bash
mysql -u root -p carbon_system < service-news/src/main/resources/init.sql
```

### 2. 启动后端服务

在IDEA中启动 `NewsApplication.java`，或使用Maven命令：

```bash
cd service-news
mvn spring-boot:run
```

启动成功后访问: http://localhost:8085

### 3. 启动前端服务

```bash
cd frontend
npm run dev
```

前端访问: http://localhost:3000

## API接口列表

### 新闻查询

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/news/list` | GET | 分页查询新闻列表 |
| `/api/news/{id}` | GET | 查询新闻详情（自动增加浏览量） |

**查询参数**:
- `page`: 页码（默认1）
- `size`: 每页大小（默认10）
- `title`: 标题关键词（可选）
- `category`: 分类（可选）
- `status`: 状态（默认1-已发布）

### 新闻管理（管理员）

| 接口 | 方法 | 说明 |
|-----|------|------|
| `/api/news` | POST | 发布新闻 |
| `/api/news` | PUT | 更新新闻 |
| `/api/news/{id}` | DELETE | 删除新闻（逻辑删除） |
| `/api/news/{id}/top` | PUT | 置顶/取消置顶 |
| `/api/news/{id}/status` | PUT | 修改状态 |

## 前端页面

### 新闻列表页
- **路由**: `/news/index`
- **功能**: 
  - 关键词搜索
  - 分类筛选（政策法规/行业动态/技术创新/市场分析）
  - 分页展示
  - 热门新闻侧边栏
  - 点击跳转详情

### 新闻详情页
- **路由**: `/news/detail/:id`
- **功能**:
  - 完整新闻内容展示
  - 自动增加浏览量
  - 相关推荐（同分类）
  - 返回列表按钮

## 数据库表结构

### news 表

```sql
CREATE TABLE `news` (
  `id` BIGINT AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NOT NULL,
  `summary` VARCHAR(500),
  `category` VARCHAR(50) NOT NULL,
  `author` VARCHAR(50) NOT NULL,
  `source` VARCHAR(100),
  `cover_image` VARCHAR(255),
  `view_count` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `is_top` TINYINT DEFAULT 0,
  `publish_time` DATETIME,
  `create_time` DATETIME,
  `update_time` DATETIME,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`)
);
```

**字段说明**:
- `status`: 0-草稿 1-已发布 2-已下线
- `is_top`: 0-否 1-是
- `deleted`: 0-正常 1-已删除（逻辑删除）

## 测试数据

初始化脚本包含8条测试新闻：
- 全国碳市场成交额突破100亿元
- 碳交易管理条例正式施行
- CCER重启交易
- 碳捕集技术突破
- 企业参与碳交易指南
- 碳价走势预测
- 绿色金融支持碳中和
- 新能源汽车发展机遇

## 功能特性

### 后端特性
- ✅ MyBatis-Plus自动化CRUD
- ✅ 分页查询支持
- ✅ 多条件组合筛选
- ✅ 浏览量自动增加
- ✅ 置顶优先排序
- ✅ 逻辑删除
- ✅ 统一Result返回

### 前端特性
- ✅ Element Plus UI组件
- ✅ 搜索+筛选功能
- ✅ 响应式分页
- ✅ 热门新闻榜
- ✅ 分类快速导航
- ✅ 相关推荐
- ✅ 日期智能格式化（今天/昨天/N天前）
- ✅ 浏览量实时显示

## 服务架构

```
service-news (独立微服务)
├── entity/News.java         # 实体类
├── mapper/NewsMapper.java   # 数据访问层
├── service/NewsService.java # 业务逻辑层
├── controller/NewsController.java # 控制器
└── NewsApplication.java     # 启动类
```

## 集成说明

service-news是**完全独立**的微服务：
- 独立端口（8085）
- 独立启动类
- 独立数据表
- 可独立部署
- 通过Nacos注册发现

## 后续扩展建议

### 管理功能
- [ ] 新闻发布界面（管理员）
- [ ] 富文本编辑器集成
- [ ] 图片上传功能
- [ ] 草稿保存功能

### 增强功能
- [ ] Markdown内容支持
- [ ] 评论功能
- [ ] 点赞收藏
- [ ] 标签系统
- [ ] 全文搜索（Elasticsearch）

### 性能优化
- [ ] 热门文章缓存（Redis）
- [ ] 图片CDN加速
- [ ] 分类统计缓存

## 故障排查

### 服务无法启动
1. 检查Nacos是否运行（8848端口）
2. 检查MySQL连接配置
3. 检查端口8085是否被占用

### 前端无法加载数据
1. 检查service-news是否启动
2. 检查浏览器控制台错误
3. 检查Vite代理配置（vite.config.js）

### 数据库表不存在
执行 `service-news/src/main/resources/init.sql` 脚本

## 联系与支持

本模块作为Phase 2 P1优先级任务已完成开发，包含：
- ✅ 完整的后端CRUD API
- ✅ 前端列表页+详情页
- ✅ 搜索筛选功能
- ✅ 8条测试数据

可直接投入使用或根据业务需求进一步定制。
