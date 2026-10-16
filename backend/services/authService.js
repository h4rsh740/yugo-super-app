const jwt = require('jsonwebtoken');
const prisma = require('../lib/db');

const JWT_SECRET = process.env.JWT_SECRET || 'yugo_jwt_access_secret_dev_382910';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'yugo_jwt_refresh_secret_dev_991823';
const ACCESS_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '15m';
const REFRESH_EXPIRES_IN = process.env.JWT_REFRESH_EXPIRES_IN || '30d';
const DEV_TEST_OTP = process.env.DEV_TEST_OTP || '123456';

function normalizePhone(input) {
  if (!input) return '';
  const digits = input.replace(/\D/g, '');
  if (digits.length === 10) {
    return `+91${digits}`;
  }
  if (digits.length === 12 && digits.startsWith('91')) {
    return `+${digits}`;
  }
  return input.trim();
}

class AuthService {
  async requestOtp(phoneInput) {
    const phone = normalizePhone(phoneInput);
    if (!phone || phone.length < 10) {
      throw new Error('A valid 10-digit phone number is required');
    }

    // In dev/test mode, use standard predictable OTP, otherwise generate random 6-digit code
    const isDev = process.env.NODE_ENV !== 'production';
    const otpCode = isDev ? DEV_TEST_OTP : Math.floor(100000 + Math.random() * 900000).toString();
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes

    await prisma.otpRequest.create({
      data: {
        phone,
        otpCode,
        purpose: 'LOGIN',
        expiresAt
      }
    });

    console.log(`📱 [YUGO AUTH] OTP for ${phone} is: ${otpCode} (expires in 10 mins)`);

    return {
      success: true,
      message: `Verification code sent to ${phone}`,
      phone,
      testOtp: isDev ? otpCode : undefined
    };
  }

