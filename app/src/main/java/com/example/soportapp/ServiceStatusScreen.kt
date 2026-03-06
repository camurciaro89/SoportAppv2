package com.example.soportapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.ui.theme.SoportAppTheme
import com.example.soportapp.ui.viewmodel.ServiceStatusUiState
import com.example.soportapp.ui.viewmodel.ServiceStatusViewModel
import com.example.soportapp.ui.viewmodel.ServiceStatusViewModelFactory
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceStatusScreen(
    supportRequestId: Long,
    technician: Technician,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val application = LocalContext.current.applicationContext as SoportApplication
    val viewModel: ServiceStatusViewModel = viewModel(
        factory = ServiceStatusViewModelFactory(application.container.soportAppRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    var showPaymentSheet by remember { mutableStateOf(false) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    val localeCO = Locale.forLanguageTag("es-CO")
    val currencyFormatter = NumberFormat.getCurrencyInstance(localeCO).apply {
        maximumFractionDigits = 0
    }

    LaunchedEffect(supportRequestId) {
        viewModel.loadRequest(supportRequestId)
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
            when (val state = uiState) {
                is ServiceStatusUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ServiceStatusUiState.Error -> {
                    Text(text = state.message, modifier = Modifier.align(Alignment.Center))
                }
                is ServiceStatusUiState.Success -> {
                    val request = state.request
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        item { StatusHeaderCardLocal(request) }

                        item {
                            EvaluationPendingCardLocal()
                        }

                        item { AssignedTechnicianCardLocal(technician) }
                        item { TimelineCardLocal(request) }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
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
                        viewModel.updateExtraPaid(supportRequestId)
                        isProcessingPayment = false
                        showPaymentSheet = false
                    }
                }
            }
        }
    }
}

@Composable
fun StatusHeaderCardLocal(request: SupportRequest) {
    val stateLabel = request.estado
    val color = Color(0xFF16A34A)
    val icon = Icons.Default.CheckCircle

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, color.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }
            Spacer(modifier = Modifier.width(16.dp)); Column { Text(stateLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color); Text("Actualizado ahora", fontSize = 12.sp, color = Color.Gray) }
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
fun AssignedTechnicianCardLocal(technician: Technician) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF3F4F6))) { Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(56.dp).background(Color(0xFFF1F5F9), CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.size(32.dp)) }; Spacer(modifier = Modifier.width(16.dp)); Column { Text(technician.name, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("Técnico asignado", fontSize = 12.sp, color = Color(0xFF2563EB)) } } }
}

@Composable
fun TimelineCardLocal(request: SupportRequest) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF3F4F6))) { Column(modifier = Modifier.padding(20.dp)) { Text("Estado del proceso", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)); TimelineItemLocal("Pago base recibido", request.pagado, true); TimelineItemLocal("Evaluación técnica", true, true); TimelineItemLocal("Ejecución del servicio", false, false) } }
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
    SoportAppTheme { 
        val mockTechnician = Technician(
            id = "camilo-murcia",
            name = "Camilo Andrés Murcia Romero",
            title = "Ingeniero de Sistemas",
            experience = "15 años",
            bio = "Especialista en mantenimiento",
            totalServices = 542,
            reviews = emptyList()
        )
        ServiceStatusScreen(supportRequestId = 1L, technician = mockTechnician, onBack = {}, onFinish = {}) 
    }
}
