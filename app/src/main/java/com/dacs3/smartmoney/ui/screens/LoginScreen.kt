package com.dacs3.smartmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dacs3.smartmoney.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dacs3.smartmoney.R
import com.google.firebase.auth.FirebaseAuthException

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    // Khai báo các chuỗi thông báo lỗi từ resources
    val errFillAll = stringResource(R.string.error_fill_all)
    val errInvalidCredentials = stringResource(R.string.error_login_invalid_credentials)
    val errUserNotFound = stringResource(R.string.error_login_user_not_found)
    val errNetwork = stringResource(R.string.error_login_network)
    val errUnknown = stringResource(R.string.error_login_unknown)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PinkLight, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Chill Wallet",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = PinkDark
            )
            
            Text(
                text = "Quản lý chi tiêu thông minh",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MediumText
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = PinkPrimary.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = PinkPrimary.copy(alpha = 0.5f)
                )
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = PinkPrimary)
            } else {
                Button(
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            errorMessage = errFillAll
                            return@Button
                        }
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    onLoginSuccess()
                                } else {
                                    val exception = task.exception
                                    errorMessage = when (exception) {
                                        is FirebaseAuthException -> {
                                            when (exception.errorCode) {
                                                "ERROR_INVALID_CREDENTIALS", "ERROR_WRONG_PASSWORD" -> errInvalidCredentials
                                                "ERROR_USER_NOT_FOUND" -> errUserNotFound
                                                "ERROR_NETWORK_REQUEST_FAILED" -> errNetwork
                                                else -> exception.localizedMessage ?: errUnknown
                                            }
                                        }
                                        else -> exception?.localizedMessage ?: errUnknown
                                    }
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                ) {
                    Text(
                        "ĐĂNG NHẬP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        "Chưa có tài khoản? Đăng ký ngay",
                        color = PinkDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
