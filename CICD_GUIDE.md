# GitHub Actions CI/CD 配置指南

## 已完成的配置

✅ 工作流文件：`.github/workflows/deploy.yml`
✅ Dockerfile：多阶段构建，镜像约 300MB
✅ .dockerignore：排除不必要的文件
✅ docker-compose.prod.yml：环境变量配置模板

## 你需要做的配置（只需一次）

### 第 1 步：在 GitHub 仓库添加 Secrets

访问你的仓库：https://github.com/zeroro1/bookkeeping-backend

点击 **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

添加以下 4 个 Secrets：

| Secret 名称 | 值 | 说明 |
|------------|-----|------|
| `DEPLOY_HOST` | `120.26.28.23` | 阿里云服务器公网 IP |
| `DEPLOY_USER` | `root` | SSH 登录用户名 |
| `DEPLOY_PASSWORD` | `Gzh1994429.` | SSH 登录密码 |
| `MYSQL_CONTAINER_NAME` | `mysql8` | MySQL 容器名称 |
| `MYSQL_PASSWORD` | `Gzh1994429.` | 数据库密码 |

**操作步骤：**
1. 点击 "New repository secret" 按钮
2. 输入 Name（如 `DEPLOY_HOST`）
3. 输入 Value（如 `120.26.28.23`）
4. 点击 "Add secret"
5. 重复以上步骤添加所有 Secrets

### 第 2 步：在服务器上安装 Docker（如果还没装）

SSH 登录你的服务器：

```bash
ssh root@120.26.28.23
```

检查 Docker 是否已安装：

```bash
docker --version
docker-compose --version
```

如果未安装，执行：

```bash
# 安装 Docker
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun

# 启动 Docker 服务
systemctl start docker
systemctl enable docker

# 安装 docker-compose（如果还没有）
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```

### 第 3 步：确保 MySQL 容器已运行

```bash
# 查看 MySQL 容器
docker ps | grep mysql8

# 如果没有运行，启动它
docker start mysql8
```

如果你的 MySQL 容器还没创建，执行：

```bash
docker run -d \
  --name mysql8 \
  --restart always \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=Gzh1994429. \
  -v /opt/mysql/data:/var/lib/mysql \
  mysql:8.0

# 创建数据库
docker exec -i mysql8 mysql -uroot -pGzh1994429. -e "CREATE DATABASE IF NOT EXISTS bookkeeping DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 第 4 步：配置阿里云安全组

确保开放以下端口：

| 端口 | 协议 | 来源 | 说明 |
|------|------|------|------|
| 22 | TCP | 0.0.0.0/0 | SSH 登录 |
| 8080 | TCP | 0.0.0.0/0 | 后端 API |
| 3306 | TCP | 按需 | MySQL（测试阶段开放，生产建议关闭） |

**配置路径：**
```
阿里云控制台 → ECS 实例 → 安全组 → 配置规则 → 添加入方向规则
```

## 如何使用

### 手动触发部署

1. 在本地修改后端代码
2. 提交并推送到 main 分支：

```bash
cd C:\Users\ze451\Documents\agnes_projext\bookkeeping-backend
git add .
git commit -m "描述你的修改"
git push origin main
```

3. 访问 GitHub 仓库页面：https://github.com/zeroro1/bookkeeping-backend/actions
4. 点击左侧 **"Actions"** → **"Deploy to Production"**
5. 点击 **"Run workflow"** 按钮旁边的绿色按钮
6. 选择分支（默认 main）→ 点击 **"Run workflow"**

### 自动部署（可选）

如果需要 push 后自动部署，修改 `.github/workflows/deploy.yml` 第一行：

```yaml
on:
  push:
    branches: [ main ]
```

去掉 `workflow_dispatch` 即可（当前配置两种都支持）。

## 部署流程说明

```
触发部署
    ↓
构建 Docker 镜像（Maven 编译 + 打包）
    ↓
推送到 GitHub Container Registry (GHCR)
    ↓
SSH 登录阿里云服务器
    ↓
拉取最新代码
    ↓
创建 .env 配置文件
    ↓
从 GHCR 拉取新镜像
    ↓
停止旧容器，启动新容器
    ↓
输出日志，确认部署成功
```

整个过程约 **3-5 分钟**。

## 常见问题

### Q1: 部署失败，如何回滚？

```bash
# SSH 登录服务器
ssh root@120.26.28.23

# 查看之前的镜像
docker images | grep bookkeeping-backend

# 停止当前容器
docker stop bookkeeping-backend
docker rm bookkeeping-backend

# 用旧版本镜像重新启动（替换 <旧镜像tag>）
docker run -d \
  --name bookkeeping-backend \
  --restart always \
  -p 8080:8080 \
  --env-file /opt/bookkeeping/.env \
  ghcr.io/zeroro1/bookkeeping-backend/bookkeeping-backend:<旧镜像tag>
```

### Q2: 如何查看部署日志？

```bash
# 查看容器日志
docker logs bookkeeping-backend

# 实时日志
docker logs -f bookkeeping-backend

# 查看最近 100 行
docker logs --tail 100 bookkeeping-backend
```

### Q3: 如何手动重启服务？

```bash
docker restart bookkeeping-backend
```

### Q4: GitHub Actions 构建失败怎么办？

1. 访问 https://github.com/zeroro1/bookkeeping-backend/actions
2. 点击失败的 workflow run
3. 查看详细日志，定位错误
4. 常见错误：
   - Maven 依赖下载超时 → 重试即可
   - 编译错误 → 检查代码
   - SSH 连接失败 → 检查 Secrets 配置

### Q5: 如何更新 MySQL 密码？

1. 修改 GitHub Secrets 中的 `MYSQL_PASSWORD`
2. 重新触发部署
3. 新的 `.env` 文件会自动更新

## 安全建议

1. **不要**在代码中硬编码密码（已避免，使用环境变量）
2. **不要**在 GitHub 上公开 `.env` 文件（已在 .gitignore 中排除）
3. 生产环境建议：
   - 关闭 MySQL 的 3306 端口外网访问
   - 使用更强的 JWT secret
   - 启用 HTTPS（可以用 Nginx 反向代理）

## 下一步

- [x] 配置 GitHub Actions 工作流
- [x] 配置 Dockerfile 多阶段构建
- [x] 配置服务器 SSH 密码登录
- [ ] 在 GitHub 添加 Secrets（你需要手动操作）
- [ ] 在服务器上安装 Docker（如果需要）
- [ ] 配置阿里云安全组
- [ ] 首次手动触发部署测试
- [ ] 小程序前端修改 API 地址为你的服务器 IP
