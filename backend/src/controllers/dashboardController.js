import { query } from '../database/db.js';
import { asyncHandler } from '../middleware/errorHandler.js';

export const getSystemStats = asyncHandler(async (req, res) => {
    // Total users
    const usersResult = await query('SELECT COUNT(*) as count FROM users WHERE is_active = true');
    const totalUsers = parseInt(usersResult.rows[0].count);

    // Total officers
    const officersResult = await query('SELECT COUNT(*) as count FROM users WHERE role = $1 AND is_active = true', ['officer']);
    const totalOfficers = parseInt(officersResult.rows[0].count);

    // Total cases
    const casesResult = await query('SELECT COUNT(*) as count FROM cases');
    const totalCases = parseInt(casesResult.rows[0].count);

    // Cases by status
    const caseStatusResult = await query(`
        SELECT status, COUNT(*) as count FROM cases GROUP BY status
    `);
    const casesByStatus = {};
    caseStatusResult.rows.forEach(row => {
        casesByStatus[row.status] = parseInt(row.count);
    });

    // Total blotters
    const blottersResult = await query('SELECT COUNT(*) as count FROM blotter_reports');
    const totalBlotters = parseInt(blottersResult.rows[0].count);

    // Blotters by status
    const blotterStatusResult = await query(`
        SELECT status, COUNT(*) as count FROM blotter_reports GROUP BY status
    `);
    const blottersByStatus = {};
    blotterStatusResult.rows.forEach(row => {
        blottersByStatus[row.status] = parseInt(row.count);
    });

    // Total evidence
    const evidenceResult = await query('SELECT COUNT(*) as count FROM case_evidence');
    const totalEvidence = parseInt(evidenceResult.rows[0].count);

    res.json({
        success: true,
        data: {
            users: {
                total: totalUsers,
                officers: totalOfficers
            },
            cases: {
                total: totalCases,
                by_status: casesByStatus
            },
            blotters: {
                total: totalBlotters,
                by_status: blottersByStatus
            },
            evidence: {
                total: totalEvidence
            }
        }
    });
});

export const getOfficerWorkloadStats = asyncHandler(async (req, res) => {
    const result = await query(`
        SELECT 
            u.id,
            u.first_name,
            u.last_name,
            u.badge_number,
            u.department,
            COUNT(DISTINCT c.id) as assigned_cases,
            COUNT(DISTINCT CASE WHEN c.status = 'resolved' THEN c.id END) as completed_cases,
            COUNT(DISTINCT CASE WHEN c.status IN ('pending', 'in-progress') THEN c.id END) as active_cases,
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

export const getCaseStatusStats = asyncHandler(async (req, res) => {
    const result = await query(`
        SELECT 
            status,
            COUNT(*) as total,
            COUNT(CASE WHEN priority = 'high' THEN 1 END) as high_priority,
            COUNT(CASE WHEN priority = 'medium' THEN 1 END) as medium_priority,
            COUNT(CASE WHEN priority = 'low' THEN 1 END) as low_priority,
            AVG(EXTRACT(DAY FROM (CURRENT_TIMESTAMP - created_at))) as avg_age_days
        FROM cases
        GROUP BY status
        ORDER BY total DESC
    `);

    res.json({
        success: true,
        data: result.rows
    });
});

export const getBlotterAnalytics = asyncHandler(async (req, res) => {
    const result = await query(`
        SELECT 
            status,
            COUNT(*) as total,
            COUNT(CASE WHEN assigned_officer_id IS NOT NULL THEN 1 END) as assigned,
            COUNT(CASE WHEN assigned_officer_id IS NULL THEN 1 END) as unassigned,
            AVG(EXTRACT(DAY FROM (CURRENT_TIMESTAMP - created_at))) as avg_age_days
        FROM blotter_reports
        GROUP BY status
        ORDER BY total DESC
    `);

    res.json({
        success: true,
        data: result.rows
    });
});

export const getEvidenceSummary = asyncHandler(async (req, res) => {
    const result = await query(`
        SELECT 
            file_type,
            COUNT(*) as total,
            COUNT(CASE WHEN verified = true THEN 1 END) as verified,
            SUM(file_size) as total_size
        FROM case_evidence
        GROUP BY file_type
        ORDER BY total DESC
    `);

    res.json({
        success: true,
        data: result.rows
    });
});

export const getRecentActivity = asyncHandler(async (req, res) => {
    const { limit = 20 } = req.query;

    const result = await query(`
        SELECT 
            al.id,
            al.action,
            al.description,
            al.created_at,
            u.first_name,
            u.last_name,
            u.role
        FROM activity_logs al
        LEFT JOIN users u ON al.user_id = u.id
        ORDER BY al.created_at DESC
        LIMIT $1
    `, [limit]);

    res.json({
        success: true,
        data: result.rows
    });
});

export const getCaseResolutionTime = asyncHandler(async (req, res) => {
    const result = await query(`
        SELECT 
            EXTRACT(DAY FROM (updated_at - created_at)) as resolution_days,
            COUNT(*) as case_count
        FROM cases
        WHERE status = 'resolved' OR status = 'closed'
        GROUP BY resolution_days
        ORDER BY resolution_days ASC
    `);

    res.json({
        success: true,
        data: result.rows
    });
});
