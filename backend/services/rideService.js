const prisma = require('../lib/db');
const paymentService = require('./paymentService');

function calculateFare(category, distanceKm) {
  const km = Math.max(1.0, distanceKm);
  switch (category.toUpperCase()) {
    case 'MOTO':
      return Math.round(Math.max(35, 25 + km * 8.0));
    case 'AUTO':
      return Math.round(Math.max(50, 35 + km * 12.0));
    case 'GO':
    case 'CAB':
      return Math.round(Math.max(90, 50 + km * 16.0));
    case 'PREMIER':
      return Math.round(Math.max(150, 80 + km * 22.0));
    case 'XL':
      return Math.round(Math.max(220, 120 + km * 30.0));
    default:
      return Math.round(Math.max(90, 50 + km * 16.0));
  }
}

class RideService {
  async getRideOptions(distanceKm = 5.8) {
    const km = parseFloat(distanceKm) || 5.8;

    const options = [
      {
        id: "opt_auto",
        name: "YUGO Auto",
        category: "AUTO",
        capacity: 3,
        etaMinutes: 2,
        fareAmount: calculateFare("AUTO", km),
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
        fareAmount: calculateFare("MOTO", km),
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
        fareAmount: calculateFare("GO", km),
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
        fareAmount: calculateFare("PREMIER", km),
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
        fareAmount: calculateFare("XL", km),
        description: "Spacious SUVs for group rides & luggage",
        isRecommended: false,
        imageKey: "img_cab_xl"
      }
    ];

    return { distanceKm: km, options };
  }

  async getNearbyDrivers(lat = 28.4630, lng = 77.0295) {
    const drivers = await prisma.driver.findMany({
      where: { isAvailable: true },
      take: 5
    });

    return drivers.map((d, index) => ({
      ...d,
      latitude: lat + (index === 0 ? 0.002 : index === 1 ? -0.0015 : index === 2 ? 0.003 : -0.002),
      longitude: lng + (index === 0 ? 0.003 : index === 1 ? 0.002 : index === 2 ? -0.0025 : 0.0015),
      otpPin: "4819"
    }));
  }

  async bookRide(userId, { pickup, destination, rideOption, paymentMethod }) {
    if (!pickup || !destination) {
      throw new Error('Pickup and destination locations are required');
    }

    const category = rideOption?.category || 'GO';
    const distanceKm = 5.8;
    const fareAmount = rideOption?.fareAmount || calculateFare(category, distanceKm);
    const otpPin = Math.floor(1000 + Math.random() * 9000).toString();

    // Assign a matching driver
    const driver = await prisma.driver.findFirst({
      where: {
        isAvailable: true,
        vehicleCategory: category
      }
    }) || await prisma.driver.findFirst();

    const ride = await prisma.ride.create({
      data: {
        userId,
        driverId: driver ? driver.id : null,
        category,
        pickupTitle: pickup.title || 'Current Location',
        pickupAddress: pickup.address || 'DLF Phase 2, Gurugram',
        pickupLat: pickup.latitude || 28.4630,
        pickupLng: pickup.longitude || 77.0295,
        destTitle: destination.title || 'Cyber City',
        destAddress: destination.address || 'DLF Cyber City, Tower B',
        destLat: destination.latitude || 28.4950,
        destLng: destination.longitude || 77.0895,
        fareAmount,
        distanceKm,
        etaMinutes: rideOption?.etaMinutes || 3,
        status: 'DRIVER_ASSIGNED',
        otpPin,
        paymentMethod: paymentMethod || 'Cash',
        paymentStatus: paymentMethod === 'Cash' ? 'PENDING' : 'PAID',
        events: {
          create: [
            { status: 'SEARCHING', note: 'Ride requested by customer' },
            { status: 'DRIVER_ASSIGNED', note: `Driver ${driver ? driver.name : 'assigned'} matched` }
          ]
        }
      },
      include: {
        driver: true,
        events: true
      }
    });

    // Record in payment transactions
    await paymentService.processPayment({
      userId,
      amount: fareAmount,
      method: paymentMethod || 'Cash',
      serviceType: 'RIDE',
      referenceId: ride.id,
      metadata: { rideId: ride.id, category }
    });

    // Record in activities log
    await prisma.activityRecord.create({
      data: {
        userId,
        title: `Ride to ${ride.destTitle}`,
        serviceType: category === 'AUTO' ? 'AUTO' : category === 'MOTO' ? 'BIKE' : 'CAB',
        dateFormatted: 'Just now',
        priceFormatted: `₹${Math.round(fareAmount)}.00`,
        status: 'In Progress',
        startLocation: ride.pickupTitle,
        endLocation: ride.destTitle,
        imageKey: category === 'AUTO' ? 'img_auto' : category === 'MOTO' ? 'img_moto' : 'img_cab_go',
        referenceId: ride.id
      }
    });

    return {
      bookingId: ride.id,
      status: ride.status,
      createdAt: ride.createdAt,
      pickup: { title: ride.pickupTitle, address: ride.pickupAddress },
      destination: { title: ride.destTitle, address: ride.destAddress },
      rideOption: {
        name: rideOption?.name || `YUGO ${category}`,
        category,
        fareAmount: ride.fareAmount
      },
      paymentMethod: ride.paymentMethod,
      driver: driver ? {
        id: driver.id,
        name: driver.name,
        rating: driver.rating,
        totalTrips: driver.totalTrips,
        vehicleModel: driver.vehicleModel,
        vehicleColor: driver.vehicleColor,
        plateNumber: driver.vehiclePlate,
        phoneNumber: driver.phone,
        otpPin: ride.otpPin,
        latitude: driver.latitude,
        longitude: driver.longitude
      } : null,
      estimatedEtaMinutes: ride.etaMinutes,
      otpPin: ride.otpPin
    };
  }

