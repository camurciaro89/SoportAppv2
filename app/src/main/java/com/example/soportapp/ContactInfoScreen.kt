package com.example.soportapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soportapp.ui.theme.SoportAppTheme

// Definición de la clase de datos necesaria para la pantalla
data class ContactInfo(
    val name: String,
    val phone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoScreen(
    onContinue: (ContactInfo) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Validación: Nombre (mínimo 3 letras) y Celular (exactamente 10 dígitos)
    val isValid = name.trim().length >= 3 && phone.length == 10

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Datos de contacto", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paso 7 de 10", fontSize = 13.sp, color = Color.Gray)
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "¿A quién debemos llamar?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                text = "Escribe tu nombre y teléfono para que el técnico pueda contactarte pronto.",
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo de Nombre
            Text(
                text = "Tu nombre completo", 
                fontSize = 15.sp, 
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Ej: María Pérez", fontSize = 16.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(24.dp)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de Celular
            Text(
                text = "Número de celular", 
                fontSize = 15.sp, 
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { input -> 
                    if (input.length <= 10) phone = input.filter { it.isDigit() } 
                },
                placeholder = { Text("300 123 4567", fontSize = 16.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(24.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                supportingText = {
                    if (phone.isNotEmpty() && phone.length < 10) {
                        Text("Ingresa los 10 dígitos", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón de Continuar
            Button(
                onClick = { onContinue(ContactInfo(name, phone)) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F172A),
                    disabledContainerColor = Color(0xFFE5E7EB)
                )
            ) {
                Text(
                    text = "Confirmar y continuar", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactInfoScreenPreview() {
    SoportAppTheme {
        ContactInfoScreen(onContinue = {}, onBack = {})
    }
}
