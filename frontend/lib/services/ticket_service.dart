import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class TicketService {
  final String baseUrl = "http://10.0.2.2"; // IP para emulador Android hacia localhost

  Future<Map<String, String>> _getHeaders() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('token');
    return {
      "Content-Type": "application/json",
      if (token != null) "Authorization": "Bearer $token",
    };
  }

  Future<List<dynamic>> getEquipments() async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(Uri.parse("$baseUrl/equipments/"), headers: headers);
      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      }
      return [];
    } catch (e) {
      print("Error Equipments: $e");
      return [];
    }
  }

  Future<List<String>> analyzeProblem(String description) async {
    try {
      final headers = await _getHeaders();
      final response = await http.post(
        Uri.parse("$baseUrl/tickets/analyze"),
        headers: headers,
        body: jsonEncode({"problem_description": description}),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return List<String>.from(data['questions']);
      }
      return [];
    } catch (e) {
      print("Error Analyze: $e");
      return [];
    }
  }

  Future<Map<String, dynamic>?> confirmTicket(Map<String, dynamic> data) async {
    try {
      final headers = await _getHeaders();
      final response = await http.post(
        Uri.parse("$baseUrl/tickets/confirm"),
        headers: headers,
        body: jsonEncode(data),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body);
      }
      return null;
    } catch (e) {
      print("Error Confirm Ticket: $e");
      return null;
    }
  }

  Future<List<dynamic>> getTickets() async {
    try {
      final headers = await _getHeaders();
      final response = await http.get(Uri.parse("$baseUrl/tickets/"), headers: headers);
      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      }
      return [];
    } catch (e) {
      print("Error Get Tickets: $e");
      return [];
    }
  }
}
