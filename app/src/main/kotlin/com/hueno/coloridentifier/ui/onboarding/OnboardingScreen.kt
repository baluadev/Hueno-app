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

/** First-run guide: three things to know, then straight to the camera. Shown once. */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuenoColors.Paper)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.hueno_mark),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(72.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(R.string.onboarding_title),
                color = HuenoColors.OnPaper,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Text(stringResource(R.string.onboarding_subtitle), color = HuenoColors.OnPaperMuted, fontSize = 16.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Step(1, stringResource(R.string.onboarding_step1_title), stringResource(R.string.onboarding_step1_body))
            Step(2, stringResource(R.string.onboarding_step2_title), stringResource(R.string.onboarding_step2_body))
            Step(3, stringResource(R.string.onboarding_step3_title), stringResource(R.string.onboarding_step3_body))
        }
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HuenoColors.Action,
                    contentColor = HuenoColors.OnAction,
                ),
            ) { Text(stringResource(R.string.onboarding_start), fontSize = 17.sp, fontWeight = FontWeight.SemiBold) }
            Text(
                stringResource(R.string.onboarding_note),
                color = HuenoColors.OnPaperMuted,
                fontSize = 13.sp,
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
            modifier = Modifier.size(40.dp).clip(CircleShape).background(HuenoColors.OnPaper),
            contentAlignment = Alignment.Center,
        ) {
            Text(number.toString(), color = HuenoColors.Paper, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = HuenoColors.OnPaper, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(body, color = HuenoColors.OnPaperMuted, fontSize = 15.sp, lineHeight = 21.sp)
        }
    }
}
