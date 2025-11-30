import express from 'express';
import { 
    getAllUsers, 
    getUserById, 
    updateUser, 
    deleteUser, 
    createOfficer, 
    getAllOfficers, 
    updateUserRole 
} from '../controllers/userController.js';
import { verifyToken, requireRole } from '../middleware/auth.js';
import { userUpdateValidation, validate } from '../middleware/validation.js';

const router = express.Router();

// User routes
router.get('/', verifyToken, requireRole('admin'), getAllUsers);
router.get('/:id', verifyToken, getUserById);
router.put('/:id', verifyToken, userUpdateValidation, validate, updateUser);
router.delete('/:id', verifyToken, requireRole('admin'), deleteUser);

// Officer routes
router.post('/officers', verifyToken, requireRole('admin'), createOfficer);
router.get('/officers', verifyToken, getAllOfficers);
router.put('/:id/role', verifyToken, requireRole('admin'), updateUserRole);

export default router;
