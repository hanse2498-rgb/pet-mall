# 宠物商城（骨架）

前后端分离的宠物用品/食品商城（自营）。已实现：商品/SKU/批次（临期 7 折、食品保质期校验）、购物车、订单与模拟支付、售后、秒杀专区、商品评价、宠物档案（品种百科 + 推荐映射）、宠物社区、AI 问答助手（宠物话题）、站内通知，以及完整后台管理系统（用户/商品/订单/秒杀/售后/数据看板/宠物档案）。

## 技术栈

- 后端：Java 17 · Spring Boot 3.3 · MyBatis-Plus · Sa-Token · Redis · MySQL 8
- 前端：Vue 3 · Vite · TypeScript · Pinia · Element Plus
- 部署：Docker Compose（MySQL / Redis / MinIO）

## 目录

```
mall/
├─ docs/                 # 需求 / 技术栈 / 功能清单 / 页面流程 / 数据库设计
├─ backend/              # Spring Boot 单体
├─ frontend/             # Vue3 + Vite
└─ docker-compose.yml    # 本地中间件
```

## 快速启动

### 1. 启动中间件（MySQL/Redis/MinIO）

```bash
docker compose up -d
```

> MySQL 首次启动会自动执行 `backend/src/main/resources/db/init.sql` 建库建表。
> 若使用本机已装 MySQL，请按 `application.yml` 的 `MYSQL_HOST/PORT/USER/PASSWORD` 环境变量调整账号密码，并手动执行 init.sql（需用 utf8mb4 字符集，如 `mysql --default-character-set=utf8mb4 < init.sql`）。本项目本机开发默认 `root/123456`。

### 2. 启动后端（端口 8080）

```bash
cd backend
mvn spring-boot:run
```

- 健康检查：`GET http://localhost:8080/api/ping`
- 接口文档：`http://localhost:8080/swagger-ui.html`
- 登录：`POST /api/auth/login` `{ "phone": "13800000000", "code": "123456" }`（骨架阶段验证码固定 123456）

### 3. 启动前端（端口 5173）

```bash
cd frontend
pnpm install
pnpm dev
```

打开 http://localhost:5173 ，首页可点击「调用 /api/ping」验证前后端连通。

## 说明

- 登录态：Sa-Token 返回 token，前端以 `satoken` 请求头携带。
- 支付为模拟模块（`MockPaymentServiceImpl`，预留微信支付位）：`待支付 → 模拟支付成功 → 待发货`。
- 数据库设计见 `docs/数据库设计.md`，建表脚本见 `backend/src/main/resources/db/init.sql`，演示数据见 `db/seed_demo.sql`。

## 已实现功能

- **商品域**：分类树、品牌、商品列表（关键词/分类/品牌/价格筛选 + 排序 + 分页）、详情（SKU/轮播/临期）、临期专区（7折）
- **账户**：手机号验证码登录/密码登录、注册、忘记密码（验证码 5min 有效/60s 冷却/每日 10 条；密码 BCrypt；登录失败 5 次锁 15 分钟）、收货地址 CRUD
- **购物车**：加购/数量/勾选/删除/件数，满 200 包邮、否则运费 10
- **订单**：下单（地址快照、乐观锁扣库存、食品批次校验+快照、幂等）、订单列表/详情、取消（回补库存）、确认收货、**待支付超时自动取消（默认 30 分钟）**
- **模拟支付**：支付成功/失败可演示，成功后清理已购购物车项
- **商品评价**：详情页评分汇总 + 评价列表（昵称脱敏/星级/图文），订单完成后可写评价（每订单商品仅一次）
- **个人中心**：顶栏点用户名进入 `/profile`；我的订单（内嵌列表）、**我的评价**、**我的收藏**、**浏览足迹**
- **宠物社区**：导航「宠物社区」独立板块（`/community`）——发布宠物日常（**本地图片上传** ≤9张）、最新/热门信息流、点赞、一级评论、删除自己的动态/评论、未登录可浏览，对应《宠物社区设计.md》V1
- **秒杀专区**：导航 `/seckill` + 首页入口横幅——多场次定时开场、倒计时、秒杀价/划线价/已抢进度、每人限购1件、独立秒杀库存（DB 防超卖）、复用订单+模拟支付；后台秒杀管理
- **问答助手**：顶栏购物车左侧入口 `/assistant`（需登录）——**仅限宠物话题**（硬性话题拦截 + 模型提示双重约束）、本地知识库默认 / **可接外部 OpenAI 兼容 LLM**、SSE 流式输出、会话历史/清空，见《问答助手设计.md》
- **后台管理（V1+）**：管理员认证（默认 admin / Admin@123）、商品维护(增删改/上下架/SKU/批次)、按分类模块查看、品牌/分类、订单列表 + **发货**、**用户管理（搜索/禁用/启用）**、批次临期自动 7 折，用户禁用后无法登录

## 后台入口

- 浏览器打开 `http://localhost:5173/admin`（会自动跳到后台登录）
- 默认账号：`admin` / `Admin@123`（启动时自动创建）
- `/api/admin/**` 接口仅管理员角色可访问（普通用户 token 返回 403）

> ⚠️ **以上所有账号/密码（含数据库 `root/123456`）均为本地演示用途，请勿用于生产环境。**
> 部署前请至少修改：`MYSQL_PASSWORD`、数据库 root 密码、管理员密码。

## 主要接口

| 模块 | 接口 |
| --- | --- |
| 商品 | `/api/product/categories` `/list` `/near-expiry` `/{id}` `/sku/{skuId}` |
| 认证 | `/api/auth/sms-code` `/register` `/login/code` `/login/password` `/forgot-password` |
| 地址 | `/api/address` (GET/POST/PUT/DELETE) `/api/address/{id}/default` |
| 购物车 | `/api/cart` (GET/POST) `/selected` `/count` `/remove-batch` PUT·DELETE `/{id}` |
| 订单 | `POST /api/order` GET `/api/order` `/{id}` `/{id}/cancel` `/{id}/receive` `/{id}/pay` |
| 后台 | `/api/admin/auth/login`、`/api/admin/product**`（商品/SKU/批次）、`/api/admin/order**`（列表/详情/发货）、`/api/admin/brand`、`/api/admin/category` |