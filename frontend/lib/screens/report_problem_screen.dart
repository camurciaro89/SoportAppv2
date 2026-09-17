import 'package:flutter/material.dart';
import '../services/ticket_service.dart';

class ReportProblemScreen extends StatefulWidget {
  @override
  _ReportProblemScreenState createState() => _ReportProblemScreenState();
}

class _ReportProblemScreenState extends State<ReportProblemScreen> {
  final _descriptionController = TextEditingController();
  final _ticketService = TicketService();

  int _currentStep = 1;
  bool _isLoading = false;

  // Datos de la sesión de diagnóstico
  List<String> _questions = [];
  List<TextEditingController> _answerControllers = [];
  String? _finalDiagnosis;
  String? _ticketNumber;

  void _getQuestions() async {
    if (_descriptionController.text.isEmpty) return;

    setState(() => _isLoading = true);
    final questions = await _ticketService.analyzeProblem(_descriptionController.text);

    setState(() {
      _questions = questions;
      _answerControllers = List.generate(questions.length, (_) => TextEditingController());
      _isLoading = false;
      _currentStep = 2;
    });
  }

  void _getFinalDiagnosis() async {
    setState(() => _isLoading = true);

    final answers = <Map<String, String>>[];
    for (int i = 0; i < _questions.length; i++) {
      answers.add({
        "question": _questions[i],
        "answer": _answerControllers[i].text,
      });
    }

    final data = {
      "problem_description": _descriptionController.text,
      "answers": answers,
      "modalidad": "Sitio",
    };

    final result = await _ticketService.confirmTicket(data);

    setState(() {
      _isLoading = false;
      if (result != null) {
        _finalDiagnosis = result['ai_diagnosis'] ?? "No se pudo generar diagnóstico";
        _ticketNumber = result['ticket_number'];
        _currentStep = 3;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Diagnóstico IA", style: TextStyle(fontWeight: FontWeight.bold)),
        leading: IconButton(
          icon: Icon(Icons.close),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: _isLoading
        ? _buildLoadingState()
        : AnimatedSwitcher(
            duration: Duration(milliseconds: 300),
            child: _buildCurrentStep(),
          ),
    );
  }

  Widget _buildLoadingState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          CircularProgressIndicator(strokeWidth: 3),
          SizedBox(height: 24),
          Text(
            _currentStep == 1 ? "Analizando tu problema..." : "Generando diagnóstico...",
            style: TextStyle(fontSize: 16, color: Colors.blue[900], fontWeight: FontWeight.w500),
          ),
          SizedBox(height: 8),
          Text("Esto tardará unos segundos", style: TextStyle(color: Colors.grey)),
        ],
      ),
    );
  }

  Widget _buildCurrentStep() {
    switch (_currentStep) {
      case 1: return _stepInitial();
      case 2: return _stepQuestions();
      case 3: return _stepResult();
      default: return _stepInitial();
    }
  }

  Widget _stepInitial() {
    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text("¿Qué le sucede a tu equipo?", style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
          SizedBox(height: 8),
          Text("Describe la falla con tus propias palabras para que nuestra IA te guíe.", style: TextStyle(color: Colors.grey[600])),
          SizedBox(height: 32),
          TextField(
            controller: _descriptionController,
            maxLines: 5,
            decoration: InputDecoration(
              hintText: "Ej: Mi portátil se apaga solo después de 10 minutos de uso...",
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(16)),
              filled: true,
              fillColor: Colors.grey[50],
            ),
          ),
          Spacer(),
          SizedBox(
            width: double.infinity,
            height: 56,
            child: ElevatedButton(
              onPressed: _getQuestions,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blue[900],
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              child: Text("Continuar", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            ),
          )
        ],
      ),
    );
  }

  Widget _stepQuestions() {
    return ListView(
      padding: const EdgeInsets.all(24.0),
      children: [
        Row(
          children: [
            Icon(Icons.auto_awesome, color: Colors.blue),
            SizedBox(width: 12),
            Expanded(child: Text("Para precisar el diagnóstico, por favor responde:", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold))),
          ],
        ),
        SizedBox(height: 32),
        for (int i = 0; i < _questions.length; i++) ...[
          Text(_questions[i], style: TextStyle(fontWeight: FontWeight.w500, fontSize: 16)),
          SizedBox(height: 12),
          TextField(
            controller: _answerControllers[i],
            decoration: InputDecoration(
              hintText: "Tu respuesta...",
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
            ),
          ),
          SizedBox(height: 24),
        ],
        SizedBox(height: 32),
        SizedBox(
          width: double.infinity,
          height: 56,
          child: ElevatedButton(
            onPressed: _getFinalDiagnosis,
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.blue[900],
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            child: Text("Ver Diagnóstico Final", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
          ),
        )
      ],
    );
  }

  Widget _stepResult() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Center(
            child: Column(
              children: [
                Icon(Icons.check_circle, color: Colors.green, size: 64),
                SizedBox(height: 16),
                Text("Ticket Creado con Éxito", style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                Text("Número: $_ticketNumber", style: TextStyle(color: Colors.blue[800], fontWeight: FontWeight.bold)),
              ],
            ),
          ),
          SizedBox(height: 40),
          Container(
            padding: EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: Colors.blue[50],
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: Colors.blue[200]!),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(Icons.auto_awesome, color: Colors.blue[800]),
                    SizedBox(width: 12),
                    Text("Análisis de la IA", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.blue[900])),
                  ],
                ),
                SizedBox(height: 16),
                Text(_finalDiagnosis!, style: TextStyle(fontSize: 15, height: 1.5)),
              ],
            ),
          ),
          SizedBox(height: 40),
          SizedBox(
            width: double.infinity,
            height: 56,
            child: OutlinedButton(
              onPressed: () => Navigator.pop(context),
              style: OutlinedButton.styleFrom(
                side: BorderSide(color: Colors.blue[900]!),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              child: Text("Regresar al Inicio", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            ),
          )
        ],
      ),
    );
  }
}
