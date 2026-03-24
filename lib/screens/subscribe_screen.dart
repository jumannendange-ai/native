// lib/screens/subscribe_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../utils/app_theme.dart';
import '../services/auth_provider.dart';

class SubscribeScreen extends StatefulWidget {
  const SubscribeScreen({super.key});

  @override
  State<SubscribeScreen> createState() => _SubscribeScreenState();
}

class _SubscribeScreenState extends State<SubscribeScreen> {
  int _selected = 2; // Default: Miezi 3

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('JIUNGE JAYNES MAX'),
        leading: IconButton(
          icon: const Icon(Icons.close_rounded),
          color: AppColors.text,
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            // Header
            ShaderMask(
              shaderCallback: (bounds) => const LinearGradient(
                colors: [AppColors.primary, AppColors.accent],
              ).createShader(bounds),
              child: const Text(
                'JAYNES MAX TV',
                style: TextStyle(
                  fontFamily: 'BebasNeue',
                  fontSize: 36,
                  letterSpacing: 6,
                  color: Colors.white,
                ),
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Channels 50+ • HD Quality • Bila Matangazo',
              style: TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted, fontSize: 13),
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 32),

            // Features
            _FeatureRow(icon: Icons.live_tv_rounded, text: 'Live TV channels 50+ za Tanzania'),
            const SizedBox(height: 12),
            _FeatureRow(icon: Icons.hd_rounded, text: 'HD & Full HD quality'),
            const SizedBox(height: 12),
            _FeatureRow(icon: Icons.devices_rounded, text: 'Android na Web'),
            const SizedBox(height: 12),
            _FeatureRow(icon: Icons.block_rounded, text: 'Bila matangazo (Ad-free)'),

            const SizedBox(height: 32),

            // Plans
            const Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'CHAGUA MPANGO',
                style: TextStyle(
                  fontFamily: 'BebasNeue',
                  fontSize: 18,
                  letterSpacing: 4,
                  color: AppColors.muted,
                ),
              ),
            ),
            const SizedBox(height: 12),

            ...AppConstants.plans.asMap().entries.map((entry) {
              final i = entry.key;
              final plan = entry.value;
              return _PlanCard(
                plan: plan,
                selected: _selected == i,
                onTap: () => setState(() => _selected = i),
              );
            }),

            const SizedBox(height: 32),

            // CTA Button
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.zero,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                onPressed: () => _subscribe(context),
                child: Ink(
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(colors: [AppColors.primary, AppColors.accent]),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    alignment: Alignment.center,
                    child: Text(
                      'LIPA ${_formatPrice(AppConstants.plans[_selected]['price'] as int)} - ${AppConstants.plans[_selected]['label']}',
                      style: const TextStyle(
                        fontFamily: 'Rajdhani',
                        fontWeight: FontWeight.w700,
                        fontSize: 16,
                        letterSpacing: 2,
                        color: Colors.white,
                      ),
                    ),
                  ),
                ),
              ),
            ),

            const SizedBox(height: 16),
            const Text(
              'Malipo yanafanywa kupitia M-Pesa au Airtel Money',
              style: TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted, fontSize: 11),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  String _formatPrice(int price) {
    return 'TZS ${price.toString().replaceAllMapped(
      RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'),
      (m) => '${m[1]},',
    )}';
  }

  void _subscribe(BuildContext context) {
    final plan = AppConstants.plans[_selected];
    showModalBottomSheet(
      context: context,
      backgroundColor: AppColors.surface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (_) => _PaymentSheet(
        plan: plan,
        onConfirm: () async {
          await context.read<AuthProvider>().subscribe(
            plan['id'] as String,
            plan['days'] as int,
          );
          if (mounted) {
            Navigator.pop(context); // close sheet
            Navigator.pop(context); // close subscribe screen
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(
                  '✅ Umefanikiwa! Karibu JAYNES MAX TV',
                  style: const TextStyle(fontFamily: 'Rajdhani', fontWeight: FontWeight.w600),
                ),
                backgroundColor: Colors.green,
              ),
            );
          }
        },
      ),
    );
  }
}

class _FeatureRow extends StatelessWidget {
  final IconData icon;
  final String text;
  const _FeatureRow({required this.icon, required this.text});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, color: AppColors.primary, size: 20),
        const SizedBox(width: 12),
        Text(text, style: const TextStyle(fontFamily: 'Rajdhani', color: AppColors.text, fontSize: 14)),
      ],
    );
  }
}

