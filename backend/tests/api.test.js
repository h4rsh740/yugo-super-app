const request = require('supertest');
const { app } = require('../server');
const prisma = require('../lib/db');

describe('YUGO Super App API Integration Tests', () => {
  let authToken;
  let refreshToken;
  let testUser;
  let testRestaurant;
  let testMenuItem;

  beforeAll(async () => {
    testUser = await prisma.user.findFirst({ where: { phone: '+919876500112' } });
    testRestaurant = await prisma.restaurant.findFirst({ include: { menuItems: true } });
    if (testRestaurant && testRestaurant.menuItems.length > 0) {
      testMenuItem = testRestaurant.menuItems[0];
    }
  });

  afterAll(async () => {
    await prisma.$disconnect();
  });

  test('GET /api/health returns healthy and database connected', async () => {
    const res = await request(app).get('/api/health');
    expect(res.statusCode).toBe(200);
    expect(res.body.status).toBe('healthy');
    expect(res.body.database).toBe('connected');
  });

  test('Auth flow: Request OTP and Verify OTP with test code', async () => {
    const phone = '+919876500999';

    // 1. Request OTP
    const reqRes = await request(app)
      .post('/api/v1/auth/request-otp')
      .send({ phone });

    expect(reqRes.statusCode).toBe(200);
    expect(reqRes.body.success).toBe(true);
    expect(reqRes.body.testOtp).toBe('123456');

    // 2. Verify OTP
    const verifyRes = await request(app)
      .post('/api/v1/auth/verify-otp')
      .send({ phone, otp: '123456' });

    expect(verifyRes.statusCode).toBe(200);
    expect(verifyRes.body.success).toBe(true);
    expect(verifyRes.body.accessToken).toBeDefined();
    expect(verifyRes.body.refreshToken).toBeDefined();
    expect(verifyRes.body.user).toBeDefined();

    authToken = verifyRes.body.accessToken;
    refreshToken = verifyRes.body.refreshToken;
  });

  test('Refresh Token flow exchanges refresh token for new access token', async () => {
    const res = await request(app)
      .post('/api/v1/auth/refresh')
      .send({ refreshToken });

    expect(res.statusCode).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.accessToken).toBeDefined();
  });

  test('Protected endpoint: GET /api/v1/auth/profile requires valid Bearer token', async () => {
    // Without token -> 401
    const unauthRes = await request(app).get('/api/v1/auth/profile');
    expect(unauthRes.statusCode).toBe(401);

    // With token -> 200
    const authRes = await request(app)
      .get('/api/v1/auth/profile')
      .set('Authorization', `Bearer ${authToken}`);

    expect(authRes.statusCode).toBe(200);
    expect(authRes.body.success).toBe(true);
    expect(authRes.body.user).toBeDefined();
  });

  test('POST /api/v1/rides/book creates ride and driver assignment', async () => {
    const res = await request(app)
      .post('/api/v1/rides/book')
      .set('Authorization', `Bearer ${authToken}`)
      .send({
        pickup: { title: "Sector 54, Gurugram", latitude: 28.4382, longitude: 77.1082 },
        destination: { title: "Cyber City, DLF Phase 2", latitude: 28.4950, longitude: 77.0895 },
        rideOption: { category: "GO", name: "YUGO Go", fareAmount: 160.0 },
        paymentMethod: "Cash"
      });

    expect(res.statusCode).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.booking).toBeDefined();
    expect(res.body.booking.bookingId).toBeDefined();
    expect(res.body.booking.status).toBe('DRIVER_ASSIGNED');
    expect(res.body.booking.otpPin).toBeDefined();
  });

  test('POST /api/v1/food/orders places order with validated bill calculation', async () => {
    if (!testRestaurant || !testMenuItem) return;

    const res = await request(app)
      .post('/api/v1/food/orders')
      .set('Authorization', `Bearer ${authToken}`)
      .send({
        restaurantId: testRestaurant.id,
        items: [{ menuItemId: testMenuItem.id, quantity: 2 }],
        deliveryAddress: "DLF Cyber City, Tower B, Gurugram",
        paymentMethod: "Online"
      });

    expect(res.statusCode).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.order).toBeDefined();
    expect(res.body.order.status).toBe('CONFIRMED');
    expect(res.body.order.totalAmount).toBeGreaterThan(0);
  });

  test('POST /api/v1/parcel/book books parcel and assigns courier with tracking ID', async () => {
    const res = await request(app)
      .post('/api/v1/parcel/book')
      .set('Authorization', `Bearer ${authToken}`)
      .send({
        recipientName: "Aditya Sharma",
        recipientPhone: "+919876512345",
        recipientAddress: "DLF Cyber City, Tower B",
        weightKg: 1.5,
        category: "DOCUMENTS",
        instructions: "Call on arrival"
      });

    expect(res.statusCode).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.parcel).toBeDefined();
    expect(res.body.parcel.trackingId).toMatch(/^PARCEL-\d{6}$/);
    expect(res.body.parcel.status).toBe('RIDER_ASSIGNED');
    expect(res.body.parcel.deliveryPin).toBeDefined();
  });

  test('GET /api/v1/activities returns persistent activity records', async () => {
    const res = await request(app)
      .get('/api/v1/activities')
      .set('Authorization', `Bearer ${authToken}`);

    expect(res.statusCode).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.activities.length).toBeGreaterThan(0);
  });
});
