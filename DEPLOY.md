# 部署到服务器

## 方式一：SSH 部署（推荐）

在本地执行：

```powershell
# 1. 打包 jar
./gradlew bootJar

# 2. 上传到服务器
scp build/libs/bookkeeping-backend-1.0.0.jar root@120.26.28.23:/opt/bookkeeping/app.jar

# 3. SSH 到服务器重新构建镜像
ssh root@120.26.28.23 "cd /opt/bookkeeping && docker build -t bookkeeping-backend:latest . && docker stop bookkeeping-backend 2>/dev/null; docker rm bookkeeping-backend 2>/dev/null; docker compose up -d"
```

## 方式二：GitHub Actions 自动部署

推送代码到 main 分支后，GitHub Actions 会自动构建 Docker 镜像并推送到 Docker Hub，
然后在服务器上自动拉取最新镜像重启。

## 验证部署

```bash
# 查看日志
docker logs bookkeeping-backend

# 检查用户数据
docker exec mysql8 mysql -uroot -p'Gzh1994429.' bookkeeping -e "SELECT id, openid, nickname FROM user ORDER BY id DESC LIMIT 5;"
```
