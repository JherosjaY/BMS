import { Elysia } from "elysia";
import { cors } from "@elysiajs/cors";
import { swagger } from "@elysiajs/swagger";
import { bearer } from "@elysiajs/bearer";
import { jwt } from "@elysiajs/jwt";
import { db } from "./db";

// Firebase Cloud Messaging initialization
// Note: FCM will be disabled if environment variables are not set
const initializeFCM = () => {
  const hasFirebaseConfig = 
    process.env.FIREBASE_PROJECT_ID && 
    process.env.FIREBASE_PRIVATE_KEY && 
    process.env.FIREBASE_CLIENT_EMAIL;
  
  if (!hasFirebaseConfig) {
    console.warn("⚠️ Firebase Admin SDK not initialized - FCM notifications will be disabled");
    console.warn("⚠️ To enable FCM, set FIREBASE_PROJECT_ID, FIREBASE_PRIVATE_KEY, and FIREBASE_CLIENT_EMAIL environment variables");
  }
};

initializeFCM();

// Import routes
import { authRoutes } from "./routes/auth";
import { reportsRoutes } from "./routes/reports";
import { usersRoutes } from "./routes/users";
import { officersRoutes } from "./routes/officers";
import { witnessesRoutes } from "./routes/witnesses";
import { suspectsRoutes } from "./routes/suspects";
import { dashboardRoutes } from "./routes/dashboard";
import { personsRoutes } from "./routes/persons";
import { evidenceRoutes } from "./routes/evidence";
import { hearingsRoutes } from "./routes/hearings";
import { resolutionsRoutes } from "./routes/resolutions";
import { activityLogsRoutes } from "./routes/activityLogs";
import { notificationsRoutes } from "./routes/notifications";
import { respondentsRoutes } from "./routes/respondents";

export const app = new Elysia()
  .use(
    cors({
      origin: process.env.ALLOWED_ORIGINS?.split(",") || "*",
    })
  )
  .use(bearer())
  .use(
    jwt({
      name: "jwt",
      secret: process.env.JWT_SECRET || "your-secret-key-change-in-production",
    })
  )
  .use(
    swagger({
      path: "/swagger",
      documentation: {
        info: {
          title: "Blotter Management System API",
          version: "1.0.0",
          description: "API for Blotter Management System - Production Ready",
        },
      },
    })
  )
  // Health check
  .get("/", () => ({
    success: true,
    message: "Blotter API is running!",
    timestamp: new Date().toISOString(),
    endpoints: {
      swagger: "/swagger",
      auth: "/api/auth",
      reports: "/api/reports",
      users: "/api/users",
      officers: "/api/officers",
      witnesses: "/api/witnesses",
      suspects: "/api/suspects",
      dashboard: "/api/dashboard",
      persons: "/api/persons",
      evidence: "/api/evidence",
      hearings: "/api/hearings",
      resolutions: "/api/resolutions",
      activityLogs: "/api/activity-logs",
      notifications: "/api/notifications",
      respondents: "/api/respondents",
    },
  }))
  .get("/health", () => ({
    success: true,
    status: "healthy",
    timestamp: new Date().toISOString(),
  }))
  // Test database connection
  .get("/test-db", async () => {
    try {
      const users = await db.query.users.findMany();
      return {
        success: true,
        database: "connected",
        user_count: users.length,
        message: "✅ Neon database is working perfectly!",
        timestamp: new Date().toISOString(),
      };
    } catch (error: any) {
      return {
        success: false,
        database: "disconnected",
        error: error.message,
        timestamp: new Date().toISOString(),
      };
    }
  })
  // Mount routes
  .group("/api", (app) =>
    app
      .use(authRoutes)
      .use(reportsRoutes)
      .use(usersRoutes)
      .use(officersRoutes)
      .use(witnessesRoutes)
      .use(suspectsRoutes)
      .use(dashboardRoutes)
      .use(personsRoutes)
      .use(evidenceRoutes)
      .use(hearingsRoutes)
      .use(resolutionsRoutes)
      .use(activityLogsRoutes)
      .use(notificationsRoutes)
      .use(respondentsRoutes)
  )
  .listen(process.env.PORT || 3000);

console.log(
  `🦊 Elysia is running at ${app.server?.hostname}:${app.server?.port}`
);

export type ElysiaApp = typeof app;
