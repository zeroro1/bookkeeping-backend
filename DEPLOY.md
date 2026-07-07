# 服务器部署指南

## 前提条件

服务器需要安装：
- Docker
- Docker Compose
- Git

## 方式一：使用一键部署脚本（推荐）

```bash
# 1. SSH 登录服务器
ssh 用户@你的服务器IP

# 2. 执行部署脚本（替换为你的实际 MySQL 信息）
bash deploy.sh 172.17.0.1 root gzh1994429

# 注意：如果 MySQL 和后端在同一台服务器且都在 Docker 中
# MYSQL_HOST 应该填 MySQL 容器的名称（如 bookkeeping-mysql）
# 如果 MySQL 装在服务器本机，填 127.0.0.1 或服务器内网IP
```

## 方式二：手动分步部署

### 第 1 步：创建目录并拉取代码

```bash
mkdir -p /opt/bookkeeping
cd /opt/bookkeeping
git clone https://github.com/zeroro1/bookkeeping-backend.git .
```

### 第 2 步：创建 .env 配置文件

```bash
# 编辑 .env 文件
vim .env

# 填入以下内容（修改为你自己的值）：
MYSQL_HOST=172.17.0.1    # MySQL 地址（见下方说明）
MYSQL_USER=root           # MySQL 用户名
MYSQL_PASSWORD=gzh1994429 # MySQL 密码
```

**MYSQL_HOST 怎么填？**
- 如果 MySQL 在 Docker 容器中：填 MySQL 容器的名字（如 `bookkeeping-mysql`）
- 如果 MySQL 装在服务器本机：填 `127.0.0.1`
- 如果 MySQL 在其他服务器：填该服务器的内网 IP

### 第 3 步：构建 Docker 镜像

```bash
docker-compose -f docker-compose.prod.yml build --no-cache
```

### 第 4 步：启动服务

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 第 5 步：查看日志确认启动成功

```bash
docker logs -f bookkeeping-backend
```

看到 `Started Application` 或类似字样表示启动成功。

### 第 6 步：测试接口

```bash
# 测试登录接口
curl http://localhost:8080/api/auth/test-login

# 预期返回：
# {"code":200,"message":"测试登录成功","data":{"userId":1,"openid":"test_openid","token":"..."}}
```

## 常用命令

```bash
# 查看容器状态
docker ps

# 查看后端日志
docker logs bookkeeping-backend

# 实时日志
docker logs -f bookkeeping-backend

# 重启服务
docker-compose -f docker-compose.prod.yml restart

# 停止服务
docker-compose -f docker-compose.prod.yml down

# 更新代码后重新部署
cd /opt/bookkeeping
git pull origin main
docker-compose -f docker-compose.prod.yml build --no-cache
docker-compose -f docker-compose.prod.yml up -d
```

## 防火墙配置

确保服务器安全组/防火墙开放 8080 端口：

```bash
# Ubuntu/Debian (ufw)
sudo ufw allow 8080/tcp

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

## 小程序前端配置

部署成功后，需要修改前端项目的 API 地址：

在 `bookkeeping-miniapp/utils/request.js` 中：
```javascript
// 改为你的服务器公网 IP
export const BASE_URL = 'http://你的服务器公网IP:8080/api'
```
