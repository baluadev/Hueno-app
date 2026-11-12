package com.hueno.monetization

import kotlin.math.max
import kotlin.math.min

/** The numbers behind the free tier and the rewarded Pro pass. Tune them here. */
object ProRules {
    /** Pro time granted for one rewarded video. */
    const val PASS_HOURS = 6

    /** Passes stack, but never beyond this much time ahead. */
    const val MAX_PASS_HOURS = 24

    const val FREE_COMPARES_PER_DAY = 3
    const val FREE_HISTORY_LIMIT = 20

    internal const val HOUR_MILLIS = 60L * 60L * 1000L

    /** Small backward clock steps (network time sync) are tolerated before a pass is voided. */
    internal const val CLOCK_TOLERANCE_MILLIS = 5L * 60L * 1000L
}

/**
 * A rewarded Pro pass. [expiresAt] and [lastSeen] are wall-clock milliseconds; [lastSeen] is the
 * latest time the app has observed, used to notice the clock being set back.
 */
data class PassState(val expiresAt: Long, val lastSeen: Long) {
    companion object {
        val NONE = PassState(0L, 0L)
    }
}

/** Pure rules for the rewarded pass. Callers supply the current time, which keeps this testable. */
object ProPass {

    /**
     * Brings the state up to [now]. If the clock has gone backwards by more than the tolerance,
     * the pass is voided: otherwise setting the clock back would extend it for free.
     */
    fun observe(state: PassState, now: Long): PassState {
        if (now < state.lastSeen - ProRules.CLOCK_TOLERANCE_MILLIS) return PassState(0L, now)
        return state.copy(lastSeen = max(state.lastSeen, now))
    }

    /** Milliseconds of Pro time left; 0 when there is no active pass. Expects an observed state. */
    fun remainingMillis(state: PassState, now: Long): Long =
        min(max(0L, state.expiresAt - now), ProRules.MAX_PASS_HOURS * ProRules.HOUR_MILLIS)

    fun isActive(state: PassState, now: Long): Boolean = remainingMillis(state, now) > 0

    /** Adds one video's worth of time on top of whatever is left, capped at the maximum. */
    fun grant(state: PassState, now: Long): PassState {
        val current = observe(state, now)
        val base = max(current.expiresAt, now)
        val expires = min(
            base + ProRules.PASS_HOURS * ProRules.HOUR_MILLIS,
            now + ProRules.MAX_PASS_HOURS * ProRules.HOUR_MILLIS,
        )
        return PassState(expires, now)
    }

    /** False when another video would add no time because the pass is already at the cap. */
    fun canGrantMore(state: PassState, now: Long): Boolean =
        remainingMillis(state, now) < ProRules.MAX_PASS_HOURS * ProRules.HOUR_MILLIS
}

/** How many free comparisons were started on a given day ([day] is a local epoch day). */
data class CompareQuota(val day: Long, val used: Int) {

    fun remaining(today: Long): Int =
        if (day == today) max(0, ProRules.FREE_COMPARES_PER_DAY - used) else ProRules.FREE_COMPARES_PER_DAY

    /** Records one comparison today. Call only when [remaining] is above zero. */
    fun use(today: Long): CompareQuota = if (day == today) copy(used = used + 1) else CompareQuota(today, 1)

    companion object {
        val NONE = CompareQuota(0L, 0)
    }
}
