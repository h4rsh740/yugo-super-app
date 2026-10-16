const prisma = require('../lib/db');
const paymentService = require('./paymentService');

class FoodService {
  async getRestaurants({ cuisine, search }) {
    const where = { isOpen: true };

    if (cuisine) {
      where.OR = [
        { cuisine: { contains: cuisine, mode: 'insensitive' } },
        { menuItems: { some: { category: { contains: cuisine, mode: 'insensitive' } } } }
      ];
    }

    if (search) {
      where.OR = [
        { name: { contains: search, mode: 'insensitive' } },
        { cuisine: { contains: search, mode: 'insensitive' } },
        { menuItems: { some: { name: { contains: search, mode: 'insensitive' } } } }
      ];
    }

    const restaurants = await prisma.restaurant.findMany({
      where,
      include: {
        menuItems: {
          where: { isAvailable: true }
        }
      },
      orderBy: { rating: 'desc' }
    });

    return restaurants;
  }

  async getRestaurantById(id) {
    const restaurant = await prisma.restaurant.findUnique({
      where: { id },
      include: {
        menuItems: {
          where: { isAvailable: true }
        }
      }
    });

    if (!restaurant) {
      throw new Error('Restaurant not found');
    }

    return restaurant;
  }

  async calculateBill({ restaurantId, items, couponCode }) {
    if (!items || items.length === 0) {
      throw new Error('Cart cannot be empty for bill calculation');
    }

    const itemIds = items.map(i => i.menuItemId || i.id);
    const dbMenuItems = await prisma.menuItem.findMany({
      where: { id: { in: itemIds } }
    });

    const menuMap = new Map(dbMenuItems.map(m => [m.id, m]));

    let subtotal = 0;
    const validatedItems = [];

    for (const item of items) {
      const itemId = item.menuItemId || item.id;
      const menuItem = menuMap.get(itemId);
      if (!menuItem) {
        throw new Error(`Menu item '${itemId}' no longer available`);
      }
      const qty = Math.max(1, parseInt(item.quantity) || 1);
      const lineTotal = menuItem.price * qty;
      subtotal += lineTotal;

      validatedItems.push({
        menuItemId: menuItem.id,
        name: menuItem.name,
        price: menuItem.price,
        quantity: qty,
        lineTotal
      });
    }

    const deliveryFee = 35.0;
    const packagingFee = 15.0;
    const taxes = Math.round(subtotal * 0.05 * 100) / 100; // 5% GST
    let discount = 0.0;

    if (couponCode && couponCode.toUpperCase() === 'YUGO50') {
      discount = Math.min(100.0, Math.round(subtotal * 0.5));
    }

    const totalAmount = Math.max(0, Math.round((subtotal + deliveryFee + packagingFee + taxes - discount) * 100) / 100);

    return {
      subtotal,
      deliveryFee,
      packagingFee,
      taxes,
      discount,
      totalAmount,
      items: validatedItems
    };
  }

