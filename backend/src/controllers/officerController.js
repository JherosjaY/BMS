import { query } from '../database/db.js';
import { asyncHandler } from '../middleware/errorHandler.js';

export const assignCaseToOfficer = asyncHandler(async (req, res) => {
    const { officer_id, case_id } = req.body;
    const assigned_by = req.user.id;

    // Verify officer exists
    const officerResult = await query('SELECT id FROM users WHERE id = $1 AND role = $2', [officer_id, 'officer']);
    if (officerResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Officer not found'
        });
    }

    // Verify case exists
    const caseResult = await query('SELECT id FROM cases WHERE id = $1', [case_id]);
    if (caseResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Case not found'
        });
    }

    // Create assignment
    const assignmentResult = await query(
        `INSERT INTO officer_assignments (officer_id, case_id, assigned_by, status)
         VALUES ($1, $2, $3, 'assigned')
         ON CONFLICT (officer_id, case_id) DO UPDATE SET status = 'assigned'
         RETURNING *`,
        [officer_id, case_id, assigned_by]
    );

    // Update case
    await query(
        'UPDATE cases SET assigned_officer_id = $1, status = $2 WHERE id = $3',
        [officer_id, 'in-progress', case_id]
    );

    // Create notification
    await query(
        `INSERT INTO notifications (user_id, title, message, type, related_id, related_type)
         VALUES ($1, $2, $3, $4, $5, $6)`,
        [officer_id, 'New Case Assignment', 'You have been assigned a new case', 'case_assigned', case_id, 'case']
    );

    res.json({
        success: true,
        message: 'Case assigned to officer successfully',
        data: assignmentResult.rows[0]
    });
});

export const getOfficerAssignedCases = asyncHandler(async (req, res) => {
    const { officerId } = req.params;
    const { status, page = 1, limit = 10 } = req.query;
    const offset = (page - 1) * limit;

    let sql = `SELECT c.*, oa.status as assignment_status
               FROM cases c
               JOIN officer_assignments oa ON c.id = oa.case_id
               WHERE oa.officer_id = $1`;
    const params = [officerId];
    let paramCount = 2;

    if (status) {
        sql += ` AND c.status = $${paramCount}`;
        params.push(status);
        paramCount++;
    }

    sql += ` ORDER BY c.created_at DESC LIMIT $${paramCount} OFFSET $${paramCount + 1}`;
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

export const getOfficersWorkload = asyncHandler(async (req, res) => {
    const result = await query(`
        SELECT 
            u.id,
            u.first_name,
            u.last_name,
            u.badge_number,
            u.department,
            COUNT(DISTINCT c.id) as assigned_cases,
            COUNT(DISTINCT CASE WHEN c.status = 'completed' THEN c.id END) as completed_cases,
            COALESCE(op.rating, 0) as rating,
            COALESCE(op.avg_resolution_time, 0) as avg_resolution_time
        FROM users u
        LEFT JOIN cases c ON u.id = c.assigned_officer_id
        LEFT JOIN officer_performance op ON u.id = op.officer_id
        WHERE u.role = 'officer' AND u.is_active = true
        GROUP BY u.id, u.first_name, u.last_name, u.badge_number, u.department, op.rating, op.avg_resolution_time
        ORDER BY assigned_cases DESC
    `);

    res.json({
        success: true,
        data: result.rows
    });
});

export const getOfficerAvailability = asyncHandler(async (req, res) => {
    const result = await query(`
        SELECT 
            u.id,
            u.first_name,
            u.last_name,
            u.badge_number,
            COUNT(c.id) as active_cases,
            CASE 
                WHEN COUNT(c.id) < 5 THEN 'available'
                WHEN COUNT(c.id) < 10 THEN 'busy'
                ELSE 'overloaded'
            END as availability_status
        FROM users u
        LEFT JOIN cases c ON u.id = c.assigned_officer_id AND c.status IN ('pending', 'in-progress')
        WHERE u.role = 'officer' AND u.is_active = true
        GROUP BY u.id, u.first_name, u.last_name, u.badge_number
        ORDER BY active_cases ASC
    `);

    res.json({
        success: true,
        data: result.rows
    });
});

export const updateOfficerStatus = asyncHandler(async (req, res) => {
    const { officerId } = req.params;
    const { status } = req.body;

    if (!['active', 'on-leave', 'busy'].includes(status)) {
        return res.status(400).json({
            success: false,
            message: 'Invalid status'
        });
    }

    const result = await query(
        `UPDATE users SET is_active = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2 AND role = 'officer' RETURNING id, first_name, last_name, is_active`,
        [status === 'active', officerId]
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Officer not found'
        });
    }

    res.json({
        success: true,
        message: 'Officer status updated successfully',
        data: result.rows[0]
    });
});

