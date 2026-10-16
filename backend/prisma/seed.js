const { PrismaClient } = require('@prisma/client');
require('dotenv').config();

const prisma = new PrismaClient();

async function main() {
  console.log('🌱 Seeding database...');

  // 1. Clean up existing data in correct order
  await prisma.notification.deleteMany({});
  await prisma.activityRecord.deleteMany({});
  await prisma.paymentTransaction.deleteMany({});
  await prisma.foodOrderItem.deleteMany({});
  await prisma.foodOrder.deleteMany({});
  await prisma.parcelDelivery.deleteMany({});
  await prisma.rideEvent.deleteMany({});
  await prisma.ride.deleteMany({});
  await prisma.menuItem.deleteMany({});
  await prisma.restaurant.deleteMany({});
  await prisma.driver.deleteMany({});
  await prisma.notificationPreference.deleteMany({});
  await prisma.emergencyContact.deleteMany({});
  await prisma.address.deleteMany({});
  await prisma.session.deleteMany({});
  await prisma.otpRequest.deleteMany({});
  await prisma.user.deleteMany({});

  // 2. Create Primary Demo User
  const demoUser = await prisma.user.create({
    data: {
      phone: "+919876500112",
      name: "Harsh Singh",
      email: "harsh@yugo.app",
      rating: 4.95,
      yugoCashBalance: 1250.0,
      isPhoneVerified: true,
      preferences: {
        create: {
          rideUpdates: true,
          foodUpdates: true,
          parcelUpdates: true,
          promotional: false,
          pushEnabled: true,
          smsEnabled: true
        }
      },
      addresses: {
        create: [
          {
            label: "HOME",
            title: "Home",
            address: "Sector 54, Golf Course Road, Gurugram",
            latitude: 28.4382,
            longitude: 77.1082,
            isDefault: true
          },
          {
            label: "WORK",
            title: "Work",
            address: "DLF Cyber City, Tower B, Gurugram",
            latitude: 28.4950,
            longitude: 77.0895,
            isDefault: false
          }
        ]
      },
      emergencyContacts: {
        create: [
          {
            name: "Sunita Singh",
            phone: "+919811122334",
            relationship: "Mother"
          }
        ]
      }
    }
  });

  console.log(`👤 Created Demo User: ${demoUser.name} (${demoUser.phone})`);

  // 3. Create Drivers
  const drivers = await Promise.all([
    prisma.driver.create({
      data: {
        name: "Rajesh Sharma",
        phone: "+91 98765 43210",
        rating: 4.92,
        totalTrips: 3840,
        vehicleModel: "Maruti Suzuki Dzire",
        vehicleColor: "White",
        vehiclePlate: "HR 26 DQ 4921",
        vehicleCategory: "GO",
        latitude: 28.4630,
        longitude: 77.0295,
        isAvailable: true
      }
    }),
    prisma.driver.create({
      data: {
        name: "Suresh Kumar",
        phone: "+91 98111 22334",
        rating: 4.88,
        totalTrips: 2190,
        vehicleModel: "Bajaj RE Auto",
        vehicleColor: "Yellow/Green",
        vehiclePlate: "HR 55 AT 1109",
        vehicleCategory: "AUTO",
        latitude: 28.4610,
        longitude: 77.0315,
        isAvailable: true
      }
    }),
    prisma.driver.create({
      data: {
        name: "Amit Verma",
        phone: "+91 98333 44556",
        rating: 4.95,
        totalTrips: 4520,
        vehicleModel: "Hyundai Verna",
        vehicleColor: "Silver",
        vehiclePlate: "DL 3C BC 8812",
        vehicleCategory: "PREMIER",
        latitude: 28.4645,
        longitude: 77.0280,
        isAvailable: true
      }
    }),
    prisma.driver.create({
      data: {
        name: "Rohit Deshmukh",
        phone: "+91 98444 55667",
        rating: 4.85,
        totalTrips: 1820,
        vehicleModel: "Honda Activa 6G",
        vehicleColor: "Black",
        vehiclePlate: "HR 26 BY 7712",
        vehicleCategory: "MOTO",
        latitude: 28.4620,
        longitude: 77.0305,
        isAvailable: true
      }
    }),
    prisma.driver.create({
      data: {
        name: "Manpreet Singh",
        phone: "+91 98555 66778",
        rating: 4.97,
        totalTrips: 5120,
        vehicleModel: "Toyota Innova Crysta",
        vehicleColor: "Pearl White",
        vehiclePlate: "DL 1Y CA 4421",
        vehicleCategory: "XL",
        latitude: 28.4650,
        longitude: 77.0270,
        isAvailable: true
      }
    })
  ]);

  console.log(`🚗 Seeded ${drivers.length} drivers.`);

  // 4. Create Restaurants and Menus
  const r1 = await prisma.restaurant.create({
    data: {
      name: "Bahar Biryani & Kebab House",
      cuisine: "Hyderabadi Biryani, Mughlai, Kebabs",
      rating: 4.8,
      ratingCount: "2.4k+",
      deliveryTimeMinutes: 24,
      priceForTwo: 450,
      distanceKm: 2.4,
      isPureVeg: false,
      offerText: "50% OFF up to ₹100",
      imageKey: "img_biryani",
      menuItems: {
        create: [
          { name: "Hyderabadi Chicken Dum Biryani", description: "Aromatic long-grain basmati rice cooked with succulent chicken and authentic spices", price: 320.0, isVeg: false, category: "Biryani", isBestseller: true, imageKey: "img_chicken_biryani" },
          { name: "Special Mutton Biryani", description: "Tender mutton slow cooked in traditional handi with saffron rice", price: 440.0, isVeg: false, category: "Biryani", isBestseller: true, imageKey: "img_mutton_biryani" },
          { name: "Paneer Tikka Biryani", description: "Marinated cottage cheese cubes layered with spiced biryani rice", price: 280.0, isVeg: true, category: "Biryani", isBestseller: false, imageKey: "img_paneer_biryani" },
          { name: "Galouti Kebab (4 Pcs)", description: "Melt-in-mouth minced mutton kebabs served with mint chutney", price: 260.0, isVeg: false, category: "Kebabs", isBestseller: true, imageKey: "img_galouti_kebab" },
          { name: "Butter Naan", description: "Traditional clay oven baked flatbread with rich butter", price: 50.0, isVeg: true, category: "Breads", isBestseller: false, imageKey: "img_butter_naan" }
        ]
      }
    }
  });

  const r2 = await prisma.restaurant.create({
    data: {
      name: "Punjabi Rasoi & Dhaba",
      cuisine: "North Indian, Thalis, Tandoori",
      rating: 4.6,
      ratingCount: "5.1k+",
      deliveryTimeMinutes: 30,
      priceForTwo: 350,
      distanceKm: 1.8,
      isPureVeg: true,
      offerText: "Flat ₹75 OFF on ₹299+",
      imageKey: "img_north_indian",
      menuItems: {
        create: [
          { name: "Dal Makhani Deluxe", description: "Slow cooked black lentils simmered overnight with cream and white butter", price: 240.0, isVeg: true, category: "Main Course", isBestseller: true, imageKey: "img_dal_makhani" },
          { name: "Paneer Butter Masala", description: "Cottage cheese cubes cooked in rich rich tomato butter gravy", price: 270.0, isVeg: true, category: "Main Course", isBestseller: true, imageKey: "img_paneer_masala" },
          { name: "Garlic Butter Naan", description: "Freshly baked tandoori naan topped with chopped garlic and butter", price: 60.0, isVeg: true, category: "Breads", isBestseller: false, imageKey: "img_garlic_naan" },
          { name: "Special Executive Veg Thali", description: "Dal Makhani, Shahi Paneer, Mix Veg, 2 Naan, Jeera Rice, Gulab Jamun", price: 310.0, isVeg: true, category: "Thalis", isBestseller: true, imageKey: "img_veg_thali" }
        ]
      }
    }
  });

  const r3 = await prisma.restaurant.create({
    data: {
      name: "Toscana Pizzeria",
      cuisine: "Woodfired Pizza, Italian, Pasta",
      rating: 4.7,
      ratingCount: "1.8k+",
      deliveryTimeMinutes: 28,
      priceForTwo: 600,
      distanceKm: 3.2,
      isPureVeg: false,
      offerText: "Free Garlic Bread on orders > ₹499",
      imageKey: "img_pizza",
      menuItems: {
        create: [
          { name: "Classic Margherita (11\")", description: "San Marzano tomato sauce, fresh mozzarella, and aromatic basil", price: 380.0, isVeg: true, category: "Pizzas", isBestseller: true, imageKey: "img_margherita_pizza" },
          { name: "Pepperoni & Spicy Jalapeño (11\")", description: "Italian pepperoni, jalapeños, chili flakes and extra mozzarella", price: 490.0, isVeg: false, category: "Pizzas", isBestseller: true, imageKey: "img_pepperoni_pizza" },
          { name: "Creamy Truffle Mushroom Pasta", description: "Penne tossed in creamy wild mushroom sauce with black truffle oil", price: 390.0, isVeg: true, category: "Pasta", isBestseller: false, imageKey: "img_truffle_pasta" },
          { name: "Cheesy Garlic Crust Sticks", description: "Baked dough sticks brushed with garlic butter and melted mozzarella", price: 180.0, isVeg: true, category: "Sides", isBestseller: false, imageKey: "img_garlic_bread" }
        ]
      }
    }
  });

  const r4 = await prisma.restaurant.create({
    data: {
      name: "Sagar Ratna South Express",
      cuisine: "South Indian, Dosa, Filter Coffee",
      rating: 4.9,
      ratingCount: "8.2k+",
      deliveryTimeMinutes: 18,
      priceForTwo: 250,
      distanceKm: 1.2,
      isPureVeg: true,
      offerText: "20% OFF on Morning Breakfast",
      imageKey: "img_south_indian",
      menuItems: {
        create: [
          { name: "Mysore Masala Dosa", description: "Crispy dosa smeared with red spicy garlic chutney and potato masala filling", price: 160.0, isVeg: true, category: "Dosa", isBestseller: true, imageKey: "img_masala_dosa" },
          { name: "Steamed Ghee Idli (2 Pcs) & Vada (1 Pc)", description: "Fluffy rice cakes and crispy lentil donut served with sambar and coconut chutney", price: 130.0, isVeg: true, category: "Breakfast", isBestseller: true, imageKey: "img_idli_vada" },
          { name: "Authentic Kumbakonam Filter Coffee", description: "Traditional strong South Indian decoction coffee brewed with hot milk", price: 60.0, isVeg: true, category: "Beverages", isBestseller: true, imageKey: "img_filter_coffee" }
        ]
      }
    }
  });

  console.log('🍕 Seeded 4 restaurants with full menus.');

  // 5. Create Initial Activity Records for Demo User
  await prisma.activityRecord.createMany({
    data: [
      {
        userId: demoUser.id,
        title: "Ride to Cyber City",
        serviceType: "CAB",
        dateFormatted: "Today, 9:15 AM",
        priceFormatted: "₹160.00",
        status: "Completed",
        startLocation: "Sector 54, Gurugram",
        endLocation: "Cyber City Phase 2",
        imageKey: "img_cab_go"
      },
      {
        userId: demoUser.id,
        title: "Bahar Biryani Order",
        serviceType: "FOOD",
        dateFormatted: "Yesterday, 8:45 PM",
        priceFormatted: "₹370.00",
        status: "Delivered",
        startLocation: "Bahar Biryani House",
        endLocation: "Home - Sector 54",
        itemsSummary: "1x Hyderabadi Chicken Biryani, 1x Butter Naan",
        imageKey: "img_chicken_biryani"
      },
      {
        userId: demoUser.id,
        title: "Document Parcel Express",
        serviceType: "PARCEL",
        dateFormatted: "07 Oct, 3:20 PM",
        priceFormatted: "₹120.00",
        status: "Delivered",
        startLocation: "DLF Phase 4",
        endLocation: "Golf Course Ext Rd",
        itemsSummary: "Legal Documents (0.4 kg)",
        imageKey: "img_service_parcel"
      },
      {
        userId: demoUser.id,
        title: "Moto Ride to Metro Station",
        serviceType: "BIKE",
        dateFormatted: "05 Oct, 6:10 PM",
        priceFormatted: "₹55.00",
        status: "Completed",
        startLocation: "Galleria Market",
        endLocation: "HUDA City Centre Metro",
        imageKey: "img_moto"
      }
    ]
  });

  console.log('📋 Seeded user activities.');
  console.log('✅ Database seeded successfully!');
}

main()
  .catch((e) => {
    console.error('❌ Seeding error:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
