import express from 'express';
import {
    createCase,
    getAllCases,
    getCaseById,
    updateCase,
    deleteCase,
    assignCase,
    updateCaseStatus,
    getOfficerCases,
    getUserCases
} from '../controllers/caseController.js';
import { verifyToken, requireRole } from '../middleware/auth.js';
import { caseValidation, validate } from '../middleware/validation.js';

const router = express.Router();

router.post('/', verifyToken, caseValidation, validate, createCase);
router.get('/', verifyToken, getAllCases);
router.get('/:id', verifyToken, getCaseById);
router.put('/:id', verifyToken, updateCase);
router.delete('/:id', verifyToken, requireRole('admin'), deleteCase);
router.post('/:id/assign', verifyToken, requireRole('admin', 'officer'), assignCase);
router.put('/:id/status', verifyToken, updateCaseStatus);
router.get('/officer/:officerId', verifyToken, getOfficerCases);
router.get('/user/:userId', verifyToken, getUserCases);

export default router;
