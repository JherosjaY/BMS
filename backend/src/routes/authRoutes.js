import express from 'express';
import { register, login, checkEmail, refreshToken, logout, googleSignIn } from '../controllers/authController.js';
import { loginValidation, registerValidation, validate } from '../middleware/validation.js';
import { verifyToken } from '../middleware/auth.js';

const router = express.Router();

router.post('/register', registerValidation, validate, register);
router.post('/login', loginValidation, validate, login);
router.post('/check-email', checkEmail);
router.post('/refresh', verifyToken, refreshToken);
router.post('/logout', verifyToken, logout);
router.post('/google', googleSignIn);

export default router;
