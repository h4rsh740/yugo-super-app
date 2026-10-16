const parcelService = require('../services/parcelService');

describe('Parcel Pricing and Validation Engine', () => {
  test('calculates correct base fare, weight charge, and distance charge', () => {
    // 3.0 kg, 5.0 km
    // Base fare: 50
    // Weight charge: (3.0 - 1.0) * 20 = 40
    // Distance charge: 5.0 * 12 = 60
    // Total = 150
    const estimate = parcelService.calculateEstimate({ weightKg: 3.0, distanceKm: 5.0 });

    expect(estimate.breakdown.baseFare).toBe(50);
    expect(estimate.breakdown.weightCharge).toBe(40);
    expect(estimate.breakdown.distanceCharge).toBe(60);
    expect(estimate.estimatedPrice).toBe(150);
  });

  test('applies zero weight charge for packages under or equal to 1kg', () => {
    const estimate = parcelService.calculateEstimate({ weightKg: 0.8, distanceKm: 4.0 });
    expect(estimate.breakdown.weightCharge).toBe(0);
    expect(estimate.breakdown.baseFare).toBe(50);
    expect(estimate.breakdown.distanceCharge).toBe(48);
    expect(estimate.estimatedPrice).toBe(98);
  });

  test('rejects parcel packages exceeding maximum weight limit of 20kg', () => {
    expect(() => {
      parcelService.calculateEstimate({ weightKg: 25.0, distanceKm: 5.0 });
    }).toThrow(/Maximum parcel weight limit/);
  });
});
