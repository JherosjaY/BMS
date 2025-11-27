FROM oven/bun:1-alpine

WORKDIR /app

# Copy all files from root
COPY package.json .
COPY package-lock.json* .
COPY tsconfig.json* .
COPY drizzle.config.ts* .
COPY biome.json* .

# Copy source code and database
COPY src ./src
COPY database ./database

# Install dependencies
RUN bun install

# Expose port
EXPOSE 3000

# Start the app
CMD ["bun", "run", "src/index.ts"]
