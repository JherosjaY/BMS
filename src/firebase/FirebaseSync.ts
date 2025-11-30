/**
 * FirebaseSync - Synchronize Neon data to Firebase Realtime Database
 * Firebase acts as a cache layer for real-time multi-device sync
 * Neon PostgreSQL remains the primary database
 */

import { initializeApp, cert } from "firebase-admin/app";
import { getDatabase } from "firebase-admin/database";
import { db } from "../db";
import { eq } from "drizzle-orm";
import { hearings, blotterReports } from "../db/schema";

interface FirebaseConfig {
  projectId: string;
  privateKey: string;
  clientEmail: string;
  databaseUrl: string;
}

export class FirebaseSync {
  private static instance: FirebaseSync;
  private firebaseDb: any;
  private isInitialized = false;

  private constructor() {}

  static getInstance(): FirebaseSync {
    if (!FirebaseSync.instance) {
      FirebaseSync.instance = new FirebaseSync();
    }
    return FirebaseSync.instance;
  }

  /**
   * Initialize Firebase connection
   */
  async initialize(config: FirebaseConfig) {
    try {
      if (this.isInitialized) {
        console.log("✅ Firebase already initialized");
        return;
      }

      const firebaseConfig = {
        type: "service_account",
        project_id: config.projectId,
        private_key_id: "key-id",
        private_key: config.privateKey.replace(/\\n/g, "\n"),
        client_email: config.clientEmail,
        client_id: "client-id",
        auth_uri: "https://accounts.google.com/o/oauth2/auth",
        token_uri: "https://oauth2.googleapis.com/token",
        auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
        client_x509_cert_url: "cert-url",
      };

      const app = initializeApp({
        credential: cert(firebaseConfig as any),
        databaseURL: config.databaseUrl,
      });

      this.firebaseDb = getDatabase(app);
      this.isInitialized = true;

      console.log("✅ Firebase initialized successfully");
    } catch (error) {
      console.error("❌ Firebase initialization error:", error);
      console.warn("⚠️ Firebase sync disabled - Neon will be primary database");
    }
  }

  /**
   * Sync hearing to Firebase
   */
  async syncHearingToFirebase(hearingId: number, eventType: string) {
    if (!this.isInitialized) {
      console.warn("⚠️ Firebase not initialized, skipping sync");
      return;
    }

    try {
      const hearing = await db.query.hearings.findFirst({
        where: eq(hearings.id, hearingId),
      });

      if (!hearing) return;

      const path = `hearings/${hearingId}`;
      await this.firebaseDb.ref(path).set({
        ...hearing,
        eventType,
        syncedAt: new Date().toISOString(),
      });

      console.log(`✅ Hearing ${hearingId} synced to Firebase`);
    } catch (error) {
      console.error("❌ Error syncing hearing to Firebase:", error);
    }
  }

  /**
   * Sync case to Firebase
   */
  async syncCaseToFirebase(caseId: number, eventType: string) {
    if (!this.isInitialized) {
      console.warn("⚠️ Firebase not initialized, skipping sync");
      return;
    }

    try {
      const report = await db.query.blotterReports.findFirst({
        where: eq(blotterReports.id, caseId),
      });

      if (!report) return;

      const path = `cases/${caseId}`;
      await this.firebaseDb.ref(path).set({
        ...report,
        eventType,
        syncedAt: new Date().toISOString(),
      });

      console.log(`✅ Case ${caseId} synced to Firebase`);
    } catch (error) {
      console.error("❌ Error syncing case to Firebase:", error);
    }
  }

  /**
   * Sync person to Firebase
   */
  async syncPersonToFirebase(personId: string, data: any, eventType: string) {
    if (!this.isInitialized) {
      console.warn("⚠️ Firebase not initialized, skipping sync");
      return;
    }

    try {
      const path = `persons/${personId}`;
      await this.firebaseDb.ref(path).set({
        ...data,
        eventType,
        syncedAt: new Date().toISOString(),
      });

      console.log(`✅ Person ${personId} synced to Firebase`);
    } catch (error) {
      console.error("❌ Error syncing person to Firebase:", error);
    }
  }

  /**
   * Listen for Firebase updates (for testing/debugging)
   */
  listenToHearings(callback: (data: any) => void) {
    if (!this.isInitialized) {
      console.warn("⚠️ Firebase not initialized");
      return;
    }

    try {
      this.firebaseDb.ref("hearings").on("value", (snapshot: any) => {
        if (snapshot.exists()) {
          callback(snapshot.val());
        }
      });

      console.log("👂 Listening to Firebase hearings");
    } catch (error) {
      console.error("❌ Error listening to Firebase:", error);
    }
  }

  /**
   * Get Firebase status
   */
  getStatus() {
    return {
      initialized: this.isInitialized,
      message: this.isInitialized
        ? "Firebase is connected"
        : "Firebase is not initialized",
    };
  }
}

export const firebaseSync = FirebaseSync.getInstance();
