import 'package:flutter/material.dart';
import '../services/ticket_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ReportProblemScreen extends StatefulWidget {
  @override
  _ReportProblemScreenState createState() => _ReportProblemScreenState();
}

class _ReportProblemScreenState extends State<ReportProblemScreen> {
  final _descriptionController = TextEditingController();
  final _ticketService = TicketService();
  bool _isSending = false;
  String? _diagnosis;

  void _submitReport() async {
    if (_descriptionController.text.isEmpty) return;

    setState(() {
      _isSending = true;
      _diagnosis = null;
    });

    final ticketData = {
      "service_catalog_id": "general",
      "problem_description": _descriptionController.text,
      "modalidad": "Remoto"
    };

    final result = await _ticketService.createTicket(ticketData, 1); // userId simulado

    setState(() {
      _isSending = false;
      if (result != null) {
        _diagnosis = result['ai_diagnosis'];
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Reportar Falla")),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          children: [
            TextField(
              controller: _descriptionController,
              maxLines: 5,
              decoration: InputDecoration(
                labelText: "Describe el problema",
                hintText: "Ej: Mi laptop se calienta mucho...",
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              ),
            ),
            SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton(
                onPressed: _isSending ? null : _submitReport,
                child: _isSending
                  ? CircularProgressIndicator(color: Colors.white)
                  : Text("Enviar y Obtener Diagnóstico IA"),
              ),
            ),
            if (_diagnosis != null) ...[
              SizedBox(height: 32),
              Container(
                padding: EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.blue[50],
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.blue[200]!),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.auto_awesome, color: Colors.blue[800]),
                        SizedBox(width: 8),
                        Text("Diagnóstico Preliminar IA", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.blue[900])),
                      ],
                    ),
                    SizedBox(height: 12),
                    Text(_diagnosis!),
                  ],
                ),
              )
            ]
          ],
        ),
      ),
    );
  }
}
