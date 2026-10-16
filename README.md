# 🚀 YUGO Super App — Full-Stack Platform

YUGO is a production-grade, India-focused **Super App** combining three core daily consumer services in a single unified platform:

1. **Ride Hailing (Uber / Ola style)**: Auto, Bike (Moto), Cab (Go), Premier, and XL with dynamic driver matching, surge pricing, fare estimation, OTP start verification, and live trip progression.
2. **Food Discovery & Delivery (Swiggy / Zomato style)**: Restaurant catalog, customizable menus with veg/non-veg tags, real-time cart bill calculation with 5% GST & packaging fees, coupon codes (`YUGO50`), live order tracking, and reorder functionality.
3. **Hyperlocal Express Parcel Courier (Dunzo / Porter style)**: Weight-tiered pricing (Documents ≤1kg, Small ≤3kg, Medium ≤7kg, Heavy ≤15kg), express delivery surcharges, fragile handling, tracking ID generator, and secure delivery PIN verification.

Built with **Jetpack Compose**, **Material 3**, **Retrofit 2.11**, and a **Node.js / Express REST API** powered by **PostgreSQL 16** and **Prisma ORM**.

---

## 🏗️ Architecture

```mermaid
graph TD
    subgraph Android Client [Jetpack Compose (Android 14+ / SDK 35)]
        UI[Compose Screens & Components] --> VM[ViewModels StateFlow]
        VM --> Repo[YugoRepository]
        Repo --> SessionMgr[SessionManager SharedPreferences]
        Repo --> ApiClient[YugoApiService / Retrofit 2]
        ApiClient --> Interceptor[AuthInterceptor & TokenAuthenticator]
    end

    subgraph Backend Engine [Node.js / Express REST API v1]
        Interceptor == "HTTP REST (10.0.2.2:8080 / localhost:8080)" ==> Router[Express v1 API Routers]
        Router --> AuthMw[JWT Auth Middleware]
        Router --> AuthAPI["/api/v1/auth (OTP, JWT, Profile, Addresses)"]
        Router --> RideAPI["/api/v1/rides (Fare, Match, Book, Track)"]
        Router --> FoodAPI["/api/v1/food (Restaurants, Cart Bill, Orders)"]
        Router --> ParcelAPI["/api/v1/parcel (Estimate, Book, PIN Track)"]
        Router --> PayAPI["/api/v1/payments (Wallet, Test UPI/Card)"]
        Router --> ActAPI["/api/v1/activities (Cross-Service History)"]
        Router --> WS[WebSocket Server /ws/updates]
    end

    subgraph Database Layer [PostgreSQL 16 + Prisma ORM]
        AuthAPI & RideAPI & FoodAPI & ParcelAPI & PayAPI & ActAPI --> Prisma[Prisma ORM Client]
        Prisma --> Postgres[(PostgreSQL yugodb)]
    end
```

---

## 🌟 Key Features & Engineering Highlights

- **Authentication & Security**:
  - Indian mobile number (+91) OTP authentication.
  - Safe local test mode (`123456` OTP) for development; extensible provider interface for SMS gateways.
  - Dual JWT token architecture (short-lived Access Token + long-lived Refresh Token) with automated token rotation and 401 transparent retry in OkHttp Authenticator.
- **Persistent Data Store**:
  - PostgreSQL schema managed with Prisma ORM containing complete relational models (`User`, `Session`, `Driver`, `Ride`, `Restaurant`, `MenuItem`, `FoodOrder`, `ParcelDelivery`, `PaymentTransaction`, `ActivityRecord`).
- **Payment Abstraction**:
  - Provider interface supporting YUGO Cash in-app wallet, local simulated UPI / Card gateway, and pluggable Razorpay / Stripe adapters.
- **Android UI/UX**:
  - 100% Jetpack Compose and Material 3 design system with custom brand tokens (`YugoBlack`, `YugoYellow`, `YugoGreen`, `YugoRed`).
  - Edge-to-edge layout, shimmer loading placeholders, error handling banners, and 33 high-resolution assets for authentic Indian food items and vehicle tiers.
- **Automated Test Coverage**:
  - Backend: 15 integration and unit tests passing in Jest (`tests/api.test.js`, `tests/fare.test.js`, `tests/food.test.js`, `tests/parcel.test.js`).
  - Android: JUnit tests for Fare math, Parcel pricing tiers, Food bill breakdown & taxes, and Phone/OTP validation (`FareCalculationTest`, `ParcelPricingTest`, `BillCalculationTest`, `UserValidationTest`).

