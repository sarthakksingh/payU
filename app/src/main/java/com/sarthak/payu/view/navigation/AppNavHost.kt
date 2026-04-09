package com.sarthak.payu.view.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sarthak.payu.data.model.PaymentMethod
import com.sarthak.payu.ui.theme.BlueAccent
import com.sarthak.payu.ui.theme.DarkBg
import com.sarthak.payu.ui.theme.DarkBorder
import com.sarthak.payu.ui.theme.DarkCard
import com.sarthak.payu.ui.theme.DarkSurface
import com.sarthak.payu.ui.theme.LightBg
import com.sarthak.payu.ui.theme.LightBorder
import com.sarthak.payu.ui.theme.LightCard
import com.sarthak.payu.ui.theme.LightSurface
import com.sarthak.payu.ui.theme.PinkAccent
import com.sarthak.payu.ui.theme.PurpleAccent
import com.sarthak.payu.ui.theme.TealGreen
import com.sarthak.payu.ui.theme.TextPrimary
import com.sarthak.payu.ui.theme.TextPrimaryLight
import com.sarthak.payu.ui.theme.TextSecondary
import com.sarthak.payu.ui.theme.TextSecondaryLight
import com.sarthak.payu.R
import com.sarthak.payu.view.screen.AddTransactionScreen
import com.sarthak.payu.view.screen.AuthScreen
import com.sarthak.payu.view.screen.BalancesScreen
import com.sarthak.payu.view.screen.CalendarScreen
import com.sarthak.payu.view.screen.HomeScreen
import com.sarthak.payu.view.screen.ProfileScreen
import com.sarthak.payu.vm.AuthViewModel
import kotlin.math.hypot

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel = hiltViewModel(),
    initialLoggedIn: Boolean = false
) {
    val navController = rememberNavController()
    val isDarkMode by authViewModel.isDarkMode.collectAsState()
    val startDestination = if (initialLoggedIn) Screen.Home.route else Screen.Login.route

    val bottomNavRoutes = listOf(Screen.Home.route, Screen.Balances.route, Screen.Profile.route)
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: startDestination
    val showBottomBar = currentRoute in bottomNavRoutes

    var themeRevealCount by remember { mutableIntStateOf(0) }
    var themeRevealActive by remember { mutableStateOf(false) }
    var themeRevealTargetDark by remember { mutableStateOf(false) }
    var themeRevealCenter by remember { mutableStateOf(Offset.Zero) }
    val themeRevealProgress = remember { Animatable(0f) }

    LaunchedEffect(themeRevealCount) {
        if (!themeRevealActive) return@LaunchedEffect
        if (themeRevealTargetDark) {
            themeRevealProgress.snapTo(0f)
            authViewModel.setDarkMode(true)
            themeRevealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1180,
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                )
            )
        } else {
            themeRevealProgress.snapTo(0f)
            themeRevealProgress.animateTo(
                targetValue = 0.82f,
                animationSpec = tween(
                    durationMillis = 1180,
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                )
            )
            authViewModel.setDarkMode(false)
            themeRevealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 220,
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                )
            )
        }
        themeRevealProgress.snapTo(0f)
        themeRevealActive = false
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val revealFraction = if (themeRevealTargetDark) 1f - themeRevealProgress.value else themeRevealProgress.value
        val revealRadius = revealFraction.coerceIn(0f, 1f) * hypot(screenWidthPx, screenHeightPx)
        val targetScheme = payULightScheme()

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    PayUBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
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
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding),
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                exitTransition = { slideOutHorizontally { -it } + fadeOut() },
                popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
                popExitTransition = { slideOutHorizontally { it } + fadeOut() }
            ) {
                composable(Screen.Login.route) {
                    AuthScreen(onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    })
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        onAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                        onGoToCalendar = { navController.navigate(Screen.Calendar.route) }
                    )
                }
                composable(Screen.Calendar.route) {
                    CalendarScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.Balances.route) {
                    BalancesScreen(onGoToCalendar = { navController.navigate(Screen.Calendar.route) })
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onGoToCalendar = { navController.navigate(Screen.Calendar.route) },
                        onThemeToggle = {
                            themeRevealTargetDark = !isDarkMode
                            themeRevealCenter = it
                            themeRevealActive = true
                            themeRevealCount += 1
                        }
                    )
                }
                composable(Screen.AddTransaction.route) {
                    AddTransactionScreen(onBack = { navController.popBackStack() })
                }
            }
        }

        if (themeRevealActive || themeRevealProgress.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleRevealShape(themeRevealCenter, revealRadius))
            ) {
                MaterialTheme(colorScheme = targetScheme) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            if (showBottomBar) {
                                PayUBottomBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
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
                    ) { padding ->
                        RoutePreview(
                            route = currentRoute,
                            navController = navController,
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutePreview(
    route: String,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (route) {
            Screen.Login.route -> AuthScreen(onAuthSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })

            Screen.Home.route -> HomeScreen(
                onAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onGoToCalendar = { navController.navigate(Screen.Calendar.route) }
            )

            Screen.Calendar.route -> CalendarScreen(onBack = { navController.popBackStack() })

            Screen.Balances.route -> BalancesScreen(onGoToCalendar = { navController.navigate(Screen.Calendar.route) })

            Screen.Profile.route -> ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGoToCalendar = { navController.navigate(Screen.Calendar.route) },
                onThemeToggle = {}
            )

            Screen.AddTransaction.route -> AddTransactionScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun payUDarkScheme() = darkColorScheme(
    primary = TealGreen,
    secondary = PinkAccent,
    tertiary = PurpleAccent,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = Color.Black,
    outline = DarkBorder
)

@Composable
private fun payULightScheme() = lightColorScheme(
    primary = Color(0xFF11857A),
    secondary = Color(0xFFCA2E78),
    tertiary = Color(0xFF6D4ACF),
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onPrimary = Color.White,
    outline = LightBorder
)

private class CircleRevealShape(
    private val center: Offset,
    private val radius: Float
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            addOval(
                Rect(
                    left = center.x - radius,
                    top = center.y - radius,
                    right = center.x + radius,
                    bottom = center.y + radius
                )
            )
        }
        return Outline.Generic(path)
    }
}

@Composable
fun PayUBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                Color(0xFF171726),
                                Color(0xFF1B1B2B),
                                Color(0xFF141420)
                            )
                        } else {
                            listOf(
                                Color(0xFFF1E6D8),
                                Color(0xFFE5D2C0),
                                Color(0xFFD7C2B1)
                            )
                        }
                    )
                )
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavPillItem(
                label = "Home",
                selected = currentRoute == Screen.Home.route,
                icon = Icons.Default.Home,
                modifier = Modifier.weight(1f)
            ) { onNavigate(Screen.Home.route) }

            NavPillItem(
                label = "Analytics",
                selected = currentRoute == Screen.Balances.route,
                icon = Icons.Default.PieChart,
                iconResId = R.drawable.analytics,
                modifier = Modifier.weight(1f)
            ) { onNavigate(Screen.Balances.route) }

            NavPillItem(
                label = "Profile",
                selected = currentRoute == Screen.Profile.route,
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f)
            ) { onNavigate(Screen.Profile.route) }
        }
    }
}

@Composable
private fun NavPillItem(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconResId: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val selectedBg = if (isDarkTheme) Color.White else Color(0xFFF7DDE5)
    val selectedContent = if (isDarkTheme) Color.Black else Color.White
    val unselectedContent = if (isDarkTheme) TextSecondary else TextSecondaryLight
    Box(
        modifier = modifier
            .height(54.dp)
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) selectedBg else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (iconResId != null) {
                androidx.compose.material3.Icon(
                    painter = painterResource(iconResId),
                    contentDescription = label,
                    tint = if (selected) selectedContent else unselectedContent,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                androidx.compose.material3.Icon(
                    icon,
                    contentDescription = label,
                    tint = if (selected) selectedContent else unselectedContent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                label,
                color = if (selected) selectedContent else unselectedContent,
                fontSize = 10.sp
            )
        }
    }
}
