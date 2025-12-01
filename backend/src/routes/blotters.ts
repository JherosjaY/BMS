import { Elysia, t } from 'elysia';
import { db } from '../db';
import { blotterReports } from '../db/schema';
import { eq } from 'drizzle-orm';

export default new Elysia({ prefix: '/api/blotters' })
  .get('/', async () => {
    const reports = await db.query.blotterReports.findMany();
    return { success: true, data: reports, count: reports.length };
  })

  .get(
    '/:id',
    async ({ params }) => {
      const report = await db.query.blotterReports.findFirst({
        where: eq(blotterReports.id, params.id),
      });
      if (!report) throw new Error('Report not found');
      return { success: true, data: report };
    },
    { params: t.Object({ id: t.String() }) }
  )

  .post(
    '/',
    async ({ body }) => {
      const newReport = await db
        .insert(blotterReports)
        .values(body)
        .returning();
      return { success: true, message: 'Report created', data: newReport[0] };
    },
    {
      body: t.Object({
        title: t.String(),
        description: t.Optional(t.String()),
        location: t.Optional(t.String()),
        reportedBy: t.Optional(t.String()),
        status: t.Optional(t.String()),
        priority: t.Optional(t.String()),
      }),
    }
  )

  .put(
    '/:id',
    async ({ params, body }) => {
      const updated = await db
        .update(blotterReports)
        .set(body)
        .where(eq(blotterReports.id, params.id))
        .returning();
      if (!updated.length) throw new Error('Report not found');
      return { success: true, message: 'Report updated', data: updated[0] };
    },
    {
      params: t.Object({ id: t.String() }),
      body: t.Object({
        title: t.Optional(t.String()),
        description: t.Optional(t.String()),
        status: t.Optional(t.String()),
        priority: t.Optional(t.String()),
      }),
    }
  )

  .delete(
    '/:id',
    async ({ params }) => {
      const deleted = await db
        .delete(blotterReports)
        .where(eq(blotterReports.id, params.id))
        .returning();
      if (!deleted.length) throw new Error('Report not found');
      return { success: true, message: 'Report deleted' };
    },
    { params: t.Object({ id: t.String() }) }
  );
