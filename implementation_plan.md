# 校食元气 App — 编码实施计划

> 完全对齐《项目技术方案》文档，分步开始从零编码。

---

## User Review Required

> [!CAUTION]
> **部署环境与技术方案存在冲突，需要确认：**
>
> 你最初说部署环境是 **CentOS 7.9 + MySQL 5.6.50**，但技术方案文档写的是：
> - Spring Boot **3.x** + Java **17** → CentOS 7.9 需手动安装 JDK 17（yum 默认无 17）
> - MySQL **8.0** → 与你说的 5.6.50 冲突
> - Redis **7.x** → CentOS 7.9 yum 默认只有 Redis 3.x
> - Docker 部署 → CentOS 7.9 支持 Docker，但需手动安装
>
> **请选择方案：**
> 1. **方案A**：按技术方案走（推荐），服务器上手动安装 JDK 17 + MySQL 8.0 + Redis 7.x + Docker，或直接用 Docker 容器化全套环境
> 2. **方案B**：降配适配服务器（Spring Boot 2.7 + JDK 8 + MySQL 5.6），但会失去一些现代特性
>
> **我建议选方案A**：用 Docker Compose 在 CentOS 7.9 上一键部署 MySQL 8.0 + Redis 7.x + JDK 17 应用，完全不受系统版本限制。

**开发模式确认：纯本地开发（推荐）**

- 本地开发只用 Docker 跑 MySQL/Redis 依赖，不把后端应用容器化
- 后端使用 Spring Boot DevTools 热重启
- 前端 Flutter 热重载
- 服务器只在发布阶段使用 Docker

---

## 技术栈（对齐技术方案）

| 层 | 选型 |
|----|------|
| App 前端 | Flutter 3.x + Riverpod + GoRouter |
| 管理后台前端 | Vue 3 + TypeScript + Element Plus (UI 风格与 App 保持一致的高级感) |
| 后端 | Spring Boot 3.x + Java 17 + MyBatis-Plus |
| 认证 | JWT 双 Token + Spring Security (分 User 与 Admin 两套体系) |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.x |
| AI | DeepSeek API + 通义千问 VL（多模态） |
| 部署 | Docker + Nginx + CentOS 7.9（仅生产） |

---

## 开发步骤 & 文件清单

### Step 1：项目骨架初始化

**后端 (Spring Boot):**

| 操作 | 文件 |
|------|------|
| Maven 项目 + pom.xml 依赖 | `backend/pom.xml` |
| 主启动类 | `XsyqApplication.java` |
| 全局配置 | `application.yml`, `application-dev.yml`, `application-prod.yml` |
| 统一响应 | `common/Result.java` |
| 全局异常处理 | `common/GlobalExceptionHandler.java` |
| 跨域配置 | `config/CorsConfig.java` |
| 数据库初始化 | `resources/db/init.sql` （17 张表 DDL + 种子数据） |

**前端 (Flutter):**

| 操作 | 文件 |
|------|------|
| Flutter 项目初始化 | `frontend/` |
| pubspec.yaml 依赖 | `pubspec.yaml` |
| 主题系统 | `app/theme.dart` |
| 路由 + 守卫 | `app/router.dart` |
| 品牌色 / 字体常量 | `core/constants/colors.dart`, `text_styles.dart` |
| Dio 封装 + JWT 拦截器 | `core/network/dio_client.dart` |
| API 路径常量 | `core/network/api_endpoints.dart` |
| Token 安全存储 | `core/storage/secure_storage.dart` |
| 毛玻璃卡片组件 | `shared/widgets/glass_card.dart` |
| 滚轮选择器组件 | `shared/widgets/premium_picker.dart` |
| 动效按钮组件 | `shared/widgets/animated_button.dart` |

---

### Step 2：用户认证 + 建档引导流程

**后端:**

| 操作 | 文件 |
|------|------|
| JWT 工具类 | `common/JwtUtils.java` |
| Security 配置 | `config/SecurityConfig.java` |
| 用户 Entity + Mapper | `entity/User.java`, `mapper/UserMapper.java` |
| 档案 Entity + Mapper | `entity/UserProfile.java`, `mapper/UserProfileMapper.java` |
| 认证 Controller | `controller/AuthController.java` — `POST /api/auth/sms-login` |
| 档案 Controller | `controller/ProfileController.java` — `POST /api/profile/setup`, `PUT /api/profile/update` |
| BMI/BMR 计算 | `engine/NutritionEngine.java` (初版 — BMR + BMI 部分) |

