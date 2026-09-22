import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/app_state.dart';
import '../services/ticket_service.dart';
import '../theme/app_colors.dart';
import '../widgets/ui_kit.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = [
      const _InicioCliente(),
      const _EquiposTab(),
      const _TicketsTab(title: 'Mis tickets', activeOnly: true),
      const _TicketsTab(title: 'Historial', activeOnly: false),
      const _PerfilTab(),
    ];

    return Scaffold(
      body: IndexedStack(index: _index, children: pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: 'Inicio'),
          NavigationDestination(icon: Icon(Icons.devices_outlined), selectedIcon: Icon(Icons.devices), label: 'Equipos'),
          NavigationDestination(icon: Icon(Icons.confirmation_number_outlined), selectedIcon: Icon(Icons.confirmation_number), label: 'Tickets'),
          NavigationDestination(icon: Icon(Icons.history), label: 'Historial'),
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: 'Perfil'),
        ],
      ),
    );
  }
}

class _InicioCliente extends StatelessWidget {
  const _InicioCliente();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Hola', style: TextStyle(fontSize: 13, color: AppColors.muted, fontWeight: FontWeight.w500)),
            Text('TuTranquilo', style: TextStyle(fontWeight: FontWeight.w800)),
          ],
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: AppColors.gradient,
              borderRadius: BorderRadius.circular(20),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Soporte técnico cuando lo necesites', style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.w800)),
                const SizedBox(height: 8),
                const Text('Describe el problema y te guiamos con un diagnóstico inicial.', style: TextStyle(color: Colors.white70)),
                const SizedBox(height: 16),
                FilledButton.icon(
                  onPressed: () => Navigator.pushNamed(context, '/report'),
                  icon: const Icon(Icons.report_problem_outlined),
                  label: const Text('Reportar problema'),
                  style: FilledButton.styleFrom(backgroundColor: Colors.white, foregroundColor: AppColors.blue),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
          const SectionTitle(
            'Promociones',
            subtitle: 'Software para potenciar tu trabajo',
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 230,
            child: ListView(
              scrollDirection: Axis.horizontal,
              children: const [
                _ProductCard(
                  name: 'Adobe Acrobat Pro',
                  price: 'S/12',
                  icon: Icons.picture_as_pdf_outlined,
                  color: Color(0xFFDC2626),
                ),
                _ProductCard(
                  name: 'Wondershare Filmora',
                  price: 'S/12',
                  icon: Icons.movie_creation_outlined,
                  color: Color(0xFF7C3AED),
                ),
                _ProductCard(
                  name: 'SketchUp Pro 2026',
                  price: 'S/12',
                  icon: Icons.architecture_outlined,
                  color: Color(0xFFEA580C),
                ),
                _ProductCard(
                  name: 'AutoCAD 2026',
                  price: 'S/12',
                  icon: Icons.design_services_outlined,
                  color: Color(0xFF2563EB),
                ),
                _ProductCard(
                  name: 'CorelDRAW',
                  price: 'S/12',
                  icon: Icons.brush_outlined,
                  color: Color(0xFF16A34A),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ProductCard extends StatelessWidget {
  final String name;
  final String price;
  final IconData icon;
  final Color color;

  const _ProductCard({
    required this.name,
    required this.price,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 178,
      margin: const EdgeInsets.only(right: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: const Color(0xFFEEF2F7)),
        boxShadow: const [
          BoxShadow(
            color: Color(0x0D0F172A),
            blurRadius: 10,
            offset: Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 52,
            height: 52,
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: color, size: 30),
          ),
          const Spacer(),
          Text(
            name,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16),
          ),
          const SizedBox(height: 8),
          const Text(
            'Desde',
            style: TextStyle(color: AppColors.muted, fontSize: 12),
          ),
          Text(
            price,
            style: TextStyle(color: color, fontSize: 20, fontWeight: FontWeight.w800),
          ),
        ],
      ),
    );
  }
}

class _EquiposTab extends StatefulWidget {
  const _EquiposTab();

  @override
  State<_EquiposTab> createState() => _EquiposTabState();
}

class _EquiposTabState extends State<_EquiposTab> {
  final _ticketService = TicketService();
  List<dynamic> _equipments = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final data = await _ticketService.getEquipments();
    if (!mounted) return;
    setState(() {
      _equipments = data;
      _loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mis equipos', style: TextStyle(fontWeight: FontWeight.w800))),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _load,
              child: _equipments.isEmpty
                  ? ListView(
                      children: const [
                        SizedBox(height: 80),
                        EmptyState(
                          icon: Icons.devices_other,
                          title: 'Sin equipos aún',
                          message: 'Cuando registres un equipo aparecerá aquí para asociarlo a tus solicitudes.',
                        ),
                      ],
                    )
                  : ListView.separated(
                      padding: const EdgeInsets.all(16),
                      itemCount: _equipments.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 8),
                      itemBuilder: (context, i) {
                        final e = _equipments[i];
                        return Card(
                          child: ListTile(
                            leading: const CircleAvatar(child: Icon(Icons.laptop)),
                            title: Text('${e['marca'] ?? ''} ${e['modelo'] ?? ''}'.trim()),
                            subtitle: Text('${e['tipo'] ?? 'Equipo'} · ${e['serial_number'] ?? ''}'),
                          ),
                        );
                      },
                    ),
            ),
    );
  }
}

