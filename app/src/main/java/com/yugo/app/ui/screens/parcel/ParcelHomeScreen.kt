package com.yugo.app.ui.screens.parcel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.model.ParcelCategory
import com.yugo.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParcelHomeScreen(
    viewModel: ParcelViewModel,
    onBackClick: () -> Unit,
    onBookClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var recipientName by remember { mutableStateOf(uiState.recipientName) }
    var recipientPhone by remember { mutableStateOf(uiState.recipientPhone) }
    var recipientAddress by remember { mutableStateOf(uiState.recipientAddress) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "YUGO Parcel",
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
                            viewModel.updateRecipient(recipientName, recipientPhone, recipientAddress)
                            viewModel.confirmParcelBooking()
                            onBookClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YugoBlack,
                            contentColor = YugoWhite
                        )
                    ) {
                        Text(
                            text = "Book Parcel • ₹${uiState.estimatedPrice.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
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
            item {
                com.yugo.app.ui.components.image.ParcelHeaderBanner()
            }

            // Recipient Information Form
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = YugoSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Recipient Details",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Recipient Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = YugoWhite,
                                focusedContainerColor = YugoWhite
                            )
                        )

                        OutlinedTextField(
                            value = recipientPhone,
                            onValueChange = { recipientPhone = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Recipient Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = YugoWhite,
                                focusedContainerColor = YugoWhite
                            )
                        )

                        OutlinedTextField(
                            value = recipientAddress,
                            onValueChange = { recipientAddress = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Drop-off Address") },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = YugoWhite,
                                focusedContainerColor = YugoWhite
                            )
                        )
                    }
                }
            }

            // Category Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "What are you sending?",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ParcelCategory.entries.forEach { category ->
                            val isSelected = uiState.selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(category) },
                                label = {
                                    Text(
                                        category.name.replace("_", " "),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) YugoBlue else YugoTextPrimary
                                    )
                                },
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) YugoBlue else YugoDivider,
                                    borderWidth = if (isSelected) 1.5.dp else 1.dp
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = androidx.compose.ui.graphics.Color(0xFFEFF6FF),
                                    containerColor = YugoSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Package Weight Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Package Weight: ${uiState.weightKg} kg",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Slider(
                        value = uiState.weightKg,
                        onValueChange = { viewModel.setWeight(it) },
                        valueRange = 0.5f..15.0f,
                        steps = 28,
                        colors = SliderDefaults.colors(
                            thumbColor = YugoBlack,
                            activeTrackColor = YugoBlack,
                            inactiveTrackColor = YugoSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
