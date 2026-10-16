package com.yugo.app.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodCheckoutScreen(
    viewModel: FoodViewModel,
    onBackClick: () -> Unit,
    onPlaceOrderClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val bill = uiState.billBreakdown

    val subtotal = bill?.subtotal ?: uiState.cartItems.sumOf { it.menuItem.price * it.quantity }
    val deliveryFee = bill?.deliveryFee ?: 35.0
    val packagingFee = bill?.packagingFee ?: 15.0
    val taxes = bill?.taxes ?: (subtotal * 0.05)
    val discount = bill?.discount ?: 0.0
    val grandTotal = bill?.totalAmount ?: (subtotal + deliveryFee + packagingFee + taxes - discount)

    var showAddressDialog by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var couponInput by remember { mutableStateOf(uiState.couponCode ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Checkout",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = YugoBlack)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YugoWhite)
            )
        },
        bottomBar = {
            Surface(
                color = YugoWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            viewModel.placeOrder {
                                onPlaceOrderClick()
                            }
                        },
                        enabled = uiState.cartItems.isNotEmpty() && !uiState.isPlacingOrder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YugoBlack,
                            contentColor = YugoWhite,
                            disabledContainerColor = YugoSurfaceVariant
                        )
                    ) {
                        if (uiState.isPlacingOrder) {
                            CircularProgressIndicator(color = YugoWhite, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Pay ₹${Math.round(grandTotal)} & Place Order",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
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
            // Delivery Address Card
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = YugoSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddressDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = YugoBlack)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Delivering to Address",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            )
                            Text(
                                text = uiState.deliveryAddress,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = YugoTextSecondary
                            )
                        }
                        Text("Change", color = YugoGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Coupon Code Card
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = YugoWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, YugoDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it.uppercase() },
                            placeholder = { Text("Enter Coupon (e.g. YUGO50)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = { viewModel.applyCoupon(couponInput.trim()) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }

            // Payment Method Selector
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = YugoWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, YugoDivider),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPaymentSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = YugoBlack)
                            Column {
                                Text("Payment Option", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(uiState.paymentMethod, color = YugoTextSecondary, fontSize = 12.sp)
                            }
                        }
                        Text("Change", color = YugoGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            item {
                Text(
                    text = "Order Summary (${uiState.selectedRestaurant?.name ?: "Restaurant"})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Cart Items List
            items(uiState.cartItems) { cartItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${cartItem.quantity}x ${cartItem.menuItem.name}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "₹${(cartItem.menuItem.price * cartItem.quantity).toInt()}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                HorizontalDivider(color = YugoDivider, modifier = Modifier.padding(vertical = 4.dp))
            }

            // Bill Breakdown
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item Total", color = YugoTextSecondary)
                        Text("₹${subtotal.toInt()}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Fee", color = YugoTextSecondary)
                        Text("₹${deliveryFee.toInt()}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Packaging Charges", color = YugoTextSecondary)
                        Text("₹${packagingFee.toInt()}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Govt Taxes (5% GST)", color = YugoTextSecondary)
                        Text("₹${taxes.toInt()}")
                    }
                    if (discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount (YUGO50)", color = YugoGreen)
                            Text("-₹${discount.toInt()}", color = YugoGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = YugoDivider, modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("To Pay", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            "₹${grandTotal.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }

    // Change Address Dialog
    if (showAddressDialog) {
        var tempAddr by remember { mutableStateOf(uiState.deliveryAddress) }
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Delivery Address", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempAddr,
                    onValueChange = { tempAddr = it },
                    label = { Text("Complete Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempAddr.isNotBlank()) {
                            viewModel.setDeliveryAddress(tempAddr.trim())
                        }
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                ) {
                    Text("Save Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancel", color = YugoTextSecondary)
                }
            }
        )
    }

    // Payment Selection Dialog
    if (showPaymentSheet) {
        AlertDialog(
            onDismissRequest = { showPaymentSheet = false },
            title = { Text("Select Payment Method", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Online UPI", "YUGO Cash Wallet", "Credit / Debit Card", "Cash on Delivery").forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setPaymentMethod(opt)
                                    showPaymentSheet = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = uiState.paymentMethod == opt,
                                onClick = {
                                    viewModel.setPaymentMethod(opt)
                                    showPaymentSheet = false
                                }
                            )
                            Text(opt, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentSheet = false }) {
                    Text("Close", color = YugoTextSecondary)
                }
            }
        )
    }
}
