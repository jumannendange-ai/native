// lib/services/channel_provider.dart
import 'package:flutter/material.dart';
import 'api_service.dart';

class ChannelProvider extends ChangeNotifier {
  List<Channel> _all = [];
  List<Channel> _filtered = [];
  bool _loading = false;
  String? _error;
  String _selectedCategory = 'ZOTE';

  List<Channel> get channels => _filtered;
  List<Channel> get allChannels => _all;
  bool get loading => _loading;
  String? get error => _error;
  String get selectedCategory => _selectedCategory;

  List<String> get categories {
    final cats = _all.map((c) => c.category).toSet().toList();
    cats.sort();
    return ['ZOTE', ...cats];
  }

  Future<void> loadChannels() async {
    _loading = true;
    _error = null;
    notifyListeners();
    try {
      _all = await ApiService().getAllChannels();
      _applyFilter();
    } catch (e) {
      _error = 'Imeshindwa kupakia channels. Angalia mtandao wako.';
    }
    _loading = false;
    notifyListeners();
  }

  void setCategory(String cat) {
    _selectedCategory = cat;
    _applyFilter();
    notifyListeners();
  }

  void search(String query) {
    if (query.isEmpty) {
      _applyFilter();
    } else {
      final q = query.toLowerCase();
      _filtered = _all.where((c) =>
        c.name.toLowerCase().contains(q) ||
        c.category.toLowerCase().contains(q)
      ).toList();
    }
    notifyListeners();
  }

  void _applyFilter() {
    if (_selectedCategory == 'ZOTE') {
      _filtered = List.from(_all);
    } else {
      _filtered = _all.where((c) => c.category == _selectedCategory).toList();
    }
  }
}
