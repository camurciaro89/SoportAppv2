import 'package:flutter/material.dart';
import '../services/ticket_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _ticketService = TicketService();
  List<dynamic> _equipments = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  void _loadData() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = 1; // Simulado hasta tener JWT real decodificado
    final data = await _ticketService.getEquipments(userId);
    setState(() {
      _equipments = data;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("SoportApp - Inicio", style: TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          IconButton(
            icon: Icon(Icons.logout),
            onPressed: () => Navigator.pushReplacementNamed(context, '/login'),
          )
        ],
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: () async => _loadData(),
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text("Mis Equipos", style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                    SizedBox(height: 16),
                    if (_equipments.isEmpty)
                      Center(child: Text("No tienes equipos registrados", style: TextStyle(color: Colors.grey)))
                    else
                      ..._equipments.map((e) => Card(
                        child: ListTile(
                          leading: Icon(Icons.laptop, color: Colors.blue),
                          title: Text("${e['marca']} ${e['modelo']}"),
                          subtitle: Text("Serial: ${e['serial_number']}"),
                        ),
                      )).toList(),
                    SizedBox(height: 32),
                    SizedBox(
                      width: double.infinity,
                      height: 56,
                      child: ElevatedButton.icon(
                        onPressed: () => Navigator.pushNamed(context, '/report'),
                        icon: Icon(Icons.report_problem),
                        label: Text("Reportar un Problema", style: TextStyle(fontWeight: FontWeight.bold)),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.blue[900],
                          foregroundColor: Colors.white,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        ),
                      ),
                    )
                  ],
                ),
              ),
            ),
    );
  }
}
