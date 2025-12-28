package com.example.asotool.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asotool.domain.model.AppData
import com.example.asotool.presentation.components.*
import com.example.asotool.presentation.viewmodel.ASOViewModel
import com.example.asotool.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ASOAnalyzerScreen(viewModel: ASOViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    val gradientBrush = remember {
        Brush.verticalGradient(
            listOf(BackgroundGradientStart, BackgroundGradientEnd, BackgroundLight)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("ASO Analyzer", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Play Store Analysis Tool", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "search") {
                    SearchCard(
                        packageName = uiState.packageName,
                        isLoading = uiState.isLoading,
                        errorMessage = uiState.errorMessage,
                        onPackageNameChange = viewModel::updatePackageName,
                        onAnalyzeClick = viewModel::analyzeApp
                    )
                }

                if (uiState.isLoading) {
                    item(key = "loading") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = PrimaryBlue)
                                Spacer(Modifier.height(16.dp))
                                Text("Analyzing app...", color = TextSecondary)
                                Text("This may take a few seconds", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }
                }

                uiState.appData?.let { data ->
                    item(key = "app_info") { AppInfoSection(data) }
                    item(key = "ratings") { RatingsSection(data) }
                    item(key = "keywords") { KeywordsSection(data) }
                    item(key = "aso_score") { ASOScoreSection(data) }
                    item(key = "competition") { CompetitionSection(data) }
                    item(key = "clone") { CloneFeasibilitySection(data) }
                    item(key = "recommendations") { RecommendationsSection(data) }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SearchCard(
    packageName: String,
    isLoading: Boolean,
    errorMessage: String?,
    onPackageNameChange: (String) -> Unit,
    onAnalyzeClick: () -> Unit
) {
    InfoCard(title = "Search App") {
        OutlinedTextField(
            value = packageName,
            onValueChange = onPackageNameChange,
            label = { Text("Package Name") },
            placeholder = { Text("e.g., com.whatsapp") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryBlue) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = CardBorder,
                focusedLabelColor = PrimaryBlue
            )
        )
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = onAnalyzeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading && packageName.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Default.Search, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (isLoading) "Analyzing..." else "Analyze App",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        errorMessage?.let { error ->
            Spacer(Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AlertWarningBg)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = AlertWarningIcon)
                    Spacer(Modifier.width(12.dp))
                    Text(error, fontSize = 13.sp, color = AlertWarningText)
                }
            }
        }
    }
}

@Composable
private fun AppInfoSection(data: AppData) {
    InfoCard(title = "App Information") {
        InfoRow("Title", data.appInfo.title)
        InfoRow("Developer", data.appInfo.developer)
        InfoRow("Category", data.appInfo.category)
        InfoRow("Installs", data.appInfo.installs)
        InfoRow("Version", data.appInfo.version)
        InfoRow("Size", data.appInfo.size)
        InfoRow("Content Rating", data.appInfo.contentRating)
    }
}

@Composable
private fun RatingsSection(data: AppData) {
    val sortedDistribution = remember(data.ratings.distribution) {
        data.ratings.distribution.entries.sortedByDescending { it.key }
    }
    
    InfoCard(title = "Ratings & Reviews") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.1f", data.ratings.overall),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Text(
                    "${data.ratings.totalReviews} reviews",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.width(24.dp))
            Column(Modifier.weight(1f)) {
                sortedDistribution.forEach { (star, percent) ->
                    RatingBar(star, percent)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordsSection(data: AppData) {
    InfoCard(title = "Keyword Analysis") {
        Text("Primary Keywords", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.keywords.primary.forEach { KeywordChip(it, true) }
        }

        Spacer(Modifier.height(20.dp))
        
        Text("Secondary Keywords", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.keywords.secondary.forEach { KeywordChip(it, false) }
        }

        if (data.keywords.positions.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("Keyword Rankings", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                data.keywords.positions.forEach { (keyword, position) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceLight)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(keyword, color = TextPrimary)
                        Text("#$position", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun ASOScoreSection(data: AppData) {
    InfoCard(title = "ASO Performance Score") {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(60.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    BackgroundGradientStart,
                                    PrimaryBlue.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${data.asoScore.overall}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Overall Score", color = TextSecondary, fontSize = 14.sp)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            data.asoScore.breakdown.forEach { (metric, score) ->
                ScoreBar(metric, score)
            }
        }
    }
}

@Composable
private fun CompetitionSection(data: AppData) {
    InfoCard(title = "Competition Analysis") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CompetitionBox("Ranking", data.competition.ranking, Modifier.weight(1f))
            CompetitionBox("Level", data.competition.level, Modifier.weight(1f))
        }
        
        Spacer(Modifier.height(12.dp))
        CompetitionBox("Market Saturation", data.competition.saturation, Modifier.fillMaxWidth())
        
        Spacer(Modifier.height(20.dp))
        Text("Opportunities", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.competition.opportunities.forEach { opp ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(opp, fontSize = 14.sp, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun CloneFeasibilitySection(data: AppData) {
    val scoreColor = remember(data.cloneFeasibility.score) {
        getScoreColor(data.cloneFeasibility.score * 10)
    }
    
    InfoCard(title = "Clone Feasibility") {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${data.cloneFeasibility.score}/10",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = scoreColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        data.cloneFeasibility.difficulty,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = scoreColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    data.cloneFeasibility.recommendation,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        
        Text("Key Factors", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        
        FactorBox("Technical Complexity", data.cloneFeasibility.factors.technical)
        FactorBox("Market Demand", data.cloneFeasibility.factors.demand)
        FactorBox("Competition Barrier", data.cloneFeasibility.factors.barrier)

        Spacer(Modifier.height(20.dp))
        
        Text("Required Features", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            data.cloneFeasibility.factors.uniqueFeatures.forEach { feature ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceLight)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PrimaryBlue)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(feature, fontSize = 14.sp, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun RecommendationsSection(data: AppData) {
    InfoCard(title = "ASO Recommendations") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            data.recommendations.forEachIndexed { index, rec ->
                RecommendationItem(index, rec)
            }
        }
    }
}
