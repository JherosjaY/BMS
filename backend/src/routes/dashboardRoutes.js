import express from 'express';
import {
    getSystemStats,
    getOfficerWorkloadStats,
    getCaseStatusStats,
    getBlotterAnalytics,
    getEvidenceSummary,
    getRecentActivity,
    getCaseResolutionTime
} from '../controllers/dashboardController.js';
import { verifyToken, requireRole } from '../middleware/auth.js';

const router = express.Router();

router.get('/stats', verifyToken, requireRole('admin'), getSystemStats);
router.get('/officer-workload', verifyToken, requireRole('admin'), getOfficerWorkloadStats);
router.get('/case-status', verifyToken, requireRole('admin'), getCaseStatusStats);
router.get('/blotter-analytics', verifyToken, requireRole('admin'), getBlotterAnalytics);
router.get('/evidence-summary', verifyToken, requireRole('admin'), getEvidenceSummary);
router.get('/recent-activity', verifyToken, requireRole('admin'), getRecentActivity);
router.get('/case-resolution-time', verifyToken, requireRole('admin'), getCaseResolutionTime);

export default router;
