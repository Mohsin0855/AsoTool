package com.example.asotool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScoreBar(metric: String, score: Int) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                metric.replace(Regex("([A-Z])"), " $1").trim().capitalize(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text("$score/100", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = score / 100f,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = when {
                score >= 80 -> Color(0xFF10B981)
                score >= 60 -> Color(0xFFFBBF24)
                else -> Color(0xFFEF4444)
            },
            trackColor = Color(0xFFE5E7EB)
        )
    }
}