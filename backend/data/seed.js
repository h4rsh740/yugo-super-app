const recentLocations = [
  { id: "1", title: "Cyber City", address: "DLF Phase 2, Gurugram, Haryana", latitude: 28.4950, longitude: 77.0895, type: "WORK" },
  { id: "2", title: "Home", address: "Sector 54, Golf Course Road, Gurugram", latitude: 28.4382, longitude: 77.1082, type: "HOME" },
  { id: "3", title: "IGI Airport T3", address: "Indira Gandhi International Airport, New Delhi", latitude: 28.5562, longitude: 77.1000, type: "AIRPORT" },
  { id: "4", title: "Connaught Place", address: "Inner Circle, New Delhi", latitude: 28.6315, longitude: 77.2167, type: "POPULAR" },
  { id: "5", title: "Galleria Market", address: "DLF Phase 4, Gurugram, Haryana", latitude: 28.4674, longitude: 77.0825, type: "RECENT" }
];

const sampleDriver = {
  id: "drv_1092",
  name: "Rajesh Sharma",
  rating: 4.92,
  totalTrips: 3840,
  vehicleModel: "Maruti Suzuki Dzire (White)",
  vehicleColor: "White",
  plateNumber: "HR 26 DQ 4921",
  phoneNumber: "+91 98765 43210",
  otpPin: "4819",
  latitude: 28.4630,
  longitude: 77.0295
};

const nearbyDrivers = [
  sampleDriver,
  {
    id: "drv_2041",
    name: "Suresh Kumar",
    rating: 4.88,
    totalTrips: 2190,
    vehicleModel: "Bajaj RE Auto (Yellow/Green)",
    vehicleColor: "Yellow/Green",
    plateNumber: "HR 55 AT 1109",
    phoneNumber: "+91 98111 22334",
    otpPin: "9102",
    latitude: 28.4610,
    longitude: 77.0315
  },
  {
    id: "drv_3108",
    name: "Amit Verma",
    rating: 4.95,
    totalTrips: 4520,
    vehicleModel: "Hyundai Verna (Silver)",
    vehicleColor: "Silver",
    plateNumber: "DL 3C BC 8812",
    phoneNumber: "+91 98333 44556",
    otpPin: "3184",
    latitude: 28.4645,
    longitude: 77.0280
  }
];

