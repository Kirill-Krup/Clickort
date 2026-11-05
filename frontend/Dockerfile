# ==========================================
# Stage 1: Build the React + TypeScript app
# ==========================================
FROM node:20-alpine AS builder

WORKDIR /app

# Cache dependencies
COPY package.json package-lock.json* ./

RUN npm ci || npm install

# Copy source code
COPY . .

# Optional build-time argument for custom WebSocket URL
ARG VITE_WS_URL
ENV VITE_WS_URL=${VITE_WS_URL}

# Compile TypeScript and Vite production bundle
RUN npm run build

# ==========================================
# Stage 2: Serve with Nginx
# ==========================================
FROM nginx:alpine

# Remove default nginx welcome page
RUN rm -rf /usr/share/nginx/html/*

# Copy custom Nginx configuration with SPA routing and WS proxy support
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy production build from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
