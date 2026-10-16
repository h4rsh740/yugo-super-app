const express = require('express');
const router = express.Router();
const { nearbyDrivers, sampleDriver, bookings, activities } = require('../data/seed');

// GET /api/rides/options - Calculate dynamic ride fares & options
router.get('/options', (req, res) => {
  const distanceKm = parseFloat(req.query.distanceKm) || 5.8;
  const baseMultiplier = Math.max(distanceKm / 5.0, 0.8);

  const options = [
    {
      id: "opt_auto",
      name: "YUGO Auto",
      category: "AUTO",
      capacity: 3,
      etaMinutes: 2,
      fareAmount: Math.round(85.0 * baseMultiplier),
      description: "Quick auto rides without haggling",
      isRecommended: false,
      imageKey: "img_auto"
    },
    {
      id: "opt_moto",
      name: "YUGO Moto",
      category: "MOTO",
      capacity: 1,
      etaMinutes: 1,
      fareAmount: Math.round(55.0 * baseMultiplier),
      description: "Fastest through traffic",
      isRecommended: false,
      imageKey: "img_moto"
    },
    {
      id: "opt_go",
      name: "YUGO Go",
      category: "GO",
      capacity: 4,
      etaMinutes: 3,
      fareAmount: Math.round(160.0 * baseMultiplier),
      description: "Affordable, comfortable hatchbacks",
      isRecommended: true,
      imageKey: "img_cab_go"
    },
    {
      id: "opt_premier",
      name: "YUGO Premier",
      category: "PREMIER",
      capacity: 4,
      etaMinutes: 4,
      fareAmount: Math.round(230.0 * baseMultiplier),
      description: "Top-rated drivers, premium sedans",
      isRecommended: false,
      imageKey: "img_cab_premier"
    },
    {
      id: "opt_xl",
      name: "YUGO XL",
      category: "XL",
      capacity: 6,
      etaMinutes: 5,
      fareAmount: Math.round(340.0 * baseMultiplier),
      description: "Spacious SUVs for group rides & luggage",
      isRecommended: false,
      imageKey: "img_cab_xl"
    }
  ];

  res.json({ success: true, distanceKm, options });
});

// GET /api/rides/drivers/nearby - Get live nearby drivers
router.get('/drivers/nearby', (req, res) => {
  const lat = parseFloat(req.query.lat) || 28.4630;
  const lng = parseFloat(req.query.lng) || 77.0295;

  // Add realistic micro-variations
  const drivers = nearbyDrivers.map((d, index) => ({
    ...d,
    latitude: lat + (index === 0 ? 0.002 : index === 1 ? -0.0015 : 0.003),
    longitude: lng + (index === 0 ? 0.003 : index === 1 ? 0.002 : -0.0025)
  }));

  res.json({ success: true, count: drivers.length, drivers });
});

// POST /api/rides/book - Create and confirm a ride booking
router.post('/book', (req, res) => {
  const { pickup, destination, rideOption, paymentMethod } = req.body;

  const bookingId = `bk_${Date.now()}`;
  const newBooking = {
    bookingId,
    status: "DRIVER_ASSIGNED",
    createdAt: new Date().toISOString(),
    pickup: pickup || { title: "Sector 54, Gurugram" },
    destination: destination || { title: "Cyber City, DLF Phase 2" },
    rideOption: rideOption || { name: "YUGO Go", fareAmount: 160 },
    paymentMethod: paymentMethod || "Cash",
    driver: sampleDriver,
    estimatedEtaMinutes: rideOption?.etaMinutes || 3,
    otpPin: Math.floor(1000 + Math.random() * 9000).toString()
  };

  bookings[bookingId] = newBooking;

  // Record in activities log
  activities.unshift({
    id: `act_${Date.now()}`,
    title: `Ride to ${newBooking.destination.title}`,
    serviceType: "CAB",
    dateFormatted: "Just now",
    priceFormatted: `₹${newBooking.rideOption.fareAmount}.00`,
    status: "In Progress",
    startLocation: newBooking.pickup.title,
    endLocation: newBooking.destination.title,
    imageKey: "img_cab_go"
  });

  res.status(201).json({
    success: true,
    message: "Ride booking confirmed successfully",
    booking: newBooking
  });
});

// GET /api/rides/:bookingId - Check ride tracking and status
router.get('/:bookingId', (req, res) => {
  const { bookingId } = req.params;
  const booking = bookings[bookingId];

  if (!booking) {
    return res.status(404).json({ success: false, message: "Booking not found" });
  }

  res.json({ success: true, booking });
});

// POST /api/rides/:bookingId/cancel - Cancel ride
router.post('/:bookingId/cancel', (req, res) => {
  const { bookingId } = req.params;
  if (bookings[bookingId]) {
    bookings[bookingId].status = "CANCELLED";
    return res.json({ success: true, message: "Ride booking cancelled" });
  }
  res.status(404).json({ success: false, message: "Booking not found" });
});

module.exports = router;
