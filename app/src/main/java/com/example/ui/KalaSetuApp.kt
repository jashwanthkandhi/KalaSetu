package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.core.model.*
import com.example.ui.screens.*
import com.example.ui.theme.KalaSetuTheme
import com.example.ui.viewmodel.KalaSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun KalaSetuApp(viewModel: KalaSetuViewModel = viewModel()) {
    val nav = rememberNavController()
    val tutorial by viewModel.tutorialSeen.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    val message by viewModel.message.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val processing by viewModel.isProcessing.collectAsState()
    val products by viewModel.products.collectAsState()
    val discoveries by viewModel.discovery.collectAsState()
    val start = remember { if (tutorial) "home" else "onboarding" }
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: start
    val snackbar = remember { SnackbarHostState() }
    val tabs = listOf("home" to "Home", "catalog" to "Catalog", "capture" to "Create", "insights" to "Insights", "profile" to "Profile")
    val icons = listOf(Icons.Default.Home, Icons.Default.GridView, Icons.Default.AddCircleOutline, Icons.Default.BarChart, Icons.Default.PersonOutline)
    fun navigate(destination: String) { nav.navigate(destination) { launchSingleTop = true } }
    fun select(product: Product, owned: Boolean = true) { navigate("detail/${product.id}/$owned") }
    val density = LocalDensity.current
    val dark = when(prefs.theme) { "Dark" -> true; "Light" -> false; else -> isSystemInDarkTheme() }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); viewModel.message.value = null } }
    LaunchedEffect(route) { viewModel.speakGuidance(translated(tabs.find { it.first == route }?.second ?: route, language)) }
    CompositionLocalProvider(LocalAppLanguage provides language,
        LocalDensity provides Density(density.density, density.fontScale * if (prefs.largerText || prefs.simpleView) 1.18f else 1f)) {
        KalaSetuTheme(darkTheme = dark, highContrast = prefs.highContrast) {
            Scaffold(
                topBar = { if (route !in tabs.map { it.first } && route != "onboarding" && route != "processing")
                        TopAppBar(title = { Text(when { route == "review" -> label("Review listing"); route.startsWith("detail") -> label("Product details"); route == "settings" -> label("Settings"); else -> "KalaSetu" }) }, navigationIcon = {
                        IconButton({ nav.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                    }) },
                bottomBar = {
                    if (route in tabs.map { it.first }) NavigationBar {
                        tabs.forEachIndexed { index, (destination, title) ->
                            NavigationBarItem(selected = route == destination, onClick = {
                                if (!processing) nav.navigate(destination) {
                                    popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true
                                }
                            }, icon = { Icon(icons[index], null) }, label = { Text(label(title), maxLines = 1) })
                        }
                    }
                }, snackbarHost = { SnackbarHost(snackbar) }
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding)) {
                    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                    NavHost(nav, startDestination = start, modifier = Modifier.weight(1f)) {
                        composable("onboarding") { OnboardingScreen(viewModel) { nav.navigate("home") { popUpTo("onboarding") { inclusive = true } } } }
                        composable("home") { HomeScreen(viewModel, ::navigate) { select(it) } }
                        composable("catalog") { CatalogScreen(viewModel, { navigate("capture") }, { select(it) }) }
                        composable("drafts") { CatalogScreen(viewModel, { navigate("capture") }, { select(it) }, ListingStatus.DRAFT) }
                        composable("capture") { CaptureScreen(viewModel, { navigate("processing") }, { navigate("catalog") }) }
                        composable("processing") { ProcessingScreen(viewModel,
                            { nav.navigate("review") { popUpTo("processing") { inclusive = true } } }, { nav.popBackStack() }) }
                        composable("review") { ReviewEditScreen(viewModel, { nav.popBackStack() },
                            { nav.navigate("catalog") { popUpTo("home") { inclusive = false } } }) }
                        composable("insights") { InsightsScreen(viewModel) }
                        composable("profile") { ProfileScreen(viewModel) { navigate("settings") } }
                        composable("settings") { SettingsScreen(viewModel, ::navigate) }
                        composable("sync") { SyncScreen(viewModel) { select(it) } }
                        composable("discover") { DiscoveryScreen(viewModel) { select(it, false) } }
                        composable("detail/{id}/{owned}") { backStack ->
                            val owned = backStack.arguments?.getString("owned") == "true"
                            val product = (if (owned) products else discoveries).find { it.id == backStack.arguments?.getString("id") }
                            if (product != null) DetailScreen(product, viewModel, owned, { target ->
                                if (target.title.isBlank()) { viewModel.resumeCapture(target); navigate("capture") }
                                else { viewModel.setDraftForReview(target); navigate("review") }
                            }, { nav.popBackStack() })
                            else Text(label("This product is unavailable. Return to the catalog."))
                        }
                    }
                }
            }
        }
    }
}
