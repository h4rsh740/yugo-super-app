package com.yugo.app

import org.junit.Assert.assertEquals
import org.junit.Test

class BillCalculationTest {

    data class CartItem(
        val name: String,
        val price: Double,
        val quantity: Int
    )

    data class OrderBill(
        val itemTotal: Double,
        val deliveryFee: Double,
        val platformFee: Double,
        val packagingCharge: Double,
        val taxesGst: Double,
        val discount: Double,
        val toPay: Double
    )

    private fun calculateBill(
        items: List<CartItem>,
        deliveryFee: Double = 40.0,
        couponCode: String? = null
    ): OrderBill {
        val itemTotal = items.sumOf { it.price * it.quantity }
        val packagingCharge = 15.0
        val platformFee = 5.0
        val taxesGst = Math.round((itemTotal * 0.05) * 100.0) / 100.0 // 5% GST on food

        var discount = 0.0
        if (couponCode == "YUGO50") {
            discount = Math.min(100.0, Math.round((itemTotal * 0.5) * 100.0) / 100.0)
        }

        val total = Math.max(0.0, Math.round((itemTotal + deliveryFee + packagingCharge + platformFee + taxesGst - discount) * 100.0) / 100.0)

        return OrderBill(
            itemTotal = itemTotal,
            deliveryFee = deliveryFee,
            platformFee = platformFee,
            packagingCharge = packagingCharge,
            taxesGst = taxesGst,
            discount = discount,
            toPay = total
        )
    }

    @Test
    fun testStandardFoodOrderBillWithoutCoupon() {
        val items = listOf(
            CartItem("Butter Chicken", 340.0, 1),
            CartItem("Garlic Naan", 60.0, 2)
        )
        // itemTotal = 340 + 120 = 460
        // packaging = 15
        // platform = 5
        // delivery = 40
        // taxes 5% of 460 = 23.0
        // discount = 0
        // total = 460 + 40 + 15 + 5 + 23 = 543.0
        val bill = calculateBill(items)
        assertEquals(460.0, bill.itemTotal, 0.01)
        assertEquals(23.0, bill.taxesGst, 0.01)
        assertEquals(0.0, bill.discount, 0.01)
        assertEquals(543.0, bill.toPay, 0.01)
    }

    @Test
    fun testCouponDiscountYUGO50CappedAt100() {
        val items = listOf(
            CartItem("Dum Biryani Handi", 350.0, 1),
            CartItem("Paneer Tikka", 250.0, 1)
        )
        // itemTotal = 600.0 -> 50% is 300, capped at 100.0
        // taxes 5% of 600 = 30.0
        // fees = 40 + 15 + 5 = 60
        // total = 600 + 60 + 30 - 100 = 590.0
        val bill = calculateBill(items, couponCode = "YUGO50")
        assertEquals(100.0, bill.discount, 0.01)
        assertEquals(590.0, bill.toPay, 0.01)
    }

    @Test
    fun testSmallOrderGstPrecision() {
        val items = listOf(
            CartItem("Masala Chai", 25.0, 1)
        )
        // itemTotal = 25.0
        // 5% of 25 = 1.25
        val bill = calculateBill(items)
        assertEquals(1.25, bill.taxesGst, 0.01)
    }
}