class _TicketsTab extends StatefulWidget {
  final String title;
  final bool activeOnly;
  const _TicketsTab({required this.title, required this.activeOnly});

  @override
  State<_TicketsTab> createState() => _TicketsTabState();
}

class _TicketsTabState extends State<_TicketsTab> {
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

  bool _matches(Map ticket) {
    final estado = (ticket['estado'] ?? '').toString().toUpperCase();
    final closed = estado == 'FINALIZADO' || estado == 'CERRADO';
    return widget.activeOnly ? !closed : closed;
  }

  @override
  Widget build(BuildContext context) {
    final items = _tickets.whereType<Map>().where(_matches).toList();
    return Scaffold(
      appBar: AppBar(title: Text(widget.title, style: const TextStyle(fontWeight: FontWeight.w800))),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _load,
              child: items.isEmpty
                  ? ListView(
                      children: [
                        const SizedBox(height: 80),
                        EmptyState(
                          icon: widget.activeOnly ? Icons.confirmation_number_outlined : Icons.history,
                          title: widget.activeOnly ? 'No hay tickets abiertos' : 'Sin historial',
                          message: widget.activeOnly
                              ? 'Reporta un problema para crear tu primera solicitud.'
                              : 'Los servicios finalizados se mostrarán aquí.',
                        ),
                      ],
                    )
                  : ListView.separated(
                      padding: const EdgeInsets.all(16),
                      itemCount: items.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 10),
                      itemBuilder: (context, i) {
                        final t = items[i];
                        final estado = (t['estado'] ?? 'NUEVO').toString();
                        return Card(
                          child: ListTile(
                            title: Text(t['ticket_number']?.toString() ?? 'Ticket #${t['id']}'),
                            subtitle: Text(
                              t['problem_description']?.toString() ?? '',
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                            ),
                            trailing: StatusChip(label: statusLabel(estado), color: statusColor(estado)),
                          ),
                        );
                      },
                    ),
            ),
    );
  }
}

class _PerfilTab extends StatelessWidget {
  const _PerfilTab();

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Perfil', style: TextStyle(fontWeight: FontWeight.w800)),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
        children: [
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: AppColors.gradient,
              borderRadius: BorderRadius.circular(22),
            ),
            child: const Row(
              children: [
                CircleAvatar(
                  radius: 34,
                  backgroundColor: Colors.white,
                  child: Icon(Icons.person, size: 38, color: AppColors.blue),
                ),
                SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Anto Limat',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 21,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      SizedBox(height: 4),
                      Text('antolimat475@gmail.com', style: TextStyle(color: Colors.white70)),
                      SizedBox(height: 8),
                      Text(
                        'USUARIO',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 12,
                          fontWeight: FontWeight.w800,
                          letterSpacing: 1,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 28),
          const Text(
            'Configuración de Cuenta',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800),
          ),
          const SizedBox(height: 10),
          _ProfileOption(
            icon: Icons.person_outline,
            title: 'Perfil de Usuario',
            subtitle: 'Revisa y edita tu información',
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const _PerfilUsuarioScreen()),
              );
            },
          ),
          _ProfileOption(
            icon: Icons.notifications_none,
            title: 'Notificaciones',
            subtitle: 'Todo al día',
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const _CentroNotificacionesScreen()),
              );
            },
          ),
          const SizedBox(height: 18),
          _ProfileOption(
            icon: Icons.support_agent,
            title: 'Contacto Oficial',
            subtitle: 'Soporte, reclamos o sugerencias',
            onTap: () => _showUnavailableMessage(context, 'Contacto Oficial'),
          ),
          _ProfileOption(
            icon: Icons.receipt_long_outlined,
            title: 'Mis Pedidos',
            subtitle: 'Historial de servicios solicitados',
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const _MisPedidosScreen()),
              );
            },
          ),
          const SizedBox(height: 18),
          Card(
            elevation: 0,
            color: Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
              side: const BorderSide(color: Color(0xFFEEF2F7)),
            ),
            child: ListTile(
              onTap: () {
                appState.logout();
                Navigator.pushNamedAndRemoveUntil(context, '/login', (_) => false);
              },
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              leading: Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: AppColors.danger.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.logout, color: AppColors.danger),
              ),
              title: const Text(
                'Cerrar Sesión',
                style: TextStyle(color: AppColors.danger, fontWeight: FontWeight.w700),
              ),
              subtitle: const Text('Salir de tu cuenta de forma segura'),
              trailing: const Icon(Icons.chevron_right, color: AppColors.muted),
            ),
          ),
        ],
      ),
    );
  }

  void _showUnavailableMessage(BuildContext context, String section) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('$section estará disponible próximamente.')),
    );
  }
}

