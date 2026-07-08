#!/bin/bash
set -e

echo "=== Step 1: Check services ==="
nginx -v 2>&1 || echo "nginx not installed"
docker ps --format '{{.Names}}: {{.Status}}'
echo ""

echo "=== Step 2: Install Nginx ==="
if ! command -v nginx &> /dev/null; then
    echo "Installing Nginx..."
    yum install -y epel-release 2>/dev/null || true
    yum install -y nginx 2>/dev/null || (apt update && apt install -y nginx 2>/dev/null) || echo "Need to install nginx manually"
    systemctl enable nginx 2>/dev/null || true
    systemctl start nginx 2>/dev/null || nginx 2>/dev/null || true
    echo "Nginx installed"
else
    echo "Nginx already installed"
fi
echo ""

echo "=== Step 3: Create SSL directory and generate self-signed cert ==="
mkdir -p /etc/nginx/ssl
if [ ! -f /etc/nginx/ssl/server.crt ]; then
    echo "Generating self-signed SSL certificate..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout /etc/nginx/ssl/server.key \
        -out /etc/nginx/ssl/server.crt \
        -subj "/CN=bookkeeping" 2>&1
    echo "SSL certificate generated"
else
    echo "SSL certificate already exists"
fi
echo ""

echo "=== Step 4: Deploy Nginx configuration ==="
cat > /etc/nginx/conf.d/default.conf << 'NGINX_EOF'
# HTTP - redirect to HTTPS
server {
    listen 80;
    server_name _;
    return 301 https://$host$request_uri;
}

# HTTPS - backend API proxy
server {
    listen 443 ssl;
    server_name _;

    ssl_certificate /etc/nginx/ssl/server.crt;
    ssl_certificate_key /etc/nginx/ssl/server.key;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        proxy_buffering off;
    }
}
NGINX_EOF

echo "Nginx config deployed"
echo ""

echo "=== Step 5: Test and reload Nginx ==="
nginx -t 2>&1
systemctl reload nginx 2>/dev/null || nginx -s reload 2>/dev/null || true
echo "Nginx reloaded"
echo ""

echo "=== Step 6: Verify ==="
echo "Listening ports:"
ss -tlnp | grep -E ':(80|443|8080)'
echo ""
echo "Testing HTTP:"
curl -s -o /dev/null -w "HTTP %{http_code}" http://127.0.0.1
echo ""
echo "Testing HTTPS:"
curl -sk -o /dev/null -w "HTTPS %{http_code}" https://127.0.0.1
echo ""
echo "Testing API:"
curl -sk https://127.0.0.1/api/auth/test-login
echo ""
echo ""
echo "=== Nginx setup complete ==="
