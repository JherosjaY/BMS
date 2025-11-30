import { query } from '../database/db.js';
import { asyncHandler } from '../middleware/errorHandler.js';

export const uploadEvidence = asyncHandler(async (req, res) => {
    const { case_id, description } = req.body;
    const uploaded_by = req.user.id;

    if (!req.file) {
        return res.status(400).json({
            success: false,
            message: 'No file uploaded'
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

    const file_url = `/uploads/${req.file.filename}`;
    const file_type = req.file.mimetype.split('/')[0];

    const result = await query(
        `INSERT INTO case_evidence (case_id, file_name, file_url, file_type, file_size, description, uploaded_by)
         VALUES ($1, $2, $3, $4, $5, $6, $7)
         RETURNING id, file_name, file_url, file_type, description, uploaded_at`,
        [case_id, req.file.originalname, file_url, file_type, req.file.size, description, uploaded_by]
    );

    res.status(201).json({
        success: true,
        message: 'Evidence uploaded successfully',
        data: result.rows[0]
    });
});

export const getCaseEvidence = asyncHandler(async (req, res) => {
    const { caseId } = req.params;

    // Verify case exists
    const caseResult = await query('SELECT id FROM cases WHERE id = $1', [caseId]);
    if (caseResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Case not found'
        });
    }

    const result = await query(
        `SELECT ce.*, u.first_name, u.last_name
         FROM case_evidence ce
         LEFT JOIN users u ON ce.uploaded_by = u.id
         WHERE ce.case_id = $1
         ORDER BY ce.uploaded_at DESC`,
        [caseId]
    );

    res.json({
        success: true,
        data: result.rows
    });
});

export const deleteEvidence = asyncHandler(async (req, res) => {
    const { id } = req.params;

    const result = await query('DELETE FROM case_evidence WHERE id = $1 RETURNING id', [id]);

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Evidence not found'
        });
    }

    res.json({
        success: true,
        message: 'Evidence deleted successfully'
    });
});

export const verifyEvidence = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const verified_by = req.user.id;

    const result = await query(
        `UPDATE case_evidence 
         SET verified = true, verified_by = $1, verified_at = CURRENT_TIMESTAMP 
         WHERE id = $2 
         RETURNING *`,
        [verified_by, id]
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Evidence not found'
        });
    }

    res.json({
        success: true,
        message: 'Evidence verified successfully',
        data: result.rows[0]
    });
});
