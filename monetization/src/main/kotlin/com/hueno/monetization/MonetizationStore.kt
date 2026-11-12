package com.hueno.monetization

import android.content.Context

/** Small persistent state: the pass, the last known purchase, today's comparisons, session count. */
internal class MonetizationStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("hueno_monetization", Context.MODE_PRIVATE)

    var pass: PassState
        get() = PassState(prefs.getLong(KEY_PASS_EXPIRES, 0L), prefs.getLong(KEY_PASS_SEEN, 0L))
        set(value) = prefs.edit().putLong(KEY_PASS_EXPIRES, value.expiresAt).putLong(KEY_PASS_SEEN, value.lastSeen).apply()

    /**
     * The last answer from Google Play about the Pro purchase. Kept so Pro works offline; Play
     * remains the source of truth and overwrites this whenever it answers.
     */
    var purchased: Boolean
        get() = prefs.getBoolean(KEY_PURCHASED, false)
        set(value) = prefs.edit().putBoolean(KEY_PURCHASED, value).apply()

    var compareQuota: CompareQuota
        get() = CompareQuota(prefs.getLong(KEY_COMPARE_DAY, 0L), prefs.getInt(KEY_COMPARE_USED, 0))
        set(value) = prefs.edit().putLong(KEY_COMPARE_DAY, value.day).putInt(KEY_COMPARE_USED, value.used).apply()

    /** Number of times the app has been started. */
    var sessions: Int
        get() = prefs.getInt(KEY_SESSIONS, 0)
        set(value) = prefs.edit().putInt(KEY_SESSIONS, value).apply()

    private companion object {
        const val KEY_PASS_EXPIRES = "pass_expires_at"
        const val KEY_PASS_SEEN = "pass_last_seen"
        const val KEY_PURCHASED = "pro_purchased"
        const val KEY_COMPARE_DAY = "compare_day"
        const val KEY_COMPARE_USED = "compare_used"
        const val KEY_SESSIONS = "sessions"
    }
}
