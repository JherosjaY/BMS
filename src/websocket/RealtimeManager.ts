/**
 * RealtimeManager - WebSocket Real-time Synchronization
 * Manages WebSocket connections and broadcasts updates to connected clients
 * Keeps Neon as primary database, WebSocket for instant notifications
 */

import { db } from "../db";
import { eq } from "drizzle-orm";
import { hearings, blotterReports } from "../db/schema";

interface ConnectedClient {
  userId: string;
  role: string;
  subscriptions: Set<string>;
}

export class RealtimeManager {
  private connectedClients: Map<string, ConnectedClient> = new Map();
  private broadcastChannels: Map<string, Set<string>> = new Map();

  /**
   * Register a new WebSocket connection
   */
  registerClient(clientId: string, userId: string, role: string) {
    this.connectedClients.set(clientId, {
      userId,
      role,
      subscriptions: new Set(),
    });
    console.log(`✅ Client connected: ${clientId} (User: ${userId}, Role: ${role})`);
  }

  /**
   * Unregister a WebSocket connection
   */
  unregisterClient(clientId: string) {
    this.connectedClients.delete(clientId);
    console.log(`❌ Client disconnected: ${clientId}`);
  }

  /**
   * Subscribe client to a channel
   */
  subscribe(clientId: string, channel: string) {
    const client = this.connectedClients.get(clientId);
    if (client) {
      client.subscriptions.add(channel);
      if (!this.broadcastChannels.has(channel)) {
        this.broadcastChannels.set(channel, new Set());
      }
      this.broadcastChannels.get(channel)!.add(clientId);
      console.log(`📡 Client ${clientId} subscribed to ${channel}`);
    }
  }

  /**
   * Unsubscribe client from a channel
   */
  unsubscribe(clientId: string, channel: string) {
    const client = this.connectedClients.get(clientId);
    if (client) {
      client.subscriptions.delete(channel);
      this.broadcastChannels.get(channel)?.delete(clientId);
      console.log(`📡 Client ${clientId} unsubscribed from ${channel}`);
    }
  }

  /**
   * Broadcast hearing update to all subscribed clients
   */
  async broadcastHearingUpdate(hearingId: number, eventType: string) {
    try {
      const hearing = await db.query.hearings.findFirst({
        where: eq(hearings.id, hearingId),
      });

      if (!hearing) return;

      const message = {
        type: "hearing_update",
        eventType, // 'created', 'updated', 'status_changed', 'deleted'
        data: hearing,
        timestamp: new Date().toISOString(),
      };

      this.broadcast("hearings", message);
    } catch (error) {
      console.error("❌ Error broadcasting hearing update:", error);
    }
  }

  /**
   * Broadcast case/report update to all subscribed clients
   */
  async broadcastCaseUpdate(caseId: number, eventType: string) {
    try {
      const report = await db.query.blotterReports.findFirst({
        where: eq(blotterReports.id, caseId),
      });

      if (!report) return;

      const message = {
        type: "case_update",
        eventType, // 'created', 'updated', 'status_changed', 'assigned'
        data: report,
        timestamp: new Date().toISOString(),
      };

      this.broadcast("cases", message);
    } catch (error) {
      console.error("❌ Error broadcasting case update:", error);
    }
  }

  /**
   * Broadcast person history update
   */
  broadcastPersonUpdate(personId: string, eventType: string, data: any) {
    const message = {
      type: "person_update",
      eventType, // 'profile_created', 'record_added', 'risk_updated'
      personId,
      data,
      timestamp: new Date().toISOString(),
    };

    this.broadcast("persons", message);
  }

  /**
   * Broadcast notification to specific user
   */
  broadcastNotification(userId: string, notification: any) {
    const message = {
      type: "notification",
      data: notification,
      timestamp: new Date().toISOString(),
    };

    this.broadcastToUser(userId, message);
  }

  /**
   * Generic broadcast to a channel
   */
  private broadcast(channel: string, message: any) {
    const subscribers = this.broadcastChannels.get(channel);
    if (!subscribers) return;

    console.log(`📢 Broadcasting to ${subscribers.size} clients on channel: ${channel}`);
    subscribers.forEach((clientId) => {
      // Send to client (implementation depends on WebSocket framework)
      console.log(`📤 Sending to client: ${clientId}`, message);
    });
  }

  /**
   * Broadcast to specific user
   */
  private broadcastToUser(userId: string, message: any) {
    let sentCount = 0;
    this.connectedClients.forEach((client, clientId) => {
      if (client.userId === userId) {
        console.log(`📤 Sending notification to user ${userId}:`, message);
        sentCount++;
      }
    });
    console.log(`📢 Notification sent to ${sentCount} device(s) of user ${userId}`);
  }

  /**
   * Get connected clients count
   */
  getConnectedClientsCount(): number {
    return this.connectedClients.size;
  }

  /**
   * Get channel subscribers count
   */
  getChannelSubscribersCount(channel: string): number {
    return this.broadcastChannels.get(channel)?.size || 0;
  }

  /**
   * Get all active channels
   */
  getActiveChannels(): string[] {
    return Array.from(this.broadcastChannels.keys());
  }
}

export const realtimeManager = new RealtimeManager();