**前端 — Page 1~2 (启动屏 + 6步建档引导):**

| 页面 | 文件 | 要点 |
|------|------|------|
| 启动屏 | `features/splash/splash_page.dart` | 激励语淡入 + 2.5s 自动跳转 |
| 性别选择 | `features/onboarding/gender_page.dart` | 圆形卡片 + spring animation |
| 身高年龄 | `features/onboarding/height_age_page.dart` | ListWheelScrollView 滚轮 |
| 体重 | `features/onboarding/weight_page.dart` | 智能默认值 = 22×(身高m)² |
| BMI 结果 | `features/onboarding/bmi_result_page.dart` | 数字跳动 + 渐变色彩条 + 健康快照 |
| 健康目标 | `features/onboarding/goal_page.dart` | 卡片多选(最多2个) |
| 饮食偏好 | `features/onboarding/preference_page.dart` | 药丸标签3组多选 |

---

### Step 3：课表系统 + 营养算法引擎

**后端:**

| 操作 | 文件 |
|------|------|
| 作息配置 Entity + Mapper | `entity/SchoolScheduleConfig.java` |
| 周课表 Entity + Mapper | `entity/Schedule.java` |
| 每日能量 Entity + Mapper | `entity/DailyEnergy.java` |
| 课表 Controller | `controller/ScheduleController.java` — save / current-week / copy-last-week |
| 营养 Controller | `controller/NutritionController.java` — `GET /api/nutrition/today` |
| PAL 计算器 | `engine/PalCalculator.java` |
| 三餐拆分器 | `engine/MealSplitter.java` |
| 营养引擎完整版 | `engine/NutritionEngine.java` — 完整 8 步流程 |

**前端 — Page 3~6:**

| 页面 | 文件 | 要点 |
|------|------|------|
| 作息设置 | `features/school_config/school_config_page.dart` | 时间滚轮 + 节数选择 |
| 课表设置 | `features/schedule/schedule_page.dart` | 三色网格核心页面 |
| 网格组件 | `features/schedule/schedule_grid.dart` | 动态 NxN 网格 |
| 单元格组件 | `features/schedule/schedule_cell.dart` | 点击循环 + 颜色渐变 200ms |
| 状态管理 | `features/schedule/schedule_provider.dart` | Riverpod 课表状态 |
| 健康授权 | `features/health_permission/health_permission_page.dart` | 步数/运动数据 |
| 过渡页 | `features/transition/transition_page.dart` | 仪式感 + 呼吸光效按钮 |

---

### Step 4：首页 + AI 推荐

**后端:**

| 操作 | 文件 |
|------|------|
| Redis 配置 | `config/RedisConfig.java` |
| AI 模型配置 | `config/AiModelConfig.java` |
| AI 客户端接口 | `ai/AiClient.java` |
| DeepSeek 客户端 | `ai/DeepSeekClient.java` |
| Prompt 构建器 | `ai/PromptBuilder.java` |
| AI 气泡生成器 | `ai/AiBubbleGenerator.java` |
| 降级规则引擎 | `engine/FallbackRuleEngine.java` |
| AI 推荐记录 Entity | `entity/AiRecommendation.java` |
| 推荐 Controller (SSE) | `controller/RecommendController.java` |
| 气泡 Controller | `controller/AiBubbleController.java` |

**前端 — Page 7 (首页):**

| 组件 | 文件 |
|------|------|
| 首页主体 | `features/home/home_page.dart` |
| 课表缩略图 | `features/home/widgets/schedule_mini.dart` |
| AI 气泡卡片 | `features/home/widgets/ai_bubble.dart` |
| 实时课程感知 | `features/home/widgets/current_class.dart` |
| 三餐推荐卡片 | `features/home/widgets/meal_cards.dart` |
| 今日状态模块 | `features/home/widgets/daily_status.dart` |
| "现在吃啥"按钮 | `features/home/widgets/fab_eat_now.dart` |
| SSE 流式客户端 | `core/network/sse_client.dart` |
| 底部导航栏 | `app/main_shell.dart` |

---

### Step 5：饮食详情 + 反馈闭环

