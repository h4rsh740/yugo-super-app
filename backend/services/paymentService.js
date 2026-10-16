const prisma = require('../lib/db');

class LocalTestPaymentProvider {
  async processPayment({ amount, method, referenceId, serviceType, userId }) {
    const txnRef = `TXN_TEST_${Date.now()}_${Math.floor(1000 + Math.random() * 9000)}`;
    return {
      success: true,
      transactionRef: txnRef,
      provider: 'LOCAL_TEST',
      status: 'SUCCESS',
      message: 'Test payment processed successfully without external credentials'
    };
  }

  async refundPayment({ transactionRef, amount, reason }) {
    return {
      success: true,
      refundRef: `REF_TEST_${Date.now()}`,
      status: 'REFUNDED',
      message: `Refund of ₹${amount} initiated for ${transactionRef}: ${reason || 'Customer requested'}`
    };
  }
}

class RazorpayPaymentProvider {
  constructor() {
    this.keyId = process.env.RAZORPAY_KEY_ID;
    this.keySecret = process.env.RAZORPAY_KEY_SECRET;
  }

  async processPayment({ amount, method, referenceId, serviceType, userId }) {
    if (!this.keyId || !this.keySecret) {
      throw new Error('Razorpay credentials not configured in environment (RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET)');
    }
    // Production integration hook
    return {
      success: true,
      transactionRef: `rzp_${Date.now()}`,
      provider: 'RAZORPAY',
      status: 'SUCCESS'
    };
  }

  async refundPayment({ transactionRef, amount, reason }) {
    if (!this.keyId || !this.keySecret) {
      throw new Error('Razorpay credentials not configured for refund');
    }
    return { success: true, refundRef: `rfnd_${Date.now()}`, status: 'REFUNDED' };
  }
}

class StripePaymentProvider {
  constructor() {
    this.secretKey = process.env.STRIPE_SECRET_KEY;
  }

  async processPayment({ amount, method, referenceId, serviceType, userId }) {
    if (!this.secretKey) {
      throw new Error('Stripe credentials not configured in environment (STRIPE_SECRET_KEY)');
    }
    // Production integration hook
    return {
      success: true,
      transactionRef: `ch_${Date.now()}`,
      provider: 'STRIPE',
      status: 'SUCCESS'
    };
  }

  async refundPayment({ transactionRef, amount, reason }) {
    if (!this.secretKey) {
      throw new Error('Stripe credentials not configured for refund');
    }
    return { success: true, refundRef: `re_${Date.now()}`, status: 'REFUNDED' };
  }
}

class PaymentService {
  constructor() {
    this.providers = {
      local_test: new LocalTestPaymentProvider(),
      razorpay: new RazorpayPaymentProvider(),
      stripe: new StripePaymentProvider()
    };
  }

  getProvider(providerName) {
    const selected = (providerName || process.env.PAYMENT_PROVIDER || 'local_test').toLowerCase();
    return this.providers[selected] || this.providers.local_test;
  }

  async processPayment({ userId, amount, method, serviceType, referenceId, metadata }) {
    const normalizedMethod = (method || 'CASH').toUpperCase();
    
    // Check YUGO Cash wallet deduction if applicable
    if (normalizedMethod === 'YUGO_CASH' || normalizedMethod.includes('WALLET') || normalizedMethod.includes('YUGO PAY')) {
      const user = await prisma.user.findUnique({ where: { id: userId } });
      if (!user) {
        throw new Error('User not found');
      }
      if (user.yugoCashBalance < amount) {
        throw new Error(`Insufficient YUGO Cash balance. Available: ₹${user.yugoCashBalance.toFixed(2)}, Required: ₹${amount.toFixed(2)}`);
      }

      // Deduct balance
      await prisma.user.update({
        where: { id: userId },
        data: { yugoCashBalance: user.yugoCashBalance - amount }
      });

      const txn = await prisma.paymentTransaction.create({
        data: {
          userId,
          referenceId: referenceId || `ref_${Date.now()}`,
          serviceType: serviceType || 'GENERAL',
          amount,
          method: 'YUGO_CASH',
          status: 'SUCCESS',
          provider: 'WALLET',
          transactionRef: `WAL_${Date.now()}`,
          metadata: JSON.stringify(metadata || {})
        }
      });

      return {
        success: true,
        transaction: txn,
        remainingBalance: user.yugoCashBalance - amount
      };
    }

    // Cash payments are recorded as pending/collected on delivery
    if (normalizedMethod === 'CASH') {
      const txn = await prisma.paymentTransaction.create({
        data: {
          userId,
          referenceId: referenceId || `ref_${Date.now()}`,
          serviceType: serviceType || 'GENERAL',
          amount,
          method: 'CASH',
          status: 'SUCCESS',
          provider: 'LOCAL_TEST',
          transactionRef: `CSH_${Date.now()}`,
          metadata: JSON.stringify(metadata || {})
        }
      });

      return { success: true, transaction: txn };
    }

    // Card or UPI payments go through configured provider
    const provider = this.getProvider();
    const result = await provider.processPayment({ amount, method: normalizedMethod, referenceId, serviceType, userId });

    const txn = await prisma.paymentTransaction.create({
      data: {
        userId,
        referenceId: referenceId || `ref_${Date.now()}`,
        serviceType: serviceType || 'GENERAL',
        amount,
        method: normalizedMethod,
        status: result.status,
        provider: result.provider,
        transactionRef: result.transactionRef,
        metadata: JSON.stringify(metadata || {})
      }
    });

    return {
      success: true,
      transaction: txn,
      providerMessage: result.message
    };
  }

  async topupWallet(userId, amount, method = 'UPI') {
    if (amount <= 0 || isNaN(amount)) {
      throw new Error('Invalid top-up amount');
    }

    const provider = this.getProvider();
    const result = await provider.processPayment({
      amount,
      method,
      referenceId: `wallet_topup_${Date.now()}`,
      serviceType: 'WALLET',
      userId
    });

    const updatedUser = await prisma.user.update({
      where: { id: userId },
      data: {
        yugoCashBalance: { increment: amount }
      }
    });

    const txn = await prisma.paymentTransaction.create({
      data: {
        userId,
        referenceId: `wallet_topup_${Date.now()}`,
        serviceType: 'WALLET',
        amount,
        method,
        status: 'SUCCESS',
        provider: result.provider,
        transactionRef: result.transactionRef,
        metadata: JSON.stringify({ reason: 'Wallet Topup' })
      }
    });

    return {
      success: true,
      newBalance: updatedUser.yugoCashBalance,
      transaction: txn
    };
  }

  async getTransactionHistory(userId, limit = 20) {
    return prisma.paymentTransaction.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      take: limit
    });
  }
}

module.exports = new PaymentService();
