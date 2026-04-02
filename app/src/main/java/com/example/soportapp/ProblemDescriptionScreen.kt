package com.example.soportapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.ui.theme.SoportAppTheme
import com.example.soportapp.ui.viewmodel.ProblemDescriptionUiState
import com.example.soportapp.ui.viewmodel.ProblemDescriptionViewModel
import com.example.soportapp.ui.viewmodel.ProblemDescriptionViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDescriptionScreen(
    userType: String,
    serviceId: String,
    onContinue: (Long) -> Unit,
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as SoportApplication
    val viewModel: ProblemDescriptionViewModel = viewModel(
        factory = ProblemDescriptionViewModelFactory(application.container.soportAppRepository)
    )

    var description by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("")) }
    var day by remember { mutableStateOf("Lunes") }
    var timeInput by remember { mutableStateOf(TextFieldValue("")) }
    
    // NUEVO: Estado para la modalidad seleccionada por el técnico
    var selectedModality by remember { mutableStateOf("Remoto") }

    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    val photos = remember { mutableStateListOf<String>() }
    val uiState by viewModel.uiState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    val isTimeValid = remember(day, timeInput.text) {
        val timeStr = timeInput.text.replace(":", "")
        val timeInt = timeStr.toIntOrNull() ?: 0
        if (day == "Sábado") timeInt in 800..1100 else timeInt in 800..1900 && (timeInt <= 1300 || timeInt >= 1400)
    }

    val isValid = description.text.isNotBlank() && location.text.isNotBlank()
    val descriptionLimit = 500
    val locationLimit = 200

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ProblemDescriptionUiState.Success -> onContinue(state.supportRequestId)
            is ProblemDescriptionUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Registro de servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 3 de 6 - Gestión Técnica", fontSize = 13.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text("Descripción del problema *", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.text.length <= descriptionLimit) description = it },
                        placeholder = { Text("Detalle técnico de la falla...", fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Text(text = "${description.text.length} / $descriptionLimit", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                        }
                    )
                }

                item {
                    Text("Dirección del servicio *", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = location,
                        onValueChange = { if (it.text.length <= locationLimit) location = it },
                        placeholder = { Text("Calle, número, barrio, ciudad", fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(24.dp)) },
                        supportingText = {
                            Text(text = "${location.text.length} / $locationLimit", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                        }
                    )
                }

                item {
                    Text("Evidencia fotográfica (opcional)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { if (photos.size < 3) photos.add("photo_${System.currentTimeMillis()}") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (photos.isEmpty()) "Capturar evidencia" else "Fotos capturadas (${photos.size}/3)")
                    }
                }

                // NUEVA SECCIÓN: Selección de Modalidad para el Técnico
                item {
                    Text("Modalidad de atención *", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Remoto", "Sitio", "Centro Diagnóstico").forEach { modality ->
                            FilterChip(
                                selected = selectedModality == modality,
                                onClick = { selectedModality = modality },
                                label = { Text(modality) },
                                leadingIcon = if (selectedModality == modality) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    var expanded by remember { mutableStateOf(false) }
                    Text("Día programado", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedCard(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = day, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(32.dp))
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            daysOfWeek.forEach { selection ->
                                DropdownMenuItem(text = { Text(selection, fontSize = 16.sp) }, onClick = { day = selection; expanded = false })
                            }
                        }
                    }
                }

                item {
                    Text("Hora programada", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        placeholder = { Text("Ej: 09:30 o 15:00", fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(24.dp)) },
                        isError = !isTimeValid && timeInput.text.isNotEmpty(),
                        supportingText = {
                            val helpText = if (day == "Sábado") "Sábados: 8:00 AM a 11:00 AM" 
                                          else "Lunes-Viernes: 8:00-13:00 y 14:00-19:00"
                            Text(text = helpText, color = if (!isTimeValid && timeInput.text.isNotEmpty()) MaterialTheme.colorScheme.error else Color.Gray)
                        }
                    )
                }

                item {
                    Button(
                        onClick = {
                            val request = SupportRequest(
                                serviceCatalogId = serviceId,
                                problemDescription = description.text,
                                serviceAddress = location.text,
                                suggestedDay = day,
                                suggestedTime = timeInput.text,
                                clientTypeId = userType,
                                modalidad = selectedModality,
                                estado = "Pendiente",
                                requestStatus = "POR_PAGAR"
                            )
                            viewModel.saveProblemDescription(request, photos.toList())
                        },
                        enabled = isValid && uiState != ProblemDescriptionUiState.Loading,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text("Continuar registro", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            if (uiState == ProblemDescriptionUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProblemDescriptionScreenPreview() {
    SoportAppTheme {
        ProblemDescriptionScreen(userType = "Hogar", serviceId = "soporte-computadores", onContinue = {}, onBack = {})
    }
}