  async verifyOtp(phoneInput, otpCode, clientInfo = {}) {
    const phone = normalizePhone(phoneInput);
    if (!phone || !otpCode) {
      throw new Error('Phone number and OTP code are required');
    }

    const cleanOtp = otpCode.trim();
    const isDev = process.env.NODE_ENV !== 'production';

    // Check OTP in DB
    const validOtp = await prisma.otpRequest.findFirst({
      where: {
        phone,
        otpCode: cleanOtp,
        isUsed: false,
        expiresAt: { gt: new Date() }
      },
      orderBy: { createdAt: 'desc' }
    });

    // Accept if valid in DB or if in dev mode with default test OTP
    const isTestOverride = isDev && cleanOtp === DEV_TEST_OTP;
    if (!validOtp && !isTestOverride) {
      throw new Error('Invalid or expired OTP code. Please try again.');
    }

    if (validOtp) {
      await prisma.otpRequest.update({
        where: { id: validOtp.id },
        data: { isUsed: true }
      });
    }

    // Find or create User
    let user = await prisma.user.findUnique({
      where: { phone },
      include: {
        addresses: true,
        emergencyContacts: true,
        preferences: true
      }
    });

    if (!user) {
      user = await prisma.user.create({
        data: {
          phone,
          name: phone === '+919876500112' ? 'Harsh Singh' : 'YUGO User',
          email: phone === '+919876500112' ? 'harsh@yugo.app' : null,
          rating: 4.95,
          yugoCashBalance: 500.0,
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
                label: 'HOME',
                title: 'Home',
                address: 'Sector 54, Golf Course Road, Gurugram',
                latitude: 28.4382,
                longitude: 77.1082,
                isDefault: true
              }
            ]
          }
        },
        include: {
          addresses: true,
          emergencyContacts: true,
          preferences: true
        }
      });
    }

    // Generate Tokens
    const accessToken = jwt.sign(
      { id: user.id, phone: user.phone },
      JWT_SECRET,
      { expiresIn: ACCESS_EXPIRES_IN }
    );

    const refreshToken = jwt.sign(
      { id: user.id, tokenType: 'refresh' },
      JWT_REFRESH_SECRET,
      { expiresIn: REFRESH_EXPIRES_IN }
    );

    const refreshExpiry = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000); // 30 days
    await prisma.session.create({
      data: {
        userId: user.id,
        refreshToken,
        userAgent: clientInfo.userAgent || 'Android Client',
        ipAddress: clientInfo.ip || '127.0.0.1',
        expiresAt: refreshExpiry
      }
    });

    return {
      success: true,
      message: 'Authentication successful',
      accessToken,
      refreshToken,
      user: {
        id: user.id,
        phone: user.phone,
        name: user.name,
        email: user.email,
        rating: user.rating,
        yugoCashBalance: user.yugoCashBalance,
        savedPlaces: user.addresses.map(a => ({ id: a.id, name: a.title, address: a.address, label: a.label })),
        emergencyContacts: user.emergencyContacts,
        preferences: user.preferences
      }
    };
  }

  async refreshToken(refreshToken) {
    if (!refreshToken) {
      throw new Error('Refresh token is required');
    }

    try {
      const decoded = jwt.verify(refreshToken, JWT_REFRESH_SECRET);
      const session = await prisma.session.findFirst({
        where: {
          refreshToken,
          userId: decoded.id,
          revokedAt: null,
          expiresAt: { gt: new Date() }
        },
        include: { user: true }
      });

      if (!session || !session.user) {
        throw new Error('Session is invalid or has been revoked');
      }

      const newAccessToken = jwt.sign(
        { id: session.user.id, phone: session.user.phone },
        JWT_SECRET,
        { expiresIn: ACCESS_EXPIRES_IN }
      );

      return {
        success: true,
        accessToken: newAccessToken
      };
    } catch (err) {
      throw new Error('Invalid or expired refresh token');
    }
  }

  async logout(refreshToken) {
    if (refreshToken) {
      await prisma.session.updateMany({
        where: { refreshToken },
        data: { revokedAt: new Date() }
      });
    }
    return { success: true, message: 'Logged out successfully' };
  }

  async getProfile(userId) {
    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: {
        addresses: true,
        emergencyContacts: true,
        preferences: true
      }
    });

    if (!user) {
      throw new Error('User not found');
    }

    return {
      id: user.id,
      phone: user.phone,
      name: user.name,
      email: user.email,
      rating: user.rating,
      yugoCashBalance: user.yugoCashBalance,
      savedPlaces: user.addresses.map(a => ({ id: a.id, name: a.title, address: a.address, label: a.label })),
      emergencyContacts: user.emergencyContacts,
      preferences: user.preferences
    };
  }

  async updateProfile(userId, { name, email }) {
    const updateData = {};
    if (name) updateData.name = name.trim();
    if (email !== undefined) updateData.email = email ? email.trim() : null;

    const updated = await prisma.user.update({
      where: { id: userId },
      data: updateData,
      include: {
        addresses: true,
        emergencyContacts: true,
        preferences: true
      }
    });

    return {
      id: updated.id,
      phone: updated.phone,
      name: updated.name,
      email: updated.email,
      rating: updated.rating,
      yugoCashBalance: updated.yugoCashBalance,
      savedPlaces: updated.addresses.map(a => ({ id: a.id, name: a.title, address: a.address, label: a.label }))
    };
  }

  async addAddress(userId, { title, address, label, latitude, longitude, isDefault }) {
    return prisma.address.create({
      data: {
        userId,
        title: title || 'Saved Place',
        address,
        label: (label || 'OTHER').toUpperCase(),
        latitude: latitude ? parseFloat(latitude) : null,
        longitude: longitude ? parseFloat(longitude) : null,
        isDefault: Boolean(isDefault)
      }
    });
  }

  async deleteAddress(userId, addressId) {
    const addr = await prisma.address.findFirst({
      where: { id: addressId, userId }
    });
    if (!addr) {
      throw new Error('Address not found or unauthorized');
    }
    await prisma.address.delete({ where: { id: addressId } });
    return { success: true, message: 'Address removed' };
  }

  async addEmergencyContact(userId, { name, phone, relationship }) {
    return prisma.emergencyContact.create({
      data: {
        userId,
        name,
        phone,
        relationship: relationship || 'Family'
      }
    });
  }

  async deleteEmergencyContact(userId, contactId) {
    const contact = await prisma.emergencyContact.findFirst({
      where: { id: contactId, userId }
    });
    if (!contact) {
      throw new Error('Contact not found or unauthorized');
    }
    await prisma.emergencyContact.delete({ where: { id: contactId } });
    return { success: true, message: 'Emergency contact removed' };
  }

  async updatePreferences(userId, data) {
    return prisma.notificationPreference.upsert({
      where: { userId },
      update: {
        rideUpdates: data.rideUpdates !== undefined ? data.rideUpdates : undefined,
        foodUpdates: data.foodUpdates !== undefined ? data.foodUpdates : undefined,
        parcelUpdates: data.parcelUpdates !== undefined ? data.parcelUpdates : undefined,
        promotional: data.promotional !== undefined ? data.promotional : undefined,
        pushEnabled: data.pushEnabled !== undefined ? data.pushEnabled : undefined,
        smsEnabled: data.smsEnabled !== undefined ? data.smsEnabled : undefined
      },
      create: {
        userId,
        rideUpdates: data.rideUpdates ?? true,
        foodUpdates: data.foodUpdates ?? true,
        parcelUpdates: data.parcelUpdates ?? true,
        promotional: data.promotional ?? false,
        pushEnabled: data.pushEnabled ?? true,
        smsEnabled: data.smsEnabled ?? true
      }
    });
  }
}

module.exports = new AuthService();
