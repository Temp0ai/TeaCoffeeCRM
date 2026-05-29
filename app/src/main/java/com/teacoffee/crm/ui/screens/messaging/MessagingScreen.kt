package com.teacoffee.crm.ui.screens.messaging

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    viewModel: MessagingViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showWebView by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messaging", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showWebView = !showWebView }) {
                        Icon(if (showWebView) Icons.Filled.Close else Icons.Filled.QrCode, "WhatsApp Web")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (showWebView) {
                WhatsAppWebView(modifier = Modifier.weight(1f))
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Compose") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Templates") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Campaigns") })
            }

            when (selectedTab) {
                0 -> ComposeTab(viewModel, state)
                1 -> TemplatesTab(viewModel, state)
                2 -> CampaignsTab(state)
            }
        }
    }
}

@Composable
fun WhatsAppWebView(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36"
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    webViewClient = WebViewClient()
                    loadUrl("https://web.whatsapp.com")
                }
            },
            modifier = Modifier.fillMaxSize().heightIn(max = 400.dp)
        )
    }
}

@Composable
fun ComposeTab(
    viewModel: MessagingViewModel,
    state: MessagingState
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        item {
            OutlinedTextField(
                value = state.campaignName,
                onValueChange = { viewModel.updateCampaignName(it) },
                label = { Text("Campaign Name (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Filter Leads", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (state.categories.isNotEmpty()) {
                Text("By Category:", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    state.categories.filter { it.type == "PRODUCT" || it.type == "EQUIPMENT" }.take(6).forEach { cat ->
                        FilterChip(
                            selected = cat.id in state.selectedCategoryIds,
                            onClick = { viewModel.toggleCategory(cat.id) },
                            label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val clientTypes = state.categories.filter { it.type == "CLIENT_TYPE" }
            if (clientTypes.isNotEmpty()) {
                Text("By Client Type:", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    clientTypes.forEach { cat ->
                        FilterChip(
                            selected = cat.name.uppercase() in state.selectedClientTypes,
                            onClick = { viewModel.toggleClientType(cat.name.uppercase()) },
                            label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.messageContent,
                onValueChange = { viewModel.updateMessageContent(it) },
                label = { Text("Message Content") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.generateAiMessage() },
                    enabled = !state.isAiGenerating
                ) {
                    if (state.isAiGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Generate")
                }

                Button(
                    onClick = { /* viewModel.sendBulkMessages() */ },
                    enabled = state.messageContent.isNotBlank()
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Bulk")
                }
            }

            if (state.sendProgress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.sendProgress.first.toFloat() / state.sendProgress.second.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Sending ${state.sendProgress.first}/${state.sendProgress.second}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Individual Leads", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(state.leads.filter { it.phone.isNotBlank() }, key = { it.id }) { lead ->
            val isSelected = lead.id in state.selectedLeadIds
            Card(
                onClick = { viewModel.toggleLeadSelection(lead.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { viewModel.toggleLeadSelection(lead.id) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lead.name, fontWeight = FontWeight.Medium)
                        Text("${lead.phone} | ${lead.productRequirement.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun TemplatesTab(
    viewModel: MessagingViewModel,
    state: MessagingState
) {
    if (state.templates.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No templates yet", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.templates) { template ->
                Card(
                    onClick = { viewModel.loadTemplate(template) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(template.title, fontWeight = FontWeight.Bold)
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    template.type.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(template.body, maxLines = 3, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Used ${template.usageCount} times", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
fun CampaignsTab(state: MessagingState) {
    if (state.campaigns.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No campaigns yet", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.campaigns) { campaign ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(campaign.name, fontWeight = FontWeight.Bold)
                        Surface(
                            color = when (campaign.status) {
                                "RUNNING" -> MaterialTheme.colorScheme.tertiaryContainer
                                "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer
                                "DRAFT" -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(campaign.status, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sent: ${campaign.sentCount} | Delivered: ${campaign.deliveredCount} | Failed: ${campaign.failedCount}", style = MaterialTheme.typography.bodySmall)
                        if (campaign.scheduledAt > 0) {
                            Text("Scheduled: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(campaign.scheduledAt))}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
