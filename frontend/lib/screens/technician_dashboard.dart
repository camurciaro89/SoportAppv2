import 'package:flutter/material.dart';
import '../services/ticket_service.dart';

class TechnicianDashboard extends StatefulWidget {
  @override
  _TechnicianDashboardState createState() => _TechnicianDashboardState();
}

class _TechnicianDashboardState extends State<TechnicianDashboard> {
  final _ticketService = TicketService();
  List<dynamic> _tickets = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadTickets();
  }

  void _loadTickets() async {
    final data = await _ticketService.getTickets(1, "TECNICO"); // ID simulado
    setState(() {
      _tickets = data;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Panel Técnico")),
      body: _isLoading
        ? Center(child: CircularProgressIndicator())
        : ListView.builder(
            padding: EdgeInsets.all(16),
            itemCount: _tickets.length,
            itemBuilder: (context, index) {
              final ticket = _tickets[index];
              return Card(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text("Ticket #${ticket['id']}", style: TextStyle(fontWeight: FontWeight.bold)),
                          _StatusBadge(ticket['estado']),
                        ],
                      ),
                      SizedBox(height: 8),
                      Text(ticket['problem_description']),
                      if (ticket['ai_diagnosis'] != null) ...[
                        SizedBox(height: 12),
                        Text("Diagnóstico IA:", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                        Text(ticket['ai_diagnosis'], style: TextStyle(fontSize: 12, color: Colors.blue[900])),
                      ]
                    ],
                  ),
                ),
              );
            },
          ),
    );
  }

  Widget _StatusBadge(String status) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.orange[100],
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(status, style: TextStyle(color: Colors.orange[900], fontSize: 12, fontWeight: FontWeight.bold)),
    );
  }
}
