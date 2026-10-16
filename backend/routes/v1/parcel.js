const express = require('express');
const router = express.Router();
const parcelService = require('../../services/parcelService');
const { optionalAuth, authenticateToken } = require('../../middleware/auth');
const prisma = require('../../lib/db');

// POST /api/v1/parcel/estimate
router.post('/estimate', (req, res) => {
  try {
    const { weightKg, distanceKm, category } = req.body;
    const result = parcelService.calculateEstimate({ weightKg, distanceKm, category });
    res.json({ success: true, ...result });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// POST /api/v1/parcel/book
router.post('/book', optionalAuth, async (req, res) => {
  try {
    let userId = req.user?.id;
    if (!userId) {
      const demoUser = await prisma.user.findFirst({ where: { phone: '+919876500112' } });
      userId = demoUser ? demoUser.id : (await prisma.user.findFirst())?.id;
    }

    const parcel = await parcelService.bookParcel(userId, req.body);
    res.status(201).json({
      success: true,
      message: 'Parcel booked successfully',
      parcel
    });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// GET /api/v1/parcel/:trackingId
router.get('/:trackingId', optionalAuth, async (req, res) => {
  try {
    const parcel = await parcelService.getParcelByTrackingId(req.params.trackingId, req.user?.id);
    res.json({ success: true, parcel });
  } catch (err) {
    res.status(404).json({ success: false, message: err.message });
  }
});

// POST /api/v1/parcel/:trackingId/status
router.post('/:trackingId/status', async (req, res) => {
  try {
    const { status } = req.body;
    const updated = await parcelService.updateParcelStatus(req.params.trackingId, status);
    res.json({ success: true, status: updated.status, message: `Parcel status updated to ${status}` });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

module.exports = router;