const restaurants = [
  {
    id: "rest_1",
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
    menuItems: [
      { id: "m1", name: "Hyderabadi Chicken Dum Biryani", description: "Aromatic long-grain basmati rice cooked with succulent chicken and authentic spices", price: 320.0, isVeg: false, category: "Biryani", isBestseller: true, imageKey: "img_chicken_biryani" },
      { id: "m2", name: "Special Mutton Biryani", description: "Tender mutton slow cooked in traditional handi with saffron rice", price: 440.0, isVeg: false, category: "Biryani", isBestseller: true, imageKey: "img_mutton_biryani" },
      { id: "m3", name: "Paneer Tikka Biryani", description: "Marinated cottage cheese cubes layered with spiced biryani rice", price: 280.0, isVeg: true, category: "Biryani", isBestseller: false, imageKey: "img_paneer_biryani" },
      { id: "m4", name: "Galouti Kebab (4 Pcs)", description: "Melt-in-mouth minced mutton kebabs served with mint chutney", price: 260.0, isVeg: false, category: "Kebabs", isBestseller: true, imageKey: "img_galouti_kebab" },
      { id: "m5", name: "Butter Naan", description: "Traditional clay oven baked flatbread with rich butter", price: 50.0, isVeg: true, category: "Breads", isBestseller: false, imageKey: "img_butter_naan" }
    ]
  },
  {
    id: "rest_2",
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
    menuItems: [
      { id: "m6", name: "Dal Makhani Deluxe", description: "Slow cooked black lentils simmered overnight with cream and white butter", price: 240.0, isVeg: true, category: "Main Course", isBestseller: true, imageKey: "img_dal_makhani" },
      { id: "m7", name: "Paneer Butter Masala", description: "Cottage cheese cubes cooked in rich rich tomato butter gravy", price: 270.0, isVeg: true, category: "Main Course", isBestseller: true, imageKey: "img_paneer_masala" },
      { id: "m8", name: "Garlic Butter Naan", description: "Freshly baked tandoori naan topped with chopped garlic and butter", price: 60.0, isVeg: true, category: "Breads", isBestseller: false, imageKey: "img_garlic_naan" },
      { id: "m9", name: "Special Executive Veg Thali", description: "Dal Makhani, Shahi Paneer, Mix Veg, 2 Naan, Jeera Rice, Gulab Jamun", price: 310.0, isVeg: true, category: "Thalis", isBestseller: true, imageKey: "img_veg_thali" }
    ]
  },
  {
    id: "rest_3",
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
    menuItems: [
      { id: "m10", name: "Classic Margherita (11\")", description: "San Marzano tomato sauce, fresh mozzarella, and aromatic basil", price: 380.0, isVeg: true, category: "Pizzas", isBestseller: true, imageKey: "img_margherita_pizza" },
      { id: "m11", name: "Pepperoni & Spicy Jalapeño (11\")", description: "Italian pepperoni, jalapeños, chili flakes and extra mozzarella", price: 490.0, isVeg: false, category: "Pizzas", isBestseller: true, imageKey: "img_pepperoni_pizza" },
      { id: "m12", name: "Creamy Truffle Mushroom Pasta", description: "Penne tossed in creamy wild mushroom sauce with black truffle oil", price: 390.0, isVeg: true, category: "Pasta", isBestseller: false, imageKey: "img_truffle_pasta" },
      { id: "m13", name: "Cheesy Garlic Crust Sticks", description: "Baked dough sticks brushed with garlic butter and melted mozzarella", price: 180.0, isVeg: true, category: "Sides", isBestseller: false, imageKey: "img_garlic_bread" }
    ]
  },
  {
    id: "rest_4",
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
    menuItems: [
      { id: "m14", name: "Mysore Masala Dosa", description: "Crispy dosa smeared with red spicy garlic chutney and potato masala filling", price: 160.0, isVeg: true, category: "Dosa", isBestseller: true, imageKey: "img_masala_dosa" },
      { id: "m15", name: "Steamed Ghee Idli (2 Pcs) & Vada (1 Pc)", description: "Fluffy rice cakes and crispy lentil donut served with sambar and coconut chutney", price: 130.0, isVeg: true, category: "Breakfast", isBestseller: true, imageKey: "img_idli_vada" },
      { id: "m16", name: "Authentic Kumbakonam Filter Coffee", description: "Traditional strong South Indian decoction coffee brewed with hot milk", price: 60.0, isVeg: true, category: "Beverages", isBestseller: true, imageKey: "img_filter_coffee" }
    ]
  }
];

let activities = [
  { id: "act_1", title: "Ride to Cyber City", serviceType: "CAB", dateFormatted: "Today, 9:15 AM", priceFormatted: "₹160.00", status: "Completed", startLocation: "Sector 54, Gurugram", endLocation: "Cyber City Phase 2", imageKey: "img_cab_go" },
  { id: "act_2", title: "Bahar Biryani Order", serviceType: "FOOD", dateFormatted: "Yesterday, 8:45 PM", priceFormatted: "₹370.00", status: "Delivered", startLocation: "Bahar Biryani House", endLocation: "Home - Sector 54", itemsSummary: "1x Hyderabadi Chicken Biryani, 1x Butter Naan", imageKey: "img_chicken_biryani" },
  { id: "act_3", title: "Document Parcel Express", serviceType: "PARCEL", dateFormatted: "07 Oct, 3:20 PM", priceFormatted: "₹120.00", status: "Delivered", startLocation: "DLF Phase 4", endLocation: "Golf Course Ext Rd", itemsSummary: "Legal Documents (0.4 kg)", imageKey: "img_service_parcel" },
  { id: "act_4", title: "Moto Ride to Metro Station", serviceType: "BIKE", dateFormatted: "05 Oct, 6:10 PM", priceFormatted: "₹55.00", status: "Completed", startLocation: "Galleria Market", endLocation: "HUDA City Centre Metro", imageKey: "img_moto" }
];

let bookings = {};
let foodOrders = {};
let parcelBookings = {};

module.exports = {
  recentLocations,
  sampleDriver,
  nearbyDrivers,
  restaurants,
  activities,
  bookings,
  foodOrders,
  parcelBookings
};
