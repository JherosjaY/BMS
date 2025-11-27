import { Elysia, t } from "elysia";

/**
 * ✅ OFFICER ROLE ENDPOINTS
 * Officers can manage assigned cases, send SMS, and upload evidence
 */
export const officerRoutes = new Elysia({ prefix: "/officer" })
  // Officer guard middleware
  .guard({
    beforeHandle: async ({ jwt, bearer, set }) => {
      try {
        const token = await jwt.verify(bearer);
        if (!token || token.role !== "Officer") {
          set.status = 403;
          return { error: "Officer access required" };
        }
      } catch (e) {
        set.status = 401;
        return { error: "Unauthorized" };
      }
    },
  },
  (app) =>
    app
      // ✅ GET OFFICER'S ASSIGNED CASES
      .get("/cases", async ({ jwt, bearer, db, query }) => {
        const token = await jwt.verify(bearer);
        const officerId = token.userId;
        const { status } = query;

        let whereClause =
          "WHERE (assigned_officer_id = $1 OR $1 = ANY(string_to_array(assigned_officer_ids, ',')))";
        let params: any[] = [officerId];
        let paramCount = 1;

        if (status && status !== "All") {
          paramCount++;
          whereClause += ` AND status = $${paramCount}`;
          params.push(status);
        }

        const cases = await db.query(
          `SELECT br.*, u_reporter.display_name as complainant_name
           FROM blotter_reports br
           LEFT JOIN users u_reporter ON br.reported_by_id = u_reporter.id
           ${whereClause}
           ORDER BY br.date_filed DESC`,
          params
        );

        return { success: true, data: cases.rows };
      })

      // ✅ UPDATE CASE STATUS (Start Investigation, Mark Resolved)
      .patch(
        "/cases/:id/status",
        async ({ params, body, jwt, bearer, db }) => {
          const token = await jwt.verify(bearer);
          const { status, resolutionType, resolutionDetails } = body;

          // Verify officer is assigned to this case
          const caseCheck = await db.query(
            `SELECT id FROM blotter_reports 
             WHERE id = $1 AND (assigned_officer_id = $2 OR $2 = ANY(string_to_array(assigned_officer_ids, ',')))`,
            [params.id, token.userId]
          );

          if (caseCheck.rows.length === 0) {
            return {
              success: false,
              error: "Case not found or not assigned to you",
            };
          }

          const updated = await db.query(
            `UPDATE blotter_reports 
             SET status = $1, resolution_type = $2, resolution_details = $3, updated_at = CURRENT_TIMESTAMP
             WHERE id = $4 
             RETURNING *`,
            [status, resolutionType, resolutionDetails, params.id]
          );

          console.log(
            `✅ Case ${updated.rows[0].case_number} status updated to ${status}`
          );

          return { success: true, data: updated.rows[0] };
        },
        {
          body: t.Object({
            status: t.String(),
            resolutionType: t.Optional(t.String()),
            resolutionDetails: t.Optional(t.String()),
          }),
        }
      )

      // ✅ SEND SMS NOTIFICATION
      .post(
        "/sms/send",
        async ({ body, jwt, bearer, db }) => {
          const token = await jwt.verify(bearer);
          const { phoneNumber, messageType, caseData } = body;

          // Log the SMS notification
          const smsLog = await db.query(
            `INSERT INTO sms_notifications 
             (report_id, phone_number, message_type, message_content, sent_by) 
             VALUES ($1, $2, $3, $4, $5)
             RETURNING *`,
            [
              caseData.caseId,
              phoneNumber,
              messageType,
              `Case ${caseData.caseNumber}: ${messageType}`,
              token.userId,
            ]
          );

          console.log(
            `✅ SMS sent to ${phoneNumber} for case ${caseData.caseNumber}`
          );

          return {
            success: true,
            data: { messageId: smsLog.rows[0].id },
            message: "SMS sent successfully",
          };
        },
        {
          body: t.Object({
            phoneNumber: t.String(),
            messageType: t.String(),
            caseData: t.Object({
              caseId: t.Number(),
              caseNumber: t.String(),
            }),
          }),
        }
      )

      // ✅ UPLOAD CASE EVIDENCE
      .post(
        "/cases/:id/evidence",
        async ({ params, body, jwt, bearer, db }) => {
          const token = await jwt.verify(bearer);
          const { imageUrl, fileName } = body;

          const evidence = await db.query(
            `INSERT INTO case_evidence (report_id, image_url, file_name, uploaded_by) 
             VALUES ($1, $2, $3, $4) 
             RETURNING *`,
            [params.id, imageUrl, fileName, token.userId]
          );

          console.log(`✅ Evidence uploaded for case ${params.id}`);

          return { success: true, data: evidence.rows[0] };
        },
        {
          body: t.Object({
            imageUrl: t.String(),
            fileName: t.String(),
          }),
        }
      )

      // ✅ GET CASE DETAILS
      .get("/cases/:id", async ({ params, jwt, bearer, db }) => {
        const token = await jwt.verify(bearer);

        const caseDetail = await db.query(
          `SELECT br.*, u_reporter.display_name as complainant_name
           FROM blotter_reports br
           LEFT JOIN users u_reporter ON br.reported_by_id = u_reporter.id
           WHERE br.id = $1 AND (br.assigned_officer_id = $2 OR $2 = ANY(string_to_array(br.assigned_officer_ids, ',')))`,
          [params.id, token.userId]
        );

        if (caseDetail.rows.length === 0) {
          return { success: false, error: "Case not found" };
        }

        // Get evidence for this case
        const evidence = await db.query(
          "SELECT * FROM case_evidence WHERE report_id = $1 ORDER BY uploaded_at DESC",
          [params.id]
        );

        return {
          success: true,
          data: {
            ...caseDetail.rows[0],
            evidence: evidence.rows,
          },
        };
      })
  );
