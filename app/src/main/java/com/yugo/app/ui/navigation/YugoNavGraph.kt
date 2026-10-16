package com.yugo.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.yugo.app.ui.components.YugoBottomNavBar
import com.yugo.app.ui.components.YugoBottomDestination
import com.yugo.app.ui.screens.account.AccountScreen
import com.yugo.app.ui.screens.activity.ActivityScreen
import com.yugo.app.ui.screens.auth.LoginScreen
import com.yugo.app.ui.screens.auth.OtpVerificationScreen
import com.yugo.app.ui.screens.food.*
import com.yugo.app.ui.screens.home.HomeScreen
import com.yugo.app.ui.screens.parcel.*
import com.yugo.app.ui.screens.ride.*
import com.yugo.app.ui.screens.services.ServicesScreen

@Composable
fun YugoApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: YugoBottomDestination.HOME.route

    val rideViewModel: RideBookingViewModel = viewModel()
    val foodViewModel: FoodViewModel = viewModel()
    val parcelViewModel: ParcelViewModel = viewModel()

    val showBottomBar = currentRoute in listOf("home", "services", "activity", "account")

    Scaffold(
        containerColor = com.yugo.app.ui.theme.YugoWhite,
        bottomBar = {
            if (showBottomBar) {
                YugoBottomNavBar(
                    currentRoute = currentRoute,
                    onDestinationSelected = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                com.yugo.app.ui.screens.splash.SplashScreen(
                    onSplashFinished = { isLoggedIn ->
                        val destination = if (isLoggedIn) "home" else "login"
                        navController.navigate(destination) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    onOtpRequested = { phone, testOtp ->
                        val code = testOtp ?: "123456"
                        navController.navigate("verify_otp/$phone/$code")
                    }
                )
            }

            composable(
                route = "verify_otp/{phone}/{testOtp}",
                arguments = listOf(
                    navArgument("phone") { type = NavType.StringType },
                    navArgument("testOtp") { type = NavType.StringType; defaultValue = "123456" }
                )
            ) { backStackEntry ->
                val phone = backStackEntry.arguments?.getString("phone") ?: ""
                val testOtp = backStackEntry.arguments?.getString("testOtp")
                OtpVerificationScreen(
                    phone = phone,
                    initialTestOtp = testOtp,
                    onBackClick = { navController.popBackStack() },
                    onVerificationSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    onNavigateToRideSearch = { navController.navigate("search_ride") },
                    onNavigateToFood = { navController.navigate("food_home") },
                    onNavigateToParcel = { navController.navigate("parcel_home") },
                    onNavigateToAccount = { navController.navigate("account") },
                    onNavigateToActivity = { navController.navigate("activity") }
                )
            }

            composable("services") {
                ServicesScreen(
                    onNavigateToRide = { navController.navigate("search_ride") },
                    onNavigateToFood = { navController.navigate("food_home") },
                    onNavigateToParcel = { navController.navigate("parcel_home") }
                )
            }

            composable("activity") {
                ActivityScreen(
                    onReorderClick = { navController.navigate("search_ride") }
                )
            }

            composable("account") {
                AccountScreen(
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable("search_ride") {
                SearchLocationScreen(
                    onBackClick = { navController.popBackStack() },
                    onLocationSelected = { location ->
                        rideViewModel.setDestination(location)
                        navController.navigate("choose_ride")
                    }
                )
            }

            composable("choose_ride") {
                ChooseRideScreen(
                    viewModel = rideViewModel,
                    onBackClick = { navController.popBackStack() },
                    onConfirmBooking = { navController.navigate("active_ride") }
                )
            }

            composable("active_ride") {
                ActiveRideScreen(
                    viewModel = rideViewModel,
                    onCancelClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    onCompleted = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
                )
            }

            composable("food_home") {
                FoodHomeScreen(
                    viewModel = foodViewModel,
                    onBackClick = { navController.popBackStack() },
                    onRestaurantClick = { restaurant ->
                        foodViewModel.selectRestaurant(restaurant)
                        navController.navigate("restaurant_detail")
                    }
                )
            }

            composable("restaurant_detail") {
                RestaurantDetailScreen(
                    viewModel = foodViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCheckoutClick = { navController.navigate("food_checkout") }
                )
            }

            composable("food_checkout") {
                FoodCheckoutScreen(
                    viewModel = foodViewModel,
                    onBackClick = { navController.popBackStack() },
                    onPlaceOrderClick = { navController.navigate("activity") }
                )
            }

            composable("parcel_home") {
                ParcelHomeScreen(
                    viewModel = parcelViewModel,
                    onBackClick = { navController.popBackStack() },
                    onBookClick = { navController.navigate("parcel_tracking") }
                )
            }

            composable("parcel_tracking") {
                ParcelTrackingScreen(
                    viewModel = parcelViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
