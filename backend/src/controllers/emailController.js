const nodemailer = require('nodemailer');
const crypto = require('crypto');
const db = require('../database/db');

// Email transporter configuration
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'official.bms.2025@gmail.com',
        pass: 'bvg vyes knki yvgi'
    }
});

// Generate 6-digit reset code
const generateResetCode = () => {
    return Math.floor(100000 + Math.random() * 900000).toString();
};

// Send password reset code
exports.sendPasswordResetCode = async (req, res) => {
    try {
        const { email } = req.body;

        // Check if user exists
        const user = await db.query('SELECT id, first_name FROM users WHERE email = $1', [email]);
        if (user.rows.length === 0) {
            return res.status(404).json({ success: false, message: 'User not found' });
        }

        // Generate reset code
        const resetCode = generateResetCode();
        const expiresAt = new Date(Date.now() + 15 * 60 * 1000); // 15 minutes

        // Save reset code to database
        await db.query(
            'INSERT INTO password_resets (email, reset_code, expires_at) VALUES ($1, $2, $3)',
            [email, resetCode, expiresAt]
        );

        // Send email
        const mailOptions = {
            from: 'official.bms.2025@gmail.com',
            to: email,
            subject: 'BMS Password Reset Code',
            html: `
                <h2>Password Reset Request</h2>
                <p>Hi ${user.rows[0].first_name},</p>
                <p>Your password reset code is:</p>
                <h1 style="color: #007bff; font-size: 32px; letter-spacing: 5px;">${resetCode}</h1>
                <p>This code expires in 15 minutes.</p>
                <p>If you didn't request this, please ignore this email.</p>
                <hr>
                <p><small>Blotter Management System</small></p>
            `
        };

        await transporter.sendMail(mailOptions);

        // Log email
        await db.query(
            'INSERT INTO email_logs (recipient, subject, type, status) VALUES ($1, $2, $3, $4)',
            [email, 'Password Reset Code', 'password_reset', 'sent']
        );

        res.json({ success: true, message: 'Reset code sent to email' });
    } catch (error) {
        console.error('Error sending reset code:', error);
        res.status(500).json({ success: false, message: 'Error sending email', error: error.message });
    }
};

// Verify reset code
exports.verifyResetCode = async (req, res) => {
    try {
        const { email, resetCode } = req.body;

        // Check if code is valid and not expired
        const result = await db.query(
            'SELECT id FROM password_resets WHERE email = $1 AND reset_code = $2 AND expires_at > NOW() AND used = false',
            [email, resetCode]
        );

        if (result.rows.length === 0) {
            return res.status(400).json({ success: false, message: 'Invalid or expired code' });
        }

        res.json({ success: true, message: 'Code verified' });
    } catch (error) {
        console.error('Error verifying code:', error);
        res.status(500).json({ success: false, message: 'Error verifying code', error: error.message });
    }
};

// Reset password with verified code
exports.resetPassword = async (req, res) => {
    try {
        const { email, resetCode, newPassword } = req.body;
        const bcrypt = require('bcryptjs');

        // Verify code
        const codeResult = await db.query(
            'SELECT id FROM password_resets WHERE email = $1 AND reset_code = $2 AND expires_at > NOW() AND used = false',
            [email, resetCode]
        );

        if (codeResult.rows.length === 0) {
            return res.status(400).json({ success: false, message: 'Invalid or expired code' });
        }

        // Hash new password
        const hashedPassword = await bcrypt.hash(newPassword, 10);

        // Update user password
        await db.query('UPDATE users SET password = $1, updated_at = NOW() WHERE email = $2', [hashedPassword, email]);

        // Mark code as used
        await db.query('UPDATE password_resets SET used = true WHERE email = $1 AND reset_code = $2', [email, resetCode]);

        // Send confirmation email
        const user = await db.query('SELECT first_name FROM users WHERE email = $1', [email]);
        const mailOptions = {
            from: 'official.bms.2025@gmail.com',
            to: email,
            subject: 'Password Reset Successful',
            html: `
                <h2>Password Reset Successful</h2>
                <p>Hi ${user.rows[0].first_name},</p>
                <p>Your password has been successfully reset.</p>
                <p>You can now log in with your new password.</p>
                <hr>
                <p><small>Blotter Management System</small></p>
            `
        };

        await transporter.sendMail(mailOptions);

        res.json({ success: true, message: 'Password reset successful' });
    } catch (error) {
        console.error('Error resetting password:', error);
        res.status(500).json({ success: false, message: 'Error resetting password', error: error.message });
    }
};

