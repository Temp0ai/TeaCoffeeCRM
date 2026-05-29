package com.teacoffee.crm.ui.screens.leads

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.teacoffee.crm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    viewModel: LeadDetailViewModel,
    leadId: Long,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showCategorySheet by remember { mutableStateOf(false) }
    var showCampaignDialog by remember { mutableStateOf(false) }
    var campaignName by remember { mutableStateOf("") }

    LaunchedEffect(leadId) {
        viewModel.loadLead(leadId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.lead?.name ?: "Lead Details", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.lead == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Lead not found")
            }
        } else {
            val lead = state.lead!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lead.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            val statusColor = when (lead.status) {
                                "NEW" -> StatusNew; "CONTACTED" -> StatusContacted
                                "FOLLOW_UP" -> StatusFollowUp; "CONVERTED" -> StatusConverted
                                "CLOSED" -> StatusClosed; else -> MaterialTheme.colorScheme.outline
                            }
                            Surface(color = statusColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                                Text(
                                    lead.status.replace("_", " "),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        if (lead.company.isNotBlank()) {
                            DetailRow(label = "Company", value = lead.company)
                        }
                        if (lead.designation.isNotBlank()) {
                            DetailRow(label = "Designation", value = lead.designation)
                        }
                        DetailRow(label = "Phone", value = lead.phone.ifBlank { "-" })
                        DetailRow(label = "Email", value = lead.email.ifBlank { "-" })
                        if (lead.productRequirement.isNotBlank()) {
                            DetailRow(label = "Product Interest", value = lead.productRequirement)
                        }
                        if (lead.orderDetails.isNotBlank()) {
                            DetailRow(label = "Order Details", value = lead.orderDetails)
                        }
                        if (lead.inquiryDetails.isNotBlank()) {
                            DetailRow(label = "Inquiry", value = lead.inquiryDetails)
                        }
                        DetailRow(label = "Source", value = lead.source)
                        DetailRow(label = "Client Type", value = lead.clientType.ifBlank { "Not set" })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (lead.phone.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call")
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${lead.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Status Pipeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val pipelineSteps = listOf("NEW", "CONTACTED", "FOLLOW_UP", "CONVERTED")
                val currentStep = pipelineSteps.indexOf(lead.status)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    pipelineSteps.forEachIndexed { index, step ->
                        val isActive = index <= currentStep
                        val isCurrent = index == currentStep
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (isActive) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = step,
                                tint = if (isActive) StatusConverted else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                step.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                        if (index < pipelineSteps.lastIndex) {
                            Divider(
                                modifier = Modifier.width(8.dp).align(Alignment.CenterVertically),
                                color = if (isActive) StatusConverted else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = {
                            val nextSteps = mapOf(
                                "NEW" to "CONTACTED", "CONTACTED" to "FOLLOW_UP",
                                "FOLLOW_UP" to "CONVERTED"
                            )
                            nextSteps[lead.status]?.let { viewModel.updateStatus(it) }
                        },
                        enabled = lead.status != "CONVERTED" && lead.status != "CLOSED",
                        modifier = Modifier.weight(1f)
                    ) { Text("Advance Stage") }

                    OutlinedButton(
                        onClick = { viewModel.updateStatus("CLOSED") },
                        enabled = lead.status != "CLOSED",
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Close") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Category Assignment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showCategorySheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(state.categories.find { it.id == lead.categoryId }?.name ?: "Assign Category")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("AI Follow-Up", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.generateAiFollowUp() },
                    enabled = !state.isGeneratingMessage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isGeneratingMessage) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (state.isGeneratingMessage) "Generating..." else "Generate AI Follow-Up")
                }

                if (state.generatedMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(state.generatedMessage, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    campaignName = ""
                                    showCampaignDialog = true
                                }
                            ) { Text("Save as Campaign") }
                        }
                    }
                }
            }

            if (showCategorySheet) {
                ModalBottomSheet(onDismissRequest = { showCategorySheet = false }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Assign Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        state.categories.forEach { cat ->
                            FilterChip(
                                selected = lead.categoryId == cat.id,
                                onClick = {
                                    viewModel.assignCategory(if (lead.categoryId == cat.id) null else cat.id)
                                    showCategorySheet = false
                                },
                                label = { Text(cat.name) },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (showCampaignDialog) {
                AlertDialog(
                    onDismissRequest = { showCampaignDialog = false },
                    title = { Text("Save as Campaign") },
                    text = {
                        OutlinedTextField(
                            value = campaignName,
                            onValueChange = { campaignName = it },
                            label = { Text("Campaign Name") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (campaignName.isNotBlank()) {
                                viewModel.saveAsCampaign(campaignName)
                                showCampaignDialog = false
                            }
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCampaignDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.35f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.65f))
    }
}
