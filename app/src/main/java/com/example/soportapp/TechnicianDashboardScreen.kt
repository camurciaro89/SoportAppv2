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
import com.example.soportapp.ui.viewmodel.TechnicianUiState
import com.example.soportapp.ui.viewmodel.TechnicianViewModel
import com.example.soportapp.ui.viewmodel.TechnicianViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianDashboardScreen(
    technicianId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as SoportApplication
    val viewModel: TechnicianViewModel = viewModel(
        factory = TechnicianViewModelFactory(application.container.soportAppRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(technicianId) {
        viewModel.loadTasks(technicianId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel del Técnico", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadTasks(technicianId) }) {
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
                is TechnicianUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is TechnicianUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
                is TechnicianUiState.Success -> {
                    if (state.tasks.isEmpty()) {
                        Text("No tienes tareas asignadas", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                            items(state.tasks) { task ->
                                TechnicianTaskItem(
                                    task = task,
                                    onComplete = { solution, parts, notes ->
                                        viewModel.completeTask(task.id.toLong(), technicianId, solution, parts, notes)
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TechnicianTaskItem(task: SupportRequest, onComplete: (String, String, String) -> Unit) {
    var showCompleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.serviceNameSnapshot, fontWeight = FontWeight.Bold)
                    Text("Ticket #${task.id} • ${task.modalidad}", fontSize = 12.sp, color = Color.Gray)
                }
                StatusBadgeTechnician(task.estado)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Problema:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(task.problemDescription, fontSize = 14.sp, color = Color.DarkGray)
            
            if (task.aiDiagnosis != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Diagnóstico IA:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF3730A3))
                        }
                        Text(task.aiDiagnosis, fontSize = 13.sp, color = Color(0xFF312E81))
                    }
                }
            }

            if (task.estado.lowercase() != "completado") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showCompleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Finalizar Servicio")
                }
            }
        }
    }

    if (showCompleteDialog) {
        CompleteTaskDialog(
            onDismiss = { showCompleteDialog = false },
            onConfirm = { solution, parts, notes ->
                onComplete(solution, parts, notes)
                showCompleteDialog = false
            }
        )
    }
}

@Composable
fun CompleteTaskDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var solution by remember { mutableStateOf("") }
    var parts by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar Servicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = solution, onValueChange = { solution = it }, label = { Text("Solución Técnica") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = parts, onValueChange = { parts = it }, label = { Text("Repuestos Usados") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(solution, parts, notes) }, enabled = solution.isNotBlank()) {
                Text("Cerrar Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun StatusBadgeTechnician(status: String) {
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
