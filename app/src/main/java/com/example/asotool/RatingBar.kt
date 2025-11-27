package com.example.asotool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingBar(star: Int, percent: Int) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("$star★", fontSize = 12.sp, modifier = Modifier.width(24.dp))
        LinearProgressIndicator(
            progress = percent / 100f,
            modifier = Modifier.weight(1f).height(6.dp),
            color = Color(0xFFFBBF24),
            trackColor = Color(0xFFE5E7EB)
        )
        Text("$percent%", fontSize = 12.sp, modifier = Modifier.width(40.dp))
    }
}