package com.yugo.app.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.yugo.app.data.api.ApiResult
import com.yugo.app.data.api.NotificationPreferencesDto
import com.yugo.app.data.local.SessionManager
import com.yugo.app.data.model.User
import com.yugo.app.data.repository.YugoRepository
import com.yugo.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    repository: YugoRepository = remember { YugoRepository() },
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val currentUser by sessionManager.currentUser.collectAsState()
    val walletBalance by sessionManager.walletBalance.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Modals state
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var showSavedPlacesDialog by remember { mutableStateOf(false) }
    var showEmergencyContactsDialog by remember { mutableStateOf(false) }
    var showNotificationPreferencesDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var savedAddresses by remember { mutableStateOf<List<com.yugo.app.data.api.SavedPlaceDto>>(emptyList()) }
    var topupReceipt by remember { mutableStateOf<Pair<Double, String>?>(null) }

    // Synchronize live profile on screen open
    LaunchedEffect(Unit) {
        when (val result = repository.fetchProfile()) {
            is ApiResult.Success -> {
                val p = result.data
                val updated = User(
                    id = p.id,
                    phone = p.phone,
                    name = p.name,
                    email = p.email,
                    rating = p.rating,
                    yugoCashBalance = p.yugoCashBalance,
                    isPhoneVerified = p.isPhoneVerified
                )
                sessionManager.updateUser(updated)
                savedAddresses = p.savedPlaces
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            Surface(color = YugoWhite) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(YugoWhite),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Info Card (Clickable to Edit)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = YugoWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, YugoDivider),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEditProfileDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val initials = (currentUser?.name ?: "Harsh Singh")
                            .split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                            .uppercase()

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(YugoBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials.ifBlank { "Y" },
                                color = YugoWhite,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.name ?: "Harsh Singh",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = YugoTextSecondary, modifier = Modifier.size(14.dp))
                            }

                            Text(
                                text = currentUser?.phone ?: "+91 98765 00112",
                                style = MaterialTheme.typography.bodyMedium,
                                color = YugoTextSecondary
                            )
                            if (!currentUser?.email.isNullOrBlank()) {
                                Text(
                                    text = currentUser!!.email!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = YugoTextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = YugoSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = YugoAmber, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${currentUser?.rating ?: 4.95} Rating",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Wallet Balance Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = YugoBlack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "YUGO Cash Balance",
                                style = MaterialTheme.typography.labelMedium,
                                color = YugoTextMuted
                            )
                            Text(
                                text = "₹${String.format("%.2f", walletBalance)}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = YugoWhite
                            )
                        }

                        Button(
                            onClick = { showAddMoneyDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = YugoWhite, contentColor = YugoBlack)
                        ) {
                            Text("+ Add Money", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Options List
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AccountOptionRow(
                        icon = Icons.Default.Place,
                        title = "Saved Places",
                        subtitle = "Home, Work & Delivery Addresses",
                        onClick = { showSavedPlacesDialog = true }
                    )
                    AccountOptionRow(
                        icon = Icons.Default.Security,
                        title = "Safety & Emergency Contacts",
                        subtitle = "Trusted contacts for trip sharing",
                        onClick = { showEmergencyContactsDialog = true }
                    )
                    AccountOptionRow(
                        icon = Icons.Default.Notifications,
                        title = "Notification Preferences",
                        subtitle = "Order updates, SMS, and WhatsApp",
                        onClick = { showNotificationPreferencesDialog = true }
                    )
                    AccountOptionRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "Help & Support",
                        subtitle = "24/7 YUGO Customer Care & FAQs",
                        onClick = { showHelpSupportDialog = true }
                    )
                    AccountOptionRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Log Out",
                        subtitle = "Sign out from this device",
                        onClick = { showLogoutConfirmDialog = true },
                        isDestructive = true
                    )
                }
            }
        }
    }

    // 1. Edit Profile Modal
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(currentUser?.name ?: "") }
        var editEmail by remember { mutableStateOf(currentUser?.email ?: "") }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSaving = true
                        coroutineScope.launch {
                            val res = repository.updateProfile(editName, editEmail)
                            if (res is ApiResult.Success) {
                                val p = res.data
                                sessionManager.updateUser(
                                    currentUser?.copy(name = p.name, email = p.email) ?: User(p.id, p.phone, p.name, p.email)
                                )
                            }
                            isSaving = false
                            showEditProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = YugoTextSecondary)
                }
            }
        )
    }

    // 2. Add Money / Wallet Top-Up Modal
    if (showAddMoneyDialog) {
        var topupAmount by remember { mutableStateOf("500") }
        var isProcessing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isProcessing) showAddMoneyDialog = false },
            title = { Text("Top Up YUGO Cash", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Select or enter amount to top up using Safe Test Mode UPI/Card:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("100", "250", "500", "1000").forEach { chip ->
                            FilterChip(
                                selected = topupAmount == chip,
                                onClick = { topupAmount = chip },
                                label = { Text("₹$chip") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = topupAmount,
                        onValueChange = { topupAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = topupAmount.toDoubleOrNull() ?: 100.0
                        isProcessing = true
                        coroutineScope.launch {
                            val res = repository.topupWallet(amount)
                            if (res is ApiResult.Success) {
                                sessionManager.updateWalletBalance(res.data)
                                val txnId = "TXN-TOPUP-${System.currentTimeMillis().toString().takeLast(6)}"
                                topupReceipt = Pair(amount, txnId)
                            }
                            isProcessing = false
                            showAddMoneyDialog = false
                        }
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = YugoWhite, strokeWidth = 2.dp)
                    } else {
                        Text("Pay ₹${topupAmount.ifBlank { "0" }} & Top Up")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddMoneyDialog = false },
                    enabled = !isProcessing
                ) {
                    Text("Cancel", color = YugoTextSecondary)
                }
            }
        )
    }

    // Top-Up Transaction Receipt Dialog
    if (topupReceipt != null) {
        AlertDialog(
            onDismissRequest = { topupReceipt = null },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = YugoGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Top-Up Successful", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "₹${String.format("%.2f", topupReceipt!!.first)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = YugoGreen
                    )
                    Text(
                        text = "Added to YUGO Cash Wallet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = YugoTextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = YugoSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Transaction Ref: ${topupReceipt!!.second}", style = MaterialTheme.typography.bodySmall, color = YugoTextMuted)
                            Text("Updated Balance: ₹${String.format("%.2f", walletBalance)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Payment Mode: Safe Test UPI / Card", style = MaterialTheme.typography.bodySmall, color = YugoTextSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { topupReceipt = null },
                    colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // 3. Saved Places Modal
    if (showSavedPlacesDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newAddress by remember { mutableStateOf("") }
        var selectedLabel by remember { mutableStateOf("OTHER") }
        var isAdding by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isAdding) showSavedPlacesDialog = false },
            title = { Text("Saved Places", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (savedAddresses.isEmpty()) {
                        Text(
                            text = "No saved places yet. Add your home, work, or favourite spot below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = YugoTextSecondary
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            savedAddresses.forEach { place ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(YugoSurfaceVariant)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val icon = when (place.label.uppercase()) {
                                        "HOME" -> Icons.Default.Home
                                        "WORK" -> Icons.Default.Work
                                        else -> Icons.Default.Place
                                    }
                                    Icon(icon, contentDescription = null, tint = YugoBlack, modifier = Modifier.size(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(place.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(place.address, style = MaterialTheme.typography.bodySmall, color = YugoTextSecondary, maxLines = 1)
                                    }
                                    place.id?.let { pid ->
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    repository.deleteSavedAddress(pid)
                                                    when (val res = repository.fetchProfile()) {
                                                        is ApiResult.Success -> savedAddresses = res.data.savedPlaces
                                                        else -> {}
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = YugoRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("Add New Place:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("HOME", "WORK", "OTHER").forEach { lbl ->
                            FilterChip(
                                selected = selectedLabel == lbl,
                                onClick = { selectedLabel = lbl },
                                label = { Text(lbl, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Place Name (e.g. Home, Gym)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Full Street Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newAddress.isNotBlank()) {
                            isAdding = true
                            coroutineScope.launch {
                                repository.addSavedAddress(newTitle, newAddress, selectedLabel)
                                when (val res = repository.fetchProfile()) {
                                    is ApiResult.Success -> savedAddresses = res.data.savedPlaces
                                    else -> {}
                                }
                                newTitle = ""
                                newAddress = ""
                                isAdding = false
                                showSavedPlacesDialog = false
                            }
                        } else {
                            showSavedPlacesDialog = false
                        }
                    },
                    enabled = !isAdding,
                    colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                ) {
                    if (isAdding) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = YugoWhite, strokeWidth = 2.dp)
                    } else {
                        Text("Save & Add")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSavedPlacesDialog = false },
                    enabled = !isAdding
                ) {
                    Text("Close", color = YugoTextSecondary)
                }
            }
        )
    }

    // 4. Emergency Contacts Modal
    if (showEmergencyContactsDialog) {
        var contactName by remember { mutableStateOf("") }
        var contactPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEmergencyContactsDialog = false },
            title = { Text("Emergency Contacts", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Share trip status & live location with loved ones during late-night rides.")
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Contact Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Phone Number (+91)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                            coroutineScope.launch {
                                repository.addEmergencyContact(contactName, contactPhone, "Family")
                                showEmergencyContactsDialog = false
                            }
                        } else {
                            showEmergencyContactsDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyContactsDialog = false }) {
                    Text("Close", color = YugoTextSecondary)
                }
            }
        )
    }

    // 5. Notification Preferences Modal
    if (showNotificationPreferencesDialog) {
        var rideUpdates by remember { mutableStateOf(true) }
        var foodUpdates by remember { mutableStateOf(true) }
        var parcelUpdates by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showNotificationPreferencesDialog = false },
            title = { Text("Notification Preferences", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ride Status Updates")
                        Switch(checked = rideUpdates, onCheckedChange = { rideUpdates = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Food Delivery Alerts")
                        Switch(checked = foodUpdates, onCheckedChange = { foodUpdates = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Parcel Tracking Alerts")
                        Switch(checked = parcelUpdates, onCheckedChange = { parcelUpdates = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.updatePreferences(
                                NotificationPreferencesDto(
                                    rideUpdates = rideUpdates,
                                    foodUpdates = foodUpdates,
                                    parcelUpdates = parcelUpdates
                                )
                            )
                            showNotificationPreferencesDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationPreferencesDialog = false }) {
                    Text("Cancel", color = YugoTextSecondary)
                }
            }
        )
    }

    // 6. Help & Support Modal
    if (showHelpSupportDialog) {
        AlertDialog(
            onDismissRequest = { showHelpSupportDialog = false },
            title = { Text("YUGO Help & Support", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("24/7 Dedicated Super App Support", fontWeight = FontWeight.SemiBold)
                    Text("• WhatsApp Support: +91 98765 00112")
                    Text("• Call Support: 1800-YUGO-APP")
                    Text("• Email: support@yugo.app")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Frequently Asked Questions:", fontWeight = FontWeight.SemiBold)
                    Text("Q: How does test payment work?\nA: Test payment allows instant confirmation without entering bank cards or UPI PINs.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpSupportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // 7. Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Log Out from YUGO?", fontWeight = FontWeight.Bold) },
            text = { Text("You will need to verify your phone number with an OTP to sign in again.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        sessionManager.clearSession()
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YugoRed)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = YugoTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AccountOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isDestructive) Color(0xFFFEE2E2) else YugoSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive) YugoRed else YugoBlack,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDestructive) YugoRed else YugoBlack
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = YugoTextSecondary
            )
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = YugoTextSecondary)
    }
}
