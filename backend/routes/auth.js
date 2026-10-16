const express = require('express');
const router = express.Router();

let currentUser = {
  id: "usr_9021",
  name: "Harsh Singh",
  phoneNumber: "+91 98765 00112",
  email: "harsh@yugo.app",
  rating: 4.95,
  yugoCashBalance: 450.00,
  savedPlaces: [
    { name: "Home", address: "Sector 54, Golf Course Road, Gurugram" },
    { name: "Work", address: "DLF Cyber City, Tower B, Gurugram" }
  ]
};

// GET /api/auth/profile
router.get('/profile', (req, res) => {
  res.json({ success: true, user: currentUser });
});

// POST /api/auth/login
router.post('/login', (req, res) => {
  const { phone, password } = req.body;
  res.json({
    success: true,
    message: "Login successful",
    token: `jwt_yugo_${Date.now()}`,
    user: currentUser
  });
});

module.exports = router;
