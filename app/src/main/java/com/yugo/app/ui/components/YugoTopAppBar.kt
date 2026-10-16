package com.yugo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.ui.theme.*

@Composable
fun YugoTopAppBar(
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    Surface(
        color = YugoWhite,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // YUGO Wordmark Logo
            Text(
                text = "YUGO",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = (-0.8).sp
                ),
                color = YugoBlack
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(YugoSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = YugoBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(YugoBlack)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = YugoWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
