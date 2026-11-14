package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ads.AdMobManager
import com.example.data.model.AppLanguage
import com.example.data.model.ThemePreference
import com.example.data.util.tr
import com.example.ui.screens.*
import com.example.ui.theme.IslamicQuizTheme
import com.example.ui.viewmodel.*

private data class TopNavItem(
    val route: String,
    val labelBn: String,
    val labelEn: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IslamicQuizRoot()
        }
    }
}

@Composable
fun IslamicQuizRoot() {
    val appViewModel: AppStateViewModel = viewModel()
    val quranViewModel: QuranViewModel = viewModel()
    val quizViewModel: QuizViewModel = viewModel()
    val tasbihViewModel: TasbihViewModel = viewModel()
    val duaViewModel: DuaViewModel = viewModel()

    val dashboardState by appViewModel.uiState.collectAsStateWithLifecycle()
    val quranState by quranViewModel.uiState.collectAsStateWithLifecycle()
    val quizState by quizViewModel.uiState.collectAsStateWithLifecycle()
    val tasbihState by tasbihViewModel.uiState.collectAsStateWithLifecycle()

    val settings = dashboardState.settings
    val lang = settings.language
    val darkTheme = when (settings.theme) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    IslamicQuizTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "home"
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(dashboardState.statusBannerMessage) {
            val msg = dashboardState.statusBannerMessage
            if (!msg.isNullOrBlank()) {
                snackbarHostState.showSnackbar(msg)
                appViewModel.clearBanner()
            }
        }

        val topNavItems = remember {
            listOf(
                TopNavItem("home", "হোম", "Home", Icons.Default.Home),
                TopNavItem("quran", "কুরআন", "Quran", Icons.AutoMirrored.Filled.MenuBook),
                TopNavItem("dua", "দোয়া", "Dua", Icons.Default.Favorite),
                TopNavItem("quiz", "কুইজ", "Quiz", Icons.Default.EmojiEvents),
                TopNavItem("prayer", "নামাজ", "Prayer", Icons.Default.AccessTimeFilled)
            )
        }

        val showPrimaryChrome = currentRoute in topNavItems.map { it.route } &&
            !(currentRoute == "quiz" && quizState.phase == QuizPhase.PLAYING) &&
            !(currentRoute == "quran" && quranState.activeSurah != null)

        val activity = LocalContext.current as? Activity

        val navigateToRoute: (String) -> Unit = { route ->
            val performNav = {
                if (route in topNavItems.map { it.route }) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                } else {
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            }
            if (route == "quiz" && currentRoute != "quiz") {
                AdMobManager.showInterstitialOnGameEntry(activity, forceIgnoreShortCooldown = true) {
                    performNav()
                }
            } else {
                performNav()
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showPrimaryChrome && !useRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            topNavItems.forEach { item ->
                                val selected = currentRoute == item.route
                                val label = if (lang == AppLanguage.BENGALI) item.labelBn else item.labelEn
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToRoute(item.route) },
                                    icon = { Icon(item.icon, contentDescription = label) },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_${item.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (showPrimaryChrome && useRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            windowInsets = WindowInsets.statusBars
                        ) {
                            Spacer(Modifier.height(12.dp))
                            topNavItems.forEach { item ->
                                val selected = currentRoute == item.route
                                val label = if (lang == AppLanguage.BENGALI) item.labelBn else item.labelEn
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToRoute(item.route) },
                                    icon = { Icon(item.icon, contentDescription = label) },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag("rail_${item.route}")
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        composable("home") {
                            HomeDashboardScreen(
                                uiState = dashboardState,
                                onNavigate = navigateToRoute
                            )
                        }
                        composable("quran") {
                            QuranScreen(
                                viewModel = quranViewModel,
                                uiState = quranState,
                                language = lang
                            )
                        }
                        composable("dua") {
                            DuaScreen(
                                viewModel = duaViewModel,
                                language = lang
                            )
                        }
                        composable("quiz") {
                            QuizScreen(
                                viewModel = quizViewModel,
                                uiState = quizState,
                                language = lang
                            )
                        }
                        composable("prayer") {
                            PrayerTimesScreen(
                                viewModel = appViewModel,
                                uiState = dashboardState
                            )
                        }
                        composable("tasbih") {
                            TasbihScreen(
                                viewModel = tasbihViewModel,
                                uiState = tasbihState,
                                language = lang,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("calendar") {
                            CalendarScreen(
                                settings = settings,
                                onHijriOffsetChange = { appViewModel.setHijriAdjustment(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = appViewModel,
                                quizViewModel = quizViewModel,
                                uiState = dashboardState,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
