import express from 'express';
import {
    createBlotter,
    getAllBlotters,
    getBlotterById,
    updateBlotter,
    updateBlotterStatus,
    deleteBlotter,
    assignBlotterToOfficer
} from '../controllers/blotterController.js';
import { verifyToken, requireRole } from '../middleware/auth.js';
import { blotterValidation, validate } from '../middleware/validation.js';

const router = express.Router();

router.post('/', verifyToken, blotterValidation, validate, createBlotter);
router.get('/', verifyToken, getAllBlotters);
router.get('/:id', verifyToken, getBlotterById);
router.put('/:id', verifyToken, updateBlotter);
router.put('/:id/status', verifyToken, updateBlotterStatus);
router.delete('/:id', verifyToken, requireRole('admin'), deleteBlotter);
router.post('/:id/assign', verifyToken, requireRole('admin'), assignBlotterToOfficer);

export default router;
