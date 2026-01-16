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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceStatusScreen(
    technician: Technician, // PARÁMETRO AÑADIDO PARA COHERENCIA
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    var currentState by remember { mutableStateOf(ServiceState.EVALUANDO) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var isExtraPaid by remember { mutableStateOf(false) }
    var isSelfDelivery by remember { mutableStateOf(false) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    
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
                        Text("Seguimiento del servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 9 de 10", fontSize = 13.sp, color = Color.Gray)
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                        ActionRequiredCard(
                            state = currentState,
                            onPayClick = { showPaymentSheet = true },
                            onSelfDeliveryClick = { isSelfDelivery = true }
                        )
                    }
                }

                if (isSelfDelivery && currentState == ServiceState.EN_DIAGNOSTICO) {
                    item { SelfDeliveryInfoCard() }
                }

                item { AssignedTechnicianCard(technician, currentState) }
                item { TimelineCard(currentState) }

                item {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Finalizar servicio", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (showPaymentSheet) {
                // Pasarela de pago (Omitida por brevedad, se mantiene igual)
            }
        }
    }
}

// DEFINICIONES DE ESTADOS Y COMPONENTES SE MANTIENEN IGUAL...
enum class ServiceState { EVALUANDO, EN_CAMINO, SOPORTE_REMOTO, EN_DIAGNOSTICO, FINALIZADO }
data class StatusUiInfo(val title: String, val subtitle: String, val icon: ImageVector, val iconBg: Color, val iconTint: Color, val progress: Float)

@Composable
fun StatusProgressCard(state: ServiceState, isExtraPaid: Boolean, isSelfDelivery: Boolean) {
    val uiInfo = when (state) {
        ServiceState.EVALUANDO -> StatusUiInfo("Evaluando tu caso", "Revisando información", Icons.AutoMirrored.Filled.FactCheck, Color(0xFFF1F5F9), Color(0xFF475569), 0.15f)
        ServiceState.EN_CAMINO -> StatusUiInfo(if (isExtraPaid) "Técnico en camino" else "Esperando pago", "Hacia tu ubicación", Icons.Default.DirectionsCar, Color(0xFFDBEAFE), Color(0xFF2563EB), 0.45f)
        ServiceState.SOPORTE_REMOTO -> StatusUiInfo("Soporte remoto activo", "Conexión segura", Icons.Default.Devices, Color(0xFFEDE9FE), Color(0xFF7C3AED), 0.45f)
        ServiceState.EN_DIAGNOSTICO -> StatusUiInfo(if (isExtraPaid) "Recolección en curso" else if (isSelfDelivery) "Esperando entrega" else "Acción requerida", "Centro de diagnóstico", Icons.Default.Apartment, Color(0xFFFEF3C7), Color(0xFFD97706), 0.45f)
        else -> StatusUiInfo("Servicio", "", Icons.Default.Build, Color.Gray, Color.White, 0.5f)
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF3F4F6))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(uiInfo.iconBg, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(uiInfo.icon, null, tint = uiInfo.iconTint) }
                Spacer(modifier = Modifier.width(16.dp)); Column { Text(uiInfo.title, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(uiInfo.subtitle, fontSize = 14.sp, color = Color.Gray) }
            }
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(progress = { uiInfo.progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = uiInfo.iconTint, trackColor = Color(0xFFE2E8F0))
        }
    }
}

@Composable
fun ActionRequiredCard(state: ServiceState, onPayClick: () -> Unit, onSelfDeliveryClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), border = BorderStroke(1.dp, Color(0xFFFED7AA))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Payments, null, tint = Color(0xFFEA580C)); Spacer(modifier = Modifier.width(12.dp)); Text("Acción requerida", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412)) }
            Spacer(modifier = Modifier.height(12.dp)); Text(text = if (state == ServiceState.EN_DIAGNOSTICO) "Se requiere traslado al laboratorio." else "Camilo debe desplazarse.", fontSize = 13.sp, color = Color(0xFF9A3412))
            Spacer(modifier = Modifier.height(16.dp)); Button(onClick = onPayClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))) { Text(if (state == ServiceState.EN_DIAGNOSTICO) "Pagar recogida" else "Pagar traslado") }
        }
    }
}

@Composable
fun SelfDeliveryInfoCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), border = BorderStroke(1.dp, Color(0xFFDCFCE7))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Store, null, tint = Color(0xFF16A34A)); Spacer(modifier = Modifier.width(12.dp)); Text("Punto de entrega", fontWeight = FontWeight.Bold, color = Color(0xFF166534)) }
            Spacer(modifier = Modifier.height(8.dp)); Text("Calle 123 #45-67, Bogotá", fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("ID de servicio: #ST-9921", fontSize = 12.sp, color = Color(0xFF166534))
        }
    }
}

@Composable
fun AssignedTechnicianCard(technician: Technician, state: ServiceState) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF3F4F6))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Técnico experto", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(56.dp).background(Color(0xFFF1F5F9), CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = Color(0xFF94A3B8)) }
                Spacer(modifier = Modifier.width(16.dp)); Column { Text(technician.name, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("Andrés te atenderá", fontSize = 12.sp, color = Color(0xFF2563EB)) }
            }
        }
    }
}

@Composable
fun TimelineCard(state: ServiceState) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF3F4F6))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Evolución", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))
            TimelineItem("Solicitud y Pago Base", "Completado", Icons.Default.CheckCircle, true, true)
            TimelineItem("Evaluación Técnica", "Realizada por Camilo", Icons.AutoMirrored.Filled.FactCheck, state == ServiceState.EVALUANDO, true)
            TimelineItem("Ejecución del Servicio", "En proceso", Icons.Default.Construction, state != ServiceState.EVALUANDO, false)
        }
    }
}

@Composable
fun TimelineItem(title: String, subtitle: String, icon: ImageVector, isActive: Boolean, showLine: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(32.dp).background(if (isActive) Color(0xFFDBEAFE) else Color(0xFFF1F5F9), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (isActive) Color(0xFF2563EB) else Color(0xFF94A3B8), modifier = Modifier.size(16.dp)) }
            if (showLine) { Box(modifier = Modifier.width(2.dp).height(24.dp).background(if (isActive) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))) }
        }
        Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.padding(top = 4.dp)) { Text(title, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, color = if (isActive) Color(0xFF111827) else Color(0xFF94A3B8)); if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 12.sp, color = Color.Gray); Spacer(modifier = Modifier.height(if (showLine) 20.dp else 0.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceStatusScreenPreview() {
    SoportAppTheme { ServiceStatusScreen(technician = TechnicianRepository.getMainTechnician(), onBack = {}, onFinish = {}) }
}
