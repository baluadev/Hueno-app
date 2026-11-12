package com.hueno.monetization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdRevenueTest {

    private fun revenue(value: Double) = AdRevenue("banner", "unit", "AdMob Network", value, "USD", "estimated")

    @Test
    fun valueIsScaledByOneHundred() {
        assertEquals(0.35, revenue(0.0035).scaledValue()!!, 1e-9)
    }

    @Test
    fun tinyAndZeroImpressionsAreNotReported() {
        assertNull("test ads report zero", revenue(0.0).scaledValue())
        assertNull("below 0.01 after scaling", revenue(0.00009).scaledValue())
        assertEquals(0.01, revenue(0.0001).scaledValue()!!, 1e-9)
    }

    @Test
    fun outliersAreCappedAtTwentyFive() {
        assertEquals(25.0, revenue(0.25).scaledValue()!!, 1e-9)
        assertEquals(25.0, revenue(3.0).scaledValue()!!, 1e-9)
    }

    @Test
    fun brokenValuesAreNotReported() {
        assertNull(revenue(Double.NaN).scaledValue())
        assertNull(revenue(-1.0).scaledValue())
    }
}
