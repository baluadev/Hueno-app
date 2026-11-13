package com.hueno.coloridentifier.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.coloridentifier.ui.HuenoType

/**
 * Languages the app is translated into, each named in its own language so a user who cannot read
 * the current one still finds theirs. Keep in step with the values-xx folders.
 */
internal val AppLanguages = listOf(
    "en" to "English",
    "de" to "Deutsch",
    "es" to "Español",
    "fr" to "Français",
    "ja" to "日本語",
    "ko" to "한국어",
)

/** The language tag chosen in the app, or null when it follows the phone. */
internal fun currentAppLanguage(): String? {
    val locales = AppCompatDelegate.getApplicationLocales()
    if (locales.isEmpty) return null
    val language = locales[0]?.language ?: return null
    return AppLanguages.firstOrNull { it.first == language }?.first
}

/** Name of the current choice for the Settings row. */
@Composable
internal fun currentLanguageName(): String {
    val tag = currentAppLanguage() ?: return stringResource(R.string.language_system)
    return AppLanguages.first { it.first == tag }.second
}

/**
 * Applies a language for this app only; null goes back to the phone language. The activity is
 * recreated in the new language and the choice is kept across launches.
 */
internal fun setAppLanguage(tag: String?) {
    AppCompatDelegate.setApplicationLocales(
        if (tag == null) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag),
    )
}

@Composable
internal fun LanguageDialog(onDismiss: () -> Unit) {
    val current = currentAppLanguage()
    val options = listOf<Pair<String?, String>>(null to stringResource(R.string.language_system)) + AppLanguages
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_dialog_title)) },
        text = {
            Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
                options.forEach { (tag, name) ->
                    val selected = tag == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(selected = selected, role = Role.RadioButton) {
                                onDismiss()
                                if (!selected) setAppLanguage(tag)
                            }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = HuenoColors.Action),
                        )
                        Text(name, fontSize = HuenoType.Body)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.calibrate_cancel)) }
        },
    )
}
