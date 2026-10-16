const express = require('express');
const router = express.Router();
const { activities } = require('../data/seed');

// GET /api/activities - Retrieve all past and active trips/orders
router.get('/', (req, res) => {
  res.json({
    success: true,
    count: activities.length,
    activities
  });
});

module.exports = router;
