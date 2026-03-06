package com.example.soportapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soportapp.ui.theme.SoportAppTheme
import com.example.soportapp.ui.viewmodel.ServiceModalityUiState
import com.example.soportapp.ui.viewmodel.ServiceModalityViewModel
import com.example.soportapp.ui.viewmodel.ServiceModalityViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceModalityScreen(
    supportRequestId: Long,
    onContinue: (Long) -> Unit,
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as SoportApplication
    val viewModel: ServiceModalityViewModel = viewModel(
        factory = ServiceModalityViewModelFactory(application.container.soportAppRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ServiceModalityUiState.Success -> onContinue(state.supportRequestId)
            is ServiceModalityUiState.Error -> {
                // Show error
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Modalidades de soporte", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 4 de 10", fontSize = 13.sp, color = Color.Gray)
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

                item {
                    ModalityInfoCard(
                        icon = Icons.Default.Devices,
                        title = "Soporte Remoto",
                        description = "Conexión segura vía internet para fallas de software, virus o configuración. Es la opción más rápida.",
                        accentColor = Color(0xFF7C3AED),
                        bgTint = Color(0xFFF5F3FF)
                    )
                }

                item {
                    ModalityInfoCard(
                        icon = Icons.Default.LocationOn,
                        title = "Servicio en Sitio",
                        description = "El técnico se desplaza a tu ubicación para reparaciones físicas o cuando el internet no funciona.",
                        accentColor = Color(0xFF16A34A),
                        bgTint = Color(0xFFF0FDF4)
                    )
                }

                item {
                    ModalityInfoCard(
                        icon = Icons.Default.Apartment,
                        title = "Centro de Diagnóstico",
                        description = "Para casos de alta complejidad que requieren herramientas de laboratorio y micro-soldadura.",
                        accentColor = Color(0xFF2563EB),
                        bgTint = Color(0xFFEFF6FF)
                    )
                }

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
                        onClick = { viewModel.updateModality(supportRequestId, "Remoto") },
                        enabled = uiState != ServiceModalityUiState.Loading,
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
            if (uiState == ServiceModalityUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
        ServiceModalityScreen(supportRequestId = 1L, onContinue = {}, onBack = {})
    }
}
