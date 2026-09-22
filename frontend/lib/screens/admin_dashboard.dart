import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/app_state.dart';
import '../services/ticket_service.dart';
import '../theme/app_colors.dart';
import '../widgets/ui_kit.dart';

class AdminDashboard extends StatefulWidget {
  const AdminDashboard({super.key});

  @override
  State<AdminDashboard> createState() => _AdminDashboardState();
}

class _AdminDashboardState extends State<AdminDashboard> {
  int _index = 0;
  final _ticketService = TicketService();
  List<dynamic> _tickets = [];

  static const _titles = [
    'Dashboard',
    'Usuarios',
    'Técnicos',
    'Tickets',
    'Servicios',
    'Indicadores',
    'Configuración',
  ];

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final data = await _ticketService.getTickets();
    if (!mounted) return;
    setState(() => _tickets = data);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_titles[_index], style: const TextStyle(fontWeight: FontWeight.w800)),
      ),
      drawer: Drawer(
        child: SafeArea(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const DrawerHeader(
                decoration: BoxDecoration(gradient: AppColors.gradient),
                child: Align(
                  alignment: Alignment.bottomLeft,
                  child: Text(
                    'Administración\nTuTranquilo',
                    style: TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.w800),
                  ),
                ),
              ),
              for (var i = 0; i < _titles.length; i++)
                ListTile(
                  selected: _index == i,
                  leading: Icon(_adminIcon(i)),
                  title: Text(_titles[i]),
                  onTap: () {
                    setState(() => _index = i);
                    Navigator.pop(context);
                  },
                ),
              const Spacer(),
              ListTile(
                leading: const Icon(Icons.logout, color: AppColors.danger),
                title: const Text('Cerrar sesión'),
                onTap: () {
                  Provider.of<AppState>(context, listen: false).logout();
                  Navigator.pushNamedAndRemoveUntil(context, '/login', (_) => false);
                },
              ),
            ],
          ),
        ),
      ),
      body: IndexedStack(
        index: _index,
        children: [
          _AdminHome(
            tickets: _tickets,
            onOpen: (i) => setState(() => _index = i),
          ),
          const _PlaceholderList(
            icon: Icons.group,
            title: 'Usuarios',
            message: 'Aquí gestionarás cuentas de clientes, altas y estados.',
          ),
          const _PlaceholderList(
            icon: Icons.engineering,
            title: 'Técnicos',
            message: 'Listado de técnicos, especialidad y verificación.',
          ),
          _AdminTickets(tickets: _tickets, onRefresh: _load),
          const _PlaceholderList(
            icon: Icons.miscellaneous_services,
            title: 'Servicios',
            message: 'Catálogo de servicios de hogar y empresa.',
          ),
          _Indicadores(tickets: _tickets),
          const _ConfigTab(),
        ],
      ),
    );
  }

  IconData _adminIcon(int i) {
    switch (i) {
      case 0:
        return Icons.dashboard_outlined;
      case 1:
        return Icons.group_outlined;
      case 2:
        return Icons.engineering_outlined;
      case 3:
        return Icons.confirmation_number_outlined;
      case 4:
        return Icons.miscellaneous_services_outlined;
      case 5:
        return Icons.insights_outlined;
      default:
        return Icons.settings_outlined;
    }
  }
}

class _AdminHome extends StatelessWidget {
  final List<dynamic> tickets;
  final ValueChanged<int> onOpen;
  const _AdminHome({required this.tickets, required this.onOpen});

  @override
  Widget build(BuildContext context) {
    final maps = tickets.whereType<Map>().toList();
    final pending = maps.where((t) {
      final e = (t['estado'] ?? '').toString().toUpperCase();
      return e == 'NUEVO' || e == 'ASIGNADO';
    }).length;

    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        const SectionTitle('Resumen', subtitle: 'Indicadores rápidos del día'),
        const SizedBox(height: 16),
        GridView.count(
          crossAxisCount: 2,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: 1.35,
          children: [
            StatCard(label: 'Tickets', value: '${maps.length}', icon: Icons.confirmation_number, color: AppColors.blue),
            StatCard(label: 'Pendientes', value: '$pending', icon: Icons.pending_actions, color: AppColors.warning),
            const StatCard(label: 'Técnicos', value: '—', icon: Icons.engineering, color: Colors.purple),
            const StatCard(label: 'Usuarios', value: '—', icon: Icons.group, color: AppColors.success),
          ],
        ),
        const SizedBox(height: 24),
        const SectionTitle('Módulos'),
        const SizedBox(height: 12),
        MenuTile(icon: Icons.group, color: AppColors.success, title: 'Usuarios', subtitle: 'Cuentas y roles', onTap: () => onOpen(1)),
        MenuTile(icon: Icons.engineering, color: Colors.purple, title: 'Técnicos', subtitle: 'Equipo de campo y verificación', onTap: () => onOpen(2)),
        MenuTile(icon: Icons.confirmation_number, color: AppColors.blue, title: 'Tickets', subtitle: 'Todas las solicitudes', onTap: () => onOpen(3)),
        MenuTile(icon: Icons.miscellaneous_services, color: AppColors.tealDark, title: 'Servicios', subtitle: 'Catálogo y precios base', onTap: () => onOpen(4)),
        MenuTile(icon: Icons.insights, color: AppColors.warning, title: 'Indicadores', subtitle: 'Métricas de operación', onTap: () => onOpen(5)),
        MenuTile(icon: Icons.settings, color: AppColors.navy, title: 'Configuración', subtitle: 'Preferencias de la plataforma', onTap: () => onOpen(6)),
      ],
    );
  }
}

