import { Elysia, t } from 'elysia';
import { db } from '../db';
import { notifications } from '../db/schema';
import { eq } from 'drizzle-orm';

export default new Elysia({ prefix: '/api/notifications' })
  // Get all notifications
  .get('/', async () => {
    const allNotifications = await db.query.notifications.findMany();
    return {
      success: true,
      data: allNotifications,
      count: allNotifications.length,
    };
  })

  // Get notifications by user
  .get(
    '/user/:userId',
    async ({ params }) => {
      const userNotifications = await db.query.notifications.findMany({
        where: eq(notifications.userId, params.userId),
      });
      return {
        success: true,
        data: userNotifications,
        count: userNotifications.length,
      };
    },
    {
      params: t.Object({
        userId: t.String(),
      }),
    }
  )

  // Create notification
  .post(
    '/',
    async ({ body }) => {
      const newNotification = await db
        .insert(notifications)
        .values(body)
        .returning();

      return {
        success: true,
        message: 'Notification created',
        data: newNotification[0],
      };
    },
    {
      body: t.Object({
        userId: t.String(),
        title: t.String(),
        message: t.Optional(t.String()),
        type: t.String(),
      }),
    }
  )

  // Mark as read
  .patch(
    '/:id/read',
    async ({ params }) => {
      const updated = await db
        .update(notifications)
        .set({ read: true })
        .where(eq(notifications.id, params.id))
        .returning();

      if (!updated.length) {
        throw new Error('Notification not found');
      }

      return {
        success: true,
        message: 'Notification marked as read',
        data: updated[0],
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
    }
  )

  // Delete notification
  .delete(
    '/:id',
    async ({ params }) => {
      const deleted = await db
        .delete(notifications)
        .where(eq(notifications.id, params.id))
        .returning();

      if (!deleted.length) {
        throw new Error('Notification not found');
      }

      return {
        success: true,
        message: 'Notification deleted',
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
    }
  );
