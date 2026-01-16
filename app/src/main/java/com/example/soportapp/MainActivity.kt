package com.example.soportapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Monitor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.soportapp.ui.theme.SoportAppTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoportAppTheme {
                SoportApp()
            }
        }
    }
}

@Composable
fun SoportApp() {
    val navController = rememberNavController()
    // Obtenemos el técnico principal desde el repositorio
    val technician = TechnicianRepository.getMainTechnician()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(onStart = { navController.navigate("userTypeSelection") })
        }
        composable("userTypeSelection") {
            UserTypeSelectionScreen(
                onSelect = { userType -> navController.navigate("serviceSelection/$userType") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "serviceSelection/{userType}",
            arguments = listOf(navArgument("userType") { type = NavType.StringType })
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: ""
            ServiceSelectionScreen(
                userType = userType,
                onSelect = { serviceId -> navController.navigate("problemDescription/$userType/$serviceId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "problemDescription/{userType}/{serviceId}",
            arguments = listOf(
                navArgument("userType") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: ""
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            
            ProblemDescriptionScreen(
                onContinue = { details ->
                    val desc = URLEncoder.encode(details.description, StandardCharsets.UTF_8.toString())
                    val loc = URLEncoder.encode(details.location, StandardCharsets.UTF_8.toString())
                    navController.navigate("serviceModality/$userType/$serviceId/$desc/$loc")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "serviceModality/{userType}/{serviceId}/{desc}/{loc}",
            arguments = listOf(
                navArgument("userType") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType },
                navArgument("desc") { type = NavType.StringType },
                navArgument("loc") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: ""
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            val desc = backStackEntry.arguments?.getString("desc") ?: ""
            val loc = backStackEntry.arguments?.getString("loc") ?: ""
            
            ServiceModalityScreen(
                onContinue = { 
                    navController.navigate("serviceSummary/$userType/$serviceId/$desc/$loc") 
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "serviceSummary/{userType}/{serviceId}/{desc}/{loc}",
            arguments = listOf(
                navArgument("userType") { type = NavType.StringType },
                navArgument("serviceId") { type = NavType.StringType },
                navArgument("desc") { type = NavType.StringType },
                navArgument("loc") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: ""
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            val descEncoded = backStackEntry.arguments?.getString("desc") ?: ""
            val locEncoded = backStackEntry.arguments?.getString("loc") ?: ""
            
            val desc = URLDecoder.decode(descEncoded, StandardCharsets.UTF_8.toString())
            val loc = URLDecoder.decode(locEncoded, StandardCharsets.UTF_8.toString())
            
            val serviceName = when(serviceId) {
                "soporte-computadores" -> "Soporte técnico de computadores"
                "diagnostico-tecnico-empresarial" -> "Diagnóstico técnico empresarial"
                "mantenimiento-preventivo-empresarial" -> "Mantenimiento preventivo empresarial"
                "soporte-m365" -> "Soporte Microsoft 365"
                "seguridad-informatica" -> "Seguridad informática"
                else -> serviceId.replace("-", " ").replaceFirstChar { it.uppercase() }
            }

            ServiceSummaryScreen(
                serviceName = serviceName,
                userType = if (userType == "empresa") "Empresa" else "Hogar",
                description = desc,
                location = loc,
                onConfirm = { navController.navigate("payment") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("payment") {
            PaymentScreen(
                onPaymentSuccess = { navController.navigate("contactInfo") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("contactInfo") {
            ContactInfoScreen(
                onContinue = { _ -> navController.navigate("technicianAssignment") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("technicianAssignment") {
            TechnicianAssignmentScreen(
                technician = technician,
                onContinue = { navController.navigate("serviceStatus") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("serviceStatus") {
            ServiceStatusScreen(
                technician = technician,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate("rating") }
            )
        }
        composable("rating") {
            RatingScreen(
                technicianName = technician.name,
                onFinish = { _, _ -> 
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF3B82F6), // blue-500
            Color(0xFF14B8A6), // teal-500
            Color(0xFF2563EB)  // blue-600
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp))
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.TwoTone.Monitor,
                        contentDescription = "Monitor Icon",
                        tint = Color(0xFF2563EB), // blue-600
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "TuTranquilo",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tecnología sin preocupaciones",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF2563EB) // blue-600
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Solicitar soporte técnico", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Soporte profesional para tu tranquilidad",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    SoportAppTheme {
        WelcomeScreen(onStart = {})
    }
}
