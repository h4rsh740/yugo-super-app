const express = require('express');
const router = express.Router();
const { parcelBookings, activities } = require('../data/seed');

// POST /api/parcel/estimate - Estimate parcel delivery pricing
router.post('/estimate', (req, res) => {
  const { weightKg, distanceKm, category } = req.body;
  const weight = parseFloat(weightKg) || 1.0;
  const distance = parseFloat(distanceKm) || 4.5;

  const baseFare = 50.0;
  const weightCharge = Math.max(0, (weight - 1.0)) * 20.0;
  const distanceCharge = distance * 12.0;
  const estimatedPrice = Math.round(baseFare + weightCharge + distanceCharge);

  res.json({
    success: true,
    estimatedPrice,
    breakdown: {
      baseFare,
      weightCharge,
      distanceCharge,
      total: estimatedPrice
    }
  });
});

// POST /api/parcel/book - Book express hyperlocal courier parcel
router.post('/book', (req, res) => {
  const { recipientName, recipientPhone, recipientAddress, weightKg, category, instructions } = req.body;

  const trackingId = `PARCEL-${Math.floor(100000 + Math.random() * 900000)}`;
  const weight = parseFloat(weightKg) || 1.5;
  const price = Math.round(60 + weight * 25);

  const newParcel = {
    trackingId,
    recipientName: recipientName || "Aditya Sharma",
    recipientPhone: recipientPhone || "+91 98765 12345",
    recipientAddress: recipientAddress || "DLF Cyber City, Tower B",
    weightKg: weight,
    category: category || "DOCUMENTS",
    instructions: instructions || "Handle with care",
    status: "RIDER_ASSIGNED",
    estimatedPickupMinutes: 12,
    price,
    bookedAt: new Date().toISOString()
  };

  parcelBookings[trackingId] = newParcel;

  // Add to activities log
  activities.unshift({
    id: `act_${Date.now()}`,
    title: `${newParcel.category} Parcel Express`,
    serviceType: "PARCEL",
    dateFormatted: "Just now",
    priceFormatted: `₹${newParcel.price}.00`,
    status: "Rider Assigned",
    startLocation: "Your Location",
    endLocation: newParcel.recipientAddress,
    itemsSummary: `${newParcel.category} (${newParcel.weightKg} kg)`,
    imageKey: "img_service_parcel"
  });

  res.status(201).json({
    success: true,
    message: "Parcel booked successfully",
    parcel: newParcel
  });
});

// GET /api/parcel/:trackingId - Track parcel
router.get('/:trackingId', (req, res) => {
  const parcel = parcelBookings[req.params.trackingId];
  if (!parcel) {
    return res.status(404).json({ success: false, message: "Parcel shipment not found" });
  }
  res.json({ success: true, parcel });
});

module.exports = router;
