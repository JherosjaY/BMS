import express from 'express';
import emailController from '../controllers/emailController.js';
import { verifyToken } from '../middleware/auth.js';

const router = express.Router();

// Password reset endpoints
router.post('/password-reset', emailController.sendPasswordResetCode);
router.post('/verify-reset-code', emailController.verifyResetCode);
router.post('/reset-password', emailController.resetPassword);

// Officer credentials
router.post('/send-officer-credentials', verifyToken, emailController.sendOfficerCredentials);

// Welcome email
router.post('/welcome', emailController.sendWelcomeEmail);

// Case assignment notification
router.post('/case-assigned', verifyToken, emailController.sendCaseAssignmentEmail);

export default router;
