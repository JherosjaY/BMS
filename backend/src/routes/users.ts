import { Elysia, t } from 'elysia';
import { db } from '../db';
import { users } from '../db/schema';
import { eq } from 'drizzle-orm';

export default new Elysia({ prefix: '/api/users' })
  // Get all users
  .get('/', async () => {
    const allUsers = await db.query.users.findMany();
    return {
      success: true,
      data: allUsers,
      count: allUsers.length,
    };
  })

  // Get user by ID
  .get(
    '/:id',
    async ({ params }) => {
      const user = await db.query.users.findFirst({
        where: eq(users.id, params.id),
        with: {
          images: true,
        },
      });

      if (!user) {
        throw new Error('User not found');
      }

      return {
        success: true,
        data: user,
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
    }
  )

  // Update user
  .put(
    '/:id',
    async ({ params, body }) => {
      const updated = await db
        .update(users)
        .set({
          ...body,
          updatedAt: new Date(),
        })
        .where(eq(users.id, params.id))
        .returning();

      if (!updated.length) {
        throw new Error('User not found');
      }

      return {
        success: true,
        message: 'User updated successfully',
        data: updated[0],
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
      body: t.Object({
        firstName: t.Optional(t.String()),
        lastName: t.Optional(t.String()),
        profilePictureUrl: t.Optional(t.String()),
      }),
    }
  )

  // Delete user
  .delete(
    '/:id',
    async ({ params }) => {
      const deleted = await db
        .delete(users)
        .where(eq(users.id, params.id))
        .returning();

      if (!deleted.length) {
        throw new Error('User not found');
      }

      return {
        success: true,
        message: 'User deleted successfully',
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
    }
  );
