import { query } from '../database/db.js';
import { asyncHandler } from '../middleware/errorHandler.js';

const generateCaseNumber = async () => {
    const result = await query(
        "SELECT COUNT(*) as count FROM cases WHERE case_number LIKE $1",
        [`CASE-${new Date().getFullYear()}-%`]
    );
    const count = parseInt(result.rows[0].count) + 1;
    return `CASE-${new Date().getFullYear()}-${String(count).padStart(5, '0')}`;
};

export const createCase = asyncHandler(async (req, res) => {
    const { title, description, priority, incident_date, incident_location } = req.body;
    const created_by = req.user.id;

    const case_number = await generateCaseNumber();

    const result = await query(
        `INSERT INTO cases (case_number, title, description, priority, incident_date, incident_location, created_by)
         VALUES ($1, $2, $3, $4, $5, $6, $7)
         RETURNING id, case_number, title, description, status, priority, incident_date, incident_location, created_at`,
        [case_number, title, description, priority, incident_date, incident_location, created_by]
    );

    res.status(201).json({
        success: true,
        message: 'Case created successfully',
        data: result.rows[0]
    });
});

export const getAllCases = asyncHandler(async (req, res) => {
    const { status, priority, assigned_officer_id, page = 1, limit = 10 } = req.query;
    const offset = (page - 1) * limit;

    let sql = `SELECT c.*, u.first_name, u.last_name, u.badge_number 
               FROM cases c
               LEFT JOIN users u ON c.assigned_officer_id = u.id
               WHERE 1=1`;
    const params = [];
    let paramCount = 1;

    if (status) {
        sql += ` AND c.status = $${paramCount}`;
        params.push(status);
        paramCount++;
    }

    if (priority) {
        sql += ` AND c.priority = $${paramCount}`;
        params.push(priority);
        paramCount++;
    }

    if (assigned_officer_id) {
        sql += ` AND c.assigned_officer_id = $${paramCount}`;
        params.push(assigned_officer_id);
        paramCount++;
    }

    sql += ` ORDER BY c.created_at DESC LIMIT $${paramCount} OFFSET $${paramCount + 1}`;
    params.push(limit, offset);

    const result = await query(sql, params);

    // Get total count
    let countSql = 'SELECT COUNT(*) as count FROM cases WHERE 1=1';
    const countParams = [];
    let countParamCount = 1;

    if (status) {
        countSql += ` AND status = $${countParamCount}`;
        countParams.push(status);
        countParamCount++;
    }

    if (priority) {
        countSql += ` AND priority = $${countParamCount}`;
        countParams.push(priority);
        countParamCount++;
    }

    if (assigned_officer_id) {
        countSql += ` AND assigned_officer_id = $${countParamCount}`;
        countParams.push(assigned_officer_id);
        countParamCount++;
    }

    const countResult = await query(countSql, countParams);
    const total = parseInt(countResult.rows[0].count);

    res.json({
        success: true,
        data: result.rows,
        pagination: {
            total,
            page: parseInt(page),
            limit: parseInt(limit),
            pages: Math.ceil(total / limit)
        }
    });
});

export const getCaseById = asyncHandler(async (req, res) => {
    const { id } = req.params;

    const caseResult = await query(
        `SELECT c.*, u.first_name, u.last_name, u.badge_number
         FROM cases c
         LEFT JOIN users u ON c.assigned_officer_id = u.id
         WHERE c.id = $1`,
        [id]
    );

    if (caseResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Case not found'
        });
    }

    const evidenceResult = await query(
        'SELECT id, file_name, file_url, file_type, description, verified FROM case_evidence WHERE case_id = $1',
        [id]
    );

    const historyResult = await query(
        `SELECT ca.id, ca.action, ca.description, ca.created_at, u.first_name, u.last_name
         FROM activity_logs ca
         LEFT JOIN users u ON ca.user_id = u.id
         WHERE ca.related_id = $1 AND ca.related_type = 'case'
         ORDER BY ca.created_at DESC`,
        [id]
    );

    res.json({
        success: true,
        data: {
            ...caseResult.rows[0],
            evidence: evidenceResult.rows,
            history: historyResult.rows
        }
    });
});

