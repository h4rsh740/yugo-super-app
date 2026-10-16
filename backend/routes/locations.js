const express = require('express');
const router = express.Router();
const { recentLocations } = require('../data/seed');

// GET /api/locations/recent - Get saved and recent destinations
router.get('/recent', (req, res) => {
  res.json({
    success: true,
    count: recentLocations.length,
    locations: recentLocations
  });
});

module.exports = router;
