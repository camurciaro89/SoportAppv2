import 'package:flutter/material.dart';
import 'screens/login_screen.dart';
import 'screens/register_screen.dart';
import 'screens/home_screen.dart';
import 'screens/report_problem_screen.dart';
import 'screens/technician_dashboard.dart';
import 'package:google_fonts/google_fonts.dart';

void main() {
  runApp(const SoportApp());
}

class SoportApp extends StatelessWidget {
  const SoportApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SoportApp',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
        textTheme: GoogleFonts.interTextTheme(),
      ),
      initialRoute: '/login',
      routes: {
        '/login': (context) => LoginScreen(),
        '/register': (context) => RegisterScreen(),
        '/welcome': (context) => HomeScreen(),
        '/report': (context) => ReportProblemScreen(),
        '/technician': (context) => TechnicianDashboard(),
      },
      debugShowCheckedModeBanner: false,
    );
  }
}
