const http = require('http');
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
const WebSocket = require('ws');
require('dotenv').config();

const prisma = require('./lib/db');

const app = express();
const server = http.createServer(app);
const PORT = process.env.PORT || 8080;

// Rate limiting for auth routes to prevent brute-force
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per window
  standardHeaders: true,
  legacyHeaders: false,
  message: { success: false, message: 'Too many authentication attempts. Please try again after 15 minutes.' }
});

// Middleware
app.use(cors());
app.use(express.json());
app.use(morgan('[:date[iso]] :method :url :status :response-time ms - :res[content-length]'));

// Health check endpoint
const getHealthStatus = async () => {
  let dbStatus = "connected";
  try {
    await prisma.$queryRaw`SELECT 1`;
  } catch (e) {
    dbStatus = "disconnected: " + e.message;
  }
  return {
    status: dbStatus === "connected" ? "healthy" : "degraded",
    database: dbStatus,
    service: "YUGO Super App API",
    version: "1.0.1",
    uptimeSeconds: Math.floor(process.uptime()),
    timestamp: new Date().toISOString()
  };
};

app.get('/api/health', async (req, res) => res.json(await getHealthStatus()));
app.get('/api/v1/health', async (req, res) => res.json(await getHealthStatus()));

app.get('/api/ready', async (req, res) => {
  const status = await getHealthStatus();
  if (status.status === 'healthy') {
    res.json({ ready: true, ...status });
  } else {
    res.status(503).json({ ready: false, ...status });
  }
});
app.get('/api/v1/ready', async (req, res) => {
  const status = await getHealthStatus();
  if (status.status === 'healthy') {
    res.json({ ready: true, ...status });
  } else {
    res.status(503).json({ ready: false, ...status });
  }
});

// Mount V1 API Routes
app.use('/api/v1/auth', authLimiter, require('./routes/v1/auth'));
app.use('/api/v1/rides', require('./routes/v1/rides'));
app.use('/api/v1/food', require('./routes/v1/food'));
app.use('/api/v1/parcel', require('./routes/v1/parcel'));
app.use('/api/v1/payments', require('./routes/v1/payments'));
app.use('/api/v1/activities', require('./routes/v1/activities'));
app.use('/api/v1/locations', require('./routes/v1/locations'));

// Backward Compatibility Aliases for older clients
app.use('/api/auth', authLimiter, require('./routes/v1/auth'));
app.use('/api/rides', require('./routes/v1/rides'));
app.use('/api/food', require('./routes/v1/food'));
app.use('/api/parcel', require('./routes/v1/parcel'));
app.use('/api/payments', require('./routes/v1/payments'));
app.use('/api/activities', require('./routes/v1/activities'));
app.use('/api/locations', require('./routes/v1/locations'));

// WebSocket Server for Real-Time Status Updates
const wss = new WebSocket.Server({ server, path: '/ws/updates' });

wss.on('connection', (ws, req) => {
  console.log(`🔌 [WebSocket] Client connected from ${req.socket.remoteAddress}`);

  ws.isAlive = true;
  ws.on('pong', () => { ws.isAlive = true; });

  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      if (data.type === 'SUBSCRIBE_RIDE') {
        ws.rideId = data.rideId;
        ws.send(JSON.stringify({ type: 'SUBSCRIBED', entity: 'RIDE', id: data.rideId }));
      } else if (data.type === 'SUBSCRIBE_FOOD') {
        ws.orderId = data.orderId;
        ws.send(JSON.stringify({ type: 'SUBSCRIBED', entity: 'FOOD', id: data.orderId }));
      } else if (data.type === 'SUBSCRIBE_PARCEL') {
        ws.trackingId = data.trackingId;
        ws.send(JSON.stringify({ type: 'SUBSCRIBED', entity: 'PARCEL', id: data.trackingId }));
      }
    } catch (e) {
      // Ignore malformed ping/json
    }
  });

  ws.on('close', () => {
    console.log('🔌 [WebSocket] Client disconnected');
  });
});

// Periodic heartbeat to keep connections alive
const pingInterval = setInterval(() => {
  wss.clients.forEach((ws) => {
    if (ws.isAlive === false) return ws.terminate();
    ws.isAlive = false;
    ws.ping();
  });
}, 30000);
if (pingInterval.unref) pingInterval.unref();

wss.on('close', () => {
  clearInterval(pingInterval);
});

// Helper to broadcast status updates to connected clients
global.broadcastEntityUpdate = (entityType, entityId, payload) => {
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      if (
        (entityType === 'RIDE' && client.rideId === entityId) ||
        (entityType === 'FOOD' && client.orderId === entityId) ||
        (entityType === 'PARCEL' && client.trackingId === entityId) ||
        entityType === 'BROADCAST'
      ) {
        client.send(JSON.stringify({ type: `${entityType}_UPDATE`, id: entityId, data: payload }));
      }
    }
  });
};

// 404 Handler
app.use((req, res) => {
  res.status(404).json({ success: false, message: `Route ${req.originalUrl} not found` });
});

// Global Error Handler
app.use((err, req, res, next) => {
  console.error("❌ Server Error:", err);
  res.status(err.status || 500).json({
    success: false,
    message: err.message || "Internal server error",
    error: process.env.NODE_ENV === 'development' ? err.stack : undefined
  });
});

if (process.env.NODE_ENV !== 'test') {
  server.listen(PORT, '0.0.0.0', () => {
    console.log(`===============================================`);
    console.log(`🚀 YUGO Backend Server running on port ${PORT}`);
    console.log(`📍 Local:            http://localhost:${PORT}`);
    console.log(`📱 Android Emulator: http://10.0.2.2:${PORT}`);
    console.log(`⚡ Health Check:     http://localhost:${PORT}/api/health`);
    console.log(`⚡ API v1:           http://localhost:${PORT}/api/v1/health`);
    console.log(`🔌 WebSocket:        ws://localhost:${PORT}/ws/updates`);
    console.log(`===============================================`);
  });
}

module.exports = { app, server };
