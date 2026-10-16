const express = require('express');
const router = express.Router();
const prisma = require('../../lib/db');
const { optionalAuth } = require('../../middleware/auth');

const defaultLocations = [
  { id: "1", title: "Cyber City", address: "DLF Phase 2, Gurugram, Haryana", latitude: 28.4950, longitude: 77.0895, type: "WORK" },
  { id: "2", title: "Home", address: "Sector 54, Golf Course Road, Gurugram", latitude: 28.4382, longitude: 77.1082, type: "HOME" },
  { id: "3", title: "IGI Airport T3", address: "Indira Gandhi International Airport, New Delhi", latitude: 28.5562, longitude: 77.1000, type: "AIRPORT" },
  { id: "4", title: "Connaught Place", address: "Inner Circle, New Delhi", latitude: 28.6315, longitude: 77.2167, type: "POPULAR" },
  { id: "5", title: "Galleria Market", address: "DLF Phase 4, Gurugram, Haryana", latitude: 28.4674, longitude: 77.0825, type: "RECENT" }
];

// GET /api/v1/locations/recent
router.get('/recent', optionalAuth, async (req, res) => {
  try {
    let saved = [];
    if (req.user?.id) {
      const addresses = await prisma.address.findMany({
        where: { userId: req.user.id }
      });
      saved = addresses.map(a => ({
        id: a.id,
        title: a.title,
        address: a.address,
        latitude: a.latitude || 28.4630,
        longitude: a.longitude || 77.0295,
        type: a.label
      }));
    }

    const locations = saved.length > 0 ? saved.concat(defaultLocations) : defaultLocations;
    res.json({
      success: true,
      count: locations.length,
      locations
    });
  } catch (err) {
    res.json({ success: true, count: defaultLocations.length, locations: defaultLocations });
  }
});

module.exports = router;
