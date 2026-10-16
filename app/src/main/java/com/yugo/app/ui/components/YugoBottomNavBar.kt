package com.yugo.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.ui.theme.*

enum class YugoBottomDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "home"),
    SERVICES("Services", Icons.Filled.GridView, Icons.Outlined.GridView, "services"),
    ACTIVITY("Activity", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "activity"),
    ACCOUNT("Account", Icons.Filled.Person, Icons.Outlined.Person, "account")
}

@Composable
fun YugoBottomNavBar(
    currentRoute: String,
    onDestinationSelected: (YugoBottomDestination) -> Unit
) {
    Surface(
        color = YugoWhite,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(color = YugoDivider)
        NavigationBar(
            containerColor = YugoWhite,
            contentColor = YugoBlack,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            YugoBottomDestination.entries.forEach { destination ->
                val isSelected = currentRoute == destination.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.title,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = destination.title,
                            fontSize = 11.sp,
                            color = if (isSelected) YugoBlack else YugoTextSecondary
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = YugoBlack,
                        selectedTextColor = YugoBlack,
                        indicatorColor = YugoSurfaceVariant,
                        unselectedIconColor = YugoTextSecondary,
                        unselectedTextColor = YugoTextSecondary
                    )
                )
            }
        }
    }
}
