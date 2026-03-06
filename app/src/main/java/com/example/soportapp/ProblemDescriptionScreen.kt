package com.example.soportapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    onContinue: (Long) -> Unit, // Pass supportRequestId
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

    // Handle state changes and navigation
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
                        Text("Describe el problema", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 3 de 10", fontSize = 13.sp, color = Color.Gray)
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
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    Text("¿Qué problema tienes? *", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Ej: El computador no prende...", fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Text("Dirección del servicio *", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text("Calle, número, ciudad", fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(24.dp)) }
                    )
                }

                item {
                    Text("Fotos del equipo (opcional)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { if (photos.size < 3) photos.add("photo_${System.currentTimeMillis()}") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (photos.isEmpty()) "Adjuntar fotos o evidencia"
                            else "Fotos adjuntadas (${photos.size}/3)",
                            fontSize = 15.sp
                        )
                    }
                }

                item {
                    var expanded by remember { mutableStateOf(false) }
                    Text("Día sugerido (opcional)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedCard(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = day, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(32.dp))
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            daysOfWeek.forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection, fontSize = 16.sp) },
                                    onClick = { day = selection; expanded = false }
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Hora sugerida (opcional)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        placeholder = { Text("Ej: 09:30 o 15:00", fontSize = 16.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(24.dp)) },
                        isError = !isTimeValid && timeInput.text.isNotEmpty()
                    )
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFED7AA))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFFEA580C), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Los servicios en domingos y festivos son excepcionales y requieren confirmación directa del técnico según disponibilidad.",
                                fontSize = 13.sp,
                                color = Color(0xFF9A3412),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }
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
                                // Set default values for fields to be filled in later steps
                                ubicacion = location.text,
                                modalidad = "",
                                estado = "Pendiente",
                                createdAt = "",
                                isHolidayException = false,
                                pagado = false,
                                reembolsado = false,
                                suggestedModality = "",
                                serviceNameSnapshot = "",
                                finalDescription = "",
                                confirmedAddress = "",
                                requestStatus = "INCOMPLETO"
                            )
                            viewModel.saveProblemDescription(request, photos.toList())
                        },
                        enabled = isValid && uiState != ProblemDescriptionUiState.Loading,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text("Continuar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
