package com.teacoffee.crm.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teacoffee.crm.ui.screens.catalog.CatalogScreen
import com.teacoffee.crm.ui.screens.catalog.CatalogViewModel
import com.teacoffee.crm.ui.screens.dashboard.DashboardScreen
import com.teacoffee.crm.ui.screens.dashboard.DashboardViewModel
import com.teacoffee.crm.ui.screens.leads.LeadDetailScreen
import com.teacoffee.crm.ui.screens.leads.LeadDetailViewModel
import com.teacoffee.crm.ui.screens.leads.LeadsScreen
import com.teacoffee.crm.ui.screens.leads.LeadsViewModel
import com.teacoffee.crm.ui.screens.messaging.MessagingScreen
import com.teacoffee.crm.ui.screens.messaging.MessagingViewModel
import com.teacoffee.crm.ui.screens.seo.SeoScreen
import com.teacoffee.crm.ui.screens.seo.SeoScreenViewModel
import com.teacoffee.crm.ui.screens.settings.SettingsScreen
import com.teacoffee.crm.ui.screens.settings.SettingsViewModel

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    data object Leads : Screen("leads", "Leads", Icons.Filled.People, Icons.Outlined.People)
    data object Messaging : Screen("messaging", "Messaging", Icons.Filled.Send, Icons.Outlined.Send)
    data object Catalog : Screen("catalog", "Catalog", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    data object Seo : Screen("seo", "SEO Tools", Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    data object LeadDetail : Screen("lead_detail/{leadId}", "Lead Details", Icons.Filled.Person, Icons.Outlined.Person)
}

data class NavKey(val route: String, val navId: String = route)

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Leads,
    Screen.Messaging,
    Screen.Catalog,
    Screen.Seo
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == screen.route) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = vm,
                    onNavigateToLeads = { navController.navigate(Screen.Leads.route) },
                    onNavigateToMessaging = { navController.navigate(Screen.Messaging.route) },
                    onNavigateToCatalog = { navController.navigate(Screen.Catalog.route) },
                    onNavigateToSeo = { navController.navigate(Screen.Seo.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Leads.route) {
                val vm: LeadsViewModel = hiltViewModel()
                LeadsScreen(
                    viewModel = vm,
                    onNavigateToDetail = { leadId -> navController.navigate("lead_detail/$leadId") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.LeadDetail.route,
                arguments = listOf(navArgument("leadId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: return@composable
                val vm: LeadDetailViewModel = hiltViewModel()
                LeadDetailScreen(
                    viewModel = vm,
                    leadId = leadId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Messaging.route) {
                val vm: MessagingViewModel = hiltViewModel()
                MessagingScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Catalog.route) {
                val vm: CatalogViewModel = hiltViewModel()
                CatalogScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Seo.route) {
                val vm: SeoScreenViewModel = hiltViewModel()
                SeoScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
