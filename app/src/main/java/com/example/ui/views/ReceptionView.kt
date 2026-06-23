package com.example.ui.views

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VisitorRecord
import com.example.ui.VmsViewModel
import com.example.ui.components.VisitorAvatar
import com.example.ui.components.VisitorPassView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReceptionView(
    viewModel: VmsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRegisterForm by remember { mutableStateOf(false) }
    
    // Memoized date-time formatters to prevent recreating objects on every recomposition
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Database states
    val visitorsList by viewModel.filteredVisitors.collectAsState()
    val hosts by viewModel.hostsList.collectAsState()
    val draft = viewModel.registrationDraft

    // Scanning OTP inputs
    var otpSearchCode by remember { mutableStateOf("") }

    // Tab state (0 = All, 1 = Pending, 2 = In-Building, 3 = Checked Out)
    var selectedTabState by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("All logs", "Pending", "In-Building", "Completed")

    // Filter local list based on tabs
    val displayList = remember(visitorsList, selectedTabState) {
        when (selectedTabState) {
            1 -> visitorsList.filter { it.status == "PENDING" }
            2 -> visitorsList.filter { it.status == "CHECKED_IN" }
            3 -> visitorsList.filter { it.status == "CHECKED_OUT" }
            else -> visitorsList
        }
    }

    // Camera launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateDraft { it.copy(photoUri = uri.toString()) }
            Toast.makeText(context, "Captured photo from gallery successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Security Control Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gate Access Control Passcode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Input 6-digit Visitor passcode or scan QR to check-in/out visitors instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = otpSearchCode,
                            onValueChange = { if (it.length <= 6) otpSearchCode = it },
                            label = { Text("6-Digit OTP") },
                            placeholder = { Text("e.g. 129482") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Button(
                            onClick = {
                                if (otpSearchCode.length == 6) {
                                    viewModel.checkInWithOtp(
                                        otp = otpSearchCode,
                                        onSuccess = {
                                            Toast.makeText(context, "Checked In: ${it.name} successfully!", Toast.LENGTH_LONG).show()
                                            otpSearchCode = ""
                                        },
                                        onError = {
                                            // Try Check-Out sequence as fallback
                                            viewModel.checkOutWithOtp(
                                                otp = otpSearchCode,
                                                onSuccess = {
                                                    Toast.makeText(context, "Checked Out: ${it.name} successfully!", Toast.LENGTH_LONG).show()
                                                    otpSearchCode = ""
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, "Please enter 6 digits.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Act")
                        }
                    }
                }
            }
        }

        // Action Trigger Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Visitor Registry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        showRegisterForm = !showRegisterForm
                        if (showRegisterForm) {
                            viewModel.resetDraft()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showRegisterForm) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (showRegisterForm) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (showRegisterForm) "Cancel" else "Register Visit")
                }
            }
        }

        // Registration form expandable card
        if (showRegisterForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Step 2: Visitor Registration File",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Personal Details block
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { viewModel.updateDraft { d -> d.copy(name = it) } },
                            label = { Text("Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = draft.phone,
                                onValueChange = { viewModel.updateDraft { d -> d.copy(phone = it) } },
                                label = { Text("Mobile Phone *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = draft.email,
                                onValueChange = { viewModel.updateDraft { d -> d.copy(email = it) } },
                                label = { Text("Email (Optional)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = draft.company,
                                onValueChange = { viewModel.updateDraft { d -> d.copy(company = it) } },
                                label = { Text("Company Name *") },
                                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = draft.designation,
                                onValueChange = { viewModel.updateDraft { d -> d.copy(designation = it) } },
                                label = { Text("Designation") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = draft.vehicleNumber,
                                onValueChange = { viewModel.updateDraft { d -> d.copy(vehicleNumber = it) } },
                                label = { Text("Vehicle Plate No.") },
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            
                            // Expected Duration Dropdown/Selector simple input
                            OutlinedTextField(
                                value = draft.expectedDurationMinutes.toString(),
                                onValueChange = { viewModel.updateDraft { d -> d.copy(expectedDurationMinutes = it.toIntOrNull() ?: 60) } },
                                label = { Text("Duration (min)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Host and Department Section
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Assign Target Host Employee",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                // Dynamic dropdown select for Host
                                var hostExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { hostExpanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = "Host: ${draft.hostName} (${draft.department})")
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = hostExpanded,
                                        onDismissRequest = { hostExpanded = false }
                                    ) {
                                        hosts.forEach { hostName ->
                                            DropdownMenuItem(
                                                text = { Text(hostName) },
                                                onClick = {
                                                    viewModel.updateDraft { d -> d.copy(hostName = hostName) }
                                                    hostExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Selection of Department
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    viewModel.departmentsList.forEach { dept ->
                                        FilterChip(
                                            selected = draft.department == dept,
                                            onClick = { viewModel.updateDraft { d -> d.copy(department = dept) } },
                                            label = { Text(dept, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        // Floor & Room assignment
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Campus Location (Meeting Room)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                var floorExpanded by remember { mutableStateOf(false) }
                                var roomExpanded by remember { mutableStateOf(false) }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { floorExpanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(draft.floor, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                        DropdownMenu(
                                            expanded = floorExpanded,
                                            onDismissRequest = { floorExpanded = false }
                                        ) {
                                            viewModel.floorList.forEach { f ->
                                                DropdownMenuItem(text = { Text(f) }, onClick = {
                                                    viewModel.updateDraft { d -> d.copy(floor = f) }
                                                    floorExpanded = false
                                                })
                                            }
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { roomExpanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(draft.meetingRoom, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                        DropdownMenu(
                                            expanded = roomExpanded,
                                            onDismissRequest = { roomExpanded = false }
                                        ) {
                                            viewModel.roomList.forEach { r ->
                                                DropdownMenuItem(text = { Text(r) }, onClick = {
                                                    viewModel.updateDraft { d -> d.copy(meetingRoom = r) }
                                                    roomExpanded = false
                                                })
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Purpose Selector List
                        Column {
                            Text(
                                text = "Purpose of Visit",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                var purposeExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { purposeExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                                        Text(draft.purpose)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                    DropdownMenu(
                                        expanded = purposeExpanded,
                                        onDismissRequest = { purposeExpanded = false }
                                    ) {
                                        viewModel.purposeList.forEach { p ->
                                            DropdownMenuItem(text = { Text(p) }, onClick = {
                                                viewModel.updateDraft { d -> d.copy(purpose = p) }
                                                purposeExpanded = false
                                            })
                                        }
                                    }
                                }
                            }
                        }

                        // Step 3: Photo Capture Viewfinder
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 4.dp))
                                Text(
                                    text = "Step 3: Security Photo Capture",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Custom Camera Viewfinder Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Dynamic screen capture overlays
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "VMS SMART VIEWFINDER [LIVE]",
                                        color = Color(0xFF00FF00),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (draft.photoUri.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            VisitorAvatar(photoUri = draft.photoUri, name = draft.name, size = 64.dp)
                                            Column {
                                                Text("✓ Photo Registered", color = Color(0xFF00FF00), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(
                                                    text = if (draft.photoUri.startsWith("preset_")) "Preset Avatar Profile selected" else "Custom picked image uri",
                                                    color = Color.LightGray,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "[Align Visitor Face inside frame Area]",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Quick simulation shortcuts & file uploader
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                // Take mock snapshot
                                                val picker = (1..4).random()
                                                viewModel.updateDraft { it.copy(photoUri = "preset_$picker") }
                                                Toast.makeText(context, "Snapshot captured!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Intense Snapshot Snap!", fontSize = 10.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Upload from Gallery", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Submit action
                        Button(
                            onClick = {
                                viewModel.registerVisitor(
                                    onComplete = {
                                        Toast.makeText(context, "Visitor ${it.name} registered and sent to ${it.hostName} for approval!", Toast.LENGTH_LONG).show()
                                        showRegisterForm = false
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Complete Step 2 & Propose host Approval")
                        }
                    }
                }
            }
        }

        // Search Bar Registry - Clean unidirectional search binding directly to ViewModel's state
        item {
            val query by viewModel.searchQuery.collectAsState()
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by name, ID, company, code...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        // Logs Filtering Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTabState,
                edgePadding = 0.dp,
                divider = {}
            ) {
                tabTitles.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTabState == i,
                        onClick = { selectedTabState = i },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Display results or empty states
        if (displayList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Visitor Records Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try clearing queries or tap 'Register Visit' above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.seedSampleData() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Seed Realistic Sample Data")
                    }
                }
            }
        } else {
            items(displayList, key = { it.id }) { record ->
                var isExpanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { isExpanded = !isExpanded }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            VisitorAvatar(photoUri = record.photoUri, name = record.name, size = 48.dp)

                            Column {
                                Text(
                                    text = record.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${record.company} • ${record.visitorId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "To meet: ${record.hostName} (${record.department})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Right side summary
                        Column(horizontalAlignment = Alignment.End) {
                            // Status Dot Badge
                            Surface(
                                color = when (record.status) {
                                    "APPROVED" -> Color(0xFFE8F5E9)
                                    "CHECKED_IN" -> MaterialTheme.colorScheme.primaryContainer
                                    "CHECKED_OUT" -> MaterialTheme.colorScheme.surfaceVariant
                                    "REJECTED" -> Color(0xFFFFEBEE)
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                contentColor = when (record.status) {
                                    "APPROVED" -> Color(0xFF2E7D32)
                                    "CHECKED_IN" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    "CHECKED_OUT" -> MaterialTheme.colorScheme.onSurfaceVariant
                                    "REJECTED" -> Color(0xFFC62828)
                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = record.status,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Time stamp - Reuses memoized format
                            val timeStr = remember(record.createdAt, timeFormat) {
                                timeFormat.format(Date(record.createdAt))
                            }
                            Text(
                                text = timeStr,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Expanded action details
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            VisitorPassView(
                                visitor = record,
                                onQuickAction = {
                                    if (record.status == "APPROVED") {
                                        viewModel.checkInVisitor(record)
                                        Toast.makeText(context, "Visitor ${record.name} Checked In!", Toast.LENGTH_SHORT).show()
                                    } else if (record.status == "CHECKED_IN") {
                                        viewModel.checkOutVisitor(record)
                                        Toast.makeText(context, "Visitor ${record.name} Checked Out!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons (reception controls)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (record.status == "PENDING") {
                                    Button(
                                        onClick = { viewModel.approveVisitor(record) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.rejectVisitor(record) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", fontSize = 11.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { viewModel.deleteRecord(record) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                    border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Standard padding at list bottom
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