  async getRideById(rideId, userId) {
    const ride = await prisma.ride.findFirst({
      where: {
        id: rideId,
        ...(userId ? { userId } : {})
      },
      include: {
        driver: true,
        events: { orderBy: { timestamp: 'asc' } }
      }
    });

    if (!ride) {
      throw new Error('Ride booking not found');
    }

    return {
      bookingId: ride.id,
      status: ride.status,
      createdAt: ride.createdAt,
      pickup: { title: ride.pickupTitle, address: ride.pickupAddress },
      destination: { title: ride.destTitle, address: ride.destAddress },
      rideOption: {
        name: `YUGO ${ride.category}`,
        category: ride.category,
        fareAmount: ride.fareAmount
      },
      paymentMethod: ride.paymentMethod,
      driver: ride.driver ? {
        id: ride.driver.id,
        name: ride.driver.name,
        rating: ride.driver.rating,
        totalTrips: ride.driver.totalTrips,
        vehicleModel: ride.driver.vehicleModel,
        vehicleColor: ride.driver.vehicleColor,
        plateNumber: ride.driver.vehiclePlate,
        phoneNumber: ride.driver.phone,
        otpPin: ride.otpPin,
        latitude: ride.driver.latitude,
        longitude: ride.driver.longitude
      } : null,
      estimatedEtaMinutes: ride.etaMinutes,
      otpPin: ride.otpPin,
      events: ride.events
    };
  }

  async updateRideStatus(rideId, newStatus) {
    const validStatuses = ['SEARCHING', 'DRIVER_ASSIGNED', 'DRIVER_ARRIVING', 'TRIP_STARTED', 'COMPLETED', 'CANCELLED'];
    if (!validStatuses.includes(newStatus)) {
      throw new Error(`Invalid status: ${newStatus}. Allowed: ${validStatuses.join(', ')}`);
    }

    const ride = await prisma.ride.findUnique({ where: { id: rideId } });
    if (!ride) {
      throw new Error('Ride not found');
    }

    const updated = await prisma.ride.update({
      where: { id: rideId },
      data: {
        status: newStatus,
        events: {
          create: {
            status: newStatus,
            note: `Ride transition to ${newStatus}`
          }
        }
      },
      include: { driver: true }
    });

    // Sync activity record status
    if (newStatus === 'COMPLETED' || newStatus === 'CANCELLED') {
      await prisma.activityRecord.updateMany({
        where: { referenceId: rideId },
        data: { status: newStatus === 'COMPLETED' ? 'Completed' : 'Cancelled' }
      });
    }

    return updated;
  }

  async cancelRide(rideId, userId) {
    const ride = await prisma.ride.findFirst({
      where: {
        id: rideId,
        ...(userId ? { userId } : {})
      }
    });

    if (!ride) {
      throw new Error('Ride not found');
    }

    await prisma.ride.update({
      where: { id: rideId },
      data: {
        status: 'CANCELLED',
        events: {
          create: {
            status: 'CANCELLED',
            note: 'Ride cancelled by user'
          }
        }
      }
    });

    await prisma.activityRecord.updateMany({
      where: { referenceId: rideId },
      data: { status: 'Cancelled' }
    });

    return { success: true, message: 'Ride booking cancelled successfully' };
  }
}

module.exports = new RideService();
