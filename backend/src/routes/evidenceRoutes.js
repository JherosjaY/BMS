import express from 'express';
import multer from 'multer';
import {
    uploadEvidence,
    getCaseEvidence,
    deleteEvidence,
    verifyEvidence
} from '../controllers/evidenceController.js';
import { verifyToken, requireRole } from '../middleware/auth.js';

const router = express.Router();

// Configure multer for file uploads
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/');
    },
    filename: (req, file, cb) => {
        cb(null, Date.now() + '-' + file.originalname);
    }
});

const upload = multer({
    storage,
    limits: { fileSize: 10 * 1024 * 1024 }, // 10MB
    fileFilter: (req, file, cb) => {
        const allowedMimes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf', 'application/msword'];
        if (allowedMimes.includes(file.mimetype)) {
            cb(null, true);
        } else {
            cb(new Error('Invalid file type'));
        }
    }
});

router.post('/', verifyToken, upload.single('file'), uploadEvidence);
router.get('/case/:caseId', verifyToken, getCaseEvidence);
router.delete('/:id', verifyToken, deleteEvidence);
router.post('/:id/verify', verifyToken, requireRole('admin', 'officer'), verifyEvidence);

export default router;
