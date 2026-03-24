// lib/services/api_service.dart
import 'dart:convert';
import 'package:dio/dio.dart';
import '../utils/app_theme.dart';

class ApiService {
  static final ApiService _i = ApiService._();
  factory ApiService() => _i;
  ApiService._();

  final _dio = Dio(BaseOptions(
    baseUrl: AppConstants.apiBase,
    connectTimeout: const Duration(seconds: 15),
    receiveTimeout: const Duration(seconds: 30),
    headers: {
      'Content-Type': 'application/json',
      'X-App-Version': AppConstants.appVersion,
    },
  ));

  // ── App Init (channels count + update + config) ──
  Future<Map<String, dynamic>> init() async {
    final r = await _dio.get('/api/app/init');
    return r.data;
  }

  // ── All Channels ──
  Future<List<Channel>> getAllChannels() async {
    final r = await _dio.get('/api/channels/all');
    final data = r.data;
    final list = (data['channels'] as List?) ?? [];
    return list.map((e) => Channel.fromJson(e)).toList();
  }

  // ── Local Channels only ──
  Future<List<Channel>> getLocalChannels() async {
    final r = await _dio.get('/api/channels/local');
    final data = r.data;
    final list = (data['channels'] as List?) ?? [];
    return list.map((e) => Channel.fromJson(e)).toList();
  }

  // ── Update Check ──
  Future<Map<String, dynamic>> checkUpdate(String version) async {
    final r = await _dio.get('/api/update/status', queryParameters: {'version': version});
    return r.data;
  }

  // ── Remote Config ──
  Future<Map<String, dynamic>> getRemoteConfig(String version) async {
    final r = await _dio.get('/api/config/remote', queryParameters: {'version': version});
    return r.data;
  }

  // ── Proxy stream URL ──
  String proxyUrl(String url) =>
      '${AppConstants.apiBase}/api/stream/proxy?url=${Uri.encodeComponent(url)}';
}

// ── Channel Model ──────────────────────────────────
class Channel {
  final String id;
  final String name;
  final String category;
  final String streamUrl;
  final String? logo;
  final String streamType;   // HLS / DASH
  final bool isFree;
  final bool isLive;
  final Map<String, dynamic>? clearkey;
  final String? description;
  final String? currentShow;

  Channel({
    required this.id,
    required this.name,
    required this.category,
    required this.streamUrl,
    this.logo,
    required this.streamType,
    required this.isFree,
    required this.isLive,
    this.clearkey,
    this.description,
    this.currentShow,
  });

  factory Channel.fromJson(Map<String, dynamic> j) => Channel(
    id:          j['id']?.toString() ?? '',
    name:        j['name'] ?? '',
    category:    j['category'] ?? '',
    streamUrl:   j['stream_url'] ?? '',
    logo:        j['logo'],
    streamType:  j['stream_type'] ?? 'HLS',
    isFree:      j['is_free'] == true,
    isLive:      j['is_live'] != false,
    clearkey:    j['clearkey'] as Map<String, dynamic>?,
    description: j['description'],
    currentShow: j['current_show'],
  );
}
