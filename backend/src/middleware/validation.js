import { body, validationResult } from 'express-validator';

export const validate = (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({
            success: false,
            message: 'Validation error',
            errors: errors.array().map(err => ({
                field: err.param,
                message: err.msg
            }))
        });
    }
    next();
};

export const loginValidation = [
    body('username').notEmpty().withMessage('Username is required'),
    body('password').notEmpty().withMessage('Password is required')
];

export const registerValidation = [
    body('username').isLength({ min: 3 }).withMessage('Username must be at least 3 characters'),
    body('email').isEmail().withMessage('Invalid email address'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
    body('first_name').notEmpty().withMessage('First name is required'),
    body('last_name').notEmpty().withMessage('Last name is required')
];

export const caseValidation = [
    body('title').notEmpty().withMessage('Title is required'),
    body('description').notEmpty().withMessage('Description is required'),
    body('incident_date').isISO8601().withMessage('Invalid incident date'),
    body('incident_location').notEmpty().withMessage('Incident location is required')
];

export const blotterValidation = [
    body('complainant_name').notEmpty().withMessage('Complainant name is required'),
    body('incident_date').isISO8601().withMessage('Invalid incident date'),
    body('incident_location').notEmpty().withMessage('Incident location is required'),
    body('description').notEmpty().withMessage('Description is required')
];

export const userUpdateValidation = [
    body('first_name').optional().notEmpty().withMessage('First name cannot be empty'),
    body('last_name').optional().notEmpty().withMessage('Last name cannot be empty'),
    body('phone_number').optional().isMobilePhone().withMessage('Invalid phone number'),
    body('department').optional().notEmpty().withMessage('Department cannot be empty')
];

export const officerAssignmentValidation = [
    body('officer_id').isInt().withMessage('Officer ID must be an integer'),
    body('case_id').isInt().withMessage('Case ID must be an integer')
];
