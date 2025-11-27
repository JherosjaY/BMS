import { Elysia, t } from "elysia";

/**
 * ✅ ADMIN ROLE ENDPOINTS
 * Complete admin access to all reports, officers, and system statistics
 */
export const adminRoutes = new Elysia({ prefix: "/admin" })
  // Admin guard middleware
  .guard({
    beforeHandle: async ({ jwt, bearer, set }) => {
      try {
        const token = await jwt.verify(bearer);
        if (!token || token.role !== "Admin") {
          set.status = 403;
          return { error: "Admin access required" };
        }
      } catch (e) {
        set.status = 401;
        return { error: "Unauthorized" };
      }
    },
  },
  (app) =>
    app
      // ✅ GET ALL REPORTS (Admin sees everything)
      .get("/reports", async ({ db, query, jwt, bearer }) => {
        const token = await jwt.verify(bearer);
        const { status, page = "1", limit = "50" } = query;

        let whereClause = "";
        let params: any[] = [];
        let paramCount = 0;

        if (status && status !== "All") {
          paramCount++;
          whereClause = `WHERE status = $${paramCount}`;
          params.push(status);
        }

        const reports = await db.query(
          `SELECT br.*, 
                  u_reporter.display_name as reporter_name,
                  u_officer.display_name as officer_name
           FROM blotter_reports br
           LEFT JOIN users u_reporter ON br.reported_by_id = u_reporter.id
           LEFT JOIN users u_officer ON br.assigned_officer_id = u_officer.id
           ${whereClause}
           ORDER BY br.date_filed DESC
           LIMIT $${paramCount + 1} OFFSET $${paramCount + 2}`,
          [...params, parseInt(limit), (parseInt(page) - 1) * parseInt(limit)]
        );

        const total = await db.query(
          `SELECT COUNT(*) FROM blotter_reports ${whereClause}`,
          params
        );

        return {
          success: true,
          data: reports.rows,
          pagination: {
            page: parseInt(page),
            limit: parseInt(limit),
            total: parseInt(total.rows[0].count),
          },
        };
      })

      // ✅ ASSIGN OFFICER TO CASE
      .post(
        "/reports/:id/assign",
        async ({ params, body, db, jwt, bearer }) => {
          const token = await jwt.verify(bearer);
          const { officerId } = body;

          const updated = await db.query(
            `UPDATE blotter_reports 
             SET assigned_officer_id = $1, status = 'ASSIGNED', updated_at = CURRENT_TIMESTAMP
             WHERE id = $2 
             RETURNING *`,
            [officerId, params.id]
          );

          if (updated.rows.length === 0) {
            return { success: false, error: "Report not found" };
          }

          console.log(
            `✅ Case ${updated.rows[0].case_number} assigned to officer ${officerId}`
          );

          return { success: true, data: updated.rows[0] };
        },
        {
          body: t.Object({
            officerId: t.Number(),
          }),
        }
      )

      // ✅ GET ALL OFFICERS
      .get("/officers", async ({ db }) => {
        const officers = await db.query(
          `SELECT id, display_name, email, phone_number, barangay
           FROM users 
           WHERE role = 'Officer' AND is_active = true
           ORDER BY display_name`
        );

        return { success: true, data: officers.rows };
      })

      // ✅ GET SYSTEM STATISTICS
      .get("/statistics", async ({ db }) => {
        const stats = await db.query(`
          SELECT 
            COUNT(*) as total_cases,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_cases,
            COUNT(CASE WHEN status = 'ASSIGNED' THEN 1 END) as assigned_cases,
            COUNT(CASE WHEN status = 'ONGOING' THEN 1 END) as ongoing_cases,
            COUNT(CASE WHEN status = 'RESOLVED' THEN 1 END) as resolved_cases,
            COUNT(DISTINCT reported_by_id) as total_complainants,
            COUNT(DISTINCT assigned_officer_id) as active_officers
          FROM blotter_reports
        `);

        const monthlyData = await db.query(`
          SELECT 
            DATE_TRUNC('month', created_at) as month,
            COUNT(*) as case_count
          FROM blotter_reports
          WHERE created_at >= CURRENT_DATE - INTERVAL '12 months'
          GROUP BY DATE_TRUNC('month', created_at)
          ORDER BY month
        `);

        return {
          success: true,
          data: {
            overview: stats.rows[0],
            monthlyTrends: monthlyData.rows,
          },
        };
      })

      // ✅ CREATE NEW OFFICER ACCOUNT
      .post(
        "/officers",
        async ({ body, db }) => {
          const { email, displayName, phoneNumber, barangay } = body;

          const officer = await db.query(
            `INSERT INTO users 
             (email, display_name, phone_number, barangay, role, auth_provider) 
             VALUES ($1, $2, $3, $4, 'Officer', 'email') 
             RETURNING id, email, display_name, phone_number, barangay, role`,
            [email, displayName, phoneNumber, barangay]
          );

          return { success: true, data: officer.rows[0] };
        },
        {
          body: t.Object({
            email: t.String(),
            displayName: t.String(),
            phoneNumber: t.String(),
            barangay: t.String(),
          }),
        }
      )
  );
