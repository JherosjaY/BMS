import express from 'express';
import {
    assignCaseToOfficer,
    getOfficerAssignedCases,
    getOfficersWorkload,
    getOfficerAvailability,
    updateOfficerStatus,
    getOfficerPerformance,
    acceptCaseAssignment,
    rejectCaseAssignment,
    completeCase
} from '../controllers/officerController.js';
import { verifyToken, requireRole } from '../middleware/auth.js';
import { officerAssignmentValidation, validate } from '../middleware/validation.js';

const router = express.Router();

router.post('/assign-case', verifyToken, requireRole('admin'), officerAssignmentValidation, validate, assignCaseToOfficer);
router.get('/:officerId/cases', verifyToken, getOfficerAssignedCases);
router.get('/workload', verifyToken, requireRole('admin'), getOfficersWorkload);
router.get('/availability', verifyToken, requireRole('admin'), getOfficerAvailability);
router.put('/:officerId/status', verifyToken, requireRole('admin'), updateOfficerStatus);
router.get('/performance', verifyToken, requireRole('admin'), getOfficerPerformance);
router.put('/case/:caseId/accept', verifyToken, requireRole('officer'), acceptCaseAssignment);
router.put('/case/:caseId/reject', verifyToken, requireRole('officer'), rejectCaseAssignment);
router.put('/case/:caseId/complete', verifyToken, requireRole('officer'), completeCase);

export default router;
