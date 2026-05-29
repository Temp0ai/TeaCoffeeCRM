package com.teacoffee.crm.ui.screens.seo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeoScreen(
    viewModel: SeoScreenViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SEO & Marketing", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Keywords") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("AI Tools") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Content") })
            }

            when (selectedTab) {
                0 -> KeywordsTab(viewModel, state)
                1 -> AiToolsTab(viewModel, state)
                2 -> ContentTab(viewModel, state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordsTab(
    viewModel: SeoViewModel,
    state: SeoState
) {
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                viewModel.search(it)
            },
            placeholder = { Text("Search keywords...") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            singleLine = true
        )

        // Category filter
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedCategory == null,
                onClick = { viewModel.selectCategory(null) },
                label = { Text("All") }
            )
            listOf("PRODUCT", "BRAND", "GENERIC").forEach { cat ->
                FilterChip(
                    selected = state.selectedCategory == cat,
                    onClick = { viewModel.selectCategory(cat) },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.keywords.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.TravelExplore, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No keywords found. Use AI to generate.", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.keywords, key = { it.id }) { kw ->
                    KeywordCard(kw, viewModel)
                }
            }
        }
    }
}

@Composable
fun KeywordCard(
    keyword: com.teacoffee.crm.data.local.entity.SeoKeywordEntity,
    viewModel: SeoScreenViewModel
) {
    val competitionColor = when (keyword.competition) {
        "LOW" -> MaterialTheme.colorScheme.primary
        "MEDIUM" -> MaterialTheme.colorScheme.tertiary
        "HIGH" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(keyword.keyword, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Vol: ${keyword.searchVolume}", style = MaterialTheme.typography.bodySmall)
                    Text("Diff: ${keyword.difficulty}%", style = MaterialTheme.typography.bodySmall)
                    Surface(color = competitionColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                        Text(
                            keyword.competition,
                            style = MaterialTheme.typography.labelSmall,
                            color = competitionColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            IconButton(onClick = { viewModel.toggleTracking(keyword.id, !keyword.isTracked) }) {
                Icon(
                    if (keyword.isTracked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Track"
                )
            }
        }
    }
}

@Composable
fun AiToolsTab(
    viewModel: SeoScreenViewModel,
    state: SeoState
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI-Powered Marketing Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TravelExplore, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keyword Research", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Generate AI-powered keyword suggestions for tea & coffee products across local, national, and international markets.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.generateAiKeywords() },
                    enabled = !state.isGenerating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isGenerating) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Keywords")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Analytics, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Competitor Analysis", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Analyze competitors in the tea and coffee industry. Identify market gaps and opportunities.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Search, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Analyze Competitors")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Store, null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google My Business", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Connect GMB for local SEO optimization and AI-powered auto-response to customer inquiries.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Link, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Connect GMB Account")
                }
            }
        }
    }
}

@Composable
fun ContentTab(
    viewModel: SeoScreenViewModel,
    state: SeoState
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Social Media Content Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Generate Instagram & Social Media Content", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("AI-powered content creation for promoting tea and coffee products to target audiences including purchase managers, cafeteria staff, and business owners.", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.generateSocialContent() },
                    enabled = !state.isGenerating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(Icons.Filled.Image, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Social Content")
                }
            }
        }

        if (state.contentResults.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Generated Content", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.contentResults, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { }) {
                            Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                            Text(" Copy")
                        }
                        OutlinedButton(onClick = { }) {
                            Icon(Icons.Filled.Share, null, modifier = Modifier.size(16.dp))
                            Text(" Share")
                        }
                    }
                }
            }
        }
    }
}
