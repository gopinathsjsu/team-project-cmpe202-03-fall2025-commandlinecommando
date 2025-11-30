# ==============================================
# Multi-Stage Dockerfile for Campus Marketplace Frontend
# Optimized for AWS EC2 + ALB Deployment
# ==============================================

# Stage 1: Build Stage
FROM node:20-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Copy source code
COPY . .

# Build arguments for environment configuration
ARG VITE_API_URL=/api
ARG VITE_AI_SERVICE_URL=/ai

# Set environment variables for build
ENV VITE_API_URL=${VITE_API_URL}
ENV VITE_AI_SERVICE_URL=${VITE_AI_SERVICE_URL}

# Build the application
RUN npm run build

# Stage 2: Production Stage with Nginx
FROM nginx:alpine

# Add metadata
LABEL maintainer="Campus Marketplace Team"
LABEL description="Campus Marketplace Frontend"

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create nginx cache directories
RUN mkdir -p /var/cache/nginx/client_temp && \
    mkdir -p /var/cache/nginx/proxy_temp && \
    mkdir -p /var/run/nginx && \
    chown -R nginx:nginx /var/cache/nginx /var/run/nginx /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Health check for ALB
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]

