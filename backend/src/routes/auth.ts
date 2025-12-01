import { Elysia, t } from 'elysia';
import { db } from '../db';
import { users } from '../db/schema';
import { eq } from 'drizzle-orm';
import bcrypt from 'bcryptjs';

export default new Elysia({ prefix: '/api/auth' })
  // Check if email exists
  .post(
    '/check-email',
    async ({ body }) => {
      const existingUser = await db.query.users.findFirst({
        where: eq(users.email, body.email),
      });

      return {
        success: true,
        exists: !!existingUser,
      };
    },
    {
      body: t.Object({
        email: t.String({ format: 'email' }),
      }),
    }
  )

  // Register
  .post(
    '/register',
    async ({ body }) => {
      const existingUser = await db.query.users.findFirst({
        where: eq(users.email, body.email),
      });

      if (existingUser) {
        throw new Error('Email already exists');
      }

      const hashedPassword = await bcrypt.hash(body.password, 10);

      const newUser = await db
        .insert(users)
        .values({
          email: body.email,
          username: body.username,
          passwordHash: hashedPassword,
          firstName: body.firstName || 'User',
          lastName: body.lastName || 'Account',
          role: 'User',
        })
        .returning();

      return {
        success: true,
        message: 'User registered successfully',
        data: newUser[0],
      };
    },
    {
      body: t.Object({
        username: t.String(),
        email: t.String({ format: 'email' }),
        password: t.String({ minLength: 6 }),
        firstName: t.Optional(t.String()),
        lastName: t.Optional(t.String()),
      }),
    }
  )

  // Login
  .post(
    '/login',
    async ({ body }) => {
      const user = await db.query.users.findFirst({
        where: eq(users.email, body.email),
      });

      if (!user || !user.passwordHash) {
        throw new Error('Invalid credentials');
      }

      const passwordMatch = await bcrypt.compare(body.password, user.passwordHash);

      if (!passwordMatch) {
        throw new Error('Invalid credentials');
      }

      return {
        success: true,
        message: 'Login successful',
        data: {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          role: user.role,
        },
      };
    },
    {
      body: t.Object({
        email: t.String({ format: 'email' }),
        password: t.String(),
      }),
    }
  )

  // Google Sign-In
  .post(
    '/google',
    async ({ body }) => {
      let user = await db.query.users.findFirst({
        where: eq(users.email, body.email),
      });

      if (!user) {
        const newUser = await db
          .insert(users)
          .values({
            email: body.email,
            firstName: body.firstName || 'User',
            lastName: body.lastName || 'Account',
            profilePictureUrl: body.profilePictureUrl,
            role: 'User',
          })
          .returning();

        user = newUser[0];
      }

      return {
        success: true,
        message: 'Google sign-in successful',
        data: {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          role: user.role,
          profilePictureUrl: user.profilePictureUrl,
        },
      };
    },
    {
      body: t.Object({
        email: t.String({ format: 'email' }),
        firstName: t.Optional(t.String()),
        lastName: t.Optional(t.String()),
        profilePictureUrl: t.Optional(t.String()),
      }),
    }
  );
