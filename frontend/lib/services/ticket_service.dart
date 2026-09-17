import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class TicketService {
  final String baseUrl = "http://localhost:8000"; // Usar 10.0.2.2 para emulador Android

  Future<List<dynamic>> getEquipments(int userId) async {
    try {
      final response = await http.get(Uri.parse("$baseUrl/equipments/?user_id=$userId"));
      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      }
      return [];
    } catch (e) {
      print("Error Equipments: $e");
      return [];
    }
  }

  Future<Map<String, dynamic>?> createTicket(Map<String, dynamic> ticketData, int userId) async {
    try {
      final response = await http.post(
        Uri.parse("$baseUrl/tickets/?user_id=$userId"),
        headers: {"Content-Type": "application/json"},
        body: jsonEncode(ticketData),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body);
      }
      return null;
    } catch (e) {
      print("Error Create Ticket: $e");
      return null;
    }
  }

  Future<List<dynamic>> getTickets(int userId, String role) async {
    try {
      final response = await http.get(Uri.parse("$baseUrl/tickets/?user_id=$userId&role=$role"));
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
