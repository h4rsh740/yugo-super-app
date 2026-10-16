const express = require('express');
const router = express.Router();
const rideService = require('../../services/rideService');
const { optionalAuth, authenticateToken } = require('../../middleware/auth');
const prisma = require('../../lib/db');

// GET /api/v1/rides/options
router.get('/options', async (req, res) => {
  try {
    const distanceKm = parseFloat(req.query.distanceKm) || 5.8;
    const result = await rideService.getRideOptions(distanceKm);
    res.json({ success: true, ...result });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// GET /api/v1/rides/drivers/nearby
router.get('/drivers/nearby', async (req, res) => {
  try {
    const lat = parseFloat(req.query.lat) || 28.4630;
    const lng = parseFloat(req.query.lng) || 77.0295;
    const drivers = await rideService.getNearbyDrivers(lat, lng);
    res.json({ success: true, count: drivers.length, drivers });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// POST /api/v1/rides/book
router.post('/book', optionalAuth, async (req, res) => {
  try {
    // If not authenticated, use default demo user
    let userId = req.user?.id;
    if (!userId) {
      const demoUser = await prisma.user.findFirst({ where: { phone: '+919876500112' } });
      userId = demoUser ? demoUser.id : (await prisma.user.findFirst())?.id;
    }

    const booking = await rideService.bookRide(userId, req.body);
    res.status(201).json({
      success: true,
      message: 'Ride booking confirmed successfully',
      booking
    });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// GET /api/v1/rides/:bookingId
router.get('/:bookingId', optionalAuth, async (req, res) => {
  try {
    const booking = await rideService.getRideById(req.params.bookingId);
    res.json({ success: true, booking });
  } catch (err) {
    res.status(404).json({ success: false, message: err.message });
  }
});

// POST /api/v1/rides/:bookingId/status
router.post('/:bookingId/status', async (req, res) => {
  try {
    const { status } = req.body;
    const updated = await rideService.updateRideStatus(req.params.bookingId, status);
    res.json({ success: true, status: updated.status, message: `Status updated to ${status}` });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// POST /api/v1/rides/:bookingId/cancel
router.post('/:bookingId/cancel', optionalAuth, async (req, res) => {
  try {
    const result = await rideService.cancelRide(req.params.bookingId, req.user?.id);
    res.json(result);
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

module.exports = router;
