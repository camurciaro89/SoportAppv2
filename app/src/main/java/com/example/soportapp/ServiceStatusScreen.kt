package com.example.soportapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soportapp.ui.theme.SoportAppTheme
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

enum class ServiceState {
    EVALUANDO, EN_CAMINO, SOPORTE_REMOTO, EN_DIAGNOSTICO, FINALIZADO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceStatusScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    var currentState by remember { mutableStateOf(ServiceState.EVALUANDO) }
    var isExtraPaid by remember { mutableStateOf(false) }
    var isSelfDelivery by remember { mutableStateOf(false) }
    
    val technician = remember { TechnicianRepository.getMainTechnician() }
    
    // Corrección de Locale deprecated
    val localeCO = Locale.forLanguageTag("es-CO")
    val currencyFormatter = NumberFormat.getCurrencyInstance(localeCO).apply {
        maximumFractionDigits = 0
    }

    LaunchedEffect(Unit) {
        delay(5000) 
        currentState = ServiceState.EN_DIAGNOSTICO 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Seguimiento del servicio", fontSize = 16.sp)
                        Text("Paso 9 de 10", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
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
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { StatusProgressCard(currentState, isExtraPaid, isSelfDelivery) }

            if (!isExtraPaid && !isSelfDelivery && (currentState == ServiceState.EN_CAMINO || currentState == ServiceState.EN_DIAGNOSTICO)) {
                item {
                    val isDiagnostic = currentState == ServiceState.EN_DIAGNOSTICO
                    val extraAmount = if (isDiagnostic) 30000 else 20000
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                        border = BorderStroke(1.dp, Color(0xFFFED7AA))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payments, null, tint = Color(0xFFEA580C))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Acción requerida", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isDiagnostic) 
                                    "Camilo sugiere llevar el equipo al laboratorio para un diagnóstico profundo." 
                                    else "Camilo debe desplazarse a tu ubicación para solucionar el problema.",
                                fontSize = 13.sp,
                                color = Color(0xFF9A3412)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { isExtraPaid = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isDiagnostic) "Solicitar recogida (${currencyFormatter.format(extraAmount)})" else "Pagar desplazamiento (${currencyFormatter.format(extraAmount)})")
                            }
                            
                            if (isDiagnostic) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { isSelfDelivery = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, Color(0xFFEA580C)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Yo lo llevo personalmente ($0)", color = Color(0xFFEA580C))
                                }
                            }
                        }
                    }
                }
            }

            if (isSelfDelivery && currentState == ServiceState.EN_DIAGNOSTICO) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFFDCFCE7))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Store, null, tint = Color(0xFF16A34A))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Punto de entrega", fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Calle 123 #45-67, Bogotá D.C.", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Horario: Lun-Vie 8:00 AM - 6:00 PM", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Muestra tu ID de servicio al llegar: #ST-9921", fontSize = 12.sp, color = Color(0xFF166534))
                        }
                    }
                }
            }

            item { AssignedTechnicianCard(technician, currentState) }
            item { TimelineCard(currentState) }

            item {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Finalizar demostración", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StatusProgressCard(state: ServiceState, isExtraPaid: Boolean, isSelfDelivery: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val title = when (state) {
                ServiceState.EVALUANDO -> "Evaluando tu caso"
                ServiceState.EN_CAMINO -> if (isExtraPaid) "Técnico en camino" else "Esperando pago"
                ServiceState.SOPORTE_REMOTO -> "Soporte remoto activo"
                ServiceState.EN_DIAGNOSTICO -> when {
                    isExtraPaid -> "Recolección en curso"
                    isSelfDelivery -> "Esperando entrega"
                    else -> "Acción requerida"
                }
                else -> "Servicio"
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (state == ServiceState.EN_CAMINO) Icons.Default.DirectionsCar else Icons.AutoMirrored.Filled.FactCheck, 
                        contentDescription = null, 
                        tint = Color(0xFF2563EB)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    // Corrección de capitalize() deprecated
                    val modalityName = state.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    Text("Modalidad: $modalityName", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AssignedTechnicianCard(technician: Technician, state: ServiceState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Técnico experto", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(56.dp).background(Color(0xFFF1F5F9), CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = Color(0xFF94A3B8))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(technician.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Andrés te atenderá", fontSize = 12.sp, color = Color(0xFF2563EB))
                }
            }
        }
    }
}

@Composable
fun TimelineCard(state: ServiceState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Evolución", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))
            TimelineItem("Solicitud y Pago Base", "Completado", Icons.Default.CheckCircle, isActive = true, showLine = true)
            TimelineItem("Evaluación Técnica", "Realizada por Camilo", Icons.AutoMirrored.Filled.FactCheck, isActive = state == ServiceState.EVALUANDO, showLine = true)
            TimelineItem("Ejecución del Servicio", "En proceso", Icons.Default.Construction, isActive = state != ServiceState.EVALUANDO, showLine = false)
        }
    }
}

@Composable
fun TimelineItem(title: String, subtitle: String, icon: ImageVector, isActive: Boolean, showLine: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(32.dp).background(if (isActive) Color(0xFFDBEAFE) else Color(0xFFF1F5F9), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (isActive) Color(0xFF2563EB) else Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            }
            if (showLine) {
                Box(modifier = Modifier.width(2.dp).height(24.dp).background(if (isActive) Color(0xFFDBEAFE) else Color(0xFFF1F5F9)))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, color = if (isActive) Color(0xFF111827) else Color(0xFF94A3B8))
            if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(if (showLine) 20.dp else 0.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceStatusScreenPreview() {
    SoportAppTheme {
        ServiceStatusScreen(onBack = {}, onFinish = {})
    }
}
