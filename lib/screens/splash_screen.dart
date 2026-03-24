// lib/screens/splash_screen.dart
import 'package:flutter/material.dart';
import '../utils/app_theme.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with TickerProviderStateMixin {
  late AnimationController _pulseCtrl;
  late AnimationController _logoCtrl;
  late AnimationController _textCtrl;
  late AnimationController _exitCtrl;

  late Animation<double> _pulse1;
  late Animation<double> _pulse2;
  late Animation<double> _logoScale;
  late Animation<double> _logoOpacity;
  late Animation<double> _textOpacity;
  late Animation<Offset> _textSlide;
  late Animation<double> _exitOpacity;

  @override
  void initState() {
    super.initState();

    _pulseCtrl = AnimationController(vsync: this, duration: const Duration(milliseconds: 2800));
    _logoCtrl  = AnimationController(vsync: this, duration: const Duration(milliseconds: 1000));
    _textCtrl  = AnimationController(vsync: this, duration: const Duration(milliseconds: 800));
    _exitCtrl  = AnimationController(vsync: this, duration: const Duration(milliseconds: 600));

    _pulse1 = Tween<double>(begin: 0.3, end: 1.0).animate(
      CurvedAnimation(parent: _pulseCtrl, curve: const Interval(0.0, 0.7, curve: Curves.easeOut)),
    );
    _pulse2 = Tween<double>(begin: 0.2, end: 1.0).animate(
      CurvedAnimation(parent: _pulseCtrl, curve: const Interval(0.2, 1.0, curve: Curves.easeOut)),
    );

    _logoScale = Tween<double>(begin: 0.6, end: 1.0).animate(
      CurvedAnimation(parent: _logoCtrl, curve: Curves.elasticOut),
    );
    _logoOpacity = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _logoCtrl, curve: const Interval(0.0, 0.4, curve: Curves.easeIn)),
    );

    _textOpacity = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _textCtrl, curve: Curves.easeIn),
    );
    _textSlide = Tween<Offset>(begin: const Offset(0, 0.3), end: Offset.zero).animate(
      CurvedAnimation(parent: _textCtrl, curve: Curves.easeOut),
    );

    _exitOpacity = Tween<double>(begin: 1.0, end: 0.0).animate(
      CurvedAnimation(parent: _exitCtrl, curve: Curves.easeIn),
    );

    _runSequence();
  }

  Future<void> _runSequence() async {
    _pulseCtrl.forward();
    await Future.delayed(const Duration(milliseconds: 300));
    _logoCtrl.forward();
    await Future.delayed(const Duration(milliseconds: 700));
    _textCtrl.forward();
    await Future.delayed(const Duration(milliseconds: 1800));
    _exitCtrl.forward();
    await Future.delayed(const Duration(milliseconds: 600));
    if (mounted) {
      Navigator.pushReplacementNamed(context, '/main');
    }
  }

  @override
  void dispose() {
    _pulseCtrl.dispose();
    _logoCtrl.dispose();
    _textCtrl.dispose();
    _exitCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return FadeTransition(
      opacity: _exitOpacity,
      child: Scaffold(
        backgroundColor: const Color(0xFF06060E),
        body: Stack(
          alignment: Alignment.center,
          children: [
            // Pulse circles
            AnimatedBuilder(
              animation: _pulseCtrl,
              builder: (_, __) => Stack(
                alignment: Alignment.center,
                children: [
                  _buildPulse(_pulse2, 300, AppColors.primary.withOpacity(0.07)),
                  _buildPulse(_pulse1, 200, AppColors.primary.withOpacity(0.15)),
                ],
              ),
            ),
            // Logo + Text
            Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                // Logo mark
                AnimatedBuilder(
                  animation: _logoCtrl,
                  builder: (_, __) => Opacity(
                    opacity: _logoOpacity.value,
                    child: Transform.scale(
                      scale: _logoScale.value,
                      child: _buildLogo(),
                    ),
                  ),
                ),
                const SizedBox(height: 32),
                // App name
                AnimatedBuilder(
                  animation: _textCtrl,
                  builder: (_, __) => FadeTransition(
                    opacity: _textOpacity,
                    child: SlideTransition(
                      position: _textSlide,
                      child: Column(
                        children: [
                          ShaderMask(
                            shaderCallback: (bounds) => const LinearGradient(
                              colors: [AppColors.primary, AppColors.accent],
                            ).createShader(bounds),
                            child: const Text(
                              'JAYNES MAX TV',
                              style: TextStyle(
                                fontFamily: 'BebasNeue',
                                fontSize: 38,
                                letterSpacing: 8,
                                color: Colors.white,
                              ),
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            'BURUDANI YA TANZANIA',
                            style: TextStyle(
                              fontFamily: 'Rajdhani',
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                              letterSpacing: 5,
                              color: AppColors.muted,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
            // Version bottom
            Positioned(
              bottom: 40,
              child: AnimatedBuilder(
                animation: _textCtrl,
                builder: (_, __) => Opacity(
                  opacity: _textOpacity.value,
                  child: Text(
                    'v${AppConstants.appVersion}',
                    style: const TextStyle(
                      fontFamily: 'Rajdhani',
                      fontSize: 12,
                      color: AppColors.muted,
                      letterSpacing: 2,
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPulse(Animation<double> anim, double size, Color color) {
    return Opacity(
      opacity: anim.value,
      child: Container(
        width: size * anim.value + size * 0.5,
        height: size * anim.value + size * 0.5,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: color,
        ),
      ),
    );
  }

  Widget _buildLogo() {
    return Container(
      width: 100,
      height: 100,
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [AppColors.primary, AppColors.accent],
        ),
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: AppColors.primary.withOpacity(0.5),
            blurRadius: 30,
            spreadRadius: 5,
          ),
        ],
      ),
      child: const Icon(
        Icons.play_circle_filled_rounded,
        size: 56,
        color: Colors.white,
      ),
    );
  }
}
