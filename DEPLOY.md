# 服务器部署指南

## 部署方式

本项目使用 **GitHub Actions CI/CD** 自动部署，无需手动操作。

### 部署流程

手动触发 GitHub Actions -> 构建 Docker 镜像 -> 推送到 Docker Hub -> SSH 登录服务器 -> 自动获取 MySQL 容器 IP -> 拉取镜像并启动容器

### 触发部署

1. 修改后端代码并推送到 main 分支：
   git add .
   git commit -m "描述你的修改"
   git push origin main

2. 访问 https://github.com/zeroro1/bookkeeping-backend/actions

3. 点击 Deploy to Production -> Run workflow -> 绿色按钮

### 手动部署（应急用）

如果 GitHub Actions 不可用时，可以在服务器上手动执行：

# 1. 获取 MySQL 容器 IP
MYSQL_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mysql8)
echo "MySQL IP: $MYSQL_IP"

# 2. 创建 .env
cat > /opt/bookkeeping/.env << EOF
SPRING_DATASOURCE_URL=jdbc:mysql://$MYSQL_IP:3306/bookkeeping?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=Gzh1994429.
TZ=Asia/Shanghai
EOF

# 3. 拉取最新镜像（从 Docker Hub）
docker pull <你的DockerHub用户名>/bookkeeping-backend:latest

# 4. 启动容器
docker run -d --name bookkeeping-backend --restart always -p 8080:8080 --env-file /opt/bookkeeping/.env <你的DockerHub用户名>/bookkeeping-backend:latest

## 常用命令

# 查看容器状态
docker ps

# 查看后端日志
docker logs bookkeeping-backend

# 实时日志
docker logs -f bookkeeping-backend

# 重启服务
docker restart bookkeeping-backend

# 停止服务
docker stop bookkeeping-backend && docker rm bookkeeping-backend

# 测试接口
curl http://localhost:8080/api/auth/test-login

## 防火墙配置

确保阿里云安全组开放 8080 端口。

## 小程序前端配置

后端部署成功后，修改前端项目的 API 地址：

在 bookkeeping-miniapp/utils/request.js 中：
export const BASE_URL = 'http://你的服务器公网IP:8080/api'
