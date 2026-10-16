const express = require('express');
const router = express.Router();
const prisma = require('../../lib/db');
const { optionalAuth } = require('../../middleware/auth');

// GET /api/v1/activities
router.get('/', optionalAuth, async (req, res) => {
  try {
    let userId = req.user?.id;
    if (!userId) {
      const demoUser = await prisma.user.findFirst({ where: { phone: '+919876500112' } });
      userId = demoUser ? demoUser.id : (await prisma.user.findFirst())?.id;
    }

    const { serviceType } = req.query;
    const where = { ...(userId ? { userId } : {}) };

    if (serviceType && serviceType.toUpperCase() !== 'ALL') {
      where.serviceType = serviceType.toUpperCase();
    }

    const activities = await prisma.activityRecord.findMany({
      where,
      orderBy: { createdAt: 'desc' },
      take: 50
    });

    res.json({
      success: true,
      count: activities.length,
      activities
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

module.exports = router;
