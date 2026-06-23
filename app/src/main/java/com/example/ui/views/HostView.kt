package com.example.ui.views

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VisitorRecord
import com.example.ui.VmsViewModel
import com.example.ui.components.VisitorAvatar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HostView(
    viewModel: VmsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hostUsers by viewModel.hostsList.collectAsState()
    val activeHostUser by viewModel.selectedHostUser.collectAsState()
    val allVisitors by viewModel.allVisitorsState.collectAsState()

    // Memoized date-time formatters to prevent rebuilding on every recomposition
    val displayFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Filter visitors for the specific active Host Employee
    val hostVisitors = remember(allVisitors, activeHostUser) {
        allVisitors.filter { it.hostName.equals(activeHostUser, ignoreCase = true) }
    }

    val pendingRequests = remember(hostVisitors) {
        hostVisitors.filter { it.status == "PENDING" }
    }

    val historyRequests = remember(hostVisitors) {
        hostVisitors.filter { it.status != "PENDING" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Host Switcher Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Log In as Host",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Employee Panel: $activeHostUser",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Selection dropdown
                    var hostSelectExpanded by remember { mutableStateOf(false) }

                    Box {
                        Button(
                            onClick = { hostSelectExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.SwitchAccount, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Switch Profile")
                        }

                        DropdownMenu(
                            expanded = hostSelectExpanded,
                            onDismissRequest = { hostSelectExpanded = false }
                        ) {
                            hostUsers.forEach { hn ->
                                DropdownMenuItem(
                                    text = { Text(hn) },
                                    onClick = {
                                        viewModel.setSelectedHostUser(hn)
                                        hostSelectExpanded = false
                                        Toast.makeText(context, "$hn's desk activated", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Notifications to Host
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "Incoming Visitor Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (pendingRequests.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pendingRequests.size.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // List pending requests if empty vs fill
        if (pendingRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Lobby is All Clear!",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "No pending visit requests for $activeHostUser at this time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(pendingRequests, key = { it.id }) { request ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "APPROVAL REQUEST",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = remember(request.createdAt, displayFormat) {
                                    displayFormat.format(Date(request.createdAt))
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VisitorAvatar(photoUri = request.photoUri, name = request.name, size = 64.dp)
                            Column {
                                Text(
                                    text = "Visitor: ${request.name}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "${request.designation} at ${request.company}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Required Specific fields print list satisfying Step 4:
                        // Visitor Name, Company, Designation, Purpose, Meeting Time
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Visitor Details:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Row {
                                Text("Visitor Name: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(request.name, fontSize = 13.sp)
                            }
                            Row {
                                Text("Company: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(request.company, fontSize = 13.sp)
                            }
                            Row {
                                Text("Designation: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(request.designation, fontSize = 13.sp)
                            }
                            Row {
                                Text("Purpose: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(request.purpose, fontSize = 13.sp)
                            }
                            Row {
                                Text("Meeting Time: ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val expectedStr = remember(request.createdAt, timeFormat) {
                                    timeFormat.format(Date(request.createdAt))
                                }
                                Text(expectedStr, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Action Controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Reject Button (Red)
                            Button(
                                onClick = {
                                    viewModel.rejectVisitor(request)
                                    Toast.makeText(context, "Visit from ${request.name} REJECTED.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reject Visit")
                            }

                            // Approve Button (Green)
                            Button(
                                onClick = {
                                    viewModel.approveVisitor(request)
                                    Toast.makeText(context, "Visit from ${request.name} APPROVED!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Approve Visit")
                            }
                        }
                    }
                }
            }
        }

        // Historic Logs of this specific Host
        item {
            Text(
                text = "Your Visitation History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (historyRequests.isEmpty()) {
            item {
                Text(
                    text = "No past visitor logs registered under your profile yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(historyRequests, key = { "hist_${it.id}" }) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VisitorAvatar(photoUri = log.photoUri, name = log.name, size = 36.dp)
                        Column {
                            Text(log.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${log.company} • ${log.purpose}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        color = when (log.status) {
                            "APPROVED" -> Color(0xFFE8F5E9)
                            "CHECKED_IN" -> MaterialTheme.colorScheme.primaryContainer
                            "CHECKED_OUT" -> MaterialTheme.colorScheme.surfaceVariant
                            else -> Color(0xFFFFEBEE)
                        },
                        contentColor = when (log.status) {
                            "APPROVED" -> Color(0xFF2E7D32)
                            "CHECKED_IN" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "CHECKED_OUT" -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> Color(0xFFC62828)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = log.status,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
