package com.dacs3.smartmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dacs3.smartmoney.R
import com.dacs3.smartmoney.data.model.GroupFund
import com.dacs3.smartmoney.data.model.GroupTransaction
import com.dacs3.smartmoney.data.remote.FirebaseSource
import com.dacs3.smartmoney.ui.theme.*
import com.dacs3.smartmoney.util.AppUtils
import com.dacs3.smartmoney.util.QRUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
    onNavigateToAddTransaction: (String) -> Unit
) {
    val firebaseSource = remember { FirebaseSource() }
    var group by remember { mutableStateOf<GroupFund?>(null) }
    var showQRDialog by remember { mutableStateOf(false) }
    val transactions by firebaseSource.getGroupTransactionsRealtime(groupId).collectAsState(initial = emptyList())

    LaunchedEffect(groupId) {
        // Lấy thông tin nhóm (có thể dùng realtime hoặc get một lần)
        // Ở đây để đơn giản ta lấy từ danh sách tham gia
        firebaseSource.getJoinedGroupsRealtime().collect { groups ->
            group = groups.find { it.groupId == groupId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        group?.name ?: stringResource(R.string.group_fund_title),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddTransaction(groupId) }, containerColor = PinkPrimary) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }
    ) { padding ->
        group?.let { currentGroup ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Thẻ thông tin tổng quan
                item {
                    GroupOverviewCard(currentGroup, onShowQR = { showQRDialog = true })
                }

                item {
                    Text(
                        stringResource(R.string.group_transactions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (transactions.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.no_transactions), color = Color.Gray)
                        }
                    }
                } else {
                    items(transactions, key = { it.transactionId }) { transaction ->
                        GroupTransactionItem(transaction)
                    }
                }
            }

            if (showQRDialog) {
                InviteQRCodeDialog(
                    inviteCode = currentGroup.inviteCode,
                    groupName = currentGroup.name,
                    onDismiss = { showQRDialog = false }
                )
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun GroupOverviewCard(group: GroupFund, onShowQR: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.group_balance),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            AppUtils.formatCurrency(group.balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = onShowQR,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.QrCode2, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                group.inviteCode,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${group.memberUids.size} ${stringResource(R.string.members)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun InviteQRCodeDialog(
    inviteCode: String,
    groupName: String,
    onDismiss: () -> Unit
) {
    val qrBitmap = remember(inviteCode) {
        QRUtils.generateQRCode(inviteCode)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.invite_code_qr),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(groupName, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                qrBitmap?.let {
                    androidx.compose.foundation.Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } ?: CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    inviteCode,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = PinkPrimary
                )
                Text(
                    stringResource(R.string.share_code_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun GroupTransactionItem(transaction: GroupTransaction) {
    val db = remember { FirebaseFirestore.getInstance() }
    
    var creatorName by remember(transaction.transactionId) { 
        mutableStateOf(if (transaction.creatorName == "Thành viên") "" else transaction.creatorName) 
    }
    var creatorPhotoUrl by remember(transaction.transactionId) { 
        mutableStateOf(transaction.creatorPhotoUrl) 
    }

    DisposableEffect(transaction.createdBy) {
        if (transaction.createdBy.isEmpty()) return@DisposableEffect onDispose {}
        
        val docRef = db.collection("users").document(transaction.createdBy)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null && snapshot.exists()) {
                val name = snapshot.getString("displayName") ?: snapshot.getString("fullName")
                val photo = snapshot.getString("photoUrl")
                
                if (!name.isNullOrEmpty()) creatorName = name
                if (!photo.isNullOrEmpty()) creatorPhotoUrl = photo
            }
        }
        
        onDispose {
            listener.remove()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh đại diện người tạo với Badge danh mục
            Box(modifier = Modifier.size(54.dp)) {
            if (creatorPhotoUrl.isNotEmpty()) {
                AsyncImage(
                    model = creatorPhotoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.AccountCircle)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SoftGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(26.dp))
                }
            }
                
                // Badge icon danh mục nhỏ ở góc
                val badgeColor = if (transaction.type == "INCOME") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .background(badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        AppUtils.getCategoryIcon(transaction.categoryName),
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    transaction.categoryName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    "Bởi: ${if (creatorName.isEmpty()) "Thành viên" else creatorName} • ${AppUtils.formatDate(transaction.date)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                if (transaction.note.isNotBlank()) {
                    Text(
                        transaction.note,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
            
            Text(
                (if (transaction.type == "INCOME") "+" else "-") + AppUtils.formatCurrency(transaction.amount),
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = if (transaction.type == "INCOME") MintDark else PinkDark
            )
        }
    }
}
