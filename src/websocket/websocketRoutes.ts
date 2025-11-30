/**
 * WebSocket Routes - Real-time Communication
 * Handles WebSocket connections for instant data synchronization
 * Neon PostgreSQL remains the primary database
 */

import { Elysia, t } from "elysia";
import { ws } from "@elysiajs/ws";
import { realtimeManager } from "./RealtimeManager";
import { v4 as uuidv4 } from "crypto";

interface WebSocketMessage {
  type: string;
  userId?: string;
  role?: string;
  channel?: string;
  data?: any;
}

export const websocketRoutes = new Elysia({ prefix: "/ws" })
  // WebSocket endpoint for real-time updates
  .use(
    ws()
  )
  .ws("/realtime", {
    open(ws) {
      const clientId = uuidv4();
      console.log(`🔌 WebSocket connection opened: ${clientId}`);
      
      // Send welcome message
      ws.send({
        type: "connected",
        clientId,
        message: "Connected to BMS Real-time Server",
        timestamp: new Date().toISOString(),
      });
    },

    message(ws, message: any) {
      try {
        const msg: WebSocketMessage = message;
        console.log(`📨 WebSocket message received:`, msg);

        switch (msg.type) {
          // Client authentication and subscription
          case "auth":
            handleAuth(ws, msg);
            break;

          // Subscribe to channel
          case "subscribe":
            handleSubscribe(ws, msg);
            break;

          // Unsubscribe from channel
          case "unsubscribe":
            handleUnsubscribe(ws, msg);
            break;

          // Ping/Pong for connection keep-alive
          case "ping":
            ws.send({
              type: "pong",
              timestamp: new Date().toISOString(),
            });
            break;

          // Sync request
          case "sync_request":
            handleSyncRequest(ws, msg);
            break;

          default:
            console.warn(`⚠️ Unknown message type: ${msg.type}`);
        }
      } catch (error) {
        console.error("❌ WebSocket message error:", error);
        ws.send({
          type: "error",
          message: "Error processing message",
          error: String(error),
        });
      }
    },

    close(ws) {
      console.log(`🔌 WebSocket connection closed`);
    },
  })

  // REST endpoint to trigger real-time updates (for backend use)
  .post("/broadcast/hearing", async ({ body }) => {
    const { hearingId, eventType } = body as { hearingId: number; eventType: string };
    await realtimeManager.broadcastHearingUpdate(hearingId, eventType);
    return {
      success: true,
      message: `Hearing update broadcasted: ${eventType}`,
    };
  })

  .post("/broadcast/case", async ({ body }) => {
    const { caseId, eventType } = body as { caseId: number; eventType: string };
    await realtimeManager.broadcastCaseUpdate(caseId, eventType);
    return {
      success: true,
      message: `Case update broadcasted: ${eventType}`,
    };
  })

  .post("/broadcast/person", async ({ body }) => {
    const { personId, eventType, data } = body as { personId: string; eventType: string; data: any };
    realtimeManager.broadcastPersonUpdate(personId, eventType, data);
    return {
      success: true,
      message: `Person update broadcasted: ${eventType}`,
    };
  })

  .post("/broadcast/notification", async ({ body }) => {
    const { userId, notification } = body as { userId: string; notification: any };
    realtimeManager.broadcastNotification(userId, notification);
    return {
      success: true,
      message: `Notification sent to user: ${userId}`,
    };
  })

  // Status endpoint
  .get("/status", () => ({
    success: true,
    connectedClients: realtimeManager.getConnectedClientsCount(),
    activeChannels: realtimeManager.getActiveChannels(),
    timestamp: new Date().toISOString(),
  }));

/**
 * Handle client authentication
 */
function handleAuth(ws: any, msg: WebSocketMessage) {
  const { userId, role } = msg;
  if (!userId || !role) {
    ws.send({
      type: "error",
      message: "Authentication failed: userId and role required",
    });
    return;
  }

  const clientId = uuidv4();
  realtimeManager.registerClient(clientId, userId, role);

  ws.send({
    type: "authenticated",
    clientId,
    userId,
    role,
    message: "Successfully authenticated",
    timestamp: new Date().toISOString(),
  });
}

/**
 * Handle channel subscription
 */
function handleSubscribe(ws: any, msg: WebSocketMessage) {
  const { channel } = msg;
  if (!channel) {
    ws.send({
      type: "error",
      message: "Subscribe failed: channel required",
    });
    return;
  }

  const clientId = uuidv4();
  realtimeManager.subscribe(clientId, channel);

  ws.send({
    type: "subscribed",
    channel,
    message: `Subscribed to ${channel}`,
    timestamp: new Date().toISOString(),
  });
}

/**
 * Handle channel unsubscription
 */
function handleUnsubscribe(ws: any, msg: WebSocketMessage) {
  const { channel } = msg;
  if (!channel) {
    ws.send({
      type: "error",
      message: "Unsubscribe failed: channel required",
    });
    return;
  }

  const clientId = uuidv4();
  realtimeManager.unsubscribe(clientId, channel);

  ws.send({
    type: "unsubscribed",
    channel,
    message: `Unsubscribed from ${channel}`,
    timestamp: new Date().toISOString(),
  });
}

/**
 * Handle sync request
 */
async function handleSyncRequest(ws: any, msg: WebSocketMessage) {
  const { data } = msg;
  if (!data) {
    ws.send({
      type: "error",
      message: "Sync request failed: data required",
    });
    return;
  }

  ws.send({
    type: "sync_response",
    data,
    message: "Sync completed",
    timestamp: new Date().toISOString(),
  });
}
