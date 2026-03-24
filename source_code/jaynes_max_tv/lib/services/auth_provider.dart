// lib/services/auth_provider.dart
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthProvider extends ChangeNotifier {
  bool _isSubscribed = false;
  String? _plan;
  DateTime? _expiresAt;

  bool get isSubscribed => _isSubscribed;
  String? get plan => _plan;
  DateTime? get expiresAt => _expiresAt;

  Future<void> load() async {
    final prefs = await SharedPreferences.getInstance();
    _isSubscribed = prefs.getBool('subscribed') ?? false;
    _plan = prefs.getString('plan');
    final exp = prefs.getString('expires_at');
    if (exp != null) {
      _expiresAt = DateTime.tryParse(exp);
      // Check if expired
      if (_expiresAt != null && _expiresAt!.isBefore(DateTime.now())) {
        _isSubscribed = false;
        await prefs.setBool('subscribed', false);
      }
    }
    notifyListeners();
  }

  Future<void> subscribe(String plan, int days) async {
    final prefs = await SharedPreferences.getInstance();
    final exp = DateTime.now().add(Duration(days: days));
    _isSubscribed = true;
    _plan = plan;
    _expiresAt = exp;
    await prefs.setBool('subscribed', true);
    await prefs.setString('plan', plan);
    await prefs.setString('expires_at', exp.toIso8601String());
    notifyListeners();
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    _isSubscribed = false;
    _plan = null;
    _expiresAt = null;
    await prefs.clear();
    notifyListeners();
  }
}
