package com.hueno.monetization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProRulesTest {

    private val hour = 60L * 60L * 1000L
    private val t0 = 1_800_000_000_000L

    @Test
    fun noPassMeansNoTime() {
        assertEquals(0L, ProPass.remainingMillis(PassState.NONE, t0))
        assertFalse(ProPass.isActive(PassState.NONE, t0))
    }

    @Test
    fun oneVideoGrantsSixHours() {
        val pass = ProPass.grant(PassState.NONE, t0)
        assertEquals(6 * hour, ProPass.remainingMillis(pass, t0))
        assertTrue(ProPass.isActive(pass, t0 + 6 * hour - 1))
        assertFalse(ProPass.isActive(pass, t0 + 6 * hour))
    }

    @Test
    fun videosStackOnRemainingTime() {
        var pass = ProPass.grant(PassState.NONE, t0)
        pass = ProPass.grant(pass, t0 + 2 * hour)
        // Four hours were left, plus six more.
        assertEquals(10 * hour, ProPass.remainingMillis(pass, t0 + 2 * hour))
    }

    @Test
    fun stackingIsCappedAtTwentyFourHours() {
        var pass = PassState.NONE
        repeat(6) { pass = ProPass.grant(pass, t0) }
        assertEquals(24 * hour, ProPass.remainingMillis(pass, t0))
        assertFalse(ProPass.canGrantMore(pass, t0))
        assertTrue(ProPass.canGrantMore(pass, t0 + hour))
    }

    @Test
    fun anExpiredPassStartsFresh() {
        val old = ProPass.grant(PassState.NONE, t0)
        val later = t0 + 30 * hour
        assertEquals(6 * hour, ProPass.remainingMillis(ProPass.grant(old, later), later))
    }

    @Test
    fun settingTheClockBackVoidsThePass() {
        val pass = ProPass.observe(ProPass.grant(PassState.NONE, t0), t0 + 5 * hour)
        // One hour is left. The user sets the clock back four hours to get more.
        val rewound = ProPass.observe(pass, t0 + hour)
        assertEquals(0L, ProPass.remainingMillis(rewound, t0 + hour))
    }

    @Test
    fun aSmallClockCorrectionIsTolerated() {
        val pass = ProPass.observe(ProPass.grant(PassState.NONE, t0), t0 + hour)
        val corrected = ProPass.observe(pass, t0 + hour - 60_000)
        assertTrue(ProPass.isActive(corrected, t0 + hour - 60_000))
    }

    @Test
    fun aTamperedFarFutureExpiryIsStillCapped() {
        val forged = PassState(expiresAt = t0 + 1000 * hour, lastSeen = t0)
        assertEquals(24 * hour, ProPass.remainingMillis(forged, t0))
    }

    @Test
    fun freeComparisonsResetEachDay() {
        var quota = CompareQuota.NONE
        assertEquals(3, quota.remaining(today = 100))
        repeat(3) { quota = quota.use(100) }
        assertEquals(0, quota.remaining(100))
        assertEquals(3, quota.remaining(101))
        assertEquals(2, quota.use(101).remaining(101))
    }
}
