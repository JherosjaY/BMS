import { query } from '../database/db.js';
import { asyncHandler } from '../middleware/errorHandler.js';

const generateReportNumber = async () => {
    const result = await query(
        "SELECT COUNT(*) as count FROM blotter_reports WHERE report_number LIKE $1",
        [`BLOTTER-${new Date().getFullYear()}-%`]
    );
    const count = parseInt(result.rows[0].count) + 1;
    return `BLOTTER-${new Date().getFullYear()}-${String(count).padStart(5, '0')}`;
};

export const createBlotter = asyncHandler(async (req, res) => {
    const { complainant_name, complainant_contact, respondent_name, respondent_address, incident_date, incident_location, description } = req.body;
    const created_by = req.user.id;

    const report_number = await generateReportNumber();

    const result = await query(
        `INSERT INTO blotter_reports (report_number, complainant_name, complainant_contact, respondent_name, respondent_address, incident_date, incident_location, description, created_by)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
         RETURNING id, report_number, complainant_name, incident_date, incident_location, status, created_at`,
        [report_number, complainant_name, complainant_contact, respondent_name, respondent_address, incident_date, incident_location, description, created_by]
    );

    res.status(201).json({
        success: true,
        message: 'Blotter report created successfully',
        data: result.rows[0]
    });
});

export const getAllBlotters = asyncHandler(async (req, res) => {
    const { status, page = 1, limit = 10 } = req.query;
    const offset = (page - 1) * limit;

    let sql = `SELECT b.*, u.first_name, u.last_name, o.first_name as officer_first_name, o.last_name as officer_last_name
               FROM blotter_reports b
               LEFT JOIN users u ON b.created_by = u.id
               LEFT JOIN users o ON b.assigned_officer_id = o.id
               WHERE 1=1`;
    const params = [];
    let paramCount = 1;

    if (status) {
        sql += ` AND b.status = $${paramCount}`;
        params.push(status);
        paramCount++;
    }

    sql += ` ORDER BY b.created_at DESC LIMIT $${paramCount} OFFSET $${paramCount + 1}`;
    params.push(limit, offset);

    const result = await query(sql, params);

    // Get total count
    let countSql = 'SELECT COUNT(*) as count FROM blotter_reports WHERE 1=1';
    const countParams = [];
    let countParamCount = 1;

    if (status) {
        countSql += ` AND status = $${countParamCount}`;
        countParams.push(status);
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

export const getBlotterById = asyncHandler(async (req, res) => {
    const { id } = req.params;

    const result = await query(
        `SELECT b.*, u.first_name, u.last_name, o.first_name as officer_first_name, o.last_name as officer_last_name
         FROM blotter_reports b
         LEFT JOIN users u ON b.created_by = u.id
         LEFT JOIN users o ON b.assigned_officer_id = o.id
         WHERE b.id = $1`,
        [id]
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Blotter report not found'
        });
    }

    res.json({
        success: true,
        data: result.rows[0]
    });
});

export const updateBlotter = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { complainant_name, respondent_name, respondent_address, description } = req.body;

    const updates = [];
    const params = [];
    let paramCount = 1;

    if (complainant_name !== undefined) {
        updates.push(`complainant_name = $${paramCount}`);
        params.push(complainant_name);
        paramCount++;
    }

    if (respondent_name !== undefined) {
        updates.push(`respondent_name = $${paramCount}`);
        params.push(respondent_name);
        paramCount++;
    }

    if (respondent_address !== undefined) {
        updates.push(`respondent_address = $${paramCount}`);
        params.push(respondent_address);
        paramCount++;
    }

    if (description !== undefined) {
        updates.push(`description = $${paramCount}`);
        params.push(description);
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
        `UPDATE blotter_reports SET ${updates.join(', ')} WHERE id = $${paramCount} RETURNING *`,
        params
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Blotter report not found'
        });
    }

    res.json({
        success: true,
        message: 'Blotter report updated successfully',
        data: result.rows[0]
    });
});

export const updateBlotterStatus = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { status } = req.body;

    if (!['pending', 'under-investigation', 'resolved', 'closed'].includes(status)) {
        return res.status(400).json({
            success: false,
            message: 'Invalid status'
        });
    }

    const result = await query(
        'UPDATE blotter_reports SET status = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2 RETURNING *',
        [status, id]
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Blotter report not found'
        });
    }

    res.json({
        success: true,
        message: 'Blotter status updated successfully',
        data: result.rows[0]
    });
});

export const deleteBlotter = asyncHandler(async (req, res) => {
    const { id } = req.params;

    const result = await query('DELETE FROM blotter_reports WHERE id = $1 RETURNING id', [id]);

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Blotter report not found'
        });
    }

    res.json({
        success: true,
        message: 'Blotter report deleted successfully'
    });
});

export const assignBlotterToOfficer = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { officer_id } = req.body;

    // Verify blotter exists
    const blotterResult = await query('SELECT id FROM blotter_reports WHERE id = $1', [id]);
    if (blotterResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Blotter report not found'
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

    // Update blotter
    const result = await query(
        'UPDATE blotter_reports SET assigned_officer_id = $1, status = $2, updated_at = CURRENT_TIMESTAMP WHERE id = $3 RETURNING *',
        [officer_id, 'under-investigation', id]
    );

    // Create notification
    await query(
        `INSERT INTO notifications (user_id, title, message, type, related_id, related_type)
         VALUES ($1, $2, $3, $4, $5, $6)`,
        [officer_id, 'New Blotter Assignment', `You have been assigned blotter #${result.rows[0].report_number}`, 'blotter_assigned', id, 'blotter']
    );

    res.json({
        success: true,
        message: 'Blotter assigned to officer successfully',
        data: result.rows[0]
    });
});
