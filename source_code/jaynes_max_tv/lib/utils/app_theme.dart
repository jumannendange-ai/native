// lib/utils/app_theme.dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class AppColors {
  static const primary    = Color(0xFFE8001D);
  static const accent     = Color(0xFFFF6B00);
  static const bg         = Color(0xFF0A0A0F);
  static const surface    = Color(0xFF111118);
  static const card       = Color(0xFF18181F);
  static const text       = Color(0xFFF0F0F8);
  static const muted      = Color(0xFF888899);
  static const navColor   = Color(0xFF111118);
  static const divider    = Color(0xFF2A2A3A);
  
  static const gradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [primary, accent],
  );
}

class AppTheme {
  static ThemeData get dark => ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    scaffoldBackgroundColor: AppColors.bg,
    colorScheme: const ColorScheme.dark(
      primary: AppColors.primary,
      secondary: AppColors.accent,
      surface: AppColors.surface,
      onSurface: AppColors.text,
    ),
    fontFamily: 'Rajdhani',
    appBarTheme: const AppBarTheme(
      backgroundColor: AppColors.bg,
      elevation: 0,
      scrolledUnderElevation: 0,
      systemOverlayStyle: SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        statusBarIconBrightness: Brightness.light,
        systemNavigationBarColor: AppColors.navColor,
      ),
      titleTextStyle: TextStyle(
        fontFamily: 'BebasNeue',
        fontSize: 24,
        letterSpacing: 4,
        color: AppColors.text,
      ),
    ),
    bottomNavigationBarTheme: const BottomNavigationBarThemeData(
      backgroundColor: AppColors.navColor,
      selectedItemColor: AppColors.primary,
      unselectedItemColor: AppColors.muted,
      type: BottomNavigationBarType.fixed,
      elevation: 0,
    ),
    cardTheme: CardTheme(
      color: AppColors.card,
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
    ),
    dividerTheme: const DividerThemeData(
      color: AppColors.divider,
      thickness: 1,
    ),
    textTheme: const TextTheme(
      displayLarge: TextStyle(fontFamily: 'BebasNeue', color: AppColors.text, letterSpacing: 4),
      displayMedium: TextStyle(fontFamily: 'BebasNeue', color: AppColors.text, letterSpacing: 3),
      titleLarge: TextStyle(fontFamily: 'Rajdhani', fontWeight: FontWeight.w700, color: AppColors.text, fontSize: 20),
      titleMedium: TextStyle(fontFamily: 'Rajdhani', fontWeight: FontWeight.w600, color: AppColors.text, fontSize: 16),
      bodyLarge: TextStyle(fontFamily: 'Rajdhani', color: AppColors.text, fontSize: 15),
      bodyMedium: TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted, fontSize: 13),
      labelSmall: TextStyle(fontFamily: 'Rajdhani', fontWeight: FontWeight.w600, color: AppColors.muted, letterSpacing: 2),
    ),
  );
}

class AppConstants {
  static const apiBase = 'https://jaynes-max-tv-api.onrender.com';
  static const appVersion = '1.0.0';
  
  // Subscription plans
  static const plans = [
    {'id': 'wiki',    'label': 'Wiki',    'days': 7,   'price': 3000,  'badge': null},
    {'id': 'mwezi',   'label': 'Mwezi',   'days': 30,  'price': 5000,  'badge': null},
    {'id': 'miezi3',  'label': 'Miezi 3', 'days': 90,  'price': 12000, 'badge': 'POPULAR'},
    {'id': 'miezi6',  'label': 'Miezi 6', 'days': 180, 'price': 20000, 'badge': null},
    {'id': 'mwaka',   'label': 'Mwaka',   'days': 365, 'price': 35000, 'badge': 'BEST VALUE'},
  ];
}
