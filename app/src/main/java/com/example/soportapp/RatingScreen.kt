package com.example.soportapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is RatingUiState.Success) {
            onFinish()
        }
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFF1F5F9), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("C", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¡Servicio finalizado!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "¿Cómo calificarías el trabajo de $technicianName?",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        Icon(
                            imageVector = if (starIndex <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = if (starIndex <= rating) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { rating = starIndex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Cuéntanos más sobre tu experiencia (opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState !is RatingUiState.Loading
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.saveRating(supportRequestId, rating, comment) },
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

                TextButton(
                    onClick = { onFinish() },
                    modifier = Modifier.padding(top = 8.dp),
                    enabled = uiState !is RatingUiState.Loading
                ) {
                    Text("Omitir", color = Color.Gray)
                }
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

@Preview(showBackground = true)
@Composable
fun RatingScreenPreview() {
    SoportAppTheme {
        RatingScreen(supportRequestId = 1L, onFinish = { })
    }
}
