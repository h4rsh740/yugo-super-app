const express = require('express');
const router = express.Router();
const prisma = require('../../lib/db');
const { optionalAuth } = require('../../middleware/auth');

const defaultLocations = [
  { id: "1", title: "Cyber City", address: "DLF Phase 2, Gurugram, Haryana", latitude: 28.4950, longitude: 77.0895, type: "WORK" },
  { id: "2", title: "Home", address: "Sector 54, Golf Course Road, Gurugram", latitude: 28.4382, longitude: 77.1082, type: "HOME" },
  { id: "3", title: "IGI Airport T3", address: "Indira Gandhi International Airport, New Delhi", latitude: 28.5562, longitude: 77.1000, type: "AIRPORT" },
  { id: "4", title: "Connaught Place", address: "Inner Circle, New Delhi", latitude: 28.6315, longitude: 77.2167, type: "POPULAR" },
  { id: "5", title: "Galleria Market", address: "DLF Phase 4, Gurugram, Haryana", latitude: 28.4674, longitude: 77.0825, type: "RECENT" }
];

// Simple in-memory cache to keep within polite OSM request rates
const nominatimCache = new Map();
const routeCache = new Map();

// GET /api/v1/locations/recent
router.get('/recent', optionalAuth, async (req, res) => {
  try {
    let saved = [];
    if (req.user?.id) {
      const addresses = await prisma.address.findMany({
        where: { userId: req.user.id }
      });
      saved = addresses.map(a => ({
        id: a.id,
        title: a.title,
        address: a.address,
        latitude: a.latitude || 28.4630,
        longitude: a.longitude || 77.0295,
        type: a.label
      }));
    }

    const locations = saved.length > 0 ? saved.concat(defaultLocations) : defaultLocations;
    res.json({
      success: true,
      count: locations.length,
      locations
    });
  } catch (err) {
    res.json({ success: true, count: defaultLocations.length, locations: defaultLocations });
  }
});

