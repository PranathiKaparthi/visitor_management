package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.VmsRole
import com.example.ui.VmsViewModel
import com.example.ui.views.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: VmsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Mandatory Edge-to-edge support
        enableEdgeToEdge()

        // 2. Pre-seed the database with professional logs on first launch so the charts are loaded
        viewModel.seedSampleData()

        setContent {
            MyApplicationTheme {
                val currentRole by viewModel.currentRole.collectAsState()
                
                // Formatted system clock - Optimized to prevent SimpleDateFormat re-creation on every tick
                val timeFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
                var currentTimeString by remember { mutableStateOf("") }
                LaunchedEffect(timeFormatter) {
                    while (true) {
                        currentTimeString = timeFormatter.format(Date())
                        kotlinx.coroutines.delay(1000)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Smart VMS",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleLarge,
                                        letterSpacing = 1.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFF00FF00), androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Text(
                                            text = currentTimeString.ifBlank { "Smart VMS Campus" },
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentRole == VmsRole.RECEPTIONIST,
                                onClick = { viewModel.setRole(VmsRole.RECEPTIONIST) },
                                label = { Text("Reception", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                icon = { Icon(Icons.Default.Security, contentDescription = "Security guard panel") }
                            )

                            NavigationBarItem(
                                selected = currentRole == VmsRole.VISITOR,
                                onClick = { viewModel.setRole(VmsRole.VISITOR) },
                                label = { Text("Lobby Kiosk", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                icon = { Icon(Icons.Default.TabletAndroid, contentDescription = "Active visitor pass scan kiosk") }
                            )

                            NavigationBarItem(
                                selected = currentRole == VmsRole.HOST,
                                onClick = { viewModel.setRole(VmsRole.HOST) },
                                label = { Text("Host Review", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                icon = { Icon(Icons.Default.DesktopMac, contentDescription = "Host approvals portal") }
                            )

                            NavigationBarItem(
                                selected = currentRole == VmsRole.FACILITY_MANAGER,
                                onClick = { viewModel.setRole(VmsRole.FACILITY_MANAGER) },
                                label = { Text("Analytics", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                icon = { Icon(Icons.Default.Analytics, contentDescription = "Campus telemetry charts") }
                            )

                            NavigationBarItem(
                                selected = currentRole == VmsRole.ADMIN,
                                onClick = { viewModel.setRole(VmsRole.ADMIN) },
                                label = { Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Maintenance settings") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AnimatedContent(
                            targetState = currentRole,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "role_screens_anim"
                        ) { role ->
                            when (role) {
                                VmsRole.RECEPTIONIST -> ReceptionView(viewModel = viewModel)
                                VmsRole.VISITOR -> KioskView(viewModel = viewModel)
                                VmsRole.HOST -> HostView(viewModel = viewModel)
                                VmsRole.FACILITY_MANAGER -> AnalyticsView(viewModel = viewModel)
                                VmsRole.ADMIN -> AdminView(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
