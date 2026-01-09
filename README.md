**项目概览**

本仓库为「碳中和数字碳资产管理系统」（carbon-system）。这是一个基于 Spring Boot / Spring Cloud（微服务）与 Vue 3 + Vite 的混合工程，包含若干后端微服务模块和一个前端单页应用。该 README 汇总项目结构、运行与构建步骤、配置说明与常见注意事项，便于开发与交付。

**主要特性**
- **微服务架构**：采用 Maven 多模块管理（父 POM），模块见下。
- **前端**：Vue 3 + Vite + Element Plus，使用 Pinia 管理状态。
- **后端**：Spring Boot、MyBatis-Plus、JWT 认证、Nacos 注册发现、Redis 缓存。
- **交易功能**：包含交易撮合、询报价、账户/资金操作等前后端接口（参见前端 API）。

**模块一览**
- `carbon-common`：公共工具与 JWT、公共依赖管理（见 [carbon-common/pom.xml](carbon-common/pom.xml#L1)）。
- `service-provider`：服务提供端（业务实现）。
- `service-consumer`：服务消费端或网关/聚合（视实现）。
- `service-assets`：资产管理微服务（示例配置见 [service-assets/target/classes/application.yml](service-assets/target/classes/application.yml#L1)）。
- `service-trade`：交易模块（订单、询价、撮合相关）。
- `service-development`、`service-news`：其他业务模块。
- `frontend`：前端单页应用（Vue 3 + Vite）。

**前置环境**
- JDK 21
- Maven 3.8+（推荐并行构建 `mvn -T 1C`）
- Node.js 18+、npm 或 yarn（用于前端）
- 数据库：MySQL（请按实际环境准备数据库和账号）
- 注册中心：Nacos（或替换为实际环境）
- Redis（缓存配置）

**快速开始（开发环境）**
1. 克隆仓库并进入目录：

```bash
git clone <repo-url>
cd carbon-system
```

2. 后端：使用 Maven 在本地构建（跳过测试以加速）：

```bash
mvn -T 1C -DskipTests package
```

3. 单模块运行（示例运行 `service-assets`）：

```bash
mvn -pl service-assets -am spring-boot:run
```

可按需替换模块名（例如 `service-trade`）。若使用 IDE，可导入为 Maven 项目并以 Spring Boot 启动类运行。

4. 前端开发：

```bash
cd frontend
npm install
npm run dev
# 或: npm run build 生成生产构建
```

前端默认通过 `request` 工具调用 `/api/...` 路径（参见 [frontend/src/api](frontend/src/api)），开发时可使用代理或后端网关转发。

**构建与部署（生产）**
- 后端：

```bash
mvn -T 1C -DskipTests package
# 各服务将生成可运行的 Spring Boot 可执行 jar（见相应模块的 target 目录）
```

- 前端：

```bash
cd frontend
npm ci
npm run build
# 将 dist 目录下内容部署到静态服务器，或由后端静态资源服务托管
```

**配置说明**
- 全局父 POM： [pom.xml](pom.xml#L1)，包含依赖管理与模块定义。
- 模块配置通常位于各模块的 `src/main/resources/application.yml` 或运行后 `target/classes/application.yml`（示例： [service-assets/target/classes/application.yml](service-assets/target/classes/application.yml#L1)）。
- 请务必替换示例配置中的数据库、Redis、Nacos 等敏感信息；仓库中可能含示例或历史凭据，不应用于生产。

**前端重要文件**
- 入口： [frontend/src/main.js](frontend/src/main.js#L1) — 异步恢复登录状态后挂载应用。
- API 示例： [frontend/src/api/trade.js](frontend/src/api/trade.js#L1) — 列举了交易相关的 REST 调用路径与参数示例。

**数据库与初始化**
- 项目内含若干 SQL 脚本（见仓库根目录多个 `数据库迁移-*.sql` 文件），用于初始化或迁移表结构。请在执行前备份目标数据库并在受控环境中运行。

**安全与敏感信息**
- 仓库中可能包含演示或历史配置示例（例如 `application.yml`），其中可能包含不可公开的凭据。不要将这些凭据用于生产环境；请在部署前替换为安全的凭据管理方式（环境变量 / 配置中心 / 密钥管理）。

**常见操作命令速查**
- 构建所有模块： `mvn -T 1C -DskipTests package`
- 运行单模块： `mvn -pl <module> -am spring-boot:run`
- 前端开发：

```bash
cd frontend
npm install
npm run dev
```

**目录导航（高频查看）**
- 服务与模块源码： `service-*/src/main/java`。
- 前端资源： `frontend/src`。
- 公共库： `carbon-common/src`。
- 数据库迁移脚本位于仓库根目录。

**下一步建议**
- 在开发/部署前，维护一份不含敏感信息的 `application.yml.template` 并在 README 中添加替换示例（需要我帮忙我可以创建）。

---
如需我把 README 中的某部分改写为更详细的部署脚本（Docker Compose、Kubernetes）或生成 `application.yml.template`，告诉我需要哪些环境变量和运行拓扑，我会继续完善。

