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
import java.util.*

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
    var isIdentityVerified by remember { mutableStateOf(false) }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    LaunchedEffect(Unit) {
        viewModel.loadRequest(supportRequestId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Estado del servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
        floatingActionButton = {
            if (uiState is ServiceStatusUiState.Success) {
                val request = (uiState as ServiceStatusUiState.Success).request
                // Permitir finalizar si el pago está hecho o la identidad verificada
                if (request.pagado || isIdentityVerified) {
                    ExtendedFloatingActionButton(
                        onClick = onFinish,
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Star, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Finalizar y Calificar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is ServiceStatusUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ServiceStatusUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
                is ServiceStatusUiState.Success -> {
                    val request = state.request
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        // 1. CÓDIGO SEGURO (OTP)
                        item {
                            SecurityCodeCardFinal(
                                code = "8429",
                                isVerified = isIdentityVerified,
                                onSimulateVerify = { isIdentityVerified = true }
                            )
                        }

                        // 2. MANERA EN QUE SE REALIZARÁ EL SERVICIO (Modalidad)
                        item { ModalityStatusCard(request.modalidad) }

                        // 3. MEDIO DE PAGO
                        item {
                            PaymentSummaryCard(
                                isPaid = request.pagado,
                                amount = 30000,
                                currencyFormatter = currencyFormatter,
                                onPayClick = { showPaymentSheet = true }
                            )
                        }

                        // 4. INFORMACIÓN DEL TÉCNICO (Opcional pero recomendado para confianza)
                        item { AssignedTechnicianCardFinal(technician) }

                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }

            if (showPaymentSheet) {
                PaymentBottomSheetFinal(
                    amount = 30000,
                    isProcessing = isProcessingPayment,
                    onConfirm = { isProcessingPayment = true },
                    onClose = { showPaymentSheet = false },
                    currencyFormatter = currencyFormatter
                )

                if (isProcessingPayment) {
                    LaunchedEffect(Unit) {
                        delay(2000)
                        viewModel.updateRequestStatus(supportRequestId, true, false)
                        isProcessingPayment = false
                        showPaymentSheet = false
                    }
                }
            }
        }
    }
}

@Composable
fun ModalityStatusCard(modalidad: String) {
    val (title, icon, color) = when (modalidad.lowercase()) {
        "remoto" -> Triple("Soporte Remoto", Icons.Default.Devices, Color(0xFF7C3AED))
        "sitio", "presencial" -> Triple("Servicio Presencial", Icons.Default.Home, Color(0xFF16A34A))
        "taller", "recogida", "laboratorio" -> Triple("Recogida para Taller", Icons.Default.LocalShipping, Color(0xFFEA580C))
        else -> Triple("Modalidad: $modalidad", Icons.Default.Info, Color(0xFF2563EB))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Cómo atenderemos tu servicio:", fontSize = 12.sp, color = Color.Gray)
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
fun PaymentSummaryCard(isPaid: Boolean, amount: Int, currencyFormatter: NumberFormat, onPayClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPaid) Color(0xFFF0FDF4) else Color.White),
        border = BorderStroke(1.dp, if (isPaid) Color(0xFFDCFCE7) else Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isPaid) Icons.Default.CheckCircle else Icons.Default.Payments, null, tint = if (isPaid) Color(0xFF16A34A) else Color(0xFF2563EB))
                Spacer(modifier = Modifier.width(12.dp))
                Text(if (isPaid) "Pago Confirmado" else "Pendiente de Pago", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            if (!isPaid) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Costo del servicio (Diagnóstico/Domicilio):", fontSize = 13.sp, color = Color.Gray)
                Text(currencyFormatter.format(amount), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("Seleccionar medio de pago")
                }
            } else {
                Text("El pago ha sido registrado exitosamente.", fontSize = 14.sp, color = Color(0xFF166534), modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun SecurityCodeCardFinal(code: String, isVerified: Boolean, onSimulateVerify: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isVerified) Color(0xFF059669) else Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (isVerified) "IDENTIDAD VERIFICADA" else "CÓDIGO DE SEGURIDAD", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (isVerified) {
                Icon(Icons.Default.VerifiedUser, null, tint = Color.White, modifier = Modifier.size(40.dp))
            } else {
                Text(code, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp)
                Text("Dile este código al técnico al llegar.", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, textAlign = TextAlign.Center)
                TextButton(onClick = onSimulateVerify) { Text("Simular Verificación", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp) }
            }
        }
    }
}

@Composable
fun AssignedTechnicianCardFinal(technician: Technician) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFF3F4F6))) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color(0xFFF1F5F9), CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = Color.LightGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(technician.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Técnico experto asignado", fontSize = 12.sp, color = Color(0xFF2563EB))
            }
        }
    }
}

@Composable
fun PaymentBottomSheetFinal(amount: Int, isProcessing: Boolean, onConfirm: () -> Unit, onClose: () -> Unit, currencyFormatter: NumberFormat) {
    var selectedMethod by remember { mutableStateOf("efectivo") }
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.6f)) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Medio de Pago", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
                    }
                    if (isProcessing) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                        PaymentMethodOptionFinal(title = "Efectivo", subtitle = "Pago directo al técnico", icon = Icons.Default.Payments, isSelected = selectedMethod == "efectivo", onClick = { selectedMethod = "efectivo" })
                        Spacer(modifier = Modifier.height(12.dp))
                        PaymentMethodOptionFinal(title = "QR Nequi / Bancolombia", subtitle = "Transferencia rápida", icon = Icons.Default.QrCodeScanner, isSelected = selectedMethod == "qr", onClick = { selectedMethod = "qr" })
                        Spacer(modifier = Modifier.height(12.dp))
                        PaymentMethodOptionFinal(title = "Wompi (PSE / Tarjetas)", subtitle = "Pago en línea seguro", icon = Icons.Default.CreditCard, isSelected = selectedMethod == "wompi", onClick = { selectedMethod = "wompi" })
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))) {
                            Text("Confirmar y continuar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodOptionFinal(title: String, subtitle: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White), border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (isSelected) Color(0xFF2563EB) else Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text(subtitle, fontSize = 12.sp, color = Color.Gray) }
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}
