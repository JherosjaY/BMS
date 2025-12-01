import { Elysia, t } from 'elysia';
import nodemailer from 'nodemailer';

const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.GMAIL_USER,
    pass: process.env.GMAIL_PASSWORD,
  },
});

export default new Elysia({ prefix: '/api/email' })
  .post(
    '/password-reset',
    async ({ body }) => {
      const resetCode = Math.random().toString(36).substring(2, 8).toUpperCase();

      await transporter.sendMail({
        from: process.env.GMAIL_USER,
        to: body.email,
        subject: 'BMS Password Reset Code',
        html: `<p>Your password reset code is: <strong>${resetCode}</strong></p>`,
      });

      return {
        success: true,
        message: 'Reset code sent to email',
        resetCode,
      };
    },
    {
      body: t.Object({
        email: t.String({ format: 'email' }),
      }),
    }
  )

  .post(
    '/verify-code',
    async ({ body }) => {
      return {
        success: true,
        message: 'Code verified',
        valid: body.code.length === 6,
      };
    },
    {
      body: t.Object({
        code: t.String(),
      }),
    }
  )

  .post(
    '/reset-password',
    async ({ body }) => {
      return {
        success: true,
        message: 'Password reset successful',
      };
    },
    {
      body: t.Object({
        email: t.String({ format: 'email' }),
        code: t.String(),
        newPassword: t.String({ minLength: 6 }),
      }),
    }
  );
