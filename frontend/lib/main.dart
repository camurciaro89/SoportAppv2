import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'screens/login_screen.dart';
import 'screens/register_screen.dart';
import 'screens/home_screen.dart';
import 'screens/report_problem_screen.dart';
import 'screens/technician_dashboard.dart';
import 'screens/splash_screen.dart';
import 'screens/admin_dashboard.dart';
import 'services/app_state.dart';
import 'theme/app_theme.dart';

void main() {
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AppState()),
      ],
      child: const SoportApp(),
    ),
  );
}

class SoportApp extends StatelessWidget {
  const SoportApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'TuTranquilo',
      theme: AppTheme.light(),
      initialRoute: '/',
      routes: {
        '/': (context) => const SplashScreen(),
        '/login': (context) => const LoginScreen(),
        '/register': (context) => const RegisterScreen(),
        '/home': (context) => const HomeScreen(),
        '/welcome': (context) => const HomeScreen(),
        '/report': (context) => ReportProblemScreen(),
        '/technician': (context) => const TechnicianDashboard(),
        '/admin': (context) => const AdminDashboard(),
      },
      debugShowCheckedModeBanner: false,
    );
  }
}
