import { Elysia } from 'elysia';
import { db } from '../db';
import { blotterReports, users, notifications } from '../db/schema';
import { count, eq } from 'drizzle-orm';

export default new Elysia({ prefix: '/api/dashboard' })
  .get('/', async () => {
    const totalCases = await db
      .select({ count: count() })
      .from(blotterReports);
    
    const totalUsers = await db
      .select({ count: count() })
      .from(users);
    
    const pendingCases = await db
      .select({ count: count() })
      .from(blotterReports)
      .where(eq(blotterReports.status, 'pending'));

    const recentCases = await db.query.blotterReports.findMany({
      limit: 5,
      orderBy: (cases, { desc }) => [desc(cases.createdAt)],
    });

    return {
      success: true,
      data: {
        totalCases: totalCases[0]?.count || 0,
        totalUsers: totalUsers[0]?.count || 0,
        pendingCases: pendingCases[0]?.count || 0,
        recentCases,
      },
    };
  })

  .get('/officer-workload', async () => {
    const workload = await db.query.blotterReports.findMany({
      with: { assignedOfficer: true },
    });

    const grouped = workload.reduce(
      (acc, report) => {
        const officerId = report.assignedOfficerId;
        if (!officerId) return acc;
        if (!acc[officerId]) {
          acc[officerId] = {
            officer: report.assignedOfficer,
            caseCount: 0,
            pendingCount: 0,
          };
        }
        acc[officerId].caseCount++;
        if (report.status === 'pending') acc[officerId].pendingCount++;
        return acc;
      },
      {} as Record<string, any>
    );

    return { success: true, data: Object.values(grouped) };
  })

  .get('/case-status', async () => {
    const allCases = await db.query.blotterReports.findMany();
    const distribution = allCases.reduce(
      (acc, report) => {
        const status = report.status || 'unknown';
        acc[status] = (acc[status] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );
    return { success: true, data: distribution };
  })

  .get('/blotter-analytics', async () => {
    const reports = await db.query.blotterReports.findMany();
    const byPriority = reports.reduce(
      (acc, report) => {
        const priority = report.priority || 'medium';
        acc[priority] = (acc[priority] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );
    return { success: true, data: { totalReports: reports.length, byPriority } };
  })

  .get('/recent-activity', async () => {
    const recentCases = await db.query.blotterReports.findMany({
      limit: 10,
      orderBy: (cases, { desc }) => [desc(cases.updatedAt)],
    });

    const recentNotifications = await db.query.notifications.findMany({
      limit: 10,
      orderBy: (notif, { desc }) => [desc(notif.createdAt)],
    });

    return {
      success: true,
      data: { recentCases, recentNotifications },
    };
  });