export const getOfficerPerformance = asyncHandler(async (req, res) => {
    const result = await query(`
        SELECT 
            u.id,
            u.first_name,
            u.last_name,
            u.badge_number,
            u.department,
            COALESCE(op.total_cases, 0) as total_cases,
            COALESCE(op.completed_cases, 0) as completed_cases,
            COALESCE(op.avg_resolution_time, 0) as avg_resolution_time,
            COALESCE(op.rating, 0) as rating,
            CASE 
                WHEN op.total_cases > 0 THEN ROUND((op.completed_cases::numeric / op.total_cases) * 100, 2)
                ELSE 0
            END as completion_rate
        FROM users u
        LEFT JOIN officer_performance op ON u.id = op.officer_id
        WHERE u.role = 'officer'
        ORDER BY op.rating DESC NULLS LAST
    `);

    res.json({
        success: true,
        data: result.rows
    });
});

export const acceptCaseAssignment = asyncHandler(async (req, res) => {
    const { caseId } = req.params;
    const officer_id = req.user.id;

    // Verify assignment exists
    const assignmentResult = await query(
        'SELECT * FROM officer_assignments WHERE case_id = $1 AND officer_id = $2',
        [caseId, officer_id]
    );

    if (assignmentResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Assignment not found'
        });
    }

    // Update assignment
    const result = await query(
        'UPDATE officer_assignments SET status = $1, accepted_at = CURRENT_TIMESTAMP WHERE case_id = $2 AND officer_id = $3 RETURNING *',
        ['accepted', caseId, officer_id]
    );

    res.json({
        success: true,
        message: 'Case assignment accepted',
        data: result.rows[0]
    });
});

export const rejectCaseAssignment = asyncHandler(async (req, res) => {
    const { caseId } = req.params;
    const { rejection_reason } = req.body;
    const officer_id = req.user.id;

    // Verify assignment exists
    const assignmentResult = await query(
        'SELECT * FROM officer_assignments WHERE case_id = $1 AND officer_id = $2',
        [caseId, officer_id]
    );

    if (assignmentResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Assignment not found'
        });
    }

    // Update assignment
    const result = await query(
        'UPDATE officer_assignments SET status = $1, rejection_reason = $2 WHERE case_id = $3 AND officer_id = $4 RETURNING *',
        ['rejected', rejection_reason, caseId, officer_id]
    );

    // Unassign case
    await query(
        'UPDATE cases SET assigned_officer_id = NULL, status = $1 WHERE id = $2',
        ['pending', caseId]
    );

    res.json({
        success: true,
        message: 'Case assignment rejected',
        data: result.rows[0]
    });
});

export const completeCase = asyncHandler(async (req, res) => {
    const { caseId } = req.params;
    const officer_id = req.user.id;

    // Verify assignment exists
    const assignmentResult = await query(
        'SELECT * FROM officer_assignments WHERE case_id = $1 AND officer_id = $2',
        [caseId, officer_id]
    );

    if (assignmentResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Assignment not found'
        });
    }

    // Update assignment
    await query(
        'UPDATE officer_assignments SET status = $1, completed_at = CURRENT_TIMESTAMP WHERE case_id = $2 AND officer_id = $3',
        ['completed', caseId, officer_id]
    );

    // Update case
    const result = await query(
        'UPDATE cases SET status = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2 RETURNING *',
        ['resolved', caseId]
    );

    // Update officer performance
    await query(
        `UPDATE officer_performance 
         SET completed_cases = completed_cases + 1, 
             last_updated = CURRENT_TIMESTAMP 
         WHERE officer_id = $1`,
        [officer_id]
    );

    res.json({
        success: true,
        message: 'Case marked as completed',
        data: result.rows[0]
    });
});
