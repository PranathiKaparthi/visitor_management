package com.example.ui.views

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun AnalyticsView(
    viewModel: VmsViewModel,
    modifier: Modifier = Modifier
) {
    val visitorsList by viewModel.allVisitorsState.collectAsState()

    // Metrics - Optimized to remember counts and avoid recalculating on every recomposition
    val totalCount = remember(visitorsList) { visitorsList.size }
    val activeCount = remember(visitorsList) { visitorsList.count { it.status == "CHECKED_IN" } }
    val pendingCount = remember(visitorsList) { visitorsList.count { it.status == "PENDING" } }
    val completedCount = remember(visitorsList) { visitorsList.count { it.status == "CHECKED_OUT" } }

    // Memoized date-time formatters to prevent rebuilding on every recomposition
    val displayFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Department-wise charts calculation
    val deptCounts = remember(visitorsList) {
        visitorsList.groupBy { it.department }
            .mapValues { it.value.size }
    }

    // Purpose metrics calculation
    val purposeCounts = remember(visitorsList) {
        visitorsList.groupBy { it.purpose }
            .mapValues { it.value.size }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Summary Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Facility Security Metrics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2x2 Grid of Quick Metrics (simulated Row cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "In Building Now",
                        value = activeCount.toString(),
                        subtitle = "Active visitor passes",
                        icon = Icons.Default.MeetingRoom,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Pending Approvals",
                        value = pendingCount.toString(),
                        subtitle = "Awaiting host action",
                        icon = Icons.Default.HourglassEmpty,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Completed Visits",
                        value = completedCount.toString(),
                        subtitle = "Safely checked-out",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Registered",
                        value = totalCount.toString(),
                        subtitle = "All-time database logs",
                        icon = Icons.Default.BarChart,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Custom Visual Circular Donut Chart for Department Loads
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Department Visitors load Share",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (deptCounts.isEmpty()) {
                        Text(
                            text = "No logs yet. Click Settings > Seed sample database data to view active visual charts.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    } else {
                        // Drawing custom Arc segments
                        val totalDeptSum = deptCounts.values.sum().toFloat()
                        val colorsList = listOf(
                            Color(0xFF3F51B5), Color(0xFFE91E63), Color(0xFF4CAF50),
                            Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFF795548)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Donut canvas
                            Box(
                                modifier = Modifier.size(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    var sweepStart = -90f
                                    deptCounts.entries.forEachIndexed { index, entry ->
                                        val sweepAngle = (entry.value / totalDeptSum) * 360f
                                        drawArc(
                                            color = colorsList[index % colorsList.size],
                                            startAngle = sweepStart,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        sweepStart += sweepAngle
                                    }
                                }
                                Text(
                                    text = "${totalDeptSum.toInt()}\nVisits",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Legends list
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                deptCounts.entries.forEachIndexed { index, entry ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    colorsList[index % colorsList.size],
                                                    CircleShape
                                                )
                                        )
                                        Text(
                                            text = "${entry.key} (${entry.value})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom horizontal Progress bars for Visitation Purposes
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Meeting Purpose Volume",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (purposeCounts.isEmpty()) {
                        Text(
                            text = "No purposes recorded.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        val maxPurposeCount = (purposeCounts.values.maxOrNull() ?: 1).toFloat()

                        purposeCounts.forEach { (purpose, count) ->
                            val progressRatio = count / maxPurposeCount

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(purpose, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("$count visits", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                // Beautiful filled row bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                                            RoundedCornerShape(4.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progressRatio)
                                            .fillMaxHeight()
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // General Visitor Log Book (Manager view)
        item {
            Text(
                text = "Campus Attendance logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (visitorsList.isEmpty()) {
            item {
                Text(
                    text = "No attendance records logbook present.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            items(visitorsList, key = { "manager_${it.id}" }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VisitorAvatar(photoUri = log.photoUri, name = log.name, size = 40.dp)
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("ID: ${log.visitorId} • ${log.company}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                            Spacer(modifier = Modifier.height(2.dp))

                            // Action Timestamp strings - Optimized to reuse and remember formatters
                            val dateStr = remember(log.createdAt, displayFormat) {
                                displayFormat.format(Date(log.createdAt))
                            }
                            Text(
                                text = "Registered: $dateStr",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (log.checkInTime > 0L) {
                                val checkInStr = remember(log.checkInTime, timeFormat) {
                                    timeFormat.format(Date(log.checkInTime))
                                }
                                Text(
                                    text = "Checked-In: $checkInStr",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (log.actualCheckOutTime > 0L) {
                                val checkOutStr = remember(log.actualCheckOutTime, timeFormat) {
                                    timeFormat.format(Date(log.actualCheckOutTime))
                                }
                                Text(
                                    text = "Checked-Out: $checkOutStr",
                                    fontSize = 10.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Right side bubble
                        Surface(
                            color = when (log.status) {
                                "CHECKED_IN" -> MaterialTheme.colorScheme.primaryContainer
                                "CHECKED_OUT" -> MaterialTheme.colorScheme.surfaceVariant
                                "APPROVED" -> Color(0xFFE8F5E9)
                                "REJECTED" -> Color(0xFFFFEBEE)
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = log.status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = when (log.status) {
                                    "CHECKED_IN" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    "CHECKED_OUT" -> MaterialTheme.colorScheme.onSurfaceVariant
                                    "APPROVED" -> Color(0xFF2E7D32)
                                    "REJECTED" -> Color(0xFFC62828)
                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
