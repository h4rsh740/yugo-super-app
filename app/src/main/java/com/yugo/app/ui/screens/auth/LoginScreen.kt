package com.yugo.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.api.ApiResult
import com.yugo.app.data.repository.YugoRepository
import com.yugo.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: YugoRepository = remember { YugoRepository() },
    onOtpRequested: (phone: String, testOtp: String?) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val isPhoneValid = phoneNumber.filter { it.isDigit() }.length == 10

    fun submitPhone() {
        if (!isPhoneValid) {
            errorMessage = "Please enter a valid 10-digit Indian phone number"
            return
        }
        focusManager.clearFocus()
        errorMessage = null
        isLoading = true

        coroutineScope.launch {
            val formatted = "+91${phoneNumber.filter { it.isDigit() }}"
            when (val result = repository.requestOtp(formatted)) {
                is ApiResult.Success -> {
                    isLoading = false
                    onOtpRequested(formatted, result.data.testOtp)
                }
                is ApiResult.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    Scaffold(
        containerColor = YugoWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // YUGO Logo Mark
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = YugoBlack,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Y",
                            color = YugoWhite,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Welcome to YUGO",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        color = YugoBlack
                    )
                )

                Text(
                    text = "Enter your mobile number to get started with Rides, Food delivery, and Express Parcels.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = YugoTextSecondary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Development Mode Notice Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🧪", fontSize = 16.sp)
                            Text(
                                text = "Local Development Mode",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter any 10-digit number (e.g. 9876500112). Test OTP code is: 123456.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = Color(0xFF14532D)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mobile Number Input Card
                Text(
                    text = "Mobile Number",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = YugoTextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Country Code Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = YugoSurfaceVariant,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🇮🇳", fontSize = 20.sp)
                            Text(
                                text = "+91",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YugoBlack
                                )
                            )
                        }
                    }

                    // Number Input
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                            if (input.filter { it.isDigit() }.length <= 10) {
                                phoneNumber = input
                                errorMessage = null
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        placeholder = { Text("98765 00112", color = YugoTextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submitPhone() }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YugoBlack,
                            unfocusedBorderColor = YugoDivider,
                            focusedContainerColor = YugoWhite,
                            unfocusedContainerColor = YugoWhite
                        )
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = YugoRed,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }

            // Bottom Section: Continue Button & Terms
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { submitPhone() },
                    enabled = isPhoneValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YugoBlack,
                        disabledContainerColor = YugoSurfaceVariant,
                        contentColor = YugoWhite,
                        disabledContentColor = YugoTextMuted
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = YugoWhite,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Get OTP Verification Code",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Text(
                    text = "By continuing, you agree to YUGO's Terms of Service & Privacy Policy.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = YugoTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
