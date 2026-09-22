import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../config/api_config.dart';

class AuthService {
  final String baseUrl = ApiConfig.baseUrl;

  Future<Map<String, dynamic>?> login(String email, String password) async {
    try {
      print("Intentando login en: $baseUrl/auth/login-json");
      final response = await http.post(
        Uri.parse("$baseUrl/auth/login-json"),
        headers: {"Content-Type": "application/json"},
        body: jsonEncode({"email": email, "contrasena": password}),
      );

      print("Respuesta Login: Status ${response.statusCode}");
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final prefs = await SharedPreferences.getInstance();
        await prefs.setString('token', data['access_token']);
        await prefs.setString('user_type', data['user_type']);
        return data;
      } else {
        print("Detalle error login: ${response.body}");
      }
      return null;
    } catch (e) {
      print("Error de conexión en Login: $e");
      return null;
    }
  }

  Future<String?> register(String nombre, String email, String telefono, String contrasena, String userType) async {
    try {
      print("Intentando registrar en: $baseUrl/auth/register");
      final response = await http.post(
        Uri.parse("$baseUrl/auth/register"),
        headers: {"Content-Type": "application/json"},
        body: jsonEncode({
          "nombre": nombre,
          "email": email,
          "telefono": telefono,
          "contrasena": contrasena,
          "user_type": userType
        }),
      );

      print("Respuesta Registro: Status ${response.statusCode}");
      if (response.statusCode == 200 || response.statusCode == 201) {
        return null;
      }

      print("Detalle error: ${response.body}");
      return _parseErrorDetail(
        response.body,
        fallback: response.statusCode == 400
            ? 'El correo ya está registrado. Prueba iniciar sesión.'
            : 'No se pudo registrar. Inténtalo de nuevo.',
      );
    } catch (e) {
      print("Error de conexión en Registro: $e");
      return 'No hay conexión con el servidor. Verifica que el API esté en marcha.';
    }
  }

  String _parseErrorDetail(String body, {required String fallback}) {
    try {
      final decoded = jsonDecode(body);
      final detail = decoded['detail'];
      if (detail is String && detail.trim().isNotEmpty) {
        return detail;
      }
    } catch (_) {}
    return fallback;
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  }

  Future<String?> getUserType() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('user_type');
  }
}
