package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VisitorRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VisitorAvatar(
    photoUri: String,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val firstChar = name.trim().firstOrNull()?.uppercase() ?: "?"
    
    // Hash name to pick an intentional background gradient
    val hash = name.hashCode().coerceAtLeast(0)
    val gradients = listOf(
        Brush.linearGradient(listOf(Color(0xFF3F51B5), Color(0xFF00BCD4))),
        Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFFFF9800))),
        Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))),
        Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFFE040FB))),
        Brush.linearGradient(listOf(Color(0xFF009688), Color(0xFF4CAF50))),
        Brush.linearGradient(listOf(Color(0xFFFF5722), Color(0xFFFFC107)))
    )
    val brush = gradients[hash % gradients.size]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush)
            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri.startsWith("preset_")) {
            // Draw custom avatar silhouette or accessories based on preset
            Icon(
                imageVector = when(photoUri) {
                    "preset_1" -> Icons.Default.Face
                    "preset_2" -> Icons.Default.FaceRetouchingNatural
                    "preset_3" -> Icons.Default.AccountCircle
                    "preset_4" -> Icons.Default.SupervisorAccount
                    else -> Icons.Default.Person
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.6f)
            )
        } else if (photoUri.isNotBlank()) {
            // Placeholder standard initial
            Text(
                text = firstChar,
                color = Color.White,
                fontSize = (size.value * 0.45f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        } else {
            // Default icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.55f)
            )
        }
    }
}

@Composable
fun VisitorPassView(
    visitor: VisitorRecord,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onQuickAction: () -> Unit = {}
) {
    // Memoized date-time formatters to prevent rebuilding on every recomposition
    val displayFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val displayTime = remember(visitor.createdAt, displayFormat) {
        displayFormat.format(Date(visitor.createdAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "VISITOR PASS",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Status Badge
                Surface(
                    color = when (visitor.status) {
                        "APPROVED" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                        "CHECKED_IN" -> MaterialTheme.colorScheme.primaryContainer
                        "CHECKED_OUT" -> MaterialTheme.colorScheme.surface
                        "REJECTED" -> Color(0xFFC62828).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = when (visitor.status) {
                        "APPROVED" -> Color(0xFF2E7D32)
                        "CHECKED_IN" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "CHECKED_OUT" -> MaterialTheme.colorScheme.onSurfaceVariant
                        "REJECTED" -> Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, when (visitor.status) {
                        "APPROVED" -> Color(0xFF2E7D32).copy(alpha = 0.3f)
                        "CHECKED_IN" -> MaterialTheme.colorScheme.primary
                        "REJECTED" -> Color(0xFFC62828).copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    })
                ) {
                    Text(
                        text = visitor.status,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Layout (Card visual split)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info block
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Visitor Name
                    Column {
                        Text(
                            text = visitor.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${visitor.designation} • ${visitor.company}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Host Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = "Host: ${visitor.hostName} (${visitor.department})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Floor/Room Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Room,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = "${visitor.floor} • ${visitor.meetingRoom}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Floor/Room Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = "Purpose: ${visitor.purpose}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Avatar Photo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    VisitorAvatar(
                        photoUri = visitor.photoUri,
                        name = visitor.name,
                        size = 80.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = visitor.visitorId,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider and Access Code / QR View
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ticket QR Details Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = "OFFICE ACCESS CODE",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = visitor.otpCode,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Share with security gate or input at kiosk",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Custom QR Generator
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QrCodeDraw(
                        content = "VMS_PASS_${visitor.visitorId}_${visitor.otpCode}",
                        qrColor = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick trigger status transition
            if (visitor.status != "CHECKED_OUT" && visitor.status != "REJECTED") {
                Button(
                    onClick = onQuickAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (visitor.status == "APPROVED") Icons.Default.Login else Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = when (visitor.status) {
                            "PENDING" -> "Request Pending Approval..."
                            "APPROVED" -> "Simulate Check-In"
                            "CHECKED_IN" -> "Simulate Check-Out"
                            else -> "Action Completed"
                        }
                    )
                }
            } else if (visitor.status == "CHECKED_OUT") {
                // Out summary - Reuses memoized time format
                val outTime = remember(visitor.actualCheckOutTime, timeFormat) {
                    timeFormat.format(Date(visitor.actualCheckOutTime))
                }
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(text = "Checked Out at $outTime")
                }
            }
        }
    }
}
