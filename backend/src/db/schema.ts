import {
  pgTable,
  uuid,
  varchar,
  text,
  timestamp,
  integer,
  boolean,
  date,
  decimal,
  primaryKey,
  foreignKey,
  index,
} from 'drizzle-orm/pg-core';
import { relations } from 'drizzle-orm';

// ============ CORE TABLES ============

export const users = pgTable(
  'users',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    email: varchar('email', { length: 255 }).unique().notNull(),
    username: varchar('username', { length: 255 }).unique(),
    password: varchar('password', { length: 255 }),
    firstName: varchar('first_name', { length: 100 }),
    lastName: varchar('last_name', { length: 100 }),
    role: varchar('role', { length: 50 }).default('user'), // admin, officer, user
    profilePictureUrl: varchar('profile_picture_url', { length: 500 }),
    firebaseUid: varchar('firebase_uid', { length: 255 }).unique(),
    status: varchar('status', { length: 20 }).default('active'),
    createdAt: timestamp('created_at').defaultNow(),
    updatedAt: timestamp('updated_at').defaultNow(),
  },
  (table) => ({
    emailIdx: index('users_email_idx').on(table.email),
    roleIdx: index('users_role_idx').on(table.role),
  })
);

export const userImages = pgTable(
  'user_images',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    userId: uuid('user_id')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    imageUrl: varchar('image_url', { length: 500 }).notNull(),
    uploadedAt: timestamp('uploaded_at').defaultNow(),
  },
  (table) => ({
    userIdIdx: index('user_images_user_id_idx').on(table.userId),
  })
);

// ============ CASE MANAGEMENT ============

export const blotterReports = pgTable(
  'blotter_reports',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    title: varchar('title', { length: 255 }).notNull(),
    description: text('description'),
    complainantName: varchar('complainant_name', { length: 255 }),
    respondentName: varchar('respondent_name', { length: 255 }),
    location: varchar('location', { length: 255 }),
    incidentDate: date('incident_date'),
    status: varchar('status', { length: 50 }).default('pending'),
    priority: varchar('priority', { length: 20 }).default('medium'),
    assignedOfficerId: uuid('assigned_officer_id').references(() => users.id),
    createdById: uuid('created_by_id').references(() => users.id),
    createdAt: timestamp('created_at').defaultNow(),
    updatedAt: timestamp('updated_at').defaultNow(),
  },
  (table) => ({
    statusIdx: index('blotter_reports_status_idx').on(table.status),
    officerIdx: index('blotter_reports_officer_idx').on(table.assignedOfficerId),
  })
);

export const caseEvidence = pgTable(
  'case_evidence',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    caseId: uuid('case_id')
      .notNull()
      .references(() => blotterReports.id, { onDelete: 'cascade' }),
    fileUrl: varchar('file_url', { length: 500 }).notNull(),
    fileType: varchar('file_type', { length: 50 }),
    uploadedAt: timestamp('uploaded_at').defaultNow(),
  },
  (table) => ({
    caseIdx: index('case_evidence_case_idx').on(table.caseId),
  })
);

// ============ PASSWORD RESET ============

export const passwordResets = pgTable(
  'password_resets',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    email: varchar('email', { length: 255 }).notNull(),
    resetCode: varchar('reset_code', { length: 10 }).notNull(),
    expiresAt: timestamp('expires_at').notNull(),
    used: boolean('used').default(false),
    createdAt: timestamp('created_at').defaultNow(),
  },
  (table) => ({
    emailIdx: index('password_resets_email_idx').on(table.email),
  })
);

// ============ EMAIL LOGS ============

export const emailLogs = pgTable(
  'email_logs',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    recipient: varchar('recipient', { length: 255 }).notNull(),
    subject: varchar('subject', { length: 255 }).notNull(),
    type: varchar('type', { length: 50 }).notNull(),
    status: varchar('status', { length: 20 }).default('sent'),
    sentAt: timestamp('sent_at').defaultNow(),
  },
  (table) => ({
    typeIdx: index('email_logs_type_idx').on(table.type),
  })
);

// ============ NOTIFICATIONS ============

export const notifications = pgTable(
  'notifications',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    userId: uuid('user_id')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    title: varchar('title', { length: 255 }).notNull(),
    message: text('message'),
    type: varchar('type', { length: 50 }).notNull(),
    read: boolean('read').default(false),
    createdAt: timestamp('created_at').defaultNow(),
  },
  (table) => ({
    userIdx: index('notifications_user_idx').on(table.userId),
    readIdx: index('notifications_read_idx').on(table.read),
  })
);

// ============ ACTIVITY LOGS ============

export const activityLogs = pgTable(
  'activity_logs',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    userId: uuid('user_id').references(() => users.id),
    action: varchar('action', { length: 255 }).notNull(),
    resourceType: varchar('resource_type', { length: 100 }),
    resourceId: varchar('resource_id', { length: 255 }),
    details: text('details'),
    createdAt: timestamp('created_at').defaultNow(),
  },
  (table) => ({
    userIdx: index('activity_logs_user_idx').on(table.userId),
    actionIdx: index('activity_logs_action_idx').on(table.action),
  })
);

// ============ RELATIONS ============

export const usersRelations = relations(users, ({ many, one }) => ({
  images: many(userImages),
  blotterReports: many(blotterReports),
  notifications: many(notifications),
  activityLogs: many(activityLogs),
}));

export const userImagesRelations = relations(userImages, ({ one }) => ({
  user: one(users, {
    fields: [userImages.userId],
    references: [users.id],
  }),
}));

export const blotterReportsRelations = relations(blotterReports, ({ one, many }) => ({
  evidence: many(caseEvidence),
  assignedOfficer: one(users, {
    fields: [blotterReports.assignedOfficerId],
    references: [users.id],
  }),
  createdBy: one(users, {
    fields: [blotterReports.createdById],
    references: [users.id],
  }),
}));

export const caseEvidenceRelations = relations(caseEvidence, ({ one }) => ({
  case: one(blotterReports, {
    fields: [caseEvidence.caseId],
    references: [blotterReports.id],
  }),
}));

export const notificationsRelations = relations(notifications, ({ one }) => ({
  user: one(users, {
    fields: [notifications.userId],
    references: [users.id],
  }),
}));

export const activityLogsRelations = relations(activityLogs, ({ one }) => ({
  user: one(users, {
    fields: [activityLogs.userId],
    references: [users.id],
  }),
}));
