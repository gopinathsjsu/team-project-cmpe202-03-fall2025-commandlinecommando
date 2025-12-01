# Application Load Balancer (ALB) Setup Guide

## Overview

This guide walks you through setting up an Application Load Balancer (ALB) to route traffic to your EC2 instance running the Campus Marketplace application.

## Prerequisites

- EC2 instance running with all services healthy
- EC2 instance security group allows traffic from ALB
- Your EC2 instance ID and private IP address

## Step 1: Update EC2 Security Group

First, ensure your EC2 security group allows traffic from the ALB:

1. Go to **EC2 Console** → **Security Groups**
2. Select your EC2 instance's security group
3. **Edit inbound rules**:
   - **HTTP (80)**: Allow from `0.0.0.0/0` (or ALB security group)
   - **HTTPS (443)**: Allow from `0.0.0.0/0` (or ALB security group)
   - **Port 8080**: Allow from ALB security group (we'll create this)
   - **Port 3001**: Allow from ALB security group (optional, for AI service)

**Note**: For better security, create an ALB security group first and reference it.

## Step 2: Create ALB Security Group

1. Go to **EC2 Console** → **Security Groups** → **Create security group**
2. **Name**: `alb-campus-marketplace-sg`
3. **Description**: Security group for Campus Marketplace ALB
4. **Inbound rules**:
   - **HTTP (80)**: `0.0.0.0/0` (or your IP for testing)
   - **HTTPS (443)**: `0.0.0.0/0` (if using SSL)
5. **Outbound rules**: Default (allow all)
6. Click **Create security group**

## Step 3: Create Target Groups

### 3.1 Frontend Target Group

1. Go to **EC2 Console** → **Target Groups** → **Create target group**
2. **Target type**: Instances
3. **Target group name**: `campus-marketplace-frontend-tg`
4. **Protocol**: HTTP
5. **Port**: 80
6. **VPC**: Select your EC2 instance's VPC
7. **Health checks**:
   - **Health check protocol**: HTTP
   - **Health check path**: `/health`
   - **Advanced health check settings**:
     - **Healthy threshold**: 2
     - **Unhealthy threshold**: 3
     - **Timeout**: 5 seconds
     - **Interval**: 30 seconds
     - **Success codes**: 200
8. Click **Next**
9. **Register targets**: Select your EC2 instance
10. Click **Include as pending below**
11. Click **Create target group**

### 3.2 Backend Target Group (Optional)

If you want to expose the backend API directly through ALB:

1. **Create target group**
2. **Target group name**: `campus-marketplace-backend-tg`
3. **Protocol**: HTTP
4. **Port**: 8080
5. **Health check path**: `/api/actuator/health`
6. Register your EC2 instance
7. Click **Create target group**

### 3.3 AI Service Target Group (Optional)

1. **Create target group**
2. **Target group name**: `campus-marketplace-ai-tg`
3. **Protocol**: HTTP
4. **Port**: 3001
5. **Health check path**: `/api/health`
6. Register your EC2 instance
7. Click **Create target group**

## Step 4: Create Application Load Balancer

1. Go to **EC2 Console** → **Load Balancers** → **Create Load Balancer**
2. Select **Application Load Balancer**
3. **Basic configuration**:
   - **Name**: `campus-marketplace-alb`
   - **Scheme**: Internet-facing
   - **IP address type**: IPv4
4. **Network mapping**:
   - **VPC**: Select your EC2 instance's VPC
   - **Availability Zones**: Select at least 2 subnets in different AZs
5. **Security groups**: Select `alb-campus-marketplace-sg`
6. **Listeners and routing**:
   - **Listener 1**: HTTP : 80
     - **Default action**: Forward to `campus-marketplace-frontend-tg`
   - **Listener 2** (Optional): HTTPS : 443
     - **Default SSL certificate**: Select from ACM (if you have one)
     - **Default action**: Forward to `campus-marketplace-frontend-tg`
7. Click **Create load balancer**

## Step 5: Configure Listener Rules (Optional)

If you created backend and AI service target groups, add routing rules:

1. Go to your ALB → **Listeners** tab
2. Select the HTTP (80) listener → **View/Edit rules**
3. Click **Add rule** (insert before default rule)
4. **Rule 1 - AI Service**:
   - **IF**: Path is `/ai/*`
   - **THEN**: Forward to `campus-marketplace-ai-tg`
5. **Rule 2 - Backend API** (if exposing directly):
   - **IF**: Path is `/api/*`
   - **THEN**: Forward to `campus-marketplace-backend-tg`
6. **Default rule**: Forward to `campus-marketplace-frontend-tg`

**Note**: Since your frontend Nginx already proxies `/api/` and `/ai/` requests, you typically only need the frontend target group. The routing rules above are optional if you want direct access.

## Step 6: Update EC2 Security Group

Update your EC2 security group to allow traffic from the ALB:

1. Go to **EC2 Console** → **Security Groups**
2. Select your EC2 instance's security group
3. **Edit inbound rules**:
   - **HTTP (80)**: Source = ALB security group ID (`alb-campus-marketplace-sg`)
   - **Port 8080**: Source = ALB security group ID (if using backend target group)
   - **Port 3001**: Source = ALB security group ID (if using AI target group)
4. Save rules

## Step 7: Verify ALB Setup

1. **Get ALB DNS name**:
   - Go to **Load Balancers** → Select your ALB
   - Copy the **DNS name** (e.g., `campus-marketplace-alb-123456789.us-west-1.elb.amazonaws.com`)

2. **Test health checks**:
   ```bash
   # Test frontend health
   curl http://<ALB-DNS-NAME>/health
   
   # Test backend health (if exposed)
   curl http://<ALB-DNS-NAME>/api/actuator/health
   
   # Test AI service health (if exposed)
   curl http://<ALB-DNS-NAME>/ai/api/health
   ```

3. **Check target health**:
   - Go to **Target Groups** → Select your target group
   - Check **Targets** tab - should show "healthy" status

## Step 8: Access Your Application

Once the ALB is active and targets are healthy:

- **Frontend**: `http://<ALB-DNS-NAME>`
- **Backend API**: `http://<ALB-DNS-NAME>/api` (proxied through frontend)
- **AI Service**: `http://<ALB-DNS-NAME>/ai` (proxied through frontend)

## Step 9: Optional - Set Up HTTPS

1. **Request SSL Certificate**:
   - Go to **AWS Certificate Manager (ACM)**
   - Request a public certificate
   - Add your domain name(s)
   - Validate the certificate

2. **Add HTTPS Listener**:
   - Go to your ALB → **Listeners** tab
   - Click **Add listener**
   - **Protocol**: HTTPS
   - **Port**: 443
   - **Default SSL certificate**: Select your ACM certificate
   - **Default action**: Forward to `campus-marketplace-frontend-tg`

3. **Redirect HTTP to HTTPS**:
   - Edit HTTP (80) listener
   - Change default action to **Redirect to HTTPS**
   - Port: 443
   - Protocol: HTTPS
   - Status code: 301 - Permanently moved

## Step 10: Optional - Configure Domain Name (Route 53)

1. Go to **Route 53** → **Hosted zones**
2. Create or select your hosted zone
3. **Create record**:
   - **Record name**: Your domain (e.g., `marketplace.yourdomain.com`)
   - **Record type**: A - Routes traffic to an IPv4 address
   - **Alias**: Yes
   - **Alias target**: Select your ALB
   - Click **Create records**

## Troubleshooting

### Targets showing as unhealthy

1. **Check security groups**: Ensure EC2 allows traffic from ALB
2. **Check health check path**: Verify `/health` endpoint works:
   ```bash
   curl http://<EC2-Private-IP>/health
   ```
3. **Check application logs**:
   ```bash
   docker-compose -f docker-compose.prod.yml logs frontend
   ```

### ALB returning 502 Bad Gateway

1. **Check target group health**: Ensure at least one target is healthy
2. **Check EC2 instance**: Verify containers are running:
   ```bash
   docker-compose -f docker-compose.prod.yml ps
   ```
3. **Check security groups**: Ensure ALB can reach EC2

### Health checks failing

1. **Verify health endpoint**: Test directly on EC2:
   ```bash
   curl http://localhost/health
   ```
2. **Check health check configuration**: Ensure path, port, and protocol match
3. **Check application startup**: Ensure frontend container is fully started

## Architecture with ALB

```
Internet
   │
   ▼
┌──────────────┐
│      ALB     │ (Port 80/443)
│  (DNS Name)  │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   EC2 Instance│
│  (Port 80)   │
│              │
│  ┌─────────┐ │
│  │Frontend │ │ (Nginx - Port 80)
│  │Container│ │
│  └────┬────┘ │
│       │      │
│  ┌────▼────┐ │
│  │ Backend │ │ (Spring Boot - Port 8080)
│  │Container│ │
│  └─────────┘ │
│              │
│  ┌─────────┐ │
│  │   AI    │ │ (Java - Port 3001)
│  │ Service │ │
│  └─────────┘ │
└──────────────┘
```

## Cost Considerations

- **ALB**: ~$0.0225 per hour (~$16/month)
- **Data transfer**: First 1 GB free, then $0.008 per GB
- **Consider**: Use ALB for production, direct EC2 IP for development/testing

## Next Steps

1. Set up CloudWatch alarms for ALB metrics
2. Configure auto-scaling if needed
3. Set up WAF (Web Application Firewall) for additional security
4. Configure access logs for monitoring