class _ProfileOption extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  const _ProfileOption({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      color: Colors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: const BorderSide(color: Color(0xFFEEF2F7)),
      ),
      child: ListTile(
        onTap: onTap,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        leading: Container(
          width: 48,
          height: 48,
          decoration: BoxDecoration(
            color: AppColors.blue.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: AppColors.blue),
        ),
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
        subtitle: Text(subtitle),
        trailing: const Icon(Icons.chevron_right, color: AppColors.muted),
      ),
    );
  }
}

class _PerfilUsuarioScreen extends StatefulWidget {
  const _PerfilUsuarioScreen();

  @override
  State<_PerfilUsuarioScreen> createState() => _PerfilUsuarioScreenState();
}

class _PerfilUsuarioScreenState extends State<_PerfilUsuarioScreen> {
  late final TextEditingController _nameController;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: 'Anto Limat');
  }

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  void _saveChanges() {
    FocusScope.of(context).unfocus();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Cambios guardados correctamente.')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Perfil de Usuario', style: TextStyle(fontWeight: FontWeight.w800)),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
        children: [
          const Center(
            child: Column(
              children: [
                CircleAvatar(
                  radius: 44,
                  backgroundColor: Color(0xFFDBEAFE),
                  child: Icon(Icons.person, size: 48, color: AppColors.blue),
                ),
                SizedBox(height: 12),
                Text(
                  'Información personal',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800),
                ),
              ],
            ),
          ),
          const SizedBox(height: 28),
          const Text('Nombre Completo', style: TextStyle(fontWeight: FontWeight.w700)),
          const SizedBox(height: 8),
          TextField(
            controller: _nameController,
            textCapitalization: TextCapitalization.words,
            decoration: InputDecoration(
              prefixIcon: const Icon(Icons.person_outline),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
            ),
          ),
          const SizedBox(height: 20),
          const Text('Correo Electrónico (No editable)', style: TextStyle(fontWeight: FontWeight.w700)),
          const SizedBox(height: 8),
          TextFormField(
            readOnly: true,
            initialValue: 'antolimat475@gmail.com',
            decoration: const InputDecoration(
              prefixIcon: Icon(Icons.email_outlined),
              border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(12))),
              filled: true,
              fillColor: Color(0xFFF3F4F6),
            ),
          ),
          const SizedBox(height: 24),
          const _ProfileInfoRow(
            label: 'Registrado el:',
            value: '21 sept 2026, 09:40 a. m.',
            icon: Icons.calendar_today_outlined,
          ),
          const _ProfileInfoRow(
            label: 'Último acceso:',
            value: '22 sept 2026, 01:40 p. m.',
            icon: Icons.login_outlined,
          ),
          const _ProfileInfoRow(
            label: 'Rol asignado:',
            value: 'CLIENTE',
            icon: Icons.badge_outlined,
          ),
          const SizedBox(height: 24),
          FilledButton(
            onPressed: _saveChanges,
            style: FilledButton.styleFrom(
              minimumSize: const Size.fromHeight(52),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            child: const Text('Guardar Cambios', style: TextStyle(fontWeight: FontWeight.w700)),
          ),
        ],
      ),
    );
  }
}

