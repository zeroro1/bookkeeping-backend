#!/bin/bash
# ============================================================
# 记账后端 - Docker 部署脚本
# 使用方法: bash deploy.sh [mysql_host] [mysql_user] [mysql_password]
# 示例:   bash deploy.sh 172.17.0.1 root gzh1994429
# ============================================================

set -e

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  记账应用后端 - Docker 部署脚本${NC}"
echo -e "${GREEN}========================================${NC}"

# 参数
MYSQL_HOST=${1:-"127.0.0.1"}
MYSQL_USER=${2:-"root"}
MYSQL_PASSWORD=${3:-""}

if [ -z "$MYSQL_PASSWORD" ]; then
    echo -e "${YELLOW}提示: 请设置 MySQL 密码${NC}"
    echo "用法: bash deploy.sh [mysql_host] [mysql_user] [mysql_password]"
    exit 1
fi

PROJECT_DIR="/opt/bookkeeping"
echo -e "\n${GREEN}[1/6] 创建项目目录...${NC}"
mkdir -p "$PROJECT_DIR"
cd "$PROJECT_DIR"

echo -e "\n${GREEN}[2/6] 拉取后端代码...${NC}"
if [ ! -d ".git" ]; then
    git clone https://github.com/zeroro1/bookkeeping-backend.git .
else
    git pull origin main
fi

echo -e "\n${GREEN}[3/6] 创建环境配置...${NC}"
printf "MYSQL_HOST=%s\nMYSQL_USER=%s\nMYSQL_PASSWORD=%s\n" "$MYSQL_HOST" "$MYSQL_USER" "$MYSQL_PASSWORD" > .env
echo -e "${GREEN}✓ MySQL 地址: ${MYSQL_HOST}${NC}"

echo -e "\n${GREEN}[4/6] 停止旧容器...${NC}"
docker-compose -f docker-compose.prod.yml down 2>/dev/null || true

echo -e "\n${GREEN}[5/6] 构建 Docker 镜像...${NC}"
docker-compose -f docker-compose.prod.yml build --no-cache

echo -e "\n${GREEN}[6/6] 启动服务...${NC}"
docker-compose -f docker-compose.prod.yml up -d

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  部署完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${YELLOW}查看日志: docker logs -f bookkeeping-backend${NC}"
echo -e "${YELLOW}测试接口: curl http://localhost:8080/api/auth/test-login${NC}"
echo -e "\n${GREEN}等待服务启动...${NC}"
sleep 5
docker logs --tail 30 bookkeeping-backend
