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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soportapp.ui.theme.SoportAppTheme

@Composable
fun RatingScreen(
    technicianName: String = "Camilo Andrés Murcia",
    onFinish: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono de Éxito o Foto del Técnico
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFF1F5F9), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Simulamos foto de Camilo
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

            // Estrellas de calificación
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

            // Campo de comentario
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Cuéntanos más sobre tu experiencia (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { onFinish(rating, comment) },
                enabled = rating > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Text("Enviar calificación", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = { onFinish(0, "") },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Omitir", color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RatingScreenPreview() {
    SoportAppTheme {
        RatingScreen(onFinish = { _, _ -> })
    }
}