// Send officer credentials
exports.sendOfficerCredentials = async (req, res) => {
    try {
        const { officerId, officerEmail, username, tempPassword } = req.body;

        // Get officer details
        const officer = await db.query('SELECT first_name, last_name FROM users WHERE id = $1', [officerId]);
        if (officer.rows.length === 0) {
            return res.status(404).json({ success: false, message: 'Officer not found' });
        }

        const mailOptions = {
            from: 'official.bms.2025@gmail.com',
            to: officerEmail,
            subject: 'Your BMS Officer Account Credentials',
            html: `
                <h2>Welcome to Blotter Management System!</h2>
                <p>Hi ${officer.rows[0].first_name} ${officer.rows[0].last_name},</p>
                <p>Your officer account has been created. Here are your login credentials:</p>
                <div style="background: #f5f5f5; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <p><strong>Username:</strong> ${username}</p>
                    <p><strong>Temporary Password:</strong> ${tempPassword}</p>
                </div>
                <p><a href="https://bms-app.com/login" style="background: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Login to BMS</a></p>
                <p><strong>Important:</strong> Please change your password after your first login.</p>
                <hr>
                <p><small>Blotter Management System</small></p>
            `
        };

        await transporter.sendMail(mailOptions);

        // Log email
        await db.query(
            'INSERT INTO email_logs (recipient, subject, type, status) VALUES ($1, $2, $3, $4)',
            ['officer_credentials', officerEmail, 'officer_credentials', 'sent']
        );

        res.json({ success: true, message: 'Officer credentials sent to email' });
    } catch (error) {
        console.error('Error sending officer credentials:', error);
        res.status(500).json({ success: false, message: 'Error sending email', error: error.message });
    }
};

// Send welcome email
exports.sendWelcomeEmail = async (req, res) => {
    try {
        const { userId, email } = req.body;

        const user = await db.query('SELECT first_name FROM users WHERE id = $1', [userId]);
        if (user.rows.length === 0) {
            return res.status(404).json({ success: false, message: 'User not found' });
        }

        const mailOptions = {
            from: 'official.bms.2025@gmail.com',
            to: email,
            subject: 'Welcome to Blotter Management System',
            html: `
                <h2>Welcome to BMS!</h2>
                <p>Hi ${user.rows[0].first_name},</p>
                <p>Your account has been successfully created.</p>
                <p>You can now log in and start using the Blotter Management System.</p>
                <p><a href="https://bms-app.com/login" style="background: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Login Now</a></p>
                <hr>
                <p><small>Blotter Management System</small></p>
            `
        };

        await transporter.sendMail(mailOptions);

        // Log email
        await db.query(
            'INSERT INTO email_logs (recipient, subject, type, status) VALUES ($1, $2, $3, $4)',
            [email, 'Welcome Email', 'welcome', 'sent']
        );

        res.json({ success: true, message: 'Welcome email sent' });
    } catch (error) {
        console.error('Error sending welcome email:', error);
        res.status(500).json({ success: false, message: 'Error sending email', error: error.message });
    }
};

// Send case assignment notification
exports.sendCaseAssignmentEmail = async (req, res) => {
    try {
        const { officerId, caseId, caseTitle } = req.body;

        const officer = await db.query('SELECT first_name, last_name, email FROM users WHERE id = $1', [officerId]);
        if (officer.rows.length === 0) {
            return res.status(404).json({ success: false, message: 'Officer not found' });
        }

        const mailOptions = {
            from: 'official.bms.2025@gmail.com',
            to: officer.rows[0].email,
            subject: 'New Case Assignment - BMS',
            html: `
                <h2>New Case Assignment</h2>
                <p>Hi ${officer.rows[0].first_name} ${officer.rows[0].last_name},</p>
                <p>You have been assigned a new case:</p>
                <div style="background: #f5f5f5; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <p><strong>Case ID:</strong> ${caseId}</p>
                    <p><strong>Title:</strong> ${caseTitle}</p>
                </div>
                <p>Please log in to BMS to view the case details and start working on it.</p>
                <p><a href="https://bms-app.com/cases/${caseId}" style="background: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">View Case</a></p>
                <hr>
                <p><small>Blotter Management System</small></p>
            `
        };

        await transporter.sendMail(mailOptions);

        // Log email
        await db.query(
            'INSERT INTO email_logs (recipient, subject, type, status) VALUES ($1, $2, $3, $4)',
            [officer.rows[0].email, 'Case Assignment', 'case_assignment', 'sent']
        );

        res.json({ success: true, message: 'Case assignment email sent' });
    } catch (error) {
        console.error('Error sending case assignment email:', error);
        res.status(500).json({ success: false, message: 'Error sending email', error: error.message });
    }
};
