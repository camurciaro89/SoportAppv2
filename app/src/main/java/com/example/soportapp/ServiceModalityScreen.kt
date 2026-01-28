package com.example.soportapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceModalityScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            SoportAppComponents.StepTopAppBar(
                title = "Modalidades de soporte",
                currentStep = 4,
                totalSteps = 10,
                onBack = onBack
            )
        },
        containerColor = Color(0xFFF9FAFB) // Fondo coherente con toda la App
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Cómo resolvemos tu falla",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Tras el pago base, un experto evaluará tu caso y definirá la ruta más eficiente:",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // MODALIDAD 1: REMOTO (MORADO - TECNOLOGÍA)
            item {
                ModalityInfoCard(
                    icon = Icons.Default.Devices,
                    title = "Soporte Remoto",
                    description = "Conexión segura vía internet para fallas de software, virus o configuración. Es la opción más rápida.",
                    accentColor = Color(0xFF7C3AED), // Violeta
                    bgTint = Color(0xFFF5F3FF)
                )
            }

            // MODALIDAD 2: EN SITIO (VERDE - CONFIANZA)
            item {
                ModalityInfoCard(
                    icon = Icons.Default.LocationOn,
                    title = "Servicio en Sitio",
                    description = "El técnico se desplaza a tu ubicación para reparaciones físicas o cuando el internet no funciona.",
                    accentColor = Color(0xFF16A34A), // Esmeralda
                    bgTint = Color(0xFFF0FDF4)
                )
            }

            // MODALIDAD 3: LABORATORIO (AZUL - RESPALDO)
            item {
                ModalityInfoCard(
                    icon = Icons.Default.Apartment,
                    title = "Centro de Diagnóstico",
                    description = "Para casos de alta complejidad que requieren herramientas de laboratorio y micro-soldadura.",
                    accentColor = Color(0xFF2563EB), // Cobalto
                    bgTint = Color(0xFFEFF6FF)
                )
            }

            // NOTA DE TRANSPARENCIA
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Nota: El desplazamiento o la recogida tienen un costo adicional de $30.000 que se abona en el Paso 9.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("Entendido, continuar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ModalityInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    bgTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(bgTint, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceModalityScreenPreview() {
    SoportAppTheme {
        ServiceModalityScreen(onContinue = {}, onBack = {})
    }
}
