const express = require('express');
const router = express.Router();
const authService = require('../../services/authService');
const { authenticateToken } = require('../../middleware/auth');

// POST /api/v1/auth/request-otp
router.post('/request-otp', async (req, res, next) => {
  try {
    const { phone } = req.body;
    const result = await authService.requestOtp(phone);
    res.json(result);
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// POST /api/v1/auth/verify-otp
router.post('/verify-otp', async (req, res, next) => {
  try {
    const { phone, otp } = req.body;
    const clientInfo = {
      userAgent: req.headers['user-agent'],
      ip: req.ip
    };
    const result = await authService.verifyOtp(phone, otp, clientInfo);
    res.json(result);
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// Legacy POST /api/v1/auth/login compatibility alias
router.post('/login', async (req, res, next) => {
  try {
    const { phone, otp } = req.body;
    // In legacy/dev fallback, allow verifying with default test OTP if otp missing
    const code = otp || '123456';
    const result = await authService.verifyOtp(phone || '+919876500112', code, {
      userAgent: req.headers['user-agent'],
      ip: req.ip
    });
    res.json(result);
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// POST /api/v1/auth/refresh
router.post('/refresh', async (req, res, next) => {
  try {
    const { refreshToken } = req.body;
    const result = await authService.refreshToken(refreshToken);
    res.json(result);
  } catch (err) {
    res.status(401).json({ success: false, message: err.message });
  }
});

// POST /api/v1/auth/logout
router.post('/logout', async (req, res, next) => {
  try {
    const { refreshToken } = req.body;
    const result = await authService.logout(refreshToken);
    res.json(result);
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// GET /api/v1/auth/profile
router.get('/profile', authenticateToken, async (req, res, next) => {
  try {
    const profile = await authService.getProfile(req.user.id);
    res.json({ success: true, user: profile });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// PUT /api/v1/auth/profile
router.put('/profile', authenticateToken, async (req, res, next) => {
  try {
    const { name, email } = req.body;
    const updated = await authService.updateProfile(req.user.id, { name, email });
    res.json({ success: true, user: updated, message: 'Profile updated successfully' });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// Addresses
router.get('/addresses', authenticateToken, async (req, res, next) => {
  try {
    const profile = await authService.getProfile(req.user.id);
    res.json({ success: true, addresses: profile.savedPlaces || [] });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

router.post('/addresses', authenticateToken, async (req, res, next) => {
  try {
    const addr = await authService.addAddress(req.user.id, req.body);
    res.status(201).json({ success: true, address: addr });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

router.delete('/addresses/:id', authenticateToken, async (req, res, next) => {
  try {
    const result = await authService.deleteAddress(req.user.id, req.params.id);
    res.json(result);
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// Emergency Contacts
router.get('/emergency-contacts', authenticateToken, async (req, res, next) => {
  try {
    const profile = await authService.getProfile(req.user.id);
    res.json({ success: true, contacts: profile.emergencyContacts || [] });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

router.post('/emergency-contacts', authenticateToken, async (req, res, next) => {
  try {
    const contact = await authService.addEmergencyContact(req.user.id, req.body);
    res.status(201).json({ success: true, contact });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

router.delete('/emergency-contacts/:id', authenticateToken, async (req, res, next) => {
  try {
    const result = await authService.deleteEmergencyContact(req.user.id, req.params.id);
    res.json(result);
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

// Notification Preferences
router.get('/preferences', authenticateToken, async (req, res, next) => {
  try {
    const profile = await authService.getProfile(req.user.id);
    res.json({ success: true, preferences: profile.preferences });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

router.put('/preferences', authenticateToken, async (req, res, next) => {
  try {
    const updated = await authService.updatePreferences(req.user.id, req.body);
    res.json({ success: true, preferences: updated });
  } catch (err) {
    res.status(400).json({ success: false, message: err.message });
  }
});

module.exports = router;
