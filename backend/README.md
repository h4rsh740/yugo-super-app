# YUGO Backend API Server

High-performance Express REST API powering the **YUGO** full-stack Android super application.

---

## ⚡ Quick Start

### 1. Install Dependencies
```bash
cd backend
npm install
```

### 2. Run Server
```bash
# Production mode
npm start

# Development mode with file watching
npm run dev
```

The API will start on `http://localhost:8080`.

- Local host: `http://localhost:8080`
- Android Emulator: `http://10.0.2.2:8080`
- Health check: `http://localhost:8080/api/health`

---

## 📡 API Endpoints

### 🩺 System Health
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Service status, timestamp, and version check |

### 🚖 Rides & Mobility
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/rides/options?distanceKm=5.8` | Get ride categories (Auto, Moto, Go, Premier, XL) with calculated fares & ETAs |
| `GET` | `/api/rides/drivers/nearby?lat=28.4&lng=77.0` | Get real-time simulated drivers around given GPS coordinates |
| `POST` | `/api/rides/book` | Book a ride, assign a nearby driver, generate OTP PIN, and record activity |
| `GET` | `/api/rides/status/:bookingId` | Check status of active ride |

### 🍔 Food Delivery
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/food/restaurants?cuisine=Biryani&search=Dum` | List curated restaurants with filter & search |
| `GET` | `/api/food/restaurants/:id` | Get restaurant menu items, categories, ratings, and dish image keys |
| `POST` | `/api/food/orders` | Place food delivery order, calculate totals, and add to user activity |
| `GET` | `/api/food/orders/:id` | Track order preparation & delivery status |

### 📦 Hyperlocal Parcel Courier
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/parcel/estimate` | Calculate fare by parcel weight and distance |
| `POST` | `/api/parcel/book` | Book courier pickup with recipient details, assign rider, record in activity |
| `GET` | `/api/parcel/:trackingId` | Track active package delivery status |

### 🕒 Activity & History
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/activities` | Chronological list of trips, food orders, and parcel deliveries |

### 📍 Locations
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/locations/recent` | Saved home, work, airport, and recent destinations |
