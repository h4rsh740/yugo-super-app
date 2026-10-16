const foodService = require('../services/foodService');
const prisma = require('../lib/db');

describe('Food Order Billing & Total Engine', () => {
  let sampleItem;

  beforeAll(async () => {
    sampleItem = await prisma.menuItem.findFirst();
  });

  afterAll(async () => {
    await prisma.$disconnect();
  });

  test('accurately calculates item subtotal, packaging, 5% GST taxes, and delivery fee', async () => {
    if (!sampleItem) return;

    const qty = 2;
    const bill = await foodService.calculateBill({
      restaurantId: sampleItem.restaurantId,
      items: [{ menuItemId: sampleItem.id, quantity: qty }]
    });

    const expectedSubtotal = sampleItem.price * qty;
    const expectedTax = Math.round(expectedSubtotal * 0.05 * 100) / 100;
    const expectedGrandTotal = expectedSubtotal + 35.0 + 15.0 + expectedTax;

    expect(bill.subtotal).toBe(expectedSubtotal);
    expect(bill.deliveryFee).toBe(35.0);
    expect(bill.packagingFee).toBe(15.0);
    expect(bill.taxes).toBe(expectedTax);
    expect(bill.totalAmount).toBe(expectedGrandTotal);
  });

  test('fails bill calculation when cart is empty', async () => {
    await expect(foodService.calculateBill({ items: [] })).rejects.toThrow(/Cart cannot be empty/);
  });
});
