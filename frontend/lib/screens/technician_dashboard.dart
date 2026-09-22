import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/app_state.dart';
import '../services/ticket_service.dart';
import '../theme/app_colors.dart';
import '../widgets/ui_kit.dart';

class TechnicianDashboard extends StatefulWidget {
  const TechnicianDashboard({super.key});

  @override
  State<TechnicianDashboard> createState() => _TechnicianDashboardState();
}

class _TechnicianDashboardState extends State<TechnicianDashboard> {
  int _index = 0;
  final _ticketService = TicketService();
  List<dynamic> _tickets = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final data = await _ticketService.getTickets();
    if (!mounted) return;
    setState(() {
      _tickets = data;
      _loading = false;
    });
  }

  List<Map> _filter(Set<String> estados) {
    return _tickets
        .whereType<Map>()
        .where((t) => estados.contains((t['estado'] ?? '').toString().toUpperCase()))
        .toList();
  }

  @override
  Widget build(BuildContext context) {
    final pages = [
      _TechList(
        title: 'Servicios asignados',
        emptyTitle: 'Nada asignado',
        emptyMessage: 'Cuando un administrador te asigne un ticket, aparecerá aquí.',
        tickets: _filter({'NUEVO', 'ASIGNADO'}),
        loading: _loading,
        onRefresh: _load,
      ),
      _TechList(
        title: 'En proceso',
        emptyTitle: 'Sin trabajo en curso',
        emptyMessage: 'Los tickets que estés atendiendo se listan en esta sección.',
        tickets: _filter({'EN_PROCESO', 'PENDIENTE_REPUESTO'}),
        loading: _loading,
        onRefresh: _load,
      ),
      _TechList(
        title: 'Finalizados',
        emptyTitle: 'Aún no hay cierres',
        emptyMessage: 'Los servicios que termines quedarán en este listado.',
        tickets: _filter({'FINALIZADO'}),
        loading: _loading,
        onRefresh: _load,
      ),
      _TechList(
        title: 'Historial',
        emptyTitle: 'Historial vacío',
        emptyMessage: 'El historial reúne servicios finalizados y cerrados.',
        tickets: _filter({'FINALIZADO', 'CERRADO'}),
        loading: _loading,
        onRefresh: _load,
        showLogout: true,
      ),
    ];

    return Scaffold(
      body: IndexedStack(index: _index, children: pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.assignment_outlined), selectedIcon: Icon(Icons.assignment), label: 'Asignados'),
          NavigationDestination(icon: Icon(Icons.handyman_outlined), selectedIcon: Icon(Icons.handyman), label: 'En proceso'),
          NavigationDestination(icon: Icon(Icons.task_alt_outlined), selectedIcon: Icon(Icons.task_alt), label: 'Finalizados'),
          NavigationDestination(icon: Icon(Icons.history), label: 'Historial'),
        ],
      ),
    );
  }
}

class _TechList extends StatelessWidget {
  final String title;
  final String emptyTitle;
  final String emptyMessage;
  final List<Map> tickets;
  final bool loading;
  final Future<void> Function() onRefresh;
  final bool showLogout;

  const _TechList({
    required this.title,
    required this.emptyTitle,
    required this.emptyMessage,
    required this.tickets,
    required this.loading,
    required this.onRefresh,
    this.showLogout = false,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Inicio técnico', style: TextStyle(fontSize: 13, color: AppColors.muted, fontWeight: FontWeight.w500)),
            Text(title, style: const TextStyle(fontWeight: FontWeight.w800)),
          ],
        ),
        actions: [
          if (showLogout)
            IconButton(
              tooltip: 'Cerrar sesión',
              onPressed: () {
                Provider.of<AppState>(context, listen: false).logout();
                Navigator.pushNamedAndRemoveUntil(context, '/login', (_) => false);
              },
              icon: const Icon(Icons.logout),
            ),
        ],
      ),
      body: loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: onRefresh,
              child: tickets.isEmpty
                  ? ListView(
                      children: [
                        const SizedBox(height: 80),
                        EmptyState(icon: Icons.engineering, title: emptyTitle, message: emptyMessage),
                      ],
                    )
                  : ListView.separated(
                      padding: const EdgeInsets.all(16),
                      itemCount: tickets.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 10),
                      itemBuilder: (context, i) {
                        final t = tickets[i];
                        final estado = (t['estado'] ?? 'NUEVO').toString();
                        return Card(
                          child: Padding(
                            padding: const EdgeInsets.all(16),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Expanded(
                                      child: Text(
                                        t['ticket_number']?.toString() ?? 'Ticket #${t['id']}',
                                        style: const TextStyle(fontWeight: FontWeight.w800),
                                      ),
                                    ),
                                    StatusChip(label: statusLabel(estado), color: statusColor(estado)),
                                  ],
                                ),
                                const SizedBox(height: 8),
                                Text(t['problem_description']?.toString() ?? ''),
                                if (t['prioridad'] != null) ...[
                                  const SizedBox(height: 10),
                                  Text('Prioridad: ${t['prioridad']}', style: const TextStyle(color: AppColors.muted, fontSize: 13)),
                                ],
                              ],
                            ),
                          ),
                        );
                      },
                    ),
            ),
    );
  }
}