---

## 🚀 Getting Started

### Prerequisites
- **Node.js**: v18.0.0 or higher
- **PostgreSQL**: v14.0 or higher (PostgreSQL 16 recommended)
- **Java**: JDK 17
- **Android Studio**: Ladybug / Meerkat (or Gradle 8.7+)

---

### Step 1: Database Setup

1. Start PostgreSQL server on port `5432`:
   ```bash
   # macOS (Homebrew)
   brew services start postgresql@16
   ```

2. Create the database:
   ```bash
   createdb yugodb
   ```

3. Configure environment variables in `backend/.env`:
   ```env
   PORT=8080
   DATABASE_URL="postgresql://localhost:5432/yugodb?schema=public"
   JWT_SECRET="yugo-super-app-secret-jwt-key-2026"
   JWT_REFRESH_SECRET="yugo-super-app-refresh-secret-jwt-key-2026"
   TEST_MODE=true
   ```

4. Push the schema and run database seed:
   ```bash
   cd backend
   npx prisma db push
   node prisma/seed.js
   ```

---

### Step 2: Start Backend Server

```bash
cd backend
npm install
npm start
```
The server will boot on `http://localhost:8080`.
Verify health check:
```bash
curl http://localhost:8080/api/health
```

---

### Step 3: Run Android Application

1. Open the project root in **Android Studio**.
2. Wait for Gradle sync to complete.
3. Launch an Android Emulator (API 26+) or connect a physical device.
   > **Note for Android Emulator**: The app automatically uses `http://10.0.2.2:8080/api/v1/` to connect to your host machine's backend server.
4. Click **Run 'app'** or press **Shift + F10**.

---

## 🧪 Running Automated Tests

### Backend Test Suite
```bash
cd backend
npm test
```
*Executes all 4 test suites (15 tests) including authentication, ride lifecycle, food order GST math, and parcel pricing engine.*

### Android Test Suite
```bash
./gradlew testDebugUnitTest
```
*Executes Android unit tests covering fare calculation, parcel weight tiers, cart taxes, and validation.*

### Build Full Debug APK
```bash
./gradlew assembleDebug
```
*Outputs APK to `app/build/outputs/apk/debug/app-debug.apk`.*

---

## 📡 REST API Reference (`/api/v1`)

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `/api/health` | GET | No | Service health check |
| `/api/v1/auth/request-otp` | POST | No | Request 6-digit phone OTP (`123456` in dev) |
| `/api/v1/auth/verify-otp` | POST | No | Verify OTP & receive JWT access + refresh tokens |
| `/api/v1/auth/refresh` | POST | No | Refresh expired access token |
| `/api/v1/auth/profile` | GET / PUT | Bearer | Fetch or update user profile |
| `/api/v1/auth/addresses` | POST / DELETE | Bearer | Manage saved home/work addresses |
| `/api/v1/rides/fare-estimate` | POST | No | Calculate fare across Auto, Moto, Go, Premier, XL |
| `/api/v1/rides/book` | POST | Optional | Request a ride & match available driver |
| `/api/v1/rides/:id` | GET | Optional | Get live ride status & driver location |
| `/api/v1/rides/:id/cancel` | POST | Optional | Cancel ongoing ride |
| `/api/v1/food/restaurants` | GET | No | Discovery catalog with cuisine & search filters |
| `/api/v1/food/calculate-bill` | POST | Optional | Calculate cart subtotal, 5% GST, packaging, & discounts |
| `/api/v1/food/orders` | POST | Optional | Place food order with payment deduction |
| `/api/v1/food/orders/:id` | GET | Optional | Live food delivery tracking & order status |
| `/api/v1/parcel/estimate` | POST | No | Estimate parcel shipping price by weight & distance |
| `/api/v1/parcel/book` | POST | Optional | Book express parcel courier with tracking ID |
| `/api/v1/parcel/track/:id` | GET | Optional | Track parcel stage & view delivery PIN |
| `/api/v1/payments/wallet/topup`| POST | Bearer | Top up YUGO Cash wallet using test gateway |
| `/api/v1/activities` | GET | Optional | Cross-service user activity history |

---

## 📄 License
MIT License. Created for the YUGO Super App platform.