class _CentroNotificacionesScreen extends StatefulWidget {
  const _CentroNotificacionesScreen();

  @override
  State<_CentroNotificacionesScreen> createState() => _CentroNotificacionesScreenState();
}

class _CentroNotificacionesScreenState extends State<_CentroNotificacionesScreen> {
  bool _notificationsEnabled = true;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Centro de Notificaciones',
          style: TextStyle(fontWeight: FontWeight.w800),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 24, 20, 24),
        children: [
          Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: AppColors.blue.withValues(alpha: 0.08),
              borderRadius: BorderRadius.circular(20),
            ),
            child: const Icon(
              Icons.notifications_active_outlined,
              size: 64,
              color: AppColors.blue,
            ),
          ),
          const SizedBox(height: 28),
          const Text(
            'Centro de Notificaciones',
            style: TextStyle(fontSize: 22, fontWeight: FontWeight.w800),
          ),
          const SizedBox(height: 10),
          const Text(
            'Mantén las notificaciones activas para recibir actualizaciones de tus tickets de soporte, avisos importantes y respuestas de Emma.',
            style: TextStyle(color: AppColors.muted, height: 1.5),
          ),
          const SizedBox(height: 28),
          Card(
            elevation: 0,
            color: Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
              side: const BorderSide(color: Color(0xFFEEF2F7)),
            ),
            child: SwitchListTile(
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              secondary: Icon(
                _notificationsEnabled
                    ? Icons.notifications_active_outlined
                    : Icons.notifications_off_outlined,
                color: _notificationsEnabled ? AppColors.blue : AppColors.muted,
              ),
              title: const Text(
                'Notificaciones Activadas',
                style: TextStyle(fontWeight: FontWeight.w700),
              ),
              value: _notificationsEnabled,
              onChanged: (value) {
                setState(() => _notificationsEnabled = value);
              },
            ),
          ),
          const SizedBox(height: 28),
          FilledButton(
            onPressed: () => Navigator.pop(context),
            style: FilledButton.styleFrom(
              minimumSize: const Size.fromHeight(52),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            child: const Text('Entendido', style: TextStyle(fontWeight: FontWeight.w700)),
          ),
        ],
      ),
    );
  }
}

class _MisPedidosScreen extends StatefulWidget {
  const _MisPedidosScreen();

  @override
  State<_MisPedidosScreen> createState() => _MisPedidosScreenState();
}

class _MisPedidosScreenState extends State<_MisPedidosScreen> {
  int _selectedFilter = 0;
  static const _filters = ['Todos', 'Vigentes', 'Vencidos'];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mis Pedidos', style: TextStyle(fontWeight: FontWeight.w800)),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
        children: [
          Container(
            padding: const EdgeInsets.all(4),
            decoration: BoxDecoration(
              color: const Color(0xFFF1F5F9),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                for (var i = 0; i < _filters.length; i++)
                  Expanded(
                    child: GestureDetector(
                      onTap: () => setState(() => _selectedFilter = i),
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 180),
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        decoration: BoxDecoration(
                          color: _selectedFilter == i ? Colors.white : Colors.transparent,
                          borderRadius: BorderRadius.circular(9),
                          boxShadow: _selectedFilter == i
                              ? const [
                                  BoxShadow(
                                    color: Color(0x140F172A),
                                    blurRadius: 5,
                                    offset: Offset(0, 2),
                                  ),
                                ]
                              : null,
                        ),
                        child: Text(
                          _filters[i],
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: _selectedFilter == i ? AppColors.blue : AppColors.muted,
                            fontWeight: FontWeight.w700,
                            fontSize: 13,
                          ),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 80),
          const Icon(Icons.inbox_outlined, size: 72, color: AppColors.muted),
          const SizedBox(height: 20),
          const Text(
            'Aún no tienes pedidos',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800),
          ),
          const SizedBox(height: 10),
          const Text(
            'Cuando adquieras un servicio con nosotros, aparecerá aquí con su garantía.',
            textAlign: TextAlign.center,
            style: TextStyle(color: AppColors.muted, height: 1.5),
          ),
        ],
      ),
    );
  }
}

class _ProfileInfoRow extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;

  const _ProfileInfoRow({
    required this.label,
    required this.value,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: AppColors.blue),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: const TextStyle(color: AppColors.muted, fontSize: 13)),
                const SizedBox(height: 3),
                Text(value, style: const TextStyle(fontWeight: FontWeight.w600)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
