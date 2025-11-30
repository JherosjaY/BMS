import { query } from '../database/db.js';
import { asyncHandler } from '../middleware/errorHandler.js';
import bcrypt from 'bcryptjs';

export const getAllUsers = asyncHandler(async (req, res) => {
    const { role, is_active } = req.query;
    let sql = 'SELECT id, username, email, first_name, last_name, role, badge_number, department, is_active, created_at FROM users WHERE 1=1';
    const params = [];

    if (role) {
        sql += ` AND role = $${params.length + 1}`;
        params.push(role);
    }

    if (is_active !== undefined) {
        sql += ` AND is_active = $${params.length + 1}`;
        params.push(is_active === 'true');
    }

    sql += ' ORDER BY created_at DESC';

    const result = await query(sql, params);

    res.json({
        success: true,
        data: result.rows,
        total: result.rows.length
    });
});

export const getUserById = asyncHandler(async (req, res) => {
    const { id } = req.params;

    const result = await query(
        'SELECT id, username, email, first_name, last_name, role, badge_number, department, phone_number, profile_picture, is_active, created_at FROM users WHERE id = $1',
        [id]
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'User not found'
        });
    }

    res.json({
        success: true,
        data: result.rows[0]
    });
});

export const updateUser = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { first_name, last_name, phone_number, department, profile_picture } = req.body;

    const updates = [];
    const params = [];
    let paramCount = 1;

    if (first_name !== undefined) {
        updates.push(`first_name = $${paramCount}`);
        params.push(first_name);
        paramCount++;
    }

    if (last_name !== undefined) {
        updates.push(`last_name = $${paramCount}`);
        params.push(last_name);
        paramCount++;
    }

    if (phone_number !== undefined) {
        updates.push(`phone_number = $${paramCount}`);
        params.push(phone_number);
        paramCount++;
    }

    if (department !== undefined) {
        updates.push(`department = $${paramCount}`);
        params.push(department);
        paramCount++;
    }

    if (profile_picture !== undefined) {
        updates.push(`profile_picture = $${paramCount}`);
        params.push(profile_picture);
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

    const sql = `UPDATE users SET ${updates.join(', ')} WHERE id = $${paramCount} RETURNING id, username, email, first_name, last_name, role, profile_picture`;

    const result = await query(sql, params);

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'User not found'
        });
    }

    res.json({
        success: true,
        message: 'User updated successfully',
        data: result.rows[0]
    });
});

export const deleteUser = asyncHandler(async (req, res) => {
    const { id } = req.params;

    // Prevent deleting admin users
    const userResult = await query('SELECT role FROM users WHERE id = $1', [id]);
    if (userResult.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'User not found'
        });
    }

    if (userResult.rows[0].role === 'admin') {
        return res.status(403).json({
            success: false,
            message: 'Cannot delete admin users'
        });
    }

    await query('DELETE FROM users WHERE id = $1', [id]);

    res.json({
        success: true,
        message: 'User deleted successfully'
    });
});

export const createOfficer = asyncHandler(async (req, res) => {
    const { username, email, password, first_name, last_name, badge_number, department } = req.body;

    // Check if user exists
    const existingUser = await query(
        'SELECT id FROM users WHERE email = $1 OR username = $2 OR badge_number = $3',
        [email, username, badge_number]
    );

    if (existingUser.rows.length > 0) {
        return res.status(400).json({
            success: false,
            message: 'Email, username, or badge number already exists'
        });
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const result = await query(
        `INSERT INTO users (username, email, password, first_name, last_name, badge_number, department, role) 
         VALUES ($1, $2, $3, $4, $5, $6, $7, 'officer') 
         RETURNING id, username, email, first_name, last_name, badge_number, department, role`,
        [username, email, hashedPassword, first_name, last_name, badge_number, department]
    );

    const officer = result.rows[0];

    // Initialize performance record
    await query(
        'INSERT INTO officer_performance (officer_id) VALUES ($1)',
        [officer.id]
    );

    res.status(201).json({
        success: true,
        message: 'Officer created successfully',
        data: officer
    });
});

export const getAllOfficers = asyncHandler(async (req, res) => {
    const result = await query(
        `SELECT u.id, u.username, u.email, u.first_name, u.last_name, u.badge_number, u.department, u.is_active,
                COALESCE(op.total_cases, 0) as total_cases,
                COALESCE(op.completed_cases, 0) as completed_cases,
                COALESCE(op.rating, 0) as rating
         FROM users u
         LEFT JOIN officer_performance op ON u.id = op.officer_id
         WHERE u.role = 'officer'
         ORDER BY u.created_at DESC`
    );

    res.json({
        success: true,
        data: result.rows,
        total: result.rows.length
    });
});

export const updateUserRole = asyncHandler(async (req, res) => {
    const { id } = req.params;
    const { role } = req.body;

    if (!['admin', 'officer', 'user'].includes(role)) {
        return res.status(400).json({
            success: false,
            message: 'Invalid role'
        });
    }

    const result = await query(
        'UPDATE users SET role = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2 RETURNING id, username, email, role',
        [role, id]
    );

    if (result.rows.length === 0) {
        return res.status(404).json({
            success: false,
            message: 'User not found'
        });
    }

    res.json({
        success: true,
        message: 'User role updated successfully',
        data: result.rows[0]
    });
});
