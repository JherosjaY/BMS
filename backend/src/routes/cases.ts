import { Elysia, t } from 'elysia';
import { db } from '../db';
import { blotterReports } from '../db/schema';
import { eq } from 'drizzle-orm';

export default new Elysia({ prefix: '/api/cases' })
  // Get all cases
  .get('/', async () => {
    const cases = await db.query.blotterReports.findMany({
      with: {
        evidence: true,
      },
    });
    return {
      success: true,
      data: cases,
      count: cases.length,
    };
  })

  // Get case by ID
  .get(
    '/:id',
    async ({ params }) => {
      const caseData = await db.query.blotterReports.findFirst({
        where: eq(blotterReports.id, params.id),
        with: {
          evidence: true,
        },
      });

      if (!caseData) {
        throw new Error('Case not found');
      }

      return {
        success: true,
        data: caseData,
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
    }
  )

  // Create case
  .post(
    '/',
    async ({ body }) => {
      const newCase = await db
        .insert(blotterReports)
        .values({
          ...body,
          status: 'pending',
        })
        .returning();

      return {
        success: true,
        message: 'Case created successfully',
        data: newCase[0],
      };
    },
    {
      body: t.Object({
        title: t.String(),
        description: t.Optional(t.String()),
        complainantName: t.Optional(t.String()),
        respondentName: t.Optional(t.String()),
        location: t.Optional(t.String()),
        incidentDate: t.Optional(t.String()),
        priority: t.Optional(t.String()),
        createdById: t.Optional(t.String()),
      }),
    }
  )

  // Update case
  .put(
    '/:id',
    async ({ params, body }) => {
      const updated = await db
        .update(blotterReports)
        .set({
          ...body,
          updatedAt: new Date(),
        })
        .where(eq(blotterReports.id, params.id))
        .returning();

      if (!updated.length) {
        throw new Error('Case not found');
      }

      return {
        success: true,
        message: 'Case updated successfully',
        data: updated[0],
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
      body: t.Object({
        title: t.Optional(t.String()),
        status: t.Optional(t.String()),
        priority: t.Optional(t.String()),
        assignedOfficerId: t.Optional(t.String()),
      }),
    }
  )

  // Delete case
  .delete(
    '/:id',
    async ({ params }) => {
      const deleted = await db
        .delete(blotterReports)
        .where(eq(blotterReports.id, params.id))
        .returning();

      if (!deleted.length) {
        throw new Error('Case not found');
      }

      return {
        success: true,
        message: 'Case deleted successfully',
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
    }
  );