class _AdminTickets extends StatelessWidget {
  final List<dynamic> tickets;
  final Future<void> Function() onRefresh;
  const _AdminTickets({required this.tickets, required this.onRefresh});

  @override
  Widget build(BuildContext context) {
    final items = tickets.whereType<Map>().toList();
    return RefreshIndicator(
      onRefresh: onRefresh,
      child: items.isEmpty
          ? ListView(
              children: const [
                SizedBox(height: 80),
                EmptyState(
                  icon: Icons.confirmation_number_outlined,
                  title: 'Sin tickets',
                  message: 'Cuando los clientes reporten problemas, aparecerán aquí.',
                ),
              ],
            )
          : ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: items.length,
              separatorBuilder: (_, __) => const SizedBox(height: 8),
              itemBuilder: (context, i) {
                final t = items[i];
                final estado = (t['estado'] ?? 'NUEVO').toString();
                return Card(
                  child: ListTile(
                    title: Text(t['ticket_number']?.toString() ?? 'Ticket #${t['id']}'),
                    subtitle: Text(t['problem_description']?.toString() ?? '', maxLines: 2, overflow: TextOverflow.ellipsis),
                    trailing: StatusChip(label: statusLabel(estado), color: statusColor(estado)),
                  ),
                );
              },
            ),
    );
  }
}

class _Indicadores extends StatelessWidget {
  final List<dynamic> tickets;
  const _Indicadores({required this.tickets});

  @override
  Widget build(BuildContext context) {
    final maps = tickets.whereType<Map>().toList();
    final byStatus = <String, int>{};
    for (final t in maps) {
      final e = (t['estado'] ?? 'NUEVO').toString();
      byStatus[e] = (byStatus[e] ?? 0) + 1;
    }

    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        const SectionTitle('Indicadores', subtitle: 'Operación en tiempo real'),
        const SizedBox(height: 16),
        if (byStatus.isEmpty)
          const EmptyState(
            icon: Icons.insights,
            title: 'Sin datos todavía',
            message: 'Los indicadores se alimentan de tickets reales en PostgreSQL.',
          )
        else
          ...byStatus.entries.map(
            (e) => Card(
              child: ListTile(
                title: Text(statusLabel(e.key)),
                trailing: Text('${e.value}', style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 18)),
              ),
            ),
          ),
      ],
    );
  }
}

class _ConfigTab extends StatelessWidget {
  const _ConfigTab();

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(20),
      children: const [
        SectionTitle('Configuración'),
        SizedBox(height: 12),
        Card(
          child: ListTile(
            leading: Icon(Icons.smart_toy_outlined),
            title: Text('IA local (Ollama)'),
            subtitle: Text('Diagnósticos sin enviar datos a APIs externas'),
          ),
        ),
        Card(
          child: ListTile(
            leading: Icon(Icons.storage_outlined),
            title: Text('PostgreSQL'),
            subtitle: Text('Base de datos del backend FastAPI'),
          ),
        ),
        Card(
          child: ListTile(
            leading: Icon(Icons.phone_android),
            title: Text('Cliente Flutter'),
            subtitle: Text('Misma app para Android e iOS'),
          ),
        ),
      ],
    );
  }
}

class _PlaceholderList extends StatelessWidget {
  final IconData icon;
  final String title;
  final String message;
  const _PlaceholderList({required this.icon, required this.title, required this.message});

  @override
  Widget build(BuildContext context) {
    return EmptyState(icon: icon, title: title, message: message);
  }
}
