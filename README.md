# 微信小程序记账应用 - 后端

## 技术栈
- JDK 17
- Spring Boot 3.2.0
- MyBatis Plus 3.5.5
- MySQL 8.0
- JWT (jjwt 0.12.3)

## 环境准备
1. 安装 JDK 17
2. 安装 MySQL 8.0
3. 创建数据库并执行 src/main/resources/schema.sql

## 配置
修改 src/main/resources/application.yml:
- 数据库地址、用户名、密码
- JWT secret

## 启动
`ash
mvn spring-boot:run
`

## API 接口

### 认证
- POST /api/auth/login - 微信登录
- GET  /api/auth/test-login - 测试登录（开发用）
- GET  /api/auth/info - 获取用户信息

### 账目
- POST   /api/account - 新增账目
- PUT    /api/account/{id} - 更新账目
- DELETE /api/account/{id} - 删除账目
- GET    /api/account/list?type=&month= - 查询账目列表
- GET    /api/account/{id} - 查询单条账目
- GET    /api/account/stats?year= - 月度统计

### 请求头
- Authorization: Bearer {token}
- X-User-Id: {userId}
