import { Elysia, t } from 'elysia';
import { db } from '../db';
import { blotterReports } from '../db/schema';
import { eq } from 'drizzle-orm';

export default new Elysia({ prefix: '/api/cases' })
  .get('/', async () => {
    const cases = await db.query.blotterReports.findMany();
    return { success: true, data: cases, count: cases.length };
  })

  .get(
    '/:id',
    async ({ params }) => {
      const caseData = await db.query.blotterReports.findFirst({
        where: eq(blotterReports.id, params.id),
      });
      if (!caseData) throw new Error('Case not found');
      return { success: true, data: caseData };
    },
    { params: t.Object({ id: t.String() }) }
  )

  .post(
    '/',
    async ({ body }) => {
      const newCase = await db
        .insert(blotterReports)
        .values(body)
        .returning();
      return { success: true, message: 'Case created', data: newCase[0] };
    },
    {
      body: t.Object({
        title: t.String(),
        description: t.Optional(t.String()),
        location: t.Optional(t.String()),
        status: t.Optional(t.String()),
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
      if (!updated.length) throw new Error('Case not found');
      return { success: true, message: 'Case updated', data: updated[0] };
    },
    {
      params: t.Object({ id: t.String() }),
      body: t.Object({
        title: t.Optional(t.String()),
        status: t.Optional(t.String()),
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
      if (!deleted.length) throw new Error('Case not found');
      return { success: true, message: 'Case deleted' };
    },
    { params: t.Object({ id: t.String() }) }
  );
