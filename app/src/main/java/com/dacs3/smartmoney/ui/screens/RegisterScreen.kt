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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dacs3.smartmoney.R
import com.dacs3.smartmoney.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MintLight, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.create_account),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = MintDark
            )
            
            Text(
                stringResource(R.string.register_subtitle),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MediumText,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.full_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintPrimary,
                    unfocusedBorderColor = MintPrimary.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintPrimary,
                    unfocusedBorderColor = MintPrimary.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintPrimary,
                    unfocusedBorderColor = MintPrimary.copy(alpha = 0.5f)
                )
            )

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = MintPrimary)
            } else {
                Button(
                    onClick = {
                        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
                        
                        when {
                            displayName.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                                error = context.getString(R.string.error_fill_all)
                            }
                            !emailPattern.matcher(email).matches() -> {
                                error = context.getString(R.string.error_invalid_email)
                            }
                            password.length < 6 -> {
                                error = context.getString(R.string.error_password_too_short)
                            }
                            else -> {
                                isLoading = true
                                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        val profileUpdates = userProfileChangeRequest {
                                            this.displayName = displayName
                                        }
                                        
                                        user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                                            if (profileTask.isSuccessful) {
                                                // Lưu vào Firestore
                                                val userData = hashMapOf(
                                                    "displayName" to displayName,
                                                    "email" to email,
                                                    "joinDate" to System.currentTimeMillis()
                                                )
                                                user.uid.let { uid ->
                                                    db.collection("users").document(uid).set(userData).addOnCompleteListener {
                                                        isLoading = false
                                                        onRegisterSuccess()
                                                    }
                                                }
                                            } else {
                                                isLoading = false
                                                error = profileTask.exception?.message ?: context.getString(R.string.error_update_profile)
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        error = task.exception?.message ?: context.getString(R.string.error_register_failed)
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintPrimary)
                ) {
                    Text(
                        stringResource(R.string.register_button),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onBackToLogin) {
                    Text(
                        stringResource(R.string.already_have_account),
                        color = MintDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
