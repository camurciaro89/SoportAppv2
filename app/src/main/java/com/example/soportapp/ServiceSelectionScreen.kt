package com.example.soportapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soportapp.ui.theme.SoportAppTheme

// CLASE DE DATOS PARA EL SERVICIO
data class Service(
    val id: String,
    val name: String,
    val description: String,
    val modality: String,
    val icon: ImageVector,
    val color: Color,
    val bg: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSelectionScreen(
    userType: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit
) {
    val services = if (userType == "empresa") enterpriseServices else homeServices

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Seleccionar servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 2 de 10", fontSize = 13.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Regresar",
                            modifier = Modifier.size(28.dp)
                        )
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
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "¿Qué servicio necesitas hoy?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Toca el servicio para continuar.",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(services) { service ->
                ServiceCard(service = service, onClick = { onSelect(service.id) })
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ServiceCard(service: Service, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(service.bg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = service.name,
                    tint = service.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.description, 
                    fontSize = 15.sp, 
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "📍 ${service.modality}",
                        fontSize = 13.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

val enterpriseServices = listOf(
    Service("soporte-computadores", "Soporte técnico", "Solución de fallas en equipos empresariales de software y hardware.", "Remoto o en sitio", Icons.Default.Build, Color(0xFF2563EB), Color(0xFFDBEAFE)),
    Service("mantenimiento-preventivo", "Mantenimiento empresarial", "Revisión programada para evitar fallas en equipos corporativos.", "En sitio o Centro", Icons.Default.Settings, Color(0xFF16A34A), Color(0xFFDCFCE7)),
    Service("diagnostico-tecnico", "Diagnóstico empresarial", "Evaluación profesional con informe y recomendaciones técnicas.", "Remoto o en sitio", Icons.Default.Search, Color(0xFFEA580C), Color(0xFFFFF7ED)),
    Service("soporte-m365", "Soporte Microsoft 365", "Configuración y administración de correo y usuarios corporativos.", "Remoto o en sitio", Icons.Default.Cloud, Color(0xFF2563EB), Color(0xFFDBEAFE)),
    Service("seguridad", "Seguridad informática", "Instalación de antivirus y protección de datos empresariales.", "Remoto o en sitio", Icons.Default.Security, Color(0xFFDC2626), Color(0xFFFEE2E2)),
)

val homeServices = listOf(
    Service("mantenimiento-preventivo-hogar", "Mantenimiento de computador", "Revisión y limpieza para mejorar el rendimiento de tu PC.", "En sitio o Centro", Icons.Default.Settings, Color(0xFF16A34A), Color(0xFFDCFCE7)),
    Service("mantenimiento-correctivo-hogar", "Reparación de computador", "Identificar y corregir fallas de funcionamiento en tu equipo.", "En sitio o Centro", Icons.Default.Build, Color(0xFF2563EB), Color(0xFFDBEAFE)),
    Service("diagnostico-tecnico-hogar", "Diagnóstico técnico", "Evaluación para identificar la causa de fallas o bajo rendimiento.", "En sitio o Centro", Icons.Default.Search, Color(0xFFEA580C), Color(0xFFFFF7ED)),
)

@Preview(showBackground = true)
@Composable
fun ServiceSelectionScreenPreview() {
    SoportAppTheme {
        ServiceSelectionScreen("empresa", onSelect = {}, onBack = {})
    }
}
