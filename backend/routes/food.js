const express = require('express');
const router = express.Router();
const { restaurants, foodOrders, activities } = require('../data/seed');

// GET /api/food/restaurants - List restaurants with optional cuisine and search filter
router.get('/restaurants', (req, res) => {
  const { cuisine, search } = req.query;
  let filtered = [...restaurants];

  if (cuisine) {
    const c = cuisine.toLowerCase();
    filtered = filtered.filter(r =>
      r.cuisine.toLowerCase().includes(c) ||
      (r.menuItems && r.menuItems.some(m => m.category.toLowerCase().includes(c) || m.name.toLowerCase().includes(c)))
    );
  }

  if (search) {
    const q = search.toLowerCase();
    filtered = filtered.filter(r =>
      r.name.toLowerCase().includes(q) ||
      r.cuisine.toLowerCase().includes(q) ||
      r.menuItems.some(m => m.name.toLowerCase().includes(q))
    );
  }

  res.json({ success: true, count: filtered.length, restaurants: filtered });
});

// GET /api/food/restaurants/:id - Get specific restaurant and full menu
router.get('/restaurants/:id', (req, res) => {
  const restaurant = restaurants.find(r => r.id === req.params.id);
  if (!restaurant) {
    return res.status(404).json({ success: false, message: "Restaurant not found" });
  }
  res.json({ success: true, restaurant });
});

// POST /api/food/orders - Place a new food delivery order
router.post('/orders', (req, res) => {
  const { restaurantId, cartItems, deliveryAddress, totalAmount, paymentMethod } = req.body;

  const restaurant = restaurants.find(r => r.id === restaurantId);
  const orderId = `order_${Date.now()}`;

  const newOrder = {
    orderId,
    restaurantId,
    restaurantName: restaurant ? restaurant.name : "YUGO Partner Restaurant",
    cartItems: cartItems || [],
    deliveryAddress: deliveryAddress || "DLF Phase 2, Gurugram",
    totalAmount: totalAmount || 350.0,
    paymentMethod: paymentMethod || "Online",
    status: "PREPARING",
    estimatedDeliveryMinutes: 25,
    placedAt: new Date().toISOString()
  };

  foodOrders[orderId] = newOrder;

  // Record in activities log
  const itemsSummary = cartItems && cartItems.length > 0
    ? cartItems.map(c => `${c.quantity}x ${c.menuItem?.name || 'Dish'}`).join(", ")
    : "Food order";

  activities.unshift({
    id: `act_${Date.now()}`,
    title: `${newOrder.restaurantName} Order`,
    serviceType: "FOOD",
    dateFormatted: "Just now",
    priceFormatted: `₹${Math.round(newOrder.totalAmount)}.00`,
    status: "Preparing",
    startLocation: newOrder.restaurantName,
    endLocation: newOrder.deliveryAddress,
    itemsSummary,
    imageKey: "img_chicken_biryani"
  });

  res.status(201).json({
    success: true,
    message: "Food order placed successfully",
    order: newOrder
  });
});

// GET /api/food/orders/:id - Get order tracking status
router.get('/orders/:id', (req, res) => {
  const order = foodOrders[req.params.id];
  if (!order) {
    return res.status(404).json({ success: false, message: "Order not found" });
  }
  res.json({ success: true, order });
});

module.exports = router;
