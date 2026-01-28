package com.example.soportapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    amount: Int = 35000,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("qr") }
    var isProcessing by remember { mutableStateOf(false) }
    
    val localeCO = Locale.forLanguageTag("es-CO")
    val currencyFormatter = NumberFormat.getCurrencyInstance(localeCO).apply {
        maximumFractionDigits = 0
    }

    if (isProcessing) {
        LaunchedEffect(Unit) {
            delay(2500)
            onPaymentSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pago seguro", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 6 de 10", fontSize = 13.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isProcessing) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        if (isProcessing) {
            ProcessingView()
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Resumen de solicitud",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Este pago garantiza el diagnóstico profesional de tu equipo.",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diagnóstico Técnico Base", color = Color.Gray, fontSize = 14.sp)
                            Text(currencyFormatter.format(amount), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF3F4F6))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total a pagar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(currencyFormatter.format(amount), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF2563EB))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Selecciona tu opción de pago", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                PaymentMethodItem(
                    title = "QR Bancolombia / Nequi",
                    subtitle = "Pago rápido desde tu celular",
                    icon = Icons.Default.QrCodeScanner,
                    isSelected = selectedMethod == "qr",
                    onClick = { selectedMethod = "qr" }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PaymentMethodItem(
                    title = "PSE / Tarjetas (Wompi)",
                    subtitle = "Pago seguro con respaldo bancario",
                    icon = Icons.Default.VerifiedUser,
                    isSelected = selectedMethod == "wompi",
                    onClick = { selectedMethod = "wompi" }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFDCFCE7))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tu dinero está protegido. En caso de no poder iniciar el servicio, se realizará la devolución total.",
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { isProcessing = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("Realizar pago seguro", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProcessingView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = Color(0xFF2563EB),
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Conectando con el banco...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Text(
            text = "Estamos verificando tu transacción de forma segura.",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun PaymentMethodItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White),
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE5E7EB))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF1F5F9), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (isSelected) Color(0xFF2563EB) else Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    SoportAppTheme {
        PaymentScreen(onPaymentSuccess = {}, onBack = {})
    }
}
