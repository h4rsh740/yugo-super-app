const express = require('express');
const router = express.Router();
const paymentService = require('../../services/paymentService');
const { optionalAuth, authenticateToken } = require('../../middleware/auth');
const prisma = require('../../lib/db');

// POST /api/v1/payments/process
router.post('/process', optionalAuth, async (req, res) => {
  try {
    let userId = req.user?.id;
    if (!userId) {
      const demoUser = await prisma.user.findFirst({ where: { phone: '+919876500112' } });
      userId = demoUser ? demoUser.id : (await prisma.user.findFirst())?.id;
    }

    const { amount, method, serviceType, referenceId, metadata } = req.body;
    const result = await paymentService.processPayment({
      userId,
      amount: parseFloat(amount),
      method,
      serviceType,
      referenceId,
      metadata
    });

    res.json(result);
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// POST /api/v1/payments/wallet/topup
router.post('/wallet/topup', optionalAuth, async (req, res) => {
  try {
    let userId = req.user?.id;
    if (!userId) {
      const demoUser = await prisma.user.findFirst({ where: { phone: '+919876500112' } });
      userId = demoUser ? demoUser.id : (await prisma.user.findFirst())?.id;
    }

    const { amount, method } = req.body;
    const result = await paymentService.topupWallet(userId, parseFloat(amount), method);
    res.json(result);
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// GET /api/v1/payments/history
router.get('/history', optionalAuth, async (req, res) => {
  try {
    let userId = req.user?.id;
    if (!userId) {
      const demoUser = await prisma.user.findFirst({ where: { phone: '+919876500112' } });
      userId = demoUser?.id;
    }

    const txns = await paymentService.getTransactionHistory(userId);
    res.json({ success: true, count: txns.length, transactions: txns });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

module.exports = router;
