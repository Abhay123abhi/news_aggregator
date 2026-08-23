# -------- Build Stage --------
FROM node:20-alpine AS build

WORKDIR /app

# Copy only package files first (for caching)
COPY package*.json ./

# Install dependencies (faster & deterministic)
RUN npm ci

# Copy rest of code
COPY . .

# Build app
RUN npm run build

# -------- Production Stage --------
FROM nginxinc/nginx-unprivileged:1.28-alpine

# Copy build output only
COPY --from=build /app/build /usr/share/nginx/html

# Custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