// GET /api/v1/locations/search?q=query
// Free Nominatim Geocoding API with OpenStreetMap
router.get('/search', async (req, res) => {
  const query = (req.query.q || '').trim();
  if (!query) {
    return res.json({ success: true, count: defaultLocations.length, locations: defaultLocations });
  }

  const cacheKey = query.toLowerCase();
  if (nominatimCache.has(cacheKey)) {
    return res.json({ success: true, count: nominatimCache.get(cacheKey).length, locations: nominatimCache.get(cacheKey) });
  }

  try {
    const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&limit=6&countrycodes=in`;
    const response = await fetch(url, {
      headers: {
        'User-Agent': 'YugoSuperApp/1.0 (support@yugo.app)',
        'Accept': 'application/json'
      }
    });

    if (!response.ok) throw new Error(`Nominatim HTTP ${response.status}`);
    const data = await response.json();

    const results = data.map((item, index) => {
      const parts = item.display_name.split(',').map(s => s.trim());
      const title = item.name || parts[0];
      const address = parts.length > 1 ? parts.slice(1, 4).join(', ') : item.display_name;

      return {
        id: `osm-${item.place_id || index}`,
        title,
        address,
        latitude: parseFloat(item.lat),
        longitude: parseFloat(item.lon),
        type: index === 0 ? "POPULAR" : "RECENT"
      };
    });

    const finalResults = results.length > 0 ? results : defaultLocations.filter(l =>
      l.title.toLowerCase().includes(query.toLowerCase()) || l.address.toLowerCase().includes(query.toLowerCase())
    );

    nominatimCache.set(cacheKey, finalResults);
    // Keep cache size bounded
    if (nominatimCache.size > 200) {
      const oldestKey = nominatimCache.keys().next().value;
      nominatimCache.delete(oldestKey);
    }

    res.json({
      success: true,
      count: finalResults.length,
      locations: finalResults
    });
  } catch (err) {
    // Graceful offline fallback
    const fallback = defaultLocations.filter(l =>
      l.title.toLowerCase().includes(query.toLowerCase()) || l.address.toLowerCase().includes(query.toLowerCase())
    );
    res.json({
      success: true,
      count: fallback.length,
      locations: fallback.length > 0 ? fallback : defaultLocations
    });
  }
});

// GET /api/v1/locations/route?fromLat=...&fromLng=...&toLat=...&toLng=...
// Free OSRM (Open Source Routing Machine) Driving Route
router.get('/route', async (req, res) => {
  const fromLat = parseFloat(req.query.fromLat || 28.4820);
  const fromLng = parseFloat(req.query.fromLng || 77.0835);
  const toLat = parseFloat(req.query.toLat || 28.4960);
  const toLng = parseFloat(req.query.toLng || 77.0910);

  const cacheKey = `${fromLat.toFixed(4)},${fromLng.toFixed(4)}_${toLat.toFixed(4)},${toLng.toFixed(4)}`;
  if (routeCache.has(cacheKey)) {
    return res.json(routeCache.get(cacheKey));
  }

  try {
    const url = `https://router.project-osrm.org/route/v1/driving/${fromLng},${fromLat};${toLng},${toLat}?overview=full&geometries=geojson`;
    const response = await fetch(url, {
      headers: {
        'User-Agent': 'YugoSuperApp/1.0',
        'Accept': 'application/json'
      }
    });

    if (!response.ok) throw new Error(`OSRM HTTP ${response.status}`);
    const data = await response.json();

    if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
      const bestRoute = data.routes[0];
      const distanceKm = +(bestRoute.distance / 1000).toFixed(2);
      const durationMins = Math.round(bestRoute.duration / 60);

      // OSRM returns coordinates as [lon, lat]. Convert to [lat, lng] for Android/Leaflet.
      const coordinates = (bestRoute.geometry?.coordinates || []).map(coord => [coord[1], coord[0]]);

      const result = {
        success: true,
        distanceKm,
        durationMinutes: durationMins,
        coordinates,
        summary: bestRoute.legs?.[0]?.summary || ""
      };

      routeCache.set(cacheKey, result);
      return res.json(result);
    }
    throw new Error('OSRM route not found');
  } catch (err) {
    // Fallback: Calculate approximate road distance & straight line coordinates
    const degToRad = Math.PI / 180;
    const dLat = (toLat - fromLat) * degToRad;
    const dLng = (toLng - fromLng) * degToRad;
    const a = Math.sin(dLat / 2) ** 2 + Math.cos(fromLat * degToRad) * Math.cos(toLat * degToRad) * Math.sin(dLng / 2) ** 2;
    const haversineDist = 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distanceKm = +(haversineDist * 1.25).toFixed(2); // 1.25x road factor

    const fallbackResult = {
      success: true,
      distanceKm,
      durationMinutes: Math.max(5, Math.round(distanceKm * 2.8)),
      coordinates: [
        [fromLat, fromLng],
        [fromLat + (toLat - fromLat) * 0.33, fromLng + (toLng - fromLng) * 0.33],
        [fromLat + (toLat - fromLat) * 0.66, fromLng + (toLng - fromLng) * 0.66],
        [toLat, toLng]
      ],
      summary: "Direct Road Route"
    };

    res.json(fallbackResult);
  }
});

// GET /api/v1/locations/reverse?lat=...&lng=...
// Free Nominatim Reverse Geocoding
router.get('/reverse', async (req, res) => {
  const lat = parseFloat(req.query.lat);
  const lng = parseFloat(req.query.lng);

  if (isNaN(lat) || isNaN(lng)) {
    return res.status(400).json({ success: false, message: "Valid lat and lng required" });
  }

  try {
    const url = `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`;
    const response = await fetch(url, {
      headers: {
        'User-Agent': 'YugoSuperApp/1.0 (support@yugo.app)',
        'Accept': 'application/json'
      }
    });

    if (!response.ok) throw new Error(`Nominatim reverse HTTP ${response.status}`);
    const data = await response.json();

    const parts = (data.display_name || "").split(',').map(s => s.trim());
    res.json({
      success: true,
      displayName: data.display_name,
      title: data.name || parts[0] || "Selected Location",
      address: parts.slice(1, 4).join(', ') || data.display_name,
      latitude: lat,
      longitude: lng
    });
  } catch (err) {
    res.json({
      success: true,
      displayName: "Sector 54, Gurugram, Haryana",
      title: "Current Location",
      address: "Sector 54, Golf Course Road, Gurugram",
      latitude: lat,
      longitude: lng
    });
  }
});

module.exports = router;
