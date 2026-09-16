package com.example.soportapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soportapp.data.database.Equipment
import com.example.soportapp.ui.viewmodel.EquipmentUiState
import com.example.soportapp.ui.viewmodel.EquipmentViewModel
import com.example.soportapp.ui.viewmodel.EquipmentViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSelectionScreen(
    userId: Int,
    onEquipmentSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as SoportApplication
    val viewModel: EquipmentViewModel = viewModel(
        factory = EquipmentViewModelFactory(application.container.soportAppRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadEquipments(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Equipos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Agregar Equipo")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Agregar")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is EquipmentUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is EquipmentUiState.Success -> {
                    if (state.equipments.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No tienes equipos registrados", color = Color.Gray)
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("Registrar el primero")
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                            items(state.equipments) { equipment ->
                                EquipmentItem(equipment, onEquipmentSelected)
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
                is EquipmentUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            }
        }

        if (showAddDialog) {
            AddEquipmentDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { type, brand, model, serial, os ->
                    viewModel.registerEquipment(
                        Equipment(
                            userId = userId,
                            tipo = type,
                            marca = brand,
                            modelo = model,
                            serialNumber = serial,
                            operatingSystem = os
                        )
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun EquipmentItem(equipment: Equipment, onSelect: (Int) -> Unit) {
    Card(
        onClick = { onSelect(equipment.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = when (equipment.tipo.lowercase()) {
                "portátil", "laptop" -> Icons.Default.Laptop
                "computador", "desktop" -> Icons.Default.DesktopWindows
                "impresora" -> Icons.Default.Print
                else -> Icons.Default.Devices
            }
            Icon(icon, null, modifier = Modifier.size(32.dp), tint = Color(0xFF2563EB))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("${equipment.marca} ${equipment.modelo}", fontWeight = FontWeight.Bold)
                Text("S/N: ${equipment.serialNumber}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEquipmentDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    var type by remember { mutableStateOf("Portátil") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var os by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Equipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Portátil", "Computador", "Impresora", "Servidor").forEach { selection ->
                            DropdownMenuItem(text = { Text(selection) }, onClick = { type = selection; expanded = false })
                        }
                    }
                }
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Marca") })
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modelo") })
                OutlinedTextField(value = serial, onValueChange = { serial = it }, label = { Text("Número de Serie") })
                OutlinedTextField(value = os, onValueChange = { os = it }, label = { Text("Sistema Operativo") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(type, brand, model, serial, os) }, enabled = brand.isNotBlank()) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
