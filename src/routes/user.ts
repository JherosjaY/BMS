import { Elysia, t } from "elysia";

/**
 * ✅ USER ROLE ENDPOINTS
 * Users can file reports, view their own reports, and update their profile
 */
export const userRoutes = new Elysia({ prefix: "/user" })
  // User guard middleware (authentication required)
  .guard({
    beforeHandle: async ({ jwt, bearer, set }) => {
      try {
        const token = await jwt.verify(bearer);
        if (!token) {
          set.status = 401;
          return { error: "Authentication required" };
        }
      } catch (e) {
        set.status = 401;
        return { error: "Unauthorized" };
      }
    },
  },
  (app) =>
    app
      // ✅ GET USER'S OWN REPORTS
      .get("/reports", async ({ jwt, bearer, db, query }) => {
        const token = await jwt.verify(bearer);
        const { status } = query;

        let whereClause = "WHERE reported_by_id = $1";
        let params: any[] = [token.userId];
        let paramCount = 1;

        if (status && status !== "All") {
          paramCount++;
          whereClause += ` AND status = $${paramCount}`;
          params.push(status);
        }

        const reports = await db.query(
          `SELECT br.*, u_officer.display_name as assigned_officer_name
           FROM blotter_reports br
           LEFT JOIN users u_officer ON br.assigned_officer_id = u_officer.id
           ${whereClause}
           ORDER BY br.date_filed DESC`,
          params
        );

        return { success: true, data: reports.rows };
      })

      // ✅ FILE NEW REPORT
      .post(
        "/reports",
        async ({ body, jwt, bearer, db }) => {
          const token = await jwt.verify(bearer);

          const {
            caseNumber,
            incidentType,
            complainantName,
            respondentName,
            incidentDate,
            incidentDetails,
            location,
          } = body;

          const newReport = await db.query(
            `INSERT INTO blotter_reports 
             (case_number, incident_type, complainant_name, respondent_name, 
              incident_date, incident_details, location, date_filed, reported_by_id, status) 
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, 'PENDING') 
             RETURNING *`,
            [
              caseNumber,
              incidentType,
              complainantName,
              respondentName,
              incidentDate,
              incidentDetails,
              location,
              Date.now(),
              token.userId,
            ]
          );

          console.log(`✅ New report filed: ${caseNumber}`);

          return { success: true, data: newReport.rows[0] };
        },
        {
          body: t.Object({
            caseNumber: t.String(),
            incidentType: t.String(),
            complainantName: t.String(),
            respondentName: t.String(),
            incidentDate: t.String(),
            incidentDetails: t.String(),
            location: t.String(),
          }),
        }
      )

      // ✅ GET REPORT DETAILS
      .get("/reports/:id", async ({ params, jwt, bearer, db }) => {
        const token = await jwt.verify(bearer);

        const report = await db.query(
          `SELECT br.*, u_officer.display_name as officer_name,
                  u_officer.phone_number as officer_phone
           FROM blotter_reports br
           LEFT JOIN users u_officer ON br.assigned_officer_id = u_officer.id
           WHERE br.id = $1 AND br.reported_by_id = $2`,
          [params.id, token.userId]
        );

        if (report.rows.length === 0) {
          return { success: false, error: "Report not found" };
        }

        // Get evidence for this report
        const evidence = await db.query(
          "SELECT * FROM case_evidence WHERE report_id = $1 ORDER BY uploaded_at DESC",
          [params.id]
        );

        return {
          success: true,
          data: {
            ...report.rows[0],
            evidence: evidence.rows,
          },
        };
      })

      // ✅ UPDATE USER PROFILE
      .put(
        "/profile",
        async ({ body, jwt, bearer, db }) => {
          const token = await jwt.verify(bearer);
          const { displayName, phoneNumber, barangay } = body;

          const updated = await db.query(
            `UPDATE users 
             SET display_name = $1, phone_number = $2, barangay = $3, updated_at = CURRENT_TIMESTAMP
             WHERE id = $4 
             RETURNING id, email, display_name, phone_number, barangay, photo_url`,
            [displayName, phoneNumber, barangay, token.userId]
          );

          console.log(`✅ User profile updated for user ${token.userId}`);

          return { success: true, data: updated.rows[0] };
        },
        {
          body: t.Object({
            displayName: t.String(),
            phoneNumber: t.String(),
            barangay: t.String(),
          }),
        }
      )

      // ✅ GET USER PROFILE
      .get("/profile", async ({ jwt, bearer, db }) => {
        const token = await jwt.verify(bearer);

        const user = await db.query(
          `SELECT id, email, display_name, phone_number, barangay, photo_url, role
           FROM users 
           WHERE id = $1`,
          [token.userId]
        );

        if (user.rows.length === 0) {
          return { success: false, error: "User not found" };
        }

        return { success: true, data: user.rows[0] };
      })
  );
