package com.example.asotool

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeywordChip(keyword: String, isPrimary: Boolean) {
    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) Color(0xFFDCEEFE) else Color(0xFFF3F4F6)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = keyword,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = if (isPrimary) Color(0xFF1E40AF) else Color(0xFF374151),
            fontWeight = if (isPrimary) FontWeight.Medium else FontWeight.Normal
        )
    }
}