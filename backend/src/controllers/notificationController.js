import { query } from '../database/db.js';
import { asyncHandler } from '../middleware/errorHandler.js';

export const getUserNotifications = asyncHandler(async (req, res) => {
    const { userId } = req.params;
    const { is_read, page = 1, limit = 20 } = req.query;
    const offset = (page - 1) * limit;

    let sql = 'SELECT * FROM notifications WHERE user_id = $1';
    const params = [userId];
    let paramCount = 2;

    if (is_read !== undefined) {
        sql += ` AND is_read = $${paramCount}`;
        params.push(is_read === 'true');
        paramCount++;
    }

    sql += ` ORDER BY created_at DESC LIMIT $${paramCount} OFFSET $${paramCount + 1}`;
    params.push(limit, offset);

    const result = await query(sql, params);

    // Get unread count
    const countResult = await query(
        'SELECT COUNT(*) as count FROM notifications WHERE user_id = $1 AND is_read = false',
        [userId]
    );

    res.json({
        success: true,
        data: result.rows,
        unread_count: parseInt(countResult.rows[0].count),
        pagination: {
            page: parseInt(page),
            limit: parseInt(limit)
        }
    });
});

export const markNotificationAsRead = asyncHandler(async (req, res) => {
    const { id } = req.params;

    const result = await query(
        'UPDATE notifications SET is_read = true, read_at = CURRENT_TIMESTAMP WHERE id = $1 RETURNING *',
        [id]
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Notification not found'
        });
    }

    res.json({
        success: true,
        message: 'Notification marked as read',
        data: result.rows[0]
    });
});

export const markAllNotificationsAsRead = asyncHandler(async (req, res) => {
    const { userId } = req.params;

    await query(
        'UPDATE notifications SET is_read = true, read_at = CURRENT_TIMESTAMP WHERE user_id = $1 AND is_read = false',
        [userId]
    );

    res.json({
        success: true,
        message: 'All notifications marked as read'
    });
});

export const deleteNotification = asyncHandler(async (req, res) => {
    const { id } = req.params;

    const result = await query('DELETE FROM notifications WHERE id = $1 RETURNING id', [id]);

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'Notification not found'
        });
    }

    res.json({
        success: true,
        message: 'Notification deleted successfully'
    });
});

export const createNotification = asyncHandler(async (req, res) => {
    const { user_id, title, message, type, related_id, related_type } = req.body;

    const result = await query(
        `INSERT INTO notifications (user_id, title, message, type, related_id, related_type)
         VALUES ($1, $2, $3, $4, $5, $6)
         RETURNING *`,
        [user_id, title, message, type, related_id, related_type]
    );

    res.status(201).json({
        success: true,
        message: 'Notification created successfully',
        data: result.rows[0]
    });
});
