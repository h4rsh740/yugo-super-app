package com.yugo.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParcelPricingTest {

    data class ParcelQuote(
        val baseFare: Double,
        val weightFare: Double,
        val distanceFare: Double,
        val speedFee: Double,
        val insuranceFee: Double,
        val totalFare: Double
    )

    private fun quoteParcel(
        weightKg: Double,
        distanceKm: Double,
        isExpress: Boolean = false,
        declaredValue: Double = 0.0
    ): ParcelQuote {
        val base = 40.0
        val weightRate = when {
            weightKg <= 1.0 -> 10.0
            weightKg <= 3.0 -> 25.0
            weightKg <= 7.0 -> 50.0
            else -> 90.0
        }
        val distanceFare = distanceKm * 8.5
        val speedFee = if (isExpress) 45.0 else 0.0
        val insuranceFee = if (declaredValue > 1000.0) declaredValue * 0.01 else 0.0

        val total = Math.round((base + weightRate + distanceFare + speedFee + insuranceFee) * 100.0) / 100.0

        return ParcelQuote(
            baseFare = base,
            weightFare = weightRate,
            distanceFare = Math.round(distanceFare * 100.0) / 100.0,
            speedFee = speedFee,
            insuranceFee = insuranceFee,
            totalFare = total
        )
    }

    @Test
    fun testLightDocumentDeliveryStandard() {
        // 0.5 kg, 6 km, standard
        // base: 40, weight: 10, dist: 6 * 8.5 = 51, express: 0, insurance: 0 -> total: 101.0
        val quote = quoteParcel(0.5, 6.0, isExpress = false)
        assertEquals(101.0, quote.totalFare, 0.01)
        assertEquals(10.0, quote.weightFare, 0.01)
    }

    @Test
    fun testMediumParcelExpress() {
        // 5.0 kg, 10 km, express
        // base: 40, weight: 50, dist: 10 * 8.5 = 85, express: 45 -> total: 220.0
        val quote = quoteParcel(5.0, 10.0, isExpress = true)
        assertEquals(220.0, quote.totalFare, 0.01)
        assertEquals(45.0, quote.speedFee, 0.01)
    }

    @Test
    fun testHighValueParcelInsurance() {
        // 2.0 kg, 4 km, standard, declared value 5000 -> 1% = 50.0
        // base: 40, weight: 25, dist: 4 * 8.5 = 34, insurance: 50 -> total: 149.0
        val quote = quoteParcel(2.0, 4.0, isExpress = false, declaredValue = 5000.0)
        assertEquals(50.0, quote.insuranceFee, 0.01)
        assertEquals(149.0, quote.totalFare, 0.01)
    }

    @Test
    fun testHeavyWeightTierOrdering() {
        val dist = 5.0
        val docQuote = quoteParcel(0.5, dist)
        val smallQuote = quoteParcel(2.5, dist)
        val medQuote = quoteParcel(6.0, dist)
        val heavyQuote = quoteParcel(12.0, dist)

        assertTrue(docQuote.totalFare < smallQuote.totalFare)
        assertTrue(smallQuote.totalFare < medQuote.totalFare)
        assertTrue(medQuote.totalFare < heavyQuote.totalFare)
    }
}
