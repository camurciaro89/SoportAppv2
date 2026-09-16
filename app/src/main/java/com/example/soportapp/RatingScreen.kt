package com.example.soportapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soportapp.ui.theme.SoportAppTheme
import com.example.soportapp.ui.viewmodel.RatingUiState
import com.example.soportapp.ui.viewmodel.RatingViewModel
import com.example.soportapp.ui.viewmodel.RatingViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    supportRequestId: Long,
    technicianName: String = "Camilo Andrés Murcia",
    onFinish: () -> Unit
) {
    val application = LocalContext.current.applicationContext as SoportApplication
    val viewModel: RatingViewModel = viewModel(
        factory = RatingViewModelFactory(application.container.soportAppRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    var rating by remember { mutableStateOf(0) }
    var techRating by remember { mutableStateOf(0) }
    var serviceRating by remember { mutableStateOf(0) }
    var supportRating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is RatingUiState.Success) {
            onFinish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Calificar servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 6 de 6", fontSize = 13.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(24.dp)) }
                
                item {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("C", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }

                item {
                    Text(
                        text = "¡Servicio finalizado!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }

                item {
                    Text(
                        text = "¿Cómo calificarías el trabajo de $technicianName?",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    RatingItemRow("Calificación general", rating) { rating = it }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Detalla tu experiencia", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
                }

                item { RatingItemRow("Técnico", techRating) { techRating = it } }
                item { RatingItemRow("Servicio", serviceRating) { serviceRating = it } }
                item { RatingItemRow("Atención", supportRating) { supportRating = it } }

                item {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text("Cuéntanos más sobre tu experiencia (opcional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState !is RatingUiState.Loading
                    )
                }

                item {
                    Button(
                        onClick = { 
                            viewModel.saveRating(
                                supportRequestId, 
                                rating, 
                                techRating, 
                                serviceRating, 
                                supportRating, 
                                comment
                            ) 
                        },
                        enabled = rating > 0 && uiState !is RatingUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        if (uiState is RatingUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Enviar calificación", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    TextButton(
                        onClick = { onFinish() },
                        enabled = uiState !is RatingUiState.Loading
                    ) {
                        Text("Omitir", color = Color.Gray)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            
            if (uiState is RatingUiState.Error) {
                Text(
                    text = (uiState as RatingUiState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun RatingItemRow(label: String, currentRating: Int, onRatingChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp)
        Row {
            repeat(5) { index ->
                val starIndex = index + 1
                Icon(
                    imageVector = if (starIndex <= currentRating) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = null,
                    tint = if (starIndex <= currentRating) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onRatingChange(starIndex) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RatingScreenPreview() {
    SoportAppTheme {
        RatingScreen(supportRequestId = 1L, onFinish = { })
    }
}
