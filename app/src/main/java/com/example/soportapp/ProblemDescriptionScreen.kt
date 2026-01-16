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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soportapp.ui.theme.SoportAppTheme

// CLASE DE DATOS GLOBAL PARA EL PROBLEMA
data class ProblemDetails(
    val description: String,
    val location: String,
    val day: String,
    val time: String,
    val photos: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDescriptionScreen(
    onContinue: (ProblemDetails) -> Unit,
    onBack: () -> Unit
) {
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf(TextFieldValue("")) }
    var day by remember { mutableStateOf("Lunes") }
    var timeInput by remember { mutableStateOf(TextFieldValue("")) }
    
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    val photos = remember { mutableStateListOf<String>() }

    val isTimeValid = remember(day, timeInput.text) {
        val timeStr = timeInput.text.replace(":", "")
        val timeInt = timeStr.toIntOrNull() ?: 0
        if (day == "Sábado") timeInt in 800..1100 else timeInt in 800..1900 && (timeInt <= 1300 || timeInt >= 1400)
    }

    // Solo son obligatorios el problema y la dirección
    val isValid = description.text.isNotBlank() && location.text.isNotBlank()

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
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Descripción (OBLIGATORIO)
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

            // Ubicación (OBLIGATORIO)
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

            // Fotos (Opcional)
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

            // Día sugerido
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

            // Hora sugerida
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

            // NOTA SOLICITADA
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

            // Botón
            item {
                Button(
                    onClick = {
                        onContinue(ProblemDetails(description.text, location.text, day, timeInput.text, photos.toList()))
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("Continuar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProblemDescriptionScreenPreview() {
    SoportAppTheme {
        ProblemDescriptionScreen(onContinue = {}, onBack = {})
    }
}
