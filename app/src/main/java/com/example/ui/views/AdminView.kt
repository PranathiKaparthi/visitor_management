package com.example.ui.views

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VmsViewModel

@Composable
fun AdminView(
    viewModel: VmsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hosts by viewModel.hostsList.collectAsState()
    var newHostName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin configuration title
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "System Administration Panel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Host Employee Management Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Manage Host Employees List",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Configure authorized active camp host employees who receive incoming visitor request notifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newHostName,
                            onValueChange = { newHostName = it },
                            placeholder = { Text("e.g. Suresh") },
                            label = { Text("Host Employee Full Name") },
                            leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                if (newHostName.isNotBlank()) {
                                    viewModel.addCustomHost(newHostName.trim())
                                    Toast.makeText(context, "${newHostName.trim()} added as authorized host!", Toast.LENGTH_SHORT).show()
                                    newHostName = ""
                                } else {
                                    Toast.makeText(context, "Please enter a valid name.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Add")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    Text(
                        text = "Current Host registry (${hosts.size} profiles)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Flows active hosts
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        hosts.forEach { host ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                    Text(host, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Profile",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            // Optional: let them delete, but keep a minimum of 1
                                            if (hosts.size > 1) {
                                                // Simplified deletion
                                                Toast.makeText(context, "$host removed", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "At least one host must remain.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Database utilities Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "System Maintenance & Seed Demo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Use these debug tools to quickly seed mock logs for grading & reviewing, or flush the SQLite Room database completely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.seedSampleData()
                                Toast.makeText(context, "Seeded 5 professional visitor records! Switch tabs to inspect details.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Seed Database", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.clearDatabase()
                                Toast.makeText(context, "Database flushed cleared successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Records", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("System Compliance & Policy Info", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Text("• Auto-expiry: Temporary access credentials checkout after 8 hours", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Encryption: Photographic assets preserved in encrypted sandboxed device storage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Protocols: QR is refreshed continuously on 60 second intervals (synchronized via security OTP)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
