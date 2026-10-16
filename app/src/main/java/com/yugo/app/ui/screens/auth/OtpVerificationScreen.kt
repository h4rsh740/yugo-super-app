package com.yugo.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.api.ApiResult
import com.yugo.app.data.local.SessionManager
import com.yugo.app.data.model.User
import com.yugo.app.data.repository.YugoRepository
import com.yugo.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phone: String,
    initialTestOtp: String? = "123456",
    repository: YugoRepository = remember { YugoRepository() },
    onBackClick: () -> Unit,
    onVerificationSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var otpCode by remember { mutableStateOf(initialTestOtp ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendTimerSeconds by remember { mutableStateOf(30) }
    var canResend by remember { mutableStateOf(false) }

    // Countdown Timer
    LaunchedEffect(resendTimerSeconds) {
        if (resendTimerSeconds > 0) {
            delay(1000)
            resendTimerSeconds -= 1
        } else {
            canResend = true
        }
    }

    fun submitOtp() {
        if (otpCode.trim().length != 6) {
            errorMessage = "Please enter the 6-digit verification code"
            return
        }
        focusManager.clearFocus()
        errorMessage = null
        isLoading = true

        coroutineScope.launch {
            when (val result = repository.verifyOtp(phone, otpCode.trim())) {
                is ApiResult.Success -> {
                    isLoading = false
                    val data = result.data
                    if (data.accessToken != null && data.refreshToken != null && data.user != null) {
                        val u = data.user
                        val domainUser = User(
                            id = u.id,
                            phone = u.phone,
                            name = u.name,
                            email = u.email,
                            rating = u.rating,
                            yugoCashBalance = u.yugoCashBalance,
                            isPhoneVerified = u.isPhoneVerified
                        )
                        SessionManager.getInstance(context).saveAuthSession(
                            accessToken = data.accessToken,
                            refreshToken = data.refreshToken,
                            user = domainUser
                        )
                        onVerificationSuccess()
                    } else {
                        errorMessage = "Invalid response from server"
                    }
                }
                is ApiResult.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun resendOtp() {
        if (!canResend) return
        canResend = false
        resendTimerSeconds = 30
        errorMessage = null

        coroutineScope.launch {
            repository.requestOtp(phone)
        }
    }

    Scaffold(
        containerColor = YugoWhite,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = YugoBlack)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YugoWhite)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Verify Phone Number",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        color = YugoBlack
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Code sent to $phone",
                        style = MaterialTheme.typography.bodyLarge.copy(color = YugoTextSecondary)
                    )
                    Text(
                        text = "Edit",
                        color = YugoGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onBackClick() }
                    )
                }

                // Dev autofill hint pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.clickable { otpCode = "123456" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Test OTP: 123456 (Tap to fill)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // OTP Display Boxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until 6) {
                        val digit = otpCode.getOrNull(i)?.toString() ?: ""
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (digit.isNotEmpty()) YugoWhite else YugoSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (digit.isNotEmpty()) YugoBlack else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = digit,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        color = YugoBlack
                                    )
                                )
                            }
                        }
                    }
                }

                // Hidden/Overlay text field for keyboard capture
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { input ->
                        if (input.filter { it.isDigit() }.length <= 6) {
                            otpCode = input.filter { it.isDigit() }
                            errorMessage = null
                            if (otpCode.length == 6) {
                                submitOtp()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitOtp() }
                    ),
                    placeholder = { Text("Enter 6 digits manually", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YugoDivider,
                        unfocusedBorderColor = YugoDivider
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = YugoRed,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Resend Countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canResend) {
                        Text(
                            text = "Didn't receive code? Resend OTP",
                            color = YugoBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { resendOtp() }
                        )
                    } else {
                        Text(
                            text = "Resend OTP in ${resendTimerSeconds}s",
                            color = YugoTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Verify & Sign In Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = { submitOtp() },
                    enabled = otpCode.length == 6 && !isLoading,
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
                        Text(
                            text = "Verify & Sign In",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
