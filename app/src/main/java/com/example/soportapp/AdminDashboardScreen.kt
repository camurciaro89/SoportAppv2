package com.example.soportapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.ui.viewmodel.AdminUiState
import com.example.soportapp.ui.viewmodel.AdminViewModel
import com.example.soportapp.ui.viewmodel.AdminViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as SoportApplication
    val viewModel: AdminViewModel = viewModel(
        factory = AdminViewModelFactory(application.container.soportAppRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboardData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is AdminUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is AdminUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
                is AdminUiState.Success -> {
                    val data = state.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        // INDICADORES RÁPIDOS
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard("Tickets", data.totalTickets.toString(), Icons.Default.ConfirmationNumber, Color(0xFF2563EB), modifier = Modifier.weight(1f))
                                StatCard("Pendientes", data.pendingTickets.toString(), Icons.Default.PendingActions, Color(0xFFEA580C), modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard("Usuarios", data.totalUsers.toString(), Icons.Default.Group, Color(0xFF16A34A), modifier = Modifier.weight(1f))
                                StatCard("Técnicos", data.totalTechnicians.toString(), Icons.Default.Engineering, Color(0xFF7C3AED), modifier = Modifier.weight(1f))
                            }
                        }

                        // RECIENTES
                        item {
                            Text("Solicitudes Recientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        }

                        items(data.recentRequests) { request ->
                            AdminRequestItem(request)
                        }

                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AdminRequestItem(request: SupportRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.serviceNameSnapshot, fontWeight = FontWeight.Bold)
                Text("Ticket #${request.id}", fontSize = 12.sp, color = Color.Gray)
                Text(request.problemDescription, maxLines = 1, fontSize = 14.sp, color = Color.DarkGray)
            }
            StatusBadgeAdmin(request.estado)
        }
    }
}

@Composable
fun StatusBadgeAdmin(status: String) {
    val (color, text) = when (status.lowercase()) {
        "pendiente" -> Color(0xFFEA580C) to "Pendiente"
        "completado" -> Color(0xFF16A34A) to "Completado"
        else -> Color(0xFF2563EB) to status
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
