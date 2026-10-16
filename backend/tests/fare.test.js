const rideService = require('../services/rideService');

describe('Ride Fare Engine Tests', () => {
  test('calculates correct base and per-km fares for all categories', async () => {
    const { options } = await rideService.getRideOptions(5.0);

    const auto = options.find(o => o.category === 'AUTO');
    const moto = options.find(o => o.category === 'MOTO');
    const go = options.find(o => o.category === 'GO');
    const premier = options.find(o => o.category === 'PREMIER');
    const xl = options.find(o => o.category === 'XL');

    expect(auto).toBeDefined();
    expect(moto).toBeDefined();
    expect(go).toBeDefined();
    expect(premier).toBeDefined();
    expect(xl).toBeDefined();

    // Auto: base 35 + 5*12 = 95
    expect(auto.fareAmount).toBe(95);
    // Moto: base 25 + 5*8 = 65
    expect(moto.fareAmount).toBe(65);
    // Go: base 50 + 5*16 = 130
    expect(go.fareAmount).toBe(130);
    // Premier: base 80 + 5*22 = 190
    expect(premier.fareAmount).toBe(190);
    // XL: base 120 + 5*30 = 270
    expect(xl.fareAmount).toBe(270);
  });

  test('enforces minimum fares for very short rides', async () => {
    const { options } = await rideService.getRideOptions(0.2);

    const auto = options.find(o => o.category === 'AUTO');
    const moto = options.find(o => o.category === 'MOTO');
    const go = options.find(o => o.category === 'GO');

    expect(auto.fareAmount).toBeGreaterThanOrEqual(50);
    expect(moto.fareAmount).toBeGreaterThanOrEqual(35);
    expect(go.fareAmount).toBeGreaterThanOrEqual(90);
  });
});
