package com.dacs3.smartmoney.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dacs3.smartmoney.R
import com.dacs3.smartmoney.data.PreferenceManager
import com.dacs3.smartmoney.ui.theme.PinkDark
import com.dacs3.smartmoney.ui.theme.PinkPrimary
import com.dacs3.smartmoney.ui.theme.SoftGray
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenDrawer: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val preferenceManager = remember { PreferenceManager(context) }
    val isDarkMode by preferenceManager.isDarkMode.collectAsState(initial = false)
    val selectedLanguage by preferenceManager.language.collectAsState(initial = "Tiếng Việt")
    val isNotificationsEnabled by preferenceManager.isNotificationsEnabled.collectAsState(initial = true)

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showUpdateEmailDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }

    // Change Password State
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = SoftGray
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // User Profile Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(PinkPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.photoUrl != null) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                tint = PinkPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user?.displayName ?: stringResource(R.string.guest),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user?.email ?: stringResource(R.string.not_updated),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            SettingsGroup(title = stringResource(R.string.account_group)) {
                SettingsItem(
                    icon = Icons.Rounded.Lock,
                    title = stringResource(R.string.change_password),
                    subtitle = stringResource(R.string.update_password_subtitle),
                    onClick = { showChangePasswordDialog = true }
                )
                SettingsItem(
                    icon = Icons.Rounded.Email,
                    title = stringResource(R.string.update_email),
                    onClick = { showUpdateEmailDialog = true }
                )
            }

            SettingsGroup(title = stringResource(R.string.app_group)) {
                SettingsItem(
                    icon = Icons.Rounded.Language,
                    title = stringResource(R.string.language),
                    subtitle = selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
                SettingsItem(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.notifications),
                    trailing = {
                        Switch(
                            checked = isNotificationsEnabled,
                            onCheckedChange = { 
                                scope.launch { preferenceManager.setNotificationsEnabled(it) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PinkPrimary)
                        )
                    }
                )
                SettingsItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.dark_mode),
                    trailing = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { 
                                scope.launch { preferenceManager.setDarkMode(it) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PinkPrimary)
                        )
                    }
                )
            }

            SettingsGroup(title = stringResource(R.string.other_group)) {
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.about_app),
                    onClick = { 
                        Toast.makeText(context, "Chill Wallet v1.0.0 - DACS3", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsItem(
                    icon = Icons.Rounded.Star,
                    title = stringResource(R.string.rate_app),
                    onClick = { 
                        Toast.makeText(context, "Cảm ơn bạn đã ủng hộ!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "${stringResource(R.string.version)} 1.0.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text(stringResource(R.string.change_password)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text(stringResource(R.string.current_password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                Icon(if (oldPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(stringResource(R.string.new_password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(if (newPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(R.string.confirm_new_password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                            Toast.makeText(context, context.getString(R.string.error_fill_info), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            Toast.makeText(context, context.getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val isPasswordValid = newPassword.length >= 6 && 
                                            newPassword.any { it.isLetter() } && 
                                            newPassword.any { it.isDigit() } && 
                                            newPassword.any { !it.isLetterOrDigit() }
                        if (!isPasswordValid) {
                            Toast.makeText(context, context.getString(R.string.error_password_too_short), Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Re-authenticate
                        val credential = EmailAuthProvider.getCredential(user?.email ?: "", oldPassword)
                        user?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(context.getString(R.string.password_update_success))
                                        }
                                        showChangePasswordDialog = false
                                        oldPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Lỗi: ${updateTask.exception?.message}")
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.error_wrong_password), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.choose_language)) },
            text = {
                Column {
                    val languages = listOf("Tiếng Việt", "English")
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { preferenceManager.setLanguage(lang) }
                                    showLanguageDialog = false
                                    Toast.makeText(context, context.getString(R.string.language_changed, lang), Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == lang,
                                onClick = {
                                    scope.launch { preferenceManager.setLanguage(lang) }
                                    showLanguageDialog = false
                                    Toast.makeText(context, context.getString(R.string.language_changed, lang), Toast.LENGTH_SHORT).show()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = PinkPrimary)
                            )
                            Text(text = lang, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.close), color = PinkPrimary)
                }
            }
        )
    }

    // Update Email Dialog
    if (showUpdateEmailDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateEmailDialog = false },
            title = { Text(stringResource(R.string.update_email)) },
            text = {
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text(stringResource(R.string.new_email)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkPrimary, focusedLabelColor = PinkPrimary),
                    shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEmail.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                            user?.updateEmail(newEmail)
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(context.getString(R.string.email_update_success))
                                        }
                                        showUpdateEmailDialog = false
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Lỗi: ${task.exception?.message}")
                                        }
                                    }
                                }
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateEmailDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = PinkDark,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PinkPrimary.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PinkPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
