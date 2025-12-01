import { Elysia, t } from 'elysia';
import { db } from '../db';
import { users } from '../db/schema';
import { eq } from 'drizzle-orm';
import bcryptjs from 'bcryptjs';

export default new Elysia({ prefix: '/api/auth' })
  // Check if email exists
  .post(
    '/check-email',
    async ({ body }) => {
      const { email } = body;
      const user = await db.query.users.findFirst({
        where: eq(users.email, email),
      });
      return {
        success: true,
        exists: !!user,
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
      const { email, username, password, firstName, lastName } = body;

      // Check if user exists
      const existingUser = await db.query.users.findFirst({
        where: eq(users.email, email),
      });

      if (existingUser) {
        throw new Error('Email already registered');
      }

      // Hash password
      const hashedPassword = await bcryptjs.hash(password, 10);

      // Create user
      const newUser = await db
        .insert(users)
        .values({
          email,
          username,
          password: hashedPassword,
          firstName,
          lastName,
          role: 'user',
        })
        .returning();

      return {
        success: true,
        message: 'User registered successfully',
        data: {
          id: newUser[0].id,
          email: newUser[0].email,
          firstName: newUser[0].firstName,
        },
      };
    },
    {
      body: t.Object({
        email: t.String({ format: 'email' }),
        username: t.String(),
        password: t.String({ minLength: 6 }),
        firstName: t.Optional(t.String()),
        lastName: t.Optional(t.String()),
      }),
    }
  )

  // Login
  .post(
    '/login',
    async ({ body, jwt }) => {
      const { email, password } = body;

      const user = await db.query.users.findFirst({
        where: eq(users.email, email),
      });

      if (!user) {
        throw new Error('User not found');
      }

      if (!user.password) {
        throw new Error('User has no password set');
      }

      const isPasswordValid = await bcryptjs.compare(password, user.password);

      if (!isPasswordValid) {
        throw new Error('Invalid password');
      }

      const token = await jwt.sign({
        id: user.id,
        email: user.email,
        role: user.role,
      });

      return {
        success: true,
        message: 'Login successful',
        token,
        data: {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
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
    async ({ body, jwt }) => {
      const { googleId, email, firstName, lastName, profilePictureUrl } = body;

      let user = await db.query.users.findFirst({
        where: eq(users.firebaseUid, googleId),
      });

      if (!user) {
        // Create new user
        const newUser = await db
          .insert(users)
          .values({
            email,
            firstName,
            lastName,
            firebaseUid: googleId,
            profilePictureUrl,
            role: 'user',
          })
          .returning();

        user = newUser[0];
      }

      const token = await jwt.sign({
        id: user.id,
        email: user.email,
        role: user.role,
      });

      return {
        success: true,
        message: 'Google sign-in successful',
        token,
        data: {
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          role: user.role,
        },
      };
    },
    {
      body: t.Object({
        googleId: t.String(),
        email: t.String({ format: 'email' }),
        firstName: t.Optional(t.String()),
        lastName: t.Optional(t.String()),
        profilePictureUrl: t.Optional(t.String()),
      }),
    }
  );
