package com.abizer_r.minitruecaller.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.abizer_r.minitruecaller.domain.model.CallerInfo

@Composable
fun CallerInfoOverlay(
    callerInfo: CallerInfo?
) {
    Surface(
        color = Color(0xFF1E88E5),
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp)),
        shadowElevation = 8.dp
    ) {

        if (callerInfo == null) {
            CircularProgressIndicator()
            return@Surface
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

        }
        Text(
            text = callerInfo.name,
            color = Color.White,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = callerInfo.number,
            color = Color.White,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
