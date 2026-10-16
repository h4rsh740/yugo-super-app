const jwt = require('jsonwebtoken');
const prisma = require('../lib/db');

const JWT_SECRET = process.env.JWT_SECRET || 'yugo_jwt_access_secret_dev_382910';

async function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.startsWith('Bearer ') ? authHeader.split(' ')[1] : null;

  if (!token) {
    return res.status(401).json({
      success: false,
      message: 'Access token required. Please authenticate.'
    });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    // Verify user exists in database
    const user = await prisma.user.findUnique({
      where: { id: decoded.id }
    });

    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'User belonging to this token no longer exists.'
      });
    }

    req.user = user;
    next();
  } catch (err) {
    if (err.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        code: 'TOKEN_EXPIRED',
        message: 'Access token has expired. Please refresh your session.'
      });
    }
    return res.status(403).json({
      success: false,
      message: 'Invalid or forged authentication token.'
    });
  }
}

async function optionalAuth(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.startsWith('Bearer ') ? authHeader.split(' ')[1] : null;

  if (!token) {
    req.user = null;
    return next();
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const user = await prisma.user.findUnique({ where: { id: decoded.id } });
    req.user = user || null;
  } catch (e) {
    req.user = null;
  }
  next();
}

module.exports = {
  authenticateToken,
  optionalAuth
};
