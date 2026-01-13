package com.example.soportapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
fun UserTypeSelectionScreen(onSelect: (String) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column { 
                        Text("Tipo de servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 1 de 10", fontSize = 13.sp, color = Color.Gray) 
                    } 
                },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", modifier = Modifier.size(28.dp)) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "¿A quién va dirigido el servicio?", 
                fontSize = 24.sp, 
                textAlign = TextAlign.Center, 
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Toca la opción que mejor te describa para empezar.", 
                fontSize = 16.sp, 
                color = Color.DarkGray, 
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(), 
                verticalArrangement = Arrangement.spacedBy(20.dp), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserTypeCard(
                    icon = Icons.Default.Business, 
                    title = "Empresa", 
                    description = "Soporte técnico para oficinas y equipos corporativos", 
                    iconBgColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)), 
                    onClick = { onSelect("empresa") }
                )
                UserTypeCard(
                    icon = Icons.Default.Home, 
                    title = "Hogar", 
                    description = "Reparación de computadores personales y portátiles", 
                    iconBgColors = listOf(Color(0xFF14B8A6), Color(0xFF0D9488)), 
                    onClick = { onSelect("hogar") }
                )
            }
        }
    }
}

@Composable
fun UserTypeCard(icon: ImageVector, title: String, description: String, iconBgColors: List<Color>, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.padding(24.dp), 
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(brush = Brush.verticalGradient(iconBgColors), shape = RoundedCornerShape(16.dp)), 
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = description, fontSize = 15.sp, color = Color.Gray, lineHeight = 20.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserTypeSelectionScreenPreview() {
    SoportAppTheme {
        UserTypeSelectionScreen(onSelect = {}, onBack = {})
    }
}