export const updateCase = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { title, description, priority, incident_date, incident_location } = req.body;

    const updates = [];
    const params = [];
    let paramCount = 1;

    if (title !== undefined) {
        updates.push(`title = $${paramCount}`);
        params.push(title);
        paramCount++;
    }

    if (description !== undefined) {
        updates.push(`description = $${paramCount}`);
        params.push(description);
        paramCount++;
    }

    if (priority !== undefined) {
        updates.push(`priority = $${paramCount}`);
        params.push(priority);
        paramCount++;
    }

    if (incident_date !== undefined) {
        updates.push(`incident_date = $${paramCount}`);
        params.push(incident_date);
        paramCount++;
    }

    if (incident_location !== undefined) {
        updates.push(`incident_location = $${paramCount}`);
        params.push(incident_location);
        paramCount++;
    }

    if (updates.length === 0) {
        return res.status(400).json({
            success: false,
            message: 'No fields to update'
        });
    }

    updates.push(`updated_at = CURRENT_TIMESTAMP`);
    params.push(id);

    const result = await query(
        `UPDATE cases SET ${updates.join(', ')} WHERE id = $${paramCount} RETURNING *`,
        params
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Case not found'
        });
    }

    res.json({
        success: true,
        message: 'Case updated successfully',
        data: result.rows[0]
    });
});

export const deleteCase = asyncHandler(async (req, res) => {
    const { id } = req.params;

    const result = await query('DELETE FROM cases WHERE id = $1 RETURNING id', [id]);

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Case not found'
        });
    }

    res.json({
        success: true,
        message: 'Case deleted successfully'
    });
});

export const assignCase = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { officer_id } = req.body;
    const assigned_by = req.user.id;

    // Verify case exists
    const caseResult = await query('SELECT id FROM cases WHERE id = $1', [id]);
    if (caseResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Case not found'
        });
    }

    // Verify officer exists
    const officerResult = await query('SELECT id FROM users WHERE id = $1 AND role = $2', [officer_id, 'officer']);
    if (officerResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Officer not found'
        });
    }

    // Update case
    const result = await query(
        'UPDATE cases SET assigned_officer_id = $1, status = $2, updated_at = CURRENT_TIMESTAMP WHERE id = $3 RETURNING *',
        [officer_id, 'in-progress', id]
    );

    // Create assignment record
    await query(
        'INSERT INTO officer_assignments (officer_id, case_id, assigned_by) VALUES ($1, $2, $3)',
        [officer_id, id, assigned_by]
    );

    // Create notification
    await query(
        `INSERT INTO notifications (user_id, title, message, type, related_id, related_type)
         VALUES ($1, $2, $3, $4, $5, $6)`,
        [officer_id, 'New Case Assignment', `You have been assigned case #${result.rows[0].case_number}`, 'case_assigned', id, 'case']
    );

    res.json({
        success: true,
        message: 'Case assigned successfully',
        data: result.rows[0]
    });
});

export const updateCaseStatus = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { status } = req.body;

    if (!['pending', 'in-progress', 'resolved', 'closed'].includes(status)) {
        return res.status(400).json({
            success: false,
            message: 'Invalid status'
        });
    }

    const result = await query(
        'UPDATE cases SET status = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2 RETURNING *',
        [status, id]
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Case not found'
        });
    }

    res.json({
        success: true,
        message: 'Case status updated successfully',
        data: result.rows[0]
    });
});

export const getOfficerCases = asyncHandler(async (req, res) => {
    const { officerId } = req.params;
    const { status, page = 1, limit = 10 } = req.query;
    const offset = (page - 1) * limit;

    let sql = 'SELECT * FROM cases WHERE assigned_officer_id = $1';
    const params = [officerId];
    let paramCount = 2;

    if (status) {
        sql += ` AND status = $${paramCount}`;
        params.push(status);
        paramCount++;
    }

    sql += ` ORDER BY created_at DESC LIMIT $${paramCount} OFFSET $${paramCount + 1}`;
    params.push(limit, offset);

    const result = await query(sql, params);

    res.json({
        success: true,
        data: result.rows,
        pagination: {
            page: parseInt(page),
            limit: parseInt(limit)
        }
    });
});

export const getUserCases = asyncHandler(async (req, res) => {
    const { userId } = req.params;
    const { status, page = 1, limit = 10 } = req.query;
    const offset = (page - 1) * limit;

    let sql = 'SELECT * FROM cases WHERE created_by = $1';
    const params = [userId];
    let paramCount = 2;

    if (status) {
        sql += ` AND status = $${paramCount}`;
        params.push(status);
        paramCount++;
    }

    sql += ` ORDER BY created_at DESC LIMIT $${paramCount} OFFSET $${paramCount + 1}`;
    params.push(limit, offset);

    const result = await query(sql, params);

    res.json({
        success: true,
        data: result.rows,
        pagination: {
            page: parseInt(page),
            limit: parseInt(limit)
        }
    });
});
