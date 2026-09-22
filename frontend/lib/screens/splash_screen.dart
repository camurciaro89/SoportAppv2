import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/app_state.dart';
import '../theme/app_colors.dart';
import '../widgets/ui_kit.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _boot();
  }

  Future<void> _boot() async {
    final appState = Provider.of<AppState>(context, listen: false);
    await appState.loadSession();
    await Future.delayed(const Duration(milliseconds: 1600));
    if (!mounted) return;

    if (!appState.isLoggedIn) {
      Navigator.pushReplacementNamed(context, '/login');
      return;
    }

    switch (appState.userType?.toUpperCase()) {
      case 'ADMIN':
        Navigator.pushReplacementNamed(context, '/admin');
        break;
      case 'TECNICO':
        Navigator.pushReplacementNamed(context, '/technician');
        break;
      default:
        Navigator.pushReplacementNamed(context, '/home');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        width: double.infinity,
        decoration: const BoxDecoration(gradient: AppColors.gradient),
        child: const Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            BrandMark(size: 96, inverted: true),
            SizedBox(height: 24),
            Text(
              'TuTranquilo',
              style: TextStyle(
                color: Colors.white,
                fontSize: 34,
                fontWeight: FontWeight.w800,
              ),
            ),
            SizedBox(height: 8),
            Text(
              'Tecnología sin preocupaciones',
              style: TextStyle(color: Colors.white70, fontSize: 16),
            ),
            SizedBox(height: 36),
            SizedBox(
              width: 28,
              height: 28,
              child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2.4),
            ),
          ],
        ),
      ),
    );
  }
}
