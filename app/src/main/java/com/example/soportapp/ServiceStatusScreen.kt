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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soportapp.ui.theme.SoportAppTheme
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

// 1. DEFINICIONES DE ESTADOS Y MODELOS
enum class ServiceState {
    EVALUANDO, REMOTO, SITIO, DIAGNOSTICO
}

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
        currentState = ServiceState.SITIO 
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
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 12.dp) {
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

                item { StatusHeaderCardLocal(currentState, isExtraPaid, isSelfDelivery) }

                item {
                    when (currentState) {
                        ServiceState.EVALUANDO -> EvaluationPendingCardLocal()
                        ServiceState.REMOTO -> RemoteDecisionCardLocal()
                        ServiceState.SITIO -> InSiteDecisionCardLocal(isExtraPaid) { showPaymentSheet = true }
                        ServiceState.DIAGNOSTICO -> DiagnosticDecisionCardLocal(isExtraPaid, isSelfDelivery, 
                            onPay = { showPaymentSheet = true }, 
                            onSelf = { isSelfDelivery = true }
                        )
                    }
                }

                if (isSelfDelivery && currentState == ServiceState.DIAGNOSTICO) {
                    item { SelfDeliveryInfoCardLocal() }
                }

                item { AssignedTechnicianCardLocal(technician) }
                item { TimelineCardLocal(currentState, isExtraPaid || isSelfDelivery) }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            if (showPaymentSheet) {
                PaymentBottomSheetLocal(
                    amount = 30000,
                    isProcessing = isProcessingPayment,
                    onConfirm = { isProcessingPayment = true },
                    onClose = { showPaymentSheet = false },
                    currencyFormatter = currencyFormatter
                )
                
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
fun StatusHeaderCardLocal(state: ServiceState, isPaid: Boolean, isSelf: Boolean) {
    val config = when (state) {
        ServiceState.EVALUANDO -> Triple("Evaluando tu caso", Color.Gray, Icons.Default.Search)
        ServiceState.REMOTO -> Triple("Solución Remota", Color(0xFF7C3AED), Icons.Default.Devices)
        ServiceState.SITIO -> Triple(if (isPaid) "Visita Confirmada" else "Servicio en Sitio", Color(0xFF16A34A), Icons.Default.LocationOn)
        ServiceState.DIAGNOSTICO -> Triple(if (isPaid || isSelf) "Laboratorio en curso" else "Centro Diagnóstico", Color(0xFF2563EB), Icons.Default.Apartment)
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, config.second.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(config.second.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(config.third, null, tint = config.second) }
            Spacer(modifier = Modifier.width(16.dp)); Column { Text(config.first, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = config.second); Text("Actualizado ahora", fontSize = 12.sp, color = Color.Gray) }
        }
    }
}

@Composable
fun EvaluationPendingCardLocal() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp, color = Color.Gray); Spacer(modifier = Modifier.height(16.dp)); Text("Analizando tu falla...", textAlign = TextAlign.Center, fontSize = 15.sp, color = Color.DarkGray) }
    }
}

@Composable
fun RemoteDecisionCardLocal() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)), border = BorderStroke(1.dp, Color(0xFFDDD6FE))) {
        Column(modifier = Modifier.padding(20.dp)) { Text("¡Todo listo!", fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED), fontSize = 16.sp); Spacer(modifier = Modifier.height(8.dp)); Text("Camilo te llamará en breve para iniciar la sesión remota en la hora solicitada.", fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp) }
    }
}

@Composable
fun InSiteDecisionCardLocal(isPaid: Boolean, onPay: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isPaid) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)), border = BorderStroke(1.dp, if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))) {
        Column(modifier = Modifier.padding(20.dp)) { if (isPaid) { Text("Traslado confirmado", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A)); Text("El técnico te llamará en minutos para confirmar la cita presencial.", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)) } else { Text("Visita requerida", fontWeight = FontWeight.Bold, color = Color(0xFFB45309)); Text("El técnico debe desplazarse a tu ubicación.", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)); Spacer(modifier = Modifier.height(16.dp)); Button(onClick = onPay, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)), shape = RoundedCornerShape(8.dp)) { Text("Pagar traslado ($30.000)", fontWeight = FontWeight.Bold) } } }
    }
}

