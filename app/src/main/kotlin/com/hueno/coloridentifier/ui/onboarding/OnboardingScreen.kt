package com.hueno.coloridentifier.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.coloridentifier.ui.HuenoPrimaryButton
import com.hueno.coloridentifier.ui.HuenoSize
import com.hueno.coloridentifier.ui.ScreenTitle
import com.hueno.coloridentifier.ui.HuenoType

/** First-run guide: three things to know, then straight to the camera. Shown once. */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuenoColors.Background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HuenoSize.ScreenPadding, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.hueno_mark),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(72.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ScreenTitle(stringResource(R.string.onboarding_title))
            Text(stringResource(R.string.onboarding_subtitle), color = HuenoColors.TextMuted, fontSize = HuenoType.Body)
        }
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Step(1, stringResource(R.string.onboarding_step1_title), stringResource(R.string.onboarding_step1_body))
            Step(2, stringResource(R.string.onboarding_step2_title), stringResource(R.string.onboarding_step2_body))
            Step(3, stringResource(R.string.onboarding_step3_title), stringResource(R.string.onboarding_step3_body))
        }
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HuenoPrimaryButton(stringResource(R.string.onboarding_start), onDone, Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.onboarding_note),
                color = HuenoColors.TextMuted,
                fontSize = HuenoType.Label,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Step(number: Int, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(HuenoColors.Text),
            contentAlignment = Alignment.Center,
        ) {
            Text(number.toString(), color = HuenoColors.Background, fontSize = HuenoType.Title, fontWeight = FontWeight.Bold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = HuenoColors.Text, fontSize = HuenoType.Title, fontWeight = FontWeight.SemiBold)
            Text(body, color = HuenoColors.TextMuted, fontSize = HuenoType.Body, lineHeight = HuenoType.BodyLine)
        }
    }
}
