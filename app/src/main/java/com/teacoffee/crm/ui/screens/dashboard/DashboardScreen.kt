package com.teacoffee.crm.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teacoffee.crm.ui.screens.leads.ImportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    importViewModel: ImportViewModel
) {
    val state by importViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TeaCoffee CRM", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            Text(
                "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Leads: ${state.leads.size}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { ActionCard("Import Excel", Icons.Filled.FileUpload) { navController.navigate("leads") } }
                item { ActionCard("Sync Gmail", Icons.Filled.Email) { navController.navigate("gmail_sync") } }
                item { ActionCard("Bulk WhatsApp", Icons.Filled.Chat) { navController.navigate("messaging") } }
                item { ActionCard("AI Follow-Up", Icons.Filled.SmartToy) { navController.navigate("leads") } }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Stats
            Text("Quick Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCard("New", state.leads.count { it.status == "NEW" }.toString(), MaterialTheme.colorScheme.primary)
                StatCard("Follow-Up", state.leads.count { it.status == "FOLLOW_UP" }.toString(), MaterialTheme.colorScheme.tertiary)
                StatCard("Converted", state.leads.count { it.status == "CONVERTED" }.toString(), MaterialTheme.colorScheme.secondary)
                StatCard("Total", state.leads.size.toString(), MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.width(80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
