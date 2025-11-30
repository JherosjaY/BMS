import express from 'express';
import {
    getUserNotifications,
    markNotificationAsRead,
    markAllNotificationsAsRead,
    deleteNotification,
    createNotification
} from '../controllers/notificationController.js';
import { verifyToken, requireRole } from '../middleware/auth.js';

const router = express.Router();

router.get('/user/:userId', verifyToken, getUserNotifications);
router.put('/:id/read', verifyToken, markNotificationAsRead);
router.put('/user/:userId/read-all', verifyToken, markAllNotificationsAsRead);
router.delete('/:id', verifyToken, deleteNotification);
router.post('/', verifyToken, requireRole('admin'), createNotification);

export default router;
