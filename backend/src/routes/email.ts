import { Elysia, t } from 'elysia';
import nodemailer from 'nodemailer';
import { db } from '../db';
import { passwordResets, emailLogs } from '../db/schema';
import { eq, and } from 'drizzle-orm';

const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: process.env.GMAIL_USER || 'official.bms.2025@gmail.com',
    pass: process.env.GMAIL_PASSWORD || 'bvg vyes knki yvgi',
  },
});

const generateResetCode = () => {
  return Math.floor(100000 + Math.random() * 900000).toString();
};

export default new Elysia({ prefix: '/api/email' })
  // Send password reset code
  .post(
    '/password-reset',
    async ({ body }) => {
      const { email } = body;

      const resetCode = generateResetCode();
      const expiresAt = new Date(Date.now() + 15 * 60 * 1000);

      await db.insert(passwordResets).values({
        email,
        resetCode,
        expiresAt,
      });

      await transporter.sendMail({
        from: process.env.GMAIL_USER,
        to: email,
        subject: 'BMS Password Reset Code',
        html: `
          <h2>Password Reset Request</h2>
          <p>Your password reset code is:</p>
          <h1 style="color: #007bff; font-size: 32px; letter-spacing: 5px;">${resetCode}</h1>
          <p>This code expires in 15 minutes.</p>
        `,
      });

      await db.insert(emailLogs).values({
        recipient: email,
        subject: 'Password Reset Code',
        type: 'password_reset',
        status: 'sent',
      });

      return {
        success: true,
        message: 'Reset code sent to email',
      };
    },
    {
      body: t.Object({
        email: t.String({ format: 'email' }),
      }),
    }
  )

  // Verify reset code
  .post(
    '/verify-reset-code',
    async ({ body }) => {
      const { email, resetCode } = body;

      const result = await db.query.passwordResets.findFirst({
        where: and(
          eq(passwordResets.email, email),
          eq(passwordResets.resetCode, resetCode)
        ),
      });

      if (!result || result.expiresAt < new Date() || result.used) {
        throw new Error('Invalid or expired code');
      }

      return {
        success: true,
        message: 'Code verified',
      };
    },
    {
      body: t.Object({
        email: t.String({ format: 'email' }),
        resetCode: t.String(),
      }),
    }
  )

  // Reset password
  .post(
    '/reset-password',
    async ({ body }) => {
      const { email, resetCode, newPassword } = body;

      const result = await db.query.passwordResets.findFirst({
        where: and(
          eq(passwordResets.email, email),
          eq(passwordResets.resetCode, resetCode)
        ),
      });

      if (!result || result.expiresAt < new Date() || result.used) {
        throw new Error('Invalid or expired code');
      }

      // Mark as used
      await db
        .update(passwordResets)
        .set({ used: true })
        .where(eq(passwordResets.id, result.id));

      return {
        success: true,
        message: 'Password reset successful',
      };
    },
    {
      body: t.Object({
        email: t.String({ format: 'email' }),
        resetCode: t.String(),
        newPassword: t.String({ minLength: 6 }),
      }),
    }
  );
