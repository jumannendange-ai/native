// lib/main.dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'utils/app_theme.dart';
import 'screens/splash_screen.dart';
import 'screens/main_screen.dart';
import 'services/channel_provider.dart';
import 'services/auth_provider.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Lock to portrait (channels view) — landscape unlocks only in player
  await SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);

  // Full immersive status bar
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.light,
    systemNavigationBarColor: AppColors.navColor,
    systemNavigationBarIconBrightness: Brightness.light,
  ));

  runApp(const JaynesMaxTvApp());
}

class JaynesMaxTvApp extends StatelessWidget {
  const JaynesMaxTvApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ChannelProvider()),
        ChangeNotifierProvider(create: (_) => AuthProvider()),
      ],
      child: MaterialApp(
        title: 'JAYNES MAX TV',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.dark,
        home: const SplashScreen(),
        routes: {
          '/main': (_) => const MainScreen(),
        },
      ),
    );
  }
}
