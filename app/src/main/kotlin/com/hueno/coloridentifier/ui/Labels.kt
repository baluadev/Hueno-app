package com.hueno.coloridentifier.ui

import androidx.annotation.StringRes
import com.hueno.coloridentifier.R
import com.hueno.core.color.ConfidenceIssue
import com.hueno.core.color.ConfidenceLevel

/** Screen text for values that the core module reports as enums. */
@StringRes
fun ConfidenceLevel.labelRes(): Int = when (this) {
    ConfidenceLevel.GOOD -> R.string.confidence_good
    ConfidenceLevel.FAIR -> R.string.confidence_fair
    ConfidenceLevel.POOR -> R.string.confidence_poor
}

@StringRes
fun ConfidenceIssue.tipRes(): Int = when (this) {
    ConfidenceIssue.TOO_DARK -> R.string.issue_too_dark
    ConfidenceIssue.TOO_BRIGHT -> R.string.issue_too_bright
    ConfidenceIssue.UNSTEADY -> R.string.issue_unsteady
    ConfidenceIssue.MIXED_AREA -> R.string.issue_mixed_area
    ConfidenceIssue.NOT_CALIBRATED -> R.string.issue_not_calibrated
}