**后端:**

| 操作 | 文件 |
|------|------|
| 餐后反馈 Entity | `entity/MealFeedback.java` |
| 食物替换表 Entity | `entity/FoodSubstitute.java` |
| 反馈 Controller | `controller/FeedbackController.java` |
| 食物替换 Controller | `controller/FoodController.java` |
| 纠偏 Service | `service/FeedbackService.java` — 吃多了-15% / 没吃饱+10% |

**前端 — Page 8 (饮食详情):**

| 组件 | 文件 |
|------|------|
| 详情页主体 | `features/meal_detail/meal_detail_page.dart` |
| 餐盘可视化 | `features/meal_detail/plate_chart.dart` — CustomPainter 扇区图 |
| 食物方案卡片 | `features/meal_detail/food_card.dart` |
| 替换建议 | `features/meal_detail/food_substitute.dart` |
| 反馈按钮条 | `features/meal_detail/feedback_bar.dart` |

---

### Step 6：辅助功能

**后端:**

| 操作 | 文件 |
|------|------|
| 饮水 Controller | `controller/WaterController.java` |
| 体重 Controller | `controller/WeightController.java` |
| 每日状态 Controller | `controller/StatusController.java` |
| 天气 Service | `service/WeatherService.java` + `scheduler/WeatherFetchJob.java` |
| 定时预算计算 | `scheduler/DailyNutritionJob.java` |

**前端 — Page 10~11 + 辅助模块:**

| 页面 | 文件 |
|------|------|
| 个人中心 | `features/profile/profile_page.dart` |
| 数据报告 | `features/report/weekly_report.dart` |
| 体重趋势 | `features/report/weight_trend.dart` |
| 饮水追踪 | `features/water/water_tracker.dart` |
| 进度环组件 | `shared/widgets/progress_ring.dart` |

---

### Step 7：高级功能

**后端:**

| 操作 | 文件 |
|------|------|
| 拍照识食 | `ai/QwenVLClient.java`, `controller/PhotoRecognizeController.java` |
| 成就系统 | `controller/AchievementController.java` |
| 社交模块 | `controller/SocialController.java` |
| 周报生成 | `scheduler/WeeklyReportJob.java` |
| 推送提醒 | `scheduler/PushReminderJob.java` |

**前端:**

| 模块 | 文件 |
|------|------|
| 拍照识食 | `features/photo_recognize/` |
| 成就墙 | `features/report/achievement_wall.dart` |
| 撒花动画 | `shared/widgets/confetti_overlay.dart` |
| 舍友排行 | `features/social/leaderboard.dart` |
| 邀请页 | `features/social/invite_page.dart` |
| 离线缓存 | `core/storage/hive_manager.dart` |
| 本地模板 | `core/constants/meal_templates.dart` |

---

### Step 8：部署上线（生产）

| 操作 | 文件 |
|------|------|
| Docker Compose | `docker-compose.yml` (MySQL 8.0 + Redis 7.x + Spring Boot JAR + Nginx) |
| Dockerfile (后端) | `backend/Dockerfile` |
| Nginx 配置 | `deploy/nginx.conf` |
| 生产配置 | `application-prod.yml` |
| 部署脚本 | `deploy/deploy.sh` |

---

## 立即开始的第一步

确认计划后，我将按此顺序执行：

1. 在项目目录下创建 `backend/` (Spring Boot Maven 项目) 和 `frontend/` (Flutter 项目)
2. 先搭后端骨架：pom.xml → 配置文件 → 通用类 → 数据库 DDL
3. 再搭前端骨架：Flutter 初始化 → 主题 → 路由 → 公共组件
4. 然后按 Step 2~8 的顺序推进

---

## Verification Plan

### 后端验证
- 每个 Controller 完成后用 Knife4j 在线文档测试 API
- 营养引擎 (BMR/TDEE/PAL) 编写 JUnit 单元测试，验证计算精度
- AI 推荐接口用 curl 测试 SSE 流式响应

### 前端验证
- 每个页面完成后在 Android 模拟器 + Chrome 上预览
- 重点验证：课表三色网格交互、BMI 色彩条动画、餐盘 CustomPainter

### 部署验证
- Docker Compose 本地先跑通全套
- 部署到 CentOS 7.9 后，Flutter APK 连接远程 API 端到端测试
