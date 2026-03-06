package com.example.soportapp

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soportapp.ui.theme.SoportAppTheme
import com.example.soportapp.ui.viewmodel.TechnicianAssignmentUiState
import com.example.soportapp.ui.viewmodel.TechnicianAssignmentViewModel
import com.example.soportapp.ui.viewmodel.TechnicianAssignmentViewModelFactory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianAssignmentScreen(
    supportRequestId: Long,
    technician: Technician,
    onContinue: (Long) -> Unit,
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as SoportApplication
    val viewModel: TechnicianAssignmentViewModel = viewModel(
        factory = TechnicianAssignmentViewModelFactory(application.container.soportAppRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    var isSearching by remember { mutableStateOf(true) }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            delay(3000) // Simulate search
            isSearching = false
        } else {
            // Se corrige la conversión para evitar el cierre de la app
            val techIdInt = technician.id.toIntOrNull() ?: 1 
            viewModel.assignTechnician(supportRequestId, techIdInt)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (isSearching) "Buscando técnico" else "Técnico asignado", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 8 de 10", fontSize = 13.sp, color = Color.Gray)
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
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isSearching) {
                SearchingContent()
            } else {
                TechnicianFoundContent(technician, onContinue = { onContinue(supportRequestId) }, uiState)
            }
        }
    }
}

@Composable
fun SearchingContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(120.dp).scale(scale).background(Color(0xFFDBEAFE), CircleShape))
            Box(modifier = Modifier.size(80.dp).background(Color(0xFF2563EB), CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Buscando al mejor experto...", 
            fontSize = 22.sp, 
            fontWeight = FontWeight.Bold, 
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Estamos asignando un técnico verificado para tu requerimiento.", 
            fontSize = 16.sp, 
            color = Color.Gray, 
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TechnicianFoundContent(technician: Technician, onContinue: () -> Unit, uiState: TechnicianAssignmentUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("¡Técnico asignado!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        Text("Hemos encontrado al experto ideal.", fontSize = 14.sp, color = Color(0xFF166534).copy(alpha = 0.8f))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp).background(Color(0xFFF1F5F9), CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = technician.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        color = Color(0xFF22C55E), 
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "VERIFICADO", 
                                color = Color.White, 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = technician.title, fontSize = 14.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp)) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${technician.averageRating} (${technician.totalServices}+ servicios)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Button(
                onClick = onContinue,
                enabled = uiState is TechnicianAssignmentUiState.Success,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                if (uiState is TechnicianAssignmentUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Continuar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TechnicianAssignmentScreenPreview() {
    SoportAppTheme {
        TechnicianAssignmentScreen(
            supportRequestId = 1L,
            technician = TechnicianRepository.getMainTechnician(),
            onContinue = {},
            onBack = {}
        )
    }
}