  async placeOrder(userId, { restaurantId, items, deliveryAddress, paymentMethod, couponCode }) {
    if (!restaurantId) {
      throw new Error('Restaurant ID is required');
    }
    if (!deliveryAddress || deliveryAddress.trim().length < 5) {
      throw new Error('A valid delivery address is required');
    }

    const restaurant = await prisma.restaurant.findUnique({
      where: { id: restaurantId }
    });

    if (!restaurant) {
      throw new Error('Restaurant not found');
    }

    const bill = await this.calculateBill({ restaurantId, items, couponCode });

    const order = await prisma.foodOrder.create({
      data: {
        userId,
        restaurantId,
        deliveryAddress: deliveryAddress.trim(),
        subtotal: bill.subtotal,
        deliveryFee: bill.deliveryFee,
        taxes: bill.taxes,
        packagingFee: bill.packagingFee,
        discount: bill.discount,
        totalAmount: bill.totalAmount,
        paymentMethod: paymentMethod || 'Online',
        paymentStatus: paymentMethod === 'Cash' ? 'PENDING' : 'PAID',
        status: 'CONFIRMED',
        estimatedDeliveryMinutes: restaurant.deliveryTimeMinutes || 30,
        items: {
          create: bill.items.map(item => ({
            menuItemId: item.menuItemId,
            name: item.name,
            price: item.price,
            quantity: item.quantity
          }))
        }
      },
      include: {
        items: true,
        restaurant: true
      }
    });

    // Process Payment
    await paymentService.processPayment({
      userId,
      amount: bill.totalAmount,
      method: paymentMethod || 'Online',
      serviceType: 'FOOD',
      referenceId: order.id,
      metadata: { orderId: order.id, restaurantName: restaurant.name }
    });

    // Record in activities log
    const itemsSummary = bill.items.map(i => `${i.quantity}x ${i.name}`).join(', ');
    await prisma.activityRecord.create({
      data: {
        userId,
        title: `${restaurant.name} Order`,
        serviceType: 'FOOD',
        dateFormatted: 'Just now',
        priceFormatted: `₹${Math.round(bill.totalAmount)}.00`,
        status: 'Confirmed',
        startLocation: restaurant.name,
        endLocation: deliveryAddress,
        itemsSummary,
        imageKey: 'img_chicken_biryani',
        referenceId: order.id
      }
    });

    return {
      orderId: order.id,
      restaurantId: order.restaurantId,
      restaurantName: restaurant.name,
      status: order.status,
      deliveryAddress: order.deliveryAddress,
      totalAmount: order.totalAmount,
      paymentMethod: order.paymentMethod,
      estimatedDeliveryMinutes: order.estimatedDeliveryMinutes,
      placedAt: order.placedAt,
      items: order.items
    };
  }

  async getOrderById(orderId, userId) {
    const order = await prisma.foodOrder.findFirst({
      where: {
        id: orderId,
        ...(userId ? { userId } : {})
      },
      include: {
        restaurant: true,
        items: true
      }
    });

    if (!order) {
      throw new Error('Food order not found');
    }

    return {
      orderId: order.id,
      restaurantId: order.restaurantId,
      restaurantName: order.restaurant.name,
      status: order.status,
      deliveryAddress: order.deliveryAddress,
      subtotal: order.subtotal,
      deliveryFee: order.deliveryFee,
      taxes: order.taxes,
      packagingFee: order.packagingFee,
      discount: order.discount,
      totalAmount: order.totalAmount,
      paymentMethod: order.paymentMethod,
      estimatedDeliveryMinutes: order.estimatedDeliveryMinutes,
      placedAt: order.placedAt,
      deliveredAt: order.deliveredAt,
      items: order.items
    };
  }

  async updateOrderStatus(orderId, newStatus) {
    const validStatuses = ['CONFIRMED', 'PREPARING', 'PICKED_UP', 'ARRIVING', 'DELIVERED', 'CANCELLED'];
    if (!validStatuses.includes(newStatus)) {
      throw new Error(`Invalid status: ${newStatus}`);
    }

    const updated = await prisma.foodOrder.update({
      where: { id: orderId },
      data: {
        status: newStatus,
        deliveredAt: newStatus === 'DELIVERED' ? new Date() : undefined
      }
    });

    const statusMap = {
      CONFIRMED: 'Confirmed',
      PREPARING: 'Preparing',
      PICKED_UP: 'Out for Delivery',
      ARRIVING: 'Arriving Soon',
      DELIVERED: 'Delivered',
      CANCELLED: 'Cancelled'
    };

    await prisma.activityRecord.updateMany({
      where: { referenceId: orderId },
      data: { status: statusMap[newStatus] || newStatus }
    });

    return updated;
  }

  async reorder(userId, pastOrderId) {
    const pastOrder = await prisma.foodOrder.findFirst({
      where: { id: pastOrderId, userId },
      include: { items: true }
    });

    if (!pastOrder) {
      throw new Error('Original order not found');
    }

    return this.placeOrder(userId, {
      restaurantId: pastOrder.restaurantId,
      items: pastOrder.items.map(i => ({ menuItemId: i.menuItemId, quantity: i.quantity })),
      deliveryAddress: pastOrder.deliveryAddress,
      paymentMethod: pastOrder.paymentMethod
    });
  }
}

module.exports = new FoodService();
