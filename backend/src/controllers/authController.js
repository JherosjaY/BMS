import bcrypt from 'bcryptjs';
import { query } from '../database/db.js';
import { generateToken } from '../middleware/auth.js';
import { asyncHandler } from '../middleware/errorHandler.js';

export const register = asyncHandler(async (req, res) => {
    const { username, email, password, first_name, last_name, phone_number } = req.body;

    // Check if user exists
    const existingUser = await query(
        'SELECT id FROM users WHERE email = $1 OR username = $2',
        [email, username]
    );

    if (existingUser.rows.length > 0) {
        return res.status(400).json({
            success: false,
            message: 'Email or username already exists'
        });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);

    // Create user with 'user' role (normal registration)
    const result = await query(
        `INSERT INTO users (username, email, password, first_name, last_name, phone_number, role, registration_method) 
         VALUES ($1, $2, $3, $4, $5, $6, 'user', 'normal') 
         RETURNING id, username, email, first_name, last_name, phone_number, role, registration_method`,
        [username, email, hashedPassword, first_name, last_name, phone_number]
    );

    const user = result.rows[0];
    const token = generateToken(user);

    res.status(201).json({
        success: true,
        message: 'User registered successfully',
        data: user,
        token
    });
});

export const login = asyncHandler(async (req, res) => {
    const { username, password } = req.body;

    // Find user
    const result = await query(
        'SELECT * FROM users WHERE username = $1 OR email = $1',
        [username]
    );

    if (result.rows.length === 0) {
        return res.status(401).json({
            success: false,
            message: 'Invalid username or password'
        });
    }

    const user = result.rows[0];

    // Verify password
    const passwordMatch = await bcrypt.compare(password, user.password);

    if (!passwordMatch) {
        return res.status(401).json({
            success: false,
            message: 'Invalid username or password'
        });
    }

    // Update last login
    await query(
        'UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = $1',
        [user.id]
    );

    const token = generateToken(user);

    res.json({
        success: true,
        message: 'Login successful',
        data: {
            id: user.id,
            username: user.username,
            email: user.email,
            first_name: user.first_name,
            last_name: user.last_name,
            role: user.role,
            profile_picture: user.profile_picture
        },
        token
    });
});

export const checkEmail = asyncHandler(async (req, res) => {
    const { email } = req.body;

    const result = await query(
        'SELECT id FROM users WHERE email = $1',
        [email]
    );

    res.json({
        success: true,
        exists: result.rows.length > 0
    });
});

export const refreshToken = asyncHandler(async (req, res) => {
    const { token } = req.body;

    if (!token) {
        return res.status(400).json({
            success: false,
            message: 'Token is required'
        });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET, { ignoreExpiration: true });
        
        const result = await query(
            'SELECT * FROM users WHERE id = $1',
            [decoded.id]
        );

        if (result.rows.length === 0) {
            return res.status(401).json({
                success: false,
                message: 'User not found'
            });
        }

        const user = result.rows[0];
        const newToken = generateToken(user);

        res.json({
            success: true,
            message: 'Token refreshed',
            token: newToken
        });
    } catch (error) {
        return res.status(401).json({
            success: false,
            message: 'Invalid token'
        });
    }
});

export const logout = asyncHandler(async (req, res) => {
    // Token invalidation can be handled on client side
    // Or implement token blacklist in production
    res.json({
        success: true,
        message: 'Logout successful'
    });
});

export const googleSignIn = asyncHandler(async (req, res) => {
    const { googleId, email, first_name, last_name, profile_picture } = req.body;

    // Check if user exists by email or google_id
    let result = await query(
        'SELECT * FROM users WHERE email = $1 OR google_id = $2',
        [email, googleId]
    );

    let user;

    if (result.rows.length > 0) {
        // Existing user - login
        user = result.rows[0];
        
        // Update google_id if not set
        if (!user.google_id) {
            await query(
                'UPDATE users SET google_id = $1 WHERE id = $2',
                [googleId, user.id]
            );
            user.google_id = googleId;
        }
        
        // Update profile picture if provided
        if (profile_picture && profile_picture !== user.profile_picture) {
            await query(
                'UPDATE users SET profile_picture = $1, updated_at = CURRENT_TIMESTAMP WHERE id = $2',
                [profile_picture, user.id]
            );
            user.profile_picture = profile_picture;
        }
        
        // Update last login
        await query(
            'UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = $1',
            [user.id]
        );
    } else {
        // New user - signup with Google
        const username = email.split('@')[0];
        
        const createResult = await query(
            `INSERT INTO users (username, email, google_id, first_name, last_name, profile_picture, role, registration_method) 
             VALUES ($1, $2, $3, $4, $5, $6, 'user', 'google') 
             RETURNING id, username, email, google_id, first_name, last_name, role, registration_method, profile_picture`,
            [username, email, googleId, first_name, last_name, profile_picture]
        );
        user = createResult.rows[0];
    }

    const token = generateToken(user);

    res.json({
        success: true,
        message: 'Google sign-in successful',
        data: {
            id: user.id,
            username: user.username,
            email: user.email,
            first_name: user.first_name,
            last_name: user.last_name,
            role: user.role,
            registration_method: user.registration_method,
            profile_picture: user.profile_picture
        },
        token
    });
});
