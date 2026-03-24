// lib/screens/account_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../utils/app_theme.dart';
import '../services/auth_provider.dart';
import 'subscribe_screen.dart';

class AccountScreen extends StatelessWidget {
  const AccountScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('AKAUNTI')),
      body: Consumer<AuthProvider>(
        builder: (ctx, auth, _) => SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            children: [
              // Avatar
              Container(
                width: 90,
                height: 90,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(colors: [AppColors.primary, AppColors.accent]),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.person_rounded, size: 50, color: Colors.white),
              ),
              const SizedBox(height: 16),
              Text(
                auth.isSubscribed ? 'Mwanachama wa PREMIUM' : 'Mgeni',
                style: const TextStyle(
                  fontFamily: 'Rajdhani',
                  fontWeight: FontWeight.w700,
                  fontSize: 18,
                  color: AppColors.text,
                ),
              ),
              if (auth.isSubscribed && auth.plan != null) ...[
                const SizedBox(height: 4),
                Text(
                  'Mpango: ${auth.plan!.toUpperCase()}',
                  style: const TextStyle(color: AppColors.muted, fontFamily: 'Rajdhani', fontSize: 13),
                ),
              ],
              if (auth.isSubscribed && auth.expiresAt != null) ...[
                const SizedBox(height: 4),
                Text(
                  'Inaisha: ${_formatDate(auth.expiresAt!)}',
                  style: TextStyle(
                    color: auth.expiresAt!.difference(DateTime.now()).inDays < 3
                        ? AppColors.primary
                        : AppColors.muted,
                    fontFamily: 'Rajdhani',
                    fontSize: 12,
                  ),
                ),
              ],

              const SizedBox(height: 32),

              // Subscription card
              if (!auth.isSubscribed) ...[
                Container(
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      colors: [AppColors.primary.withOpacity(0.15), AppColors.accent.withOpacity(0.05)],
                    ),
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: AppColors.primary.withOpacity(0.3)),
                  ),
                  child: Column(
                    children: [
                      const Text(
                        'JIUNGE JAYNES MAX TV',
                        style: TextStyle(fontFamily: 'BebasNeue', fontSize: 22, letterSpacing: 3, color: AppColors.text),
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'Angalia channels zote 50+ HD bila vikwazo',
                        style: TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted, fontSize: 13),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 16),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppColors.primary,
                            padding: const EdgeInsets.symmetric(vertical: 14),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                          ),
                          onPressed: () => Navigator.push(ctx, MaterialPageRoute(builder: (_) => const SubscribeScreen())),
                          child: const Text(
                            'JIUNGE SASA',
                            style: TextStyle(fontFamily: 'Rajdhani', fontWeight: FontWeight.w700, fontSize: 15, letterSpacing: 2, color: Colors.white),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 24),
              ],

              // Settings list
              _SettingsTile(
                icon: Icons.info_outline_rounded,
                title: 'Kuhusu App',
                subtitle: 'v${AppConstants.appVersion}',
                onTap: () => _showAbout(ctx),
              ),
              _SettingsTile(
                icon: Icons.help_outline_rounded,
                title: 'Msaada',
                subtitle: 'WhatsApp, Email',
                onTap: () {},
              ),
              if (auth.isSubscribed)
                _SettingsTile(
                  icon: Icons.logout_rounded,
                  title: 'Toka',
                  subtitle: 'Ondoa akaunti',
                  danger: true,
                  onTap: () => _confirmLogout(ctx, auth),
                ),
            ],
          ),
        ),
      ),
    );
  }

  String _formatDate(DateTime dt) {
    return '${dt.day}/${dt.month}/${dt.year}';
  }

  void _showAbout(BuildContext ctx) {
    showDialog(
      context: ctx,
      builder: (_) => AlertDialog(
        backgroundColor: AppColors.surface,
        title: const Text('JAYNES MAX TV', style: TextStyle(fontFamily: 'BebasNeue', letterSpacing: 3, color: AppColors.text, fontSize: 22)),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Version: 1.0.0', style: TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted)),
            SizedBox(height: 8),
            Text('Tanzania Live TV Streaming', style: TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted)),
            SizedBox(height: 4),
            Text('© 2025 JAYNES MAX TV', style: TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted, fontSize: 12)),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(_),
            child: const Text('FUNGA', style: TextStyle(color: AppColors.primary, fontFamily: 'Rajdhani', fontWeight: FontWeight.w700)),
          ),
        ],
      ),
    );
  }

  void _confirmLogout(BuildContext ctx, AuthProvider auth) {
    showDialog(
      context: ctx,
      builder: (_) => AlertDialog(
        backgroundColor: AppColors.surface,
        title: const Text('Toka?', style: TextStyle(fontFamily: 'Rajdhani', fontWeight: FontWeight.w700, color: AppColors.text)),
        content: const Text('Una uhakika unataka kutoka?', style: TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted)),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(_),
            child: const Text('Hapana', style: TextStyle(color: AppColors.muted, fontFamily: 'Rajdhani')),
          ),
          TextButton(
            onPressed: () {
              auth.logout();
              Navigator.pop(_);
            },
            child: const Text('Ndiyo, Toka', style: TextStyle(color: AppColors.primary, fontFamily: 'Rajdhani', fontWeight: FontWeight.w700)),
          ),
        ],
      ),
    );
  }
}

class _SettingsTile extends StatelessWidget {
  final IconData icon;
  final String title, subtitle;
  final bool danger;
  final VoidCallback onTap;
  const _SettingsTile({
    required this.icon, required this.title,
    required this.subtitle, required this.onTap, this.danger = false,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.divider),
      ),
      child: ListTile(
        leading: Icon(icon, color: danger ? AppColors.primary : AppColors.muted, size: 22),
        title: Text(title, style: TextStyle(fontFamily: 'Rajdhani', fontWeight: FontWeight.w600, color: danger ? AppColors.primary : AppColors.text, fontSize: 15)),
        subtitle: Text(subtitle, style: const TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted, fontSize: 12)),
        trailing: const Icon(Icons.chevron_right_rounded, color: AppColors.muted),
        onTap: onTap,
      ),
    );
  }
}
