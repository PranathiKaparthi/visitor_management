package com.example.ui.views

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VisitorRecord
import com.example.ui.VmsViewModel
import com.example.ui.components.VisitorPassView

// Extension property for Material3 ColorScheme to define successContainer
private val ColorScheme.successContainer: Color
    get() = Color(0xFFE8F5E9)

@Composable
fun KioskView(
    viewModel: VmsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hosts by viewModel.hostsList.collectAsState()

    var showSelfRegisterDialog by remember { mutableStateOf(false) }
    var showOtpCheckInDialog by remember { mutableStateOf(false) }
    var showSuccessInNotification by remember { mutableStateOf<VisitorRecord?>(null) }
    var showCreatedPass by remember { mutableStateOf<VisitorRecord?>(null) }

    val selfDraft = viewModel.registrationDraft

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.img_vms_hero),
                    contentDescription = "Welcome receptionist lobby banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                            )
                        )
                )

                // Welcoming overlay texts
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "WELCOME TO THE CAMPUS",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Smart Kiosk Registry & Self Check-in",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center visual icon & system callout
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = "Please Select an Option to Begin",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Check in using your pre-approved 6-digit passcode or register on-site below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Button 1: QR OTP Scan / Code lookup
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showOtpCheckInDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Check In with Pass Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Enter 6-digit OTP code to check-in/out",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Button 2: Walk-In Registration
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.resetDraft()
                        showSelfRegisterDialog = true
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AppRegistration, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "New Visitor Self-Registration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "No pre-booking? Register yourself in our lobby database",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick active pass display banner
        if (showCreatedPass != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.successContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "REGISTRATION COMPLETE - PENDING APPROVAL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E4620)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hello ${showCreatedPass?.name}! Your Pass request is created and sent of host approval. Share this pass with security.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    VisitorPassView(
                        visitor = showCreatedPass!!,
                        onQuickAction = {
                            viewModel.checkInVisitor(showCreatedPass!!)
                            showCreatedPass = showCreatedPass!!.copy(status = "CHECKED_IN")
                            Toast.makeText(context, "Checked in!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showCreatedPass = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hide Active Kiosk Pass")
                    }
                }
            }
        }

        // Active notification
        if (showSuccessInNotification != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = BorderStroke(1.5.dp, Color(0xFF2E7D32))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ACCESS MOUNTED SUCCESSFULLY",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hello ${showSuccessInNotification?.name}! Proceed to floor details:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${showSuccessInNotification?.floor} \n${showSuccessInNotification?.meetingRoom}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Host employee has been notified and expects you shortly.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showSuccessInNotification = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Finish Check-in Screen")
                    }
                }
            }
        }

        // --- Dialogs ---

        // 1. Pass Code Input Dialog popup
        if (showOtpCheckInDialog) {
            var inputOtp by remember { mutableStateOf("") }
            var isSearching by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showOtpCheckInDialog = false },
                title = { Text("Lobby Access Code") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Enter your 6-digit registration PIN to Check-in/out.")
                        OutlinedTextField(
                            value = inputOtp,
                            onValueChange = { if (it.length <= 6) inputOtp = it },
                            placeholder = { Text("PIN Number") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputOtp.length == 6) {
                                isSearching = true
                                // Try Check-in
                                viewModel.checkInWithOtp(
                                    otp = inputOtp,
                                    onSuccess = { record ->
                                        isSearching = false
                                        showOtpCheckInDialog = false
                                        showCreatedPass = null
                                        showSuccessInNotification = record
                                    },
                                    onError = { err ->
                                        // Try Check-out alternative
                                        viewModel.checkOutWithOtp(
                                            otp = inputOtp,
                                            onSuccess = { record ->
                                                isSearching = false
                                                showOtpCheckInDialog = false
                                                showCreatedPass = null
                                                Toast.makeText(context, "Checked Out successfully!", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { finalErr ->
                                                isSearching = false
                                                Toast.makeText(context, finalErr, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                )
                            } else {
                                Toast.makeText(context, "Enter 6 digits.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Submit Code")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOtpCheckInDialog = false }) {
                        Text("Back")
                    }
                }
            )
        }

        // 2. Self - Walk-In Registration dialog
        if (showSelfRegisterDialog) {
            AlertDialog(
                onDismissRequest = { showSelfRegisterDialog = false },
                title = { Text("Walk-In Lobby Registration") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Quickly register your visit details:")

                        OutlinedTextField(
                            value = selfDraft.name,
                            onValueChange = { viewModel.updateDraft { d -> d.copy(name = it) } },
                            label = { Text("Your Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = selfDraft.phone,
                            onValueChange = { viewModel.updateDraft { d -> d.copy(phone = it) } },
                            label = { Text("Phone Number *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = selfDraft.company,
                            onValueChange = { viewModel.updateDraft { d -> d.copy(company = it) } },
                            label = { Text("Your Company *") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Host selection
                        Text("Who are you meeting?", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        var hostExp by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { hostExp = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selfDraft.hostName)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = hostExp, onDismissRequest = { hostExp = false }) {
                                hosts.forEach { h ->
                                    DropdownMenuItem(text = { Text(h) }, onClick = {
                                        viewModel.updateDraft { d -> d.copy(hostName = h) }
                                        hostExp = false
                                    })
                                }
                            }
                        }

                        // Purpose
                        Text("Purpose of Visit?", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        var pExp by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { pExp = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selfDraft.purpose)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = pExp, onDismissRequest = { pExp = false }) {
                                viewModel.purposeList.forEach { p ->
                                    DropdownMenuItem(text = { Text(p) }, onClick = {
                                        viewModel.updateDraft { d -> d.copy(purpose = p) }
                                        pExp = false
                                    })
                                }
                            }
                        }

                        // Picture prompt
                        Button(
                            onClick = {
                                val picker = (1..4).random()
                                viewModel.updateDraft { it.copy(photoUri = "preset_$picker") }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selfDraft.photoUri.isNotBlank()) "Photo Attached ✓" else "Take Kiosk Quick Snapshot")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selfDraft.name.isNotBlank() && selfDraft.phone.isNotBlank() && selfDraft.company.isNotBlank()) {
                                viewModel.registerVisitor(
                                    onComplete = { record ->
                                        showSelfRegisterDialog = false
                                        showSuccessInNotification = null
                                        showCreatedPass = record
                                    }
                                )
                            } else {
                                Toast.makeText(context, "Fill Name, Phone and Company.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Complete Registration")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSelfRegisterDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
