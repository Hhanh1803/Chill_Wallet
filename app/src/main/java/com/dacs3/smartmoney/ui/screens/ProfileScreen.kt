package com.dacs3.smartmoney.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dacs3.smartmoney.R
import com.dacs3.smartmoney.ui.theme.PinkPrimary
import com.dacs3.smartmoney.ui.theme.SoftGray
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenDrawer: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(user?.photoUrl) }
    
    // Tải dữ liệu từ Firestore khi vào màn hình
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            try {
                val document = db.collection("users").document(uid).get().await()
                if (document.exists()) {
                    phoneNumber = document.getString("phoneNumber") ?: ""
                    address = document.getString("address") ?: ""
                    bio = document.getString("bio") ?: ""
                    gender = document.getString("gender") ?: ""
                    birthday = document.getString("birthday") ?: ""
                    occupation = document.getString("occupation") ?: ""
                }
            } catch (e: Exception) {
                // Xử lý lỗi nếu cần
            }
        }
    }
    val email = user?.email ?: ""
    val joinDate = remember {
        val timestamp = user?.metadata?.creationTimestamp ?: 0L
        if (timestamp != 0L) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else "N/A"
    }

    var isEditing by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        birthday = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = PinkPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel), color = PinkPrimary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            scope.launch {
                                try {
                                    // 1. Kiểm tra dữ liệu
                                    if (phoneNumber.isNotEmpty() && !android.util.Patterns.PHONE.matcher(phoneNumber).matches()) {
                                        snackbarHostState.showSnackbar(context.getString(R.string.error_invalid_phone))
                                        return@launch
                                    }

                                    // 2. Cập nhật Firebase Auth (Tên và Ảnh)
                                    val profileUpdates = userProfileChangeRequest {
                                        this.displayName = displayName
                                        this.photoUri = selectedImageUri
                                    }
                                    user?.updateProfile(profileUpdates)?.await()

                                    // 3. Cập nhật Firestore (Các trường còn lại)
                                    val userData = hashMapOf(
                                        "displayName" to displayName,
                                        "phoneNumber" to phoneNumber,
                                        "address" to address,
                                        "bio" to bio,
                                        "gender" to gender,
                                        "birthday" to birthday,
                                        "occupation" to occupation,
                                        "photoUrl" to (selectedImageUri?.toString() ?: "")
                                    )
                                    
                                    user?.uid?.let { uid ->
                                        db.collection("users").document(uid).update(userData as Map<String, Any>).await()
                                    }

                                    snackbarHostState.showSnackbar(context.getString(R.string.update_success))
                                    isEditing = false
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Lỗi: ${e.message}")
                                }
                            }
                        }) {
                            Icon(Icons.Rounded.Check, contentDescription = "Save", tint = PinkPrimary)
                        }
                    } else {
                        TextButton(onClick = { isEditing = true }) {
                            Text(stringResource(R.string.edit), color = PinkPrimary)
                        }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)
                    .clickable(enabled = isEditing) {
                        photoPickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(Icons.Rounded.Person),
                    error = rememberVectorPainter(Icons.Rounded.Person)
                )
                
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.basic_info),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ProfileField(
                        label = stringResource(R.string.full_name),
                        value = displayName,
                        icon = Icons.Rounded.Person,
                        isEditing = isEditing,
                        onValueChange = { displayName = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(
                        label = stringResource(R.string.email),
                        value = email,
                        icon = Icons.Rounded.Email,
                        isEditing = false,
                        onValueChange = {}
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(
                        label = stringResource(R.string.phone_number),
                        value = phoneNumber,
                        icon = Icons.Rounded.Phone,
                        isEditing = isEditing,
                        onValueChange = { phoneNumber = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(
                        label = stringResource(R.string.address),
                        value = address,
                        icon = Icons.Rounded.LocationOn,
                        isEditing = isEditing,
                        onValueChange = { address = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(
                        label = stringResource(R.string.bio),
                        value = bio,
                        icon = Icons.Rounded.Description,
                        isEditing = isEditing,
                        onValueChange = { bio = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(
                        label = stringResource(R.string.gender),
                        value = gender,
                        icon = Icons.Rounded.Wc,
                        isEditing = isEditing,
                        onValueChange = { gender = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(
                        label = stringResource(R.string.birthday),
                        value = birthday,
                        icon = Icons.Rounded.Cake,
                        isEditing = isEditing,
                        onValueChange = { birthday = it },
                        isDatePicker = true,
                        onDatePickerRequest = { showDatePicker = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(
                        label = stringResource(R.string.occupation),
                        value = occupation,
                        icon = Icons.Rounded.Work,
                        isEditing = isEditing,
                        onValueChange = { occupation = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileField(
                        label = stringResource(R.string.join_date),
                        value = joinDate,
                        icon = Icons.Rounded.CalendarMonth,
                        isEditing = false,
                        onValueChange = {}
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: ImageVector,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    isDatePicker: Boolean = false,
    onDatePickerRequest: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PinkPrimary,
            modifier = Modifier.padding(top = 20.dp).size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .onFocusEvent { if (it.isFocused && isDatePicker) {
                            onDatePickerRequest()
                            focusManager.clearFocus()
                        } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkPrimary,
                        focusedLabelColor = PinkPrimary
                    ),
                    singleLine = true,
                    readOnly = isDatePicker,
                    shape = RoundedCornerShape(16.dp)
                )
            } else {
                Text(
                    text = if (value.isEmpty()) stringResource(R.string.not_updated) else value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = SoftGray
                )
            }
        }
    }
}
