package com.teacoffee.crm.ui.screens.leads

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsScreen(
    viewModel: LeadsViewModel,
    onNavigateToDetail: (Long) -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val excelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.importExcel(context, uri)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        TextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                viewModel.search(it)
                            },
                            placeholder = { Text("Search leads...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    } else {
                        Text("Leads", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch; if (!showSearch) { searchText = ""; viewModel.search("") } }) {
                        Icon(if (showSearch) Icons.Filled.Close else Icons.Filled.Search, "Search")
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Filled.FilterList, "Filter")
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        }
                        excelLauncher.launch(intent)
                    }) {
                        Icon(Icons.Filled.FileUpload, "Import Excel")
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.leads.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.PeopleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No leads found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                    Text("Import Excel or sync Gmail to add leads", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.leads, key = { it.id }) { lead ->
                        LeadCard(lead = lead, viewModel = viewModel, onClick = { onNavigateToDetail(lead.id) })
                    }
                }
            }

            if (state.showImportDialog && state.importResult != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissImportDialog() },
                    title = { Text("Import Results") },
                    text = {
                        Column {
                            Text("Total rows: ${state.importResult!!.totalRows}")
                            Text("Imported: ${state.importResult!!.importedRows}")
                            if (state.importResult!!.errors.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Errors:", fontWeight = FontWeight.Bold)
                                state.importResult!!.errors.take(5).forEach { error ->
                                    Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissImportDialog() }) { Text("OK") }
                    }
                )
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            selectedClientType = state.selectedClientType,
            selectedStatus = state.selectedStatus,
            onCategorySelect = { viewModel.selectCategory(it) },
            onClientTypeSelect = { viewModel.selectClientType(it) },
            onStatusSelect = { viewModel.selectStatus(it) },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
fun LeadCard(
    lead: com.teacoffee.crm.data.local.entity.LeadEntity,
    viewModel: LeadsViewModel,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
    val statusColor = when (lead.status) {
        "NEW" -> com.teacoffee.crm.ui.theme.StatusNew
        "CONTACTED" -> com.teacoffee.crm.ui.theme.StatusContacted
        "FOLLOW_UP" -> com.teacoffee.crm.ui.theme.StatusFollowUp
        "CONVERTED" -> com.teacoffee.crm.ui.theme.StatusConverted
        "CLOSED" -> com.teacoffee.crm.ui.theme.StatusClosed
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lead.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = lead.status.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (lead.company.isNotBlank()) {
                Text(lead.company, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.width(4.dp))
                Text(lead.phone.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.width(4.dp))
                Text(lead.email.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            if (lead.productRequirement.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Interested: ${lead.productRequirement}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (lead.status == "NEW") {
                    FilledTonalButton(
                        onClick = { viewModel.updateLeadStatus(lead.id, "CONTACTED") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("Contact", style = MaterialTheme.typography.labelSmall) }
                }
                if (lead.status == "CONTACTED") {
                    FilledTonalButton(
                        onClick = { viewModel.updateLeadStatus(lead.id, "FOLLOW_UP") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("Follow Up", style = MaterialTheme.typography.labelSmall) }
                }
                if (lead.status == "FOLLOW_UP") {
                    FilledTonalButton(
                        onClick = { viewModel.updateLeadStatus(lead.id, "CONVERTED") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("Convert", style = MaterialTheme.typography.labelSmall) }
                }
                FilledTonalButton(
                    onClick = { viewModel.deleteLead(lead) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                ) { Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    categories: List<com.teacoffee.crm.data.local.entity.CategoryEntity>,
    selectedCategory: Long?,
    selectedClientType: String?,
    selectedStatus: String?,
    onCategorySelect: (Long?) -> Unit,
    onClientTypeSelect: (String?) -> Unit,
    onStatusSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filter Leads", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text("By Category", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(categories.filter { it.type == "PRODUCT" || it.type == "EQUIPMENT" }) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.id,
                        onClick = { onCategorySelect(if (selectedCategory == cat.id) null else cat.id) },
                        label = { Text(cat.name) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("By Client Type", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(categories.filter { it.type == "CLIENT_TYPE" }) { cat ->
                    FilterChip(
                        selected = selectedClientType == cat.name.uppercase(),
                        onClick = { onClientTypeSelect(if (selectedClientType == cat.name.uppercase()) null else cat.name.uppercase()) },
                        label = { Text(cat.name) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("By Status", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("NEW", "CONTACTED", "FOLLOW_UP", "CONVERTED", "CLOSED").forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onStatusSelect(if (selectedStatus == status) null else status) },
                        label = { Text(status.replace("_", " ")) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
