const express = require('express');
const router = express.Router();
const foodService = require('../../services/foodService');
const { optionalAuth, authenticateToken } = require('../../middleware/auth');
const prisma = require('../../lib/db');

// GET /api/v1/food/restaurants
router.get('/restaurants', async (req, res) => {
  try {
    const { cuisine, search } = req.query;
    const restaurants = await foodService.getRestaurants({ cuisine, search });
    res.json({ success: true, count: restaurants.length, restaurants });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// GET /api/v1/food/restaurants/:id
router.get('/restaurants/:id', async (req, res) => {
  try {
    const restaurant = await foodService.getRestaurantById(req.params.id);
    res.json({ success: true, restaurant });
  } catch (err) {
    res.status(404).json({ success: false, message: err.message });
  }
});

// POST /api/v1/food/calculate-bill
router.post('/calculate-bill', async (req, res) => {
  try {
    const { restaurantId, items, couponCode } = req.body;
    const bill = await foodService.calculateBill({ restaurantId, items, couponCode });
    res.json({ success: true, bill });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// POST /api/v1/food/orders
router.post('/orders', optionalAuth, async (req, res) => {
  try {
    let userId = req.user?.id;
    if (!userId) {
      const demoUser = await prisma.user.findFirst({ where: { phone: '+919876500112' } });
      userId = demoUser ? demoUser.id : (await prisma.user.findFirst())?.id;
    }

    const { restaurantId, cartItems, items, deliveryAddress, paymentMethod, couponCode } = req.body;
    const orderItems = items || (cartItems && cartItems.map(c => ({
      menuItemId: c.menuItem?.id || c.id,
      quantity: c.quantity
    })));

    const order = await foodService.placeOrder(userId, {
      restaurantId,
      items: orderItems,
      deliveryAddress,
      paymentMethod,
      couponCode
    });

    res.status(201).json({
      success: true,
      message: 'Food order placed successfully',
      order
    });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// GET /api/v1/food/orders/:id
router.get('/orders/:id', optionalAuth, async (req, res) => {
  try {
    const order = await foodService.getOrderById(req.params.id, req.user?.id);
    res.json({ success: true, order });
  } catch (err) {
    res.status(404).json({ success: false, message: err.message });
  }
});

// POST /api/v1/food/orders/:id/status
router.post('/orders/:id/status', async (req, res) => {
  try {
    const { status } = req.body;
    const updated = await foodService.updateOrderStatus(req.params.id, status);
    res.json({ success: true, status: updated.status, message: `Order status updated to ${status}` });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// POST /api/v1/food/orders/:id/reorder
router.post('/orders/:id/reorder', optionalAuth, async (req, res) => {
  try {
    let userId = req.user?.id;
    if (!userId) {
      const demoUser = await prisma.user.findFirst({ where: { phone: '+919876500112' } });
      userId = demoUser?.id;
    }
    const order = await foodService.reorder(userId, req.params.id);
    res.status(201).json({ success: true, message: 'Reordered successfully', order });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

module.exports = router;
