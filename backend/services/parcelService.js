const prisma = require('../lib/db');
const paymentService = require('./paymentService');

function normalizePhone(input) {
  if (!input) return '';
  const digits = input.replace(/\D/g, '');
  if (digits.length === 10) return `+91${digits}`;
  if (digits.length === 12 && digits.startsWith('91')) return `+${digits}`;
  return input.trim();
}

class ParcelService {
  calculateEstimate({ weightKg = 1.0, distanceKm = 4.5, category = 'DOCUMENTS' }) {
    const weight = Math.max(0.1, parseFloat(weightKg) || 1.0);
    const distance = Math.max(0.5, parseFloat(distanceKm) || 4.5);

    if (weight > 20.0) {
      throw new Error('Maximum parcel weight limit for 2-wheeler express courier is 20 kg');
    }

    const baseFare = 50.0;
    const weightCharge = Math.round(Math.max(0, weight - 1.0) * 20.0);
    const distanceCharge = Math.round(distance * 12.0);
    const total = Math.round(baseFare + weightCharge + distanceCharge);

    return {
      estimatedPrice: total,
      breakdown: {
        baseFare,
        weightCharge,
        distanceCharge,
        total
      }
    };
  }

  async bookParcel(userId, {
    senderName,
    senderPhone,
    pickupAddress,
    recipientName,
    recipientPhone,
    recipientAddress,
    weightKg,
    category,
    instructions,
    paymentMethod
  }) {
    if (!recipientName || recipientName.trim().length < 2) {
      throw new Error('Recipient name must be at least 2 characters');
    }

    const normRecipientPhone = normalizePhone(recipientPhone);
    if (!normRecipientPhone || normRecipientPhone.length < 10) {
      throw new Error('A valid recipient phone number is required');
    }

    if (!recipientAddress || recipientAddress.trim().length < 5) {
      throw new Error('Recipient drop-off address is required (minimum 5 characters)');
    }

    const weight = Math.max(0.1, parseFloat(weightKg) || 1.0);
    if (weight > 20.0) {
      throw new Error('Maximum parcel weight allowed is 20 kg');
    }

    const estimate = this.calculateEstimate({ weightKg: weight, distanceKm: 4.8, category });
    const trackingId = `PARCEL-${Math.floor(100000 + Math.random() * 900000)}`;
    const deliveryPin = Math.floor(1000 + Math.random() * 9000).toString();

    const parcel = await prisma.parcelDelivery.create({
      data: {
        userId,
        trackingId,
        senderName: senderName || 'Harsh Singh',
        senderPhone: senderPhone ? normalizePhone(senderPhone) : '+919876500112',
        pickupAddress: pickupAddress || 'DLF Phase 2, Gurugram',
        recipientName: recipientName.trim(),
        recipientPhone: normRecipientPhone,
        recipientAddress: recipientAddress.trim(),
        category: (category || 'DOCUMENTS').toUpperCase(),
        weightKg: weight,
        instructions: instructions || 'Handle with care',
        price: estimate.estimatedPrice,
        baseFare: estimate.breakdown.baseFare,
        weightCharge: estimate.breakdown.weightCharge,
        distanceCharge: estimate.breakdown.distanceCharge,
        status: 'RIDER_ASSIGNED',
        deliveryPin,
        riderName: 'Vikram Singh',
        riderPhone: '+91 98765 88990',
        estimatedPickupMinutes: 12,
        estimatedDeliveryMinutes: 35,
        paymentMethod: paymentMethod || 'Online',
        paymentStatus: paymentMethod === 'Cash' ? 'PENDING' : 'PAID'
      }
    });

    // Process payment
    await paymentService.processPayment({
      userId,
      amount: estimate.estimatedPrice,
      method: paymentMethod || 'Online',
      serviceType: 'PARCEL',
      referenceId: parcel.trackingId,
      metadata: { trackingId, category: parcel.category }
    });

    // Record in activity log
    await prisma.activityRecord.create({
      data: {
        userId,
        title: `${parcel.category} Parcel Express`,
        serviceType: 'PARCEL',
        dateFormatted: 'Just now',
        priceFormatted: `₹${parcel.price}.00`,
        status: 'Rider Assigned',
        startLocation: parcel.pickupAddress,
        endLocation: parcel.recipientAddress,
        itemsSummary: `${parcel.category} (${parcel.weightKg} kg)`,
        imageKey: 'img_service_parcel',
        referenceId: parcel.trackingId
      }
    });

    return parcel;
  }

  async getParcelByTrackingId(trackingId, userId) {
    const parcel = await prisma.parcelDelivery.findFirst({
      where: {
        trackingId,
        ...(userId ? { userId } : {})
      }
    });

    if (!parcel) {
      throw new Error(`Parcel shipment '${trackingId}' not found`);
    }

    return parcel;
  }

  async updateParcelStatus(trackingId, newStatus) {
    const validStatuses = ['CREATED', 'RIDER_ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'];
    if (!validStatuses.includes(newStatus)) {
      throw new Error(`Invalid status: ${newStatus}`);
    }

    const updated = await prisma.parcelDelivery.update({
      where: { trackingId },
      data: { status: newStatus }
    });

    const statusMap = {
      CREATED: 'Created',
      RIDER_ASSIGNED: 'Rider Assigned',
      PICKED_UP: 'Package Picked Up',
      IN_TRANSIT: 'In Transit',
      DELIVERED: 'Delivered',
      CANCELLED: 'Cancelled'
    };

    await prisma.activityRecord.updateMany({
      where: { referenceId: trackingId },
      data: { status: statusMap[newStatus] || newStatus }
    });

    return updated;
  }
}

module.exports = new ParcelService();
