package com.yugo.app.ui.components.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.local.SessionManager
import com.yugo.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSelectionBottomSheet(
    amount: Double,
    selectedMethod: String,
    onMethodSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onTopUpClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val walletBalance by sessionManager.walletBalance.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = YugoWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(YugoDivider)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Select Payment Method",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "Amount to Pay: ₹${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = YugoTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Text(
                        text = "Safe Test Mode",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = YugoDivider)

            // 1. YUGO Cash Wallet Option
            val isWalletSelected = selectedMethod.contains("Wallet", ignoreCase = true) || selectedMethod.contains("YUGO Cash", ignoreCase = true)
            val hasSufficientBalance = walletBalance >= amount

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isWalletSelected) Color(0xFFF0FDF4) else YugoWhite,
                border = androidx.compose.foundation.BorderStroke(
                    if (isWalletSelected) 2.dp else 1.dp,
                    if (isWalletSelected) Color(0xFF16A34A) else YugoDivider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onMethodSelected("YUGO Cash")
                        onDismissRequest()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = YugoWhite, modifier = Modifier.size(22.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "YUGO Cash Wallet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (hasSufficientBalance) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = if (hasSufficientBalance) "Active" else "Low Balance",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasSufficientBalance) Color(0xFF16A34A) else YugoRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Available Balance: ₹${String.format("%.2f", walletBalance)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = YugoTextSecondary)
                        )
                    }

                    if (!hasSufficientBalance) {
                        TextButton(onClick = onTopUpClick) {
                            Text("+ Add", fontWeight = FontWeight.Bold, color = YugoGreen)
                        }
                    } else if (isWalletSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                    }
                }
            }

            // 2. Safe Test UPI
            PaymentMethodTile(
                icon = Icons.Default.QrCodeScanner,
                iconBg = Color(0xFFEEF2FF),
                iconTint = Color(0xFF4F46E5),
                title = "UPI (GPay / PhonePe / Paytm)",
                subtitle = "Instant simulated UPI confirmation",
                badgeText = "Fast & Safe",
                isSelected = selectedMethod.contains("UPI", ignoreCase = true),
                onClick = {
                    onMethodSelected("Online UPI")
                    onDismissRequest()
                }
            )

            // 3. Safe Test Card
            PaymentMethodTile(
                icon = Icons.Default.CreditCard,
                iconBg = Color(0xFFFFFBEB),
                iconTint = Color(0xFFD97706),
                title = "Credit / Debit Card",
                subtitle = "Visa, Mastercard, RuPay (Test mode)",
                badgeText = "Auto-verified",
                isSelected = selectedMethod.contains("Card", ignoreCase = true),
                onClick = {
                    onMethodSelected("Card (Test Mode)")
                    onDismissRequest()
                }
            )

            // 4. Cash
            PaymentMethodTile(
                icon = Icons.Default.Payments,
                iconBg = Color(0xFFF1F5F9),
                iconTint = YugoBlack,
                title = "Cash on Delivery / Trip End",
                subtitle = "Pay directly to driver or delivery partner",
                badgeText = "Standard",
                isSelected = selectedMethod.contains("Cash", ignoreCase = true) && !isWalletSelected,
                onClick = {
                    onMethodSelected("Cash")
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun PaymentMethodTile(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFFF8FAFC) else YugoWhite,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) YugoBlack else YugoDivider
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    )
                    Surface(shape = RoundedCornerShape(4.dp), color = YugoSurfaceVariant) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = YugoTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = YugoTextSecondary)
                )
            }

            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = YugoBlack)
            }
        }
    }
}
