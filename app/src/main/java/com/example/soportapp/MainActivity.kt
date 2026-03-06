package com.example.soportapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Monitor
import androidx.compose.material3.*
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
    val technician = TechnicianRepository.getMainTechnician()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onStart = { navController.navigate("userTypeSelection") },
                onHistory = { navController.navigate("serviceHistory") }
            )
        }
        composable("serviceHistory") {
            ServiceHistoryScreen(
                onBack = { navController.popBackStack() },
                onSelectRequest = { id -> navController.navigate("serviceStatus/$id") }
            )
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
                userType = userType,
                serviceId = serviceId,
                onContinue = { supportRequestId -> navController.navigate("serviceModality/$supportRequestId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "serviceModality/{supportRequestId}",
            arguments = listOf(navArgument("supportRequestId") { type = NavType.LongType })
        ) { backStackEntry ->
            val supportRequestId = backStackEntry.arguments?.getLong("supportRequestId") ?: -1
            ServiceModalityScreen(
                supportRequestId = supportRequestId,
                onContinue = { newSupportRequestId -> navController.navigate("serviceSummary/$newSupportRequestId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "serviceSummary/{supportRequestId}",
            arguments = listOf(navArgument("supportRequestId") { type = NavType.LongType })
        ) { backStackEntry ->
             val supportRequestId = backStackEntry.arguments?.getLong("supportRequestId") ?: -1
            ServiceSummaryScreen(
                supportRequestId = supportRequestId,
                onConfirm = { newSupportRequestId -> navController.navigate("payment/$newSupportRequestId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "payment/{supportRequestId}",
            arguments = listOf(navArgument("supportRequestId") { type = NavType.LongType })
        ) { backStackEntry ->
            val supportRequestId = backStackEntry.arguments?.getLong("supportRequestId") ?: -1
            PaymentScreen(
                supportRequestId = supportRequestId,
                onPaymentSuccess = { newSupportRequestId -> navController.navigate("contactInfo/$newSupportRequestId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "contactInfo/{supportRequestId}",
            arguments = listOf(navArgument("supportRequestId") { type = NavType.LongType })
        ) { backStackEntry ->
            val supportRequestId = backStackEntry.arguments?.getLong("supportRequestId") ?: -1
            ContactInfoScreen(
                supportRequestId = supportRequestId,
                onContinue = { newSupportRequestId -> 
                    navController.navigate("technicianAssignment/$newSupportRequestId") 
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "technicianAssignment/{supportRequestId}",
            arguments = listOf(navArgument("supportRequestId") { type = NavType.LongType })
        ) { backStackEntry ->
            val supportRequestId = backStackEntry.arguments?.getLong("supportRequestId") ?: -1
            TechnicianAssignmentScreen(
                supportRequestId = supportRequestId,
                technician = technician,
                onContinue = { newSupportRequestId -> navController.navigate("serviceStatus/$newSupportRequestId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "serviceStatus/{supportRequestId}",
            arguments = listOf(navArgument("supportRequestId") { type = NavType.LongType })
        ) { backStackEntry ->
             val supportRequestId = backStackEntry.arguments?.getLong("supportRequestId") ?: -1
            ServiceStatusScreen(
                supportRequestId = supportRequestId,
                technician = technician,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate("rating/$supportRequestId") }
            )
        }
        composable(
            "rating/{supportRequestId}",
            arguments = listOf(navArgument("supportRequestId") { type = NavType.LongType })
        ) { backStackEntry ->
             val supportRequestId = backStackEntry.arguments?.getLong("supportRequestId") ?: -1
            RatingScreen(
                supportRequestId = supportRequestId,
                technicianName = technician.name,
                onFinish = {
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit, onHistory: () -> Unit) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF3B82F6),
            Color(0xFF14B8A6),
            Color(0xFF2563EB)
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
                        tint = Color(0xFF2563EB),
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
                    contentColor = Color(0xFF2563EB)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Solicitar soporte técnico", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Ver mis servicios", fontSize = 16.sp)
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
        WelcomeScreen(onStart = {}, onHistory = {})
    }
}
