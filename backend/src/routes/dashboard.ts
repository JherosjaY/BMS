import { Elysia } from 'elysia';
import { db } from '../db';
import { blotterReports, users, notifications } from '../db/schema';
import { count, eq } from 'drizzle-orm';

export default new Elysia({ prefix: '/api/dashboard' })
  // Dashboard stats
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

  // Officer workload
  .get('/officer-workload', async () => {
    const workload = await db.query.blotterReports.findMany({
      with: {
        assignedOfficer: true,
      },
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
        if (report.status === 'pending') {
          acc[officerId].pendingCount++;
        }
        
        return acc;
      },
      {} as Record<string, any>
    );

    return {
      success: true,
      data: Object.values(grouped),
    };
  })

  // Case status distribution
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

    return {
      success: true,
      data: distribution,
    };
  })

  // Blotter analytics
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

    return {
      success: true,
      data: {
        totalReports: reports.length,
        byPriority,
      },
    };
  })

  // Evidence summary
  .get('/evidence-summary', async () => {
    const casesWithEvidence = await db.query.blotterReports.findMany({
      with: {
        evidence: true,
      },
    });

    const totalEvidence = casesWithEvidence.reduce(
      (sum, report) => sum + (report.evidence?.length || 0),
      0
    );

    return {
      success: true,
      data: {
        totalEvidence,
        casesWithEvidence: casesWithEvidence.filter((c) => c.evidence.length > 0)
          .length,
      },
    };
  })

  // Recent activity
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
      data: {
        recentCases,
        recentNotifications,
      },
    };
  })

  // Case resolution time
  .get('/case-resolution-time', async () => {
    const resolvedCases = await db.query.blotterReports.findMany({
      where: eq(blotterReports.status, 'resolved'),
    });

    const avgResolutionTime =
      resolvedCases.length > 0
        ? resolvedCases.reduce((sum, report) => {
            if (report.createdAt && report.updatedAt) {
              const diff =
                new Date(report.updatedAt).getTime() -
                new Date(report.createdAt).getTime();
              return sum + diff;
            }
            return sum;
          }, 0) / resolvedCases.length
        : 0;

    return {
      success: true,
      data: {
        resolvedCases: resolvedCases.length,
        avgResolutionTimeMs: Math.round(avgResolutionTime),
        avgResolutionTimeDays: Math.round(avgResolutionTime / (1000 * 60 * 60 * 24)),
      },
    };
  });
