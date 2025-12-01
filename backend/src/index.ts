import { Elysia } from 'elysia';
import { cors } from '@elysiajs/cors';
import { swagger } from '@elysiajs/swagger';
import { jwt } from '@elysiajs/jwt';
import 'dotenv/config';

// Import routes
import authRoutes from './routes/auth';
import userRoutes from './routes/users';
import blotterRoutes from './routes/blotters';
import caseRoutes from './routes/cases';
import emailRoutes from './routes/email';
import notificationRoutes from './routes/notifications';
import dashboardRoutes from './routes/dashboard';

const PORT = parseInt(process.env.PORT || '3000');
const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key-change-in-production';

const app = new Elysia()
  // Middleware
  .use(cors())
  .use(swagger({
    documentation: {
      info: {
        title: 'BMS Backend API',
        version: '2.0.0',
        description: 'Blotter Management System Backend - Elysia + TypeScript',
      },
      servers: [
        {
          url: 'http://localhost:3000',
          description: 'Development',
        },
        {
          url: 'https://bms-backend.onrender.com',
          description: 'Production',
        },
      ],
    },
  }))
  .use(jwt({
    name: 'jwt',
    secret: JWT_SECRET,
  }))

  // Health check
  .get('/', () => ({
    message: 'BMS Backend API',
    version: '2.0.0',
    status: 'running',
    timestamp: new Date().toISOString(),
  }))

  // Routes
  .use(authRoutes)
  .use(userRoutes)
  .use(blotterRoutes)
  .use(caseRoutes)
  .use(emailRoutes)
  .use(notificationRoutes)
  .use(dashboardRoutes)

  // Error handling
  .error(({ code, error }) => {
    console.error(`[${code}]`, error);
    return {
      success: false,
      message: error instanceof Error ? error.message : 'Internal server error',
      code,
    };
  })

  .listen(PORT, () => {
    console.log(`🚀 BMS Backend running on http://localhost:${PORT}`);
    console.log(`📚 Swagger docs: http://localhost:${PORT}/swagger`);
  });

export default app;
