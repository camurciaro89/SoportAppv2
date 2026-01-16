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

// 1. DEFINICIONES DE ESTADOS Y MODELOS
enum class ServiceState {
    EVALUANDO, EN_CAMINO, SOPORTE_REMOTO, EN_DIAGNOSTICO, FINALIZADO
}

data class StatusUiInfo(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val progress: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceStatusScreen(
    technician: Technician,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    var currentState by remember { mutableStateOf<ServiceState>(ServiceState.EVALUANDO) }
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
        // BOTÓN FIJO EN LA PARTE INFERIOR: COHERENCIA Y FACILIDAD DE USO
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text("Finalizar servicio y calificar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item { StatusProgressCard(currentState, isExtraPaid, isSelfDelivery) }

                // CARD DE ACCIÓN REQUERIDA (Unificado a $30.000 para seriedad y honestidad)
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
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            // PASARELA DE PAGO INTEGRADA (Simulando Paso 6)
            if (showPaymentSheet) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.BottomCenter) {
                        Card(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Confirmar pago adicional", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { showPaymentSheet = false }) {
                                        Icon(Icons.Default.Close, null)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                if (isProcessingPayment) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFF2563EB))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Procesando pago seguro...", color = Color.Gray)
                                    }
                                } else {
                                    val amount = 30000 // TARIFA ÚNICA DE DESPLAZAMIENTO
                                    Text("Valor a pagar: ${currencyFormatter.format(amount)}", fontSize = 22.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.ExtraBold)
                                    Text("Concepto: Traslado técnico especializado", fontSize = 14.sp, color = Color.Gray)
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    var selectedMethod by remember { mutableStateOf("nequi") }
                                    PaymentMethodItemLocal(
                                        title = "Nequi / Daviplata",
                                        icon = Icons.Default.QrCodeScanner,
                                        isSelected = selectedMethod == "nequi",
                                        onClick = { selectedMethod = "nequi" }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    PaymentMethodItemLocal(
                                        title = "Tarjeta / PSE",
                                        icon = Icons.Default.CreditCard,
                                        isSelected = selectedMethod == "card",
                                        onClick = { selectedMethod = "card" }
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    Button(
                                        onClick = { isProcessingPayment = true },
                                        modifier = Modifier.fillMaxWidth().height(60.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                                    ) {
                                        Text("Confirmar Pago de $30.000", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (isProcessingPayment) {
                    LaunchedEffect(Unit) {
                        delay(2500)
                        isExtraPaid = true
                        isProcessingPayment = false
                        showPaymentSheet = false
                    }
                }
            }
        }
    }
}

@Composable
fun ActionRequiredCard(state: ServiceState, onPayClick: () -> Unit, onSelfDeliveryClick: () -> Unit) {
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
                text = if (state == ServiceState.EN_DIAGNOSTICO) "El técnico sugiere traslado al laboratorio. Elige una opción:" else "El técnico debe desplazarse a tu ubicación para continuar.",
                fontSize = 14.sp, color = Color(0xFF9A3412), lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onPayClick, 
                modifier = Modifier.fillMaxWidth().height(52.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Pagar traslado o recogida ($30.000)", fontWeight = FontWeight.Bold)
            }
            if (state == ServiceState.EN_DIAGNOSTICO) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onSelfDeliveryClick, 
                    modifier = Modifier.fillMaxWidth().height(52.dp), 
                    border = BorderStroke(1.dp, Color(0xFFEA580C)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Yo lo llevo personalmente ($0)", color = Color(0xFFEA580C), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun StatusProgressCard(state: ServiceState, isExtraPaid: Boolean, isSelfDelivery: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF3F4F6))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(if (state == ServiceState.EN_CAMINO) Icons.Default.DirectionsCar else Icons.AutoMirrored.Filled.FactCheck, null, tint = Color(0xFF2563EB)) }
                Spacer(modifier = Modifier.width(16.dp)); Column { 
                    val title = when(state) {
                        ServiceState.EVALUANDO -> "Evaluando tu caso"
                        ServiceState.EN_CAMINO -> if (isExtraPaid) "Técnico en camino" else "Esperando pago"
                        ServiceState.EN_DIAGNOSTICO -> if (isExtraPaid) "Recolección en curso" else if (isSelfDelivery) "Esperando entrega" else "Acción requerida"
                        else -> "Servicio activo"
                    }
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Estado: ${state.name.lowercase().replaceFirstChar { it.uppercase() }}", fontSize = 14.sp, color = Color.Gray) 
                }
            }
        }
    }
}

@Composable
fun SelfDeliveryInfoCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), border = BorderStroke(1.dp, Color(0xFFDCFCE7))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Store, null, tint = Color(0xFF16A34A)); Spacer(modifier = Modifier.width(12.dp)); Text("Punto de entrega", fontWeight = FontWeight.Bold, color = Color(0xFF166534)) }
            Spacer(modifier = Modifier.height(8.dp)); Text("Calle 123 #45-67, Bogotá", fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text("Horario: Lun-Vie 8:00 AM - 6:00 PM", fontSize = 12.sp, color = Color.Gray); Text("ID de servicio: #ST-9921", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
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
            TimelineItemLocal("Solicitud y Pago Base", "Completado", Icons.Default.CheckCircle, true, true)
            TimelineItemLocal("Evaluación Técnica", "Realizada por el experto", Icons.AutoMirrored.Filled.FactCheck, state == ServiceState.EVALUANDO, true)
            TimelineItemLocal("Ejecución del Servicio", "En proceso", Icons.Default.Construction, state != ServiceState.EVALUANDO, false)
        }
    }
}

@Composable
fun TimelineItemLocal(title: String, subtitle: String, icon: ImageVector, isActive: Boolean, showLine: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(32.dp).background(if (isActive) Color(0xFFDBEAFE) else Color(0xFFF1F5F9), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (isActive) Color(0xFF2563EB) else Color(0xFF94A3B8), modifier = Modifier.size(16.dp)) }
            if (showLine) { Box(modifier = Modifier.width(2.dp).height(24.dp).background(if (isActive) Color(0xFFDBEAFE) else Color(0xFFF1F5F9))) }
        }
        Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.padding(top = 4.dp)) { Text(title, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, color = if (isActive) Color(0xFF111827) else Color(0xFF94A3B8)); if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 12.sp, color = Color.Gray); Spacer(modifier = Modifier.height(if (showLine) 20.dp else 0.dp)) }
    }
}

@Composable
fun PaymentMethodItemLocal(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White), border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFF3F4F6))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (isSelected) Color(0xFF2563EB) else Color.Gray) }
            Spacer(modifier = Modifier.width(16.dp)); Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f)); RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceStatusScreenPreview() {
    SoportAppTheme { ServiceStatusScreen(technician = TechnicianRepository.getMainTechnician(), onBack = {}, onFinish = {}) }
}