class _PlanCard extends StatelessWidget {
  final Map<String, dynamic> plan;
  final bool selected;
  final VoidCallback onTap;
  const _PlanCard({required this.plan, required this.selected, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        margin: const EdgeInsets.only(bottom: 10),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        decoration: BoxDecoration(
          color: selected ? AppColors.primary.withOpacity(0.1) : AppColors.card,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: selected ? AppColors.primary : AppColors.divider,
            width: selected ? 1.5 : 1,
          ),
        ),
        child: Row(
          children: [
            // Radio
            Container(
              width: 20,
              height: 20,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(
                  color: selected ? AppColors.primary : AppColors.muted,
                  width: 2,
                ),
              ),
              child: selected
                  ? Center(
                      child: Container(
                        width: 10,
                        height: 10,
                        decoration: const BoxDecoration(
                          shape: BoxShape.circle,
                          color: AppColors.primary,
                        ),
                      ),
                    )
                  : null,
            ),
            const SizedBox(width: 14),
            // Plan name
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  plan['label'] as String,
                  style: TextStyle(
                    fontFamily: 'Rajdhani',
                    fontWeight: FontWeight.w700,
                    fontSize: 16,
                    color: selected ? AppColors.text : AppColors.muted,
                  ),
                ),
                Text(
                  '${plan['days']} siku',
                  style: const TextStyle(fontFamily: 'Rajdhani', fontSize: 11, color: AppColors.muted),
                ),
              ],
            ),
            const Spacer(),
            // Badge
            if (plan['badge'] != null)
              Container(
                margin: const EdgeInsets.only(right: 12),
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(colors: [AppColors.primary, AppColors.accent]),
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text(
                  plan['badge'] as String,
                  style: const TextStyle(
                    fontFamily: 'Rajdhani',
                    fontWeight: FontWeight.w700,
                    fontSize: 9,
                    letterSpacing: 1,
                    color: Colors.white,
                  ),
                ),
              ),
            // Price
            Text(
              'TZS ${(plan['price'] as int).toString().replaceAllMapped(RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'), (m) => '${m[1]},')}',
              style: TextStyle(
                fontFamily: 'Rajdhani',
                fontWeight: FontWeight.w700,
                fontSize: 15,
                color: selected ? AppColors.primary : AppColors.text,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PaymentSheet extends StatelessWidget {
  final Map<String, dynamic> plan;
  final VoidCallback onConfirm;
  const _PaymentSheet({required this.plan, required this.onConfirm});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(width: 40, height: 4, decoration: BoxDecoration(color: AppColors.divider, borderRadius: BorderRadius.circular(2))),
          const SizedBox(height: 20),
          const Text('MALIPO', style: TextStyle(fontFamily: 'BebasNeue', fontSize: 24, letterSpacing: 4, color: AppColors.text)),
          const SizedBox(height: 20),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(color: AppColors.card, borderRadius: BorderRadius.circular(12)),
            child: Column(
              children: [
                _PayRow('Mpango', plan['label'] as String),
                const Divider(color: AppColors.divider, height: 24),
                _PayRow('Muda', '${plan['days']} siku'),
                const Divider(color: AppColors.divider, height: 24),
                _PayRow(
                  'Jumla',
                  'TZS ${(plan['price'] as int).toString().replaceAllMapped(RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'), (m) => '${m[1]},')}',
                  bold: true,
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),
          // Payment methods
          Row(
            children: [
              Expanded(child: _PayMethodBtn(label: 'M-Pesa', icon: Icons.phone_android_rounded, onTap: onConfirm)),
              const SizedBox(width: 12),
              Expanded(child: _PayMethodBtn(label: 'Airtel Money', icon: Icons.phone_android_rounded, onTap: onConfirm)),
            ],
          ),
          const SizedBox(height: 8),
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Ghairi', style: TextStyle(color: AppColors.muted, fontFamily: 'Rajdhani')),
          ),
        ],
      ),
    );
  }
}

class _PayRow extends StatelessWidget {
  final String label, value;
  final bool bold;
  const _PayRow(this.label, this.value, {this.bold = false});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: const TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted, fontSize: 14)),
        Text(value, style: TextStyle(
          fontFamily: 'Rajdhani',
          fontWeight: bold ? FontWeight.w700 : FontWeight.w600,
          color: bold ? AppColors.primary : AppColors.text,
          fontSize: bold ? 16 : 14,
        )),
      ],
    );
  }
}

class _PayMethodBtn extends StatelessWidget {
  final String label;
  final IconData icon;
  final VoidCallback onTap;
  const _PayMethodBtn({required this.label, required this.icon, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 14),
        decoration: BoxDecoration(
          gradient: const LinearGradient(colors: [AppColors.primary, AppColors.accent]),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Column(
          children: [
            Icon(icon, color: Colors.white, size: 24),
            const SizedBox(height: 4),
            Text(label, style: const TextStyle(fontFamily: 'Rajdhani', fontWeight: FontWeight.w700, color: Colors.white, fontSize: 13)),
          ],
        ),
      ),
    );
  }
}