@Composable
fun DiagnosticDecisionCardLocal(isPaid: Boolean, isSelf: Boolean, onPay: () -> Unit, onSelf: () -> Unit) {
    if (isPaid) { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), border = BorderStroke(1.dp, Color(0xFFDCFCE7))) { Column(modifier = Modifier.padding(20.dp)) { Text("Recogida programada", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A)); Text("El experto pasará por tu equipo pronto.", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)) } } } else if (!isSelf) { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)), border = BorderStroke(1.dp, Color(0xFFDBEAFE))) { Column(modifier = Modifier.padding(20.dp)) { Text("Requiere Laboratorio", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB)); Text("Tu equipo necesita herramientas de nuestro centro especializado.", fontSize = 14.sp); Spacer(modifier = Modifier.height(16.dp)); Button(onClick = onPay, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(8.dp)) { Text("Pagar recogida ($30.000)", fontWeight = FontWeight.Bold) }; Spacer(modifier = Modifier.height(12.dp)); OutlinedButton(onClick = onSelf, modifier = Modifier.fillMaxWidth().height(52.dp), border = BorderStroke(1.dp, Color(0xFF2563EB)), shape = RoundedCornerShape(8.dp)) { Text("Yo lo llevo personalmente ($0)", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold) } } } }
}

@Composable
fun SelfDeliveryInfoCardLocal() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), border = BorderStroke(1.dp, Color(0xFFDCFCE7))) { Column(modifier = Modifier.padding(20.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Store, null, tint = Color(0xFF16A34A)); Spacer(modifier = Modifier.width(12.dp)); Text("Punto de entrega", fontWeight = FontWeight.Bold, color = Color(0xFF166534)) }; Text("Calle 123 #45-67, Edificio Tech, Cali.", fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Medium); Text("ID: #ST-9921", fontSize = 12.sp, color = Color.Gray) } }
}

@Composable
fun AssignedTechnicianCardLocal(technician: Technician) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF3F4F6))) { Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(56.dp).background(Color(0xFFF1F5F9), CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(32.dp)) }; Spacer(modifier = Modifier.width(16.dp)); Column { Text(technician.name, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("Técnico asignado", fontSize = 12.sp, color = Color(0xFF2563EB)) } } }
}

@Composable
fun TimelineCardLocal(state: ServiceState, actionDone: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF3F4F6))) { Column(modifier = Modifier.padding(20.dp)) { Text("Estado del proceso", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)); TimelineItemLocal("Pago base recibido", true, true); TimelineItemLocal("Evaluación técnica", state != ServiceState.EVALUANDO, true); TimelineItemLocal("Ejecución del servicio", actionDone || state == ServiceState.REMOTO, false) } }
}

@Composable
fun TimelineItemLocal(text: String, isDone: Boolean, showLine: Boolean) {
    Row { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (isDone) Color(0xFF16A34A) else Color.LightGray, modifier = Modifier.size(20.dp)); if (showLine) Box(modifier = Modifier.width(2.dp).height(24.dp).background(Color.LightGray.copy(alpha = 0.5f))) }; Text(text, fontSize = 14.sp, color = if (isDone) Color.Black else Color.Gray, modifier = Modifier.padding(start = 12.dp, top = 2.dp)) }
}

@Composable
fun PaymentBottomSheetLocal(amount: Int, isProcessing: Boolean, onConfirm: () -> Unit, onClose: () -> Unit, currencyFormatter: NumberFormat) {
    var selectedMethod by remember { mutableStateOf("qr") }
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.6f)) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Confirmar pago adicional", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
                    }
                    if (isProcessing) {
                        Column(modifier = Modifier.fillMaxSize().weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(color = Color(0xFF2563EB), strokeWidth = 6.dp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Verificando pago seguro...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Conectando con tu entidad bancaria.", color = Color.Gray)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Valor a pagar: ${currencyFormatter.format(amount)}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                        Text("Concepto: Traslado técnico especializado", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Selecciona tu opción de pago", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        PaymentMethodOptionLocal(title = "QR Bancolombia / Nequi", subtitle = "Pago rápido desde tu celular", icon = Icons.Default.QrCodeScanner, isSelected = selectedMethod == "qr", onClick = { selectedMethod = "qr" })
                        Spacer(modifier = Modifier.height(12.dp))
                        PaymentMethodOptionLocal(title = "PSE / Tarjetas (Wompi)", subtitle = "Pago seguro bancario", icon = Icons.Default.VerifiedUser, isSelected = selectedMethod == "wompi", onClick = { selectedMethod = "wompi" })
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))) {
                            Text("Realizar pago seguro", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodOptionLocal(title: String, subtitle: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White), border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (isSelected) Color(0xFF2563EB) else Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text(subtitle, fontSize = 12.sp, color = Color.Gray) }
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceStatusScreenPreview() {
    SoportAppTheme { ServiceStatusScreen(technician = TechnicianRepository.getMainTechnician(), onBack = {}, onFinish = {}) }
}
