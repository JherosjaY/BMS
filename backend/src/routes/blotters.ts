import { Elysia, t } from 'elysia';
import { db } from '../db';
import { blotterReports } from '../db/schema';
import { eq } from 'drizzle-orm';

export default new Elysia({ prefix: '/api/blotters' })
  // Get all blotter reports
  .get('/', async () => {
    const reports = await db.query.blotterReports.findMany({
      with: {
        evidence: true,
      },
    });
    return {
      success: true,
      data: reports,
      count: reports.length,
    };
  })

  // Get blotter by ID
  .get(
    '/:id',
    async ({ params }) => {
      const report = await db.query.blotterReports.findFirst({
        where: eq(blotterReports.id, params.id),
        with: {
          evidence: true,
        },
      });

      if (!report) {
        throw new Error('Blotter report not found');
      }

      return {
        success: true,
        data: report,
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
    }
  )

  // Create blotter report
  .post(
    '/',
    async ({ body }) => {
      const newReport = await db
        .insert(blotterReports)
        .values({
          ...body,
          status: 'pending',
        })
        .returning();

      return {
        success: true,
        message: 'Blotter report created successfully',
        data: newReport[0],
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

  // Update blotter report
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
        throw new Error('Blotter report not found');
      }

      return {
        success: true,
        message: 'Blotter report updated successfully',
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

  // Delete blotter report
  .delete(
    '/:id',
    async ({ params }) => {
      const deleted = await db
        .delete(blotterReports)
        .where(eq(blotterReports.id, params.id))
        .returning();

      if (!deleted.length) {
        throw new Error('Blotter report not found');
      }

      return {
        success: true,
        message: 'Blotter report deleted successfully',
      };
    },
    {
      params: t.Object({
        id: t.String(),
      }),
    }
  );
