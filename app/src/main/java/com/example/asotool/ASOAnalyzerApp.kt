package com.example.asotool

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asotool.modelclass.AppData
import com.example.asotool.util.analyzeApp
import com.example.asotool.util.getDemoData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ASOAnalyzerApp() {
    var packageName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var appData by remember { mutableStateOf<AppData?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ASO Analyzer", fontWeight = FontWeight.Bold)
                        Text("Play Store Analysis Tool", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2563EB),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEFF6FF),
                            Color(0xFFE0E7FF)
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = packageName,
                            onValueChange = { packageName = it },
                            label = { Text("Package Name") },
                            placeholder = { Text("e.g., com.whatsapp") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, "Search") },
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = ""
                                    try {
                                        appData = analyzeApp(packageName)
                                    } catch (e: Exception) {
                                        errorMessage = "Error: ${e.message}"
                                        appData = getDemoData(packageName)
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading && packageName.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isLoading) "Analyzing..." else "Analyze App")
                        }

                        if (errorMessage.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFEF3C7)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = Color(0xFFD97706))
                                    Text(errorMessage, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Loading Indicator
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // App Data Display
            appData?.let { data ->
                // App Info Card
                item {
                    InfoCard(title = "App Information") {
                        InfoRow("Title", data.appInfo.title)
                        InfoRow("Developer", data.appInfo.developer)
                        InfoRow("Category", data.appInfo.category)
                        InfoRow("Installs", data.appInfo.installs)
                        InfoRow("Version", data.appInfo.version)
                        InfoRow("Size", data.appInfo.size)
                        InfoRow("Rating", data.appInfo.contentRating)
                    }
                }

                // Ratings Card
                item {
                    InfoCard(title = "Ratings & Reviews") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = data.ratings.overall.toString(),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${data.ratings.totalReviews} reviews",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                                data.ratings.distribution.entries.sortedByDescending { it.key }.forEach { (star, percent) ->
                                    RatingBar(star, percent)
                                }
                            }
                        }
                    }
                }

                // Keywords Card
                item {
                    InfoCard(title = "Keyword Analysis") {
                        Text("Primary Keywords", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        FlowRow(modifier = Modifier.padding(vertical = 8.dp)) {
                            data.keywords.primary.forEach { keyword ->
                                KeywordChip(keyword, true)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text("Secondary Keywords", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        FlowRow(modifier = Modifier.padding(vertical = 8.dp)) {
                            data.keywords.secondary.forEach { keyword ->
                                KeywordChip(keyword, false)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text("Keyword Rankings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        data.keywords.positions.forEach { (keyword, position) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(keyword)
                                Text("#$position", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }
                    }
                }

                // ASO Score Card
                item {
                    InfoCard(title = "ASO Performance Score") {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = data.asoScore.overall.toString(),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                        Text(
                            "Overall ASO Score",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(16.dp))

                        data.asoScore.breakdown.forEach { (metric, score) ->
                            ScoreBar(metric, score)
                        }
                    }
                }

                // Competition Card
                item {
                    InfoCard(title = "Competition Analysis") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompetitionBox("Ranking", data.competition.ranking, Modifier.weight(1f))
                            CompetitionBox("Level", data.competition.level, Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(8.dp))

                        CompetitionBox("Market Saturation", data.competition.saturation, Modifier.fillMaxWidth())

                        Spacer(Modifier.height(12.dp))

                        Text("Opportunities", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        data.competition.opportunities.forEach { opp ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                Text(opp, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Clone Feasibility Card
                item {
                    InfoCard(title = "Clone Feasibility Analysis") {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${data.cloneFeasibility.score}/10",
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getScoreColor(data.cloneFeasibility.score)
                                )
                                Text(
                                    data.cloneFeasibility.difficulty,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    data.cloneFeasibility.recommendation,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Key Factors", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))

                        FactorBox("Technical Complexity", data.cloneFeasibility.factors.technical)
                        FactorBox("Market Demand", data.cloneFeasibility.factors.demand)
                        FactorBox("Competition Barrier", data.cloneFeasibility.factors.barrier)

                        Spacer(Modifier.height(12.dp))

                        Text("Unique Features", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        data.cloneFeasibility.factors.uniqueFeatures.forEach { feature ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("•", color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold)
                                Text(feature, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Recommendations Card
                item {
                    InfoCard(title = "ASO Recommendations") {
                        data.recommendations.forEachIndexed { index, rec ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFDCEEFE)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFF2563EB), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Text(rec, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}