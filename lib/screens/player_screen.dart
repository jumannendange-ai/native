// lib/screens/player_screen.dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:better_player/better_player.dart';
import 'package:provider/provider.dart';
import '../utils/app_theme.dart';
import '../services/api_service.dart';
import '../services/auth_provider.dart';
import 'subscribe_screen.dart';

class PlayerScreen extends StatefulWidget {
  final Channel channel;
  const PlayerScreen({super.key, required this.channel});

  @override
  State<PlayerScreen> createState() => _PlayerScreenState();
}

class _PlayerScreenState extends State<PlayerScreen> {
  BetterPlayerController? _ctrl;
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    // Allow landscape in player
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.portraitUp,
      DeviceOrientation.landscapeLeft,
      DeviceOrientation.landscapeRight,
    ]);
    _initPlayer();
  }

  Future<void> _initPlayer() async {
    final auth = context.read<AuthProvider>();
    // Check if channel requires subscription
    if (!widget.channel.isFree && !auth.isSubscribed) {
      setState(() => _loading = false);
      return;
    }

    final streamUrl = ApiService().proxyUrl(widget.channel.streamUrl);
    final dataSourceType = widget.channel.streamType == 'DASH'
        ? BetterPlayerDataSourceType.network
        : BetterPlayerDataSourceType.network;

    BetterPlayerDataSource dataSource;

    if (widget.channel.streamType == 'DASH' && widget.channel.clearkey != null) {
      final ck = widget.channel.clearkey!;
      final parts = ck['key']?.toString().split(':') ?? [];
      dataSource = BetterPlayerDataSource(
        dataSourceType,
        streamUrl,
        videoFormat: BetterPlayerVideoFormat.dash,
        drmConfiguration: parts.length == 2
            ? BetterPlayerDrmConfiguration(
                drmType: BetterPlayerDrmType.clearKey,
                clearKey: '{"keys":[{"kty":"oct","k":"${_b64(parts[1])}","kid":"${_b64(parts[0])}"}],"type":"temporary"}',
              )
            : null,
      );
    } else {
      dataSource = BetterPlayerDataSource(
        dataSourceType,
        streamUrl,
        videoFormat: BetterPlayerVideoFormat.hls,
      );
    }

    _ctrl = BetterPlayerController(
      BetterPlayerConfiguration(
        autoPlay: true,
        looping: false,
        fullScreenByDefault: false,
        allowedScreenSleep: false,
        autoDetectFullscreenDeviceOrientation: true,
        fit: BoxFit.contain,
        controlsConfiguration: BetterPlayerControlsConfiguration(
          controlBarColor: Colors.black54,
          iconsColor: AppColors.text,
          progressBarPlayedColor: AppColors.primary,
          progressBarHandleColor: AppColors.primary,
          progressBarBackgroundColor: AppColors.divider,
          loadingColor: AppColors.primary,
          enableSubtitles: false,
          enableQualities: false,
          showControlsOnInitialize: true,
        ),
        errorBuilder: (ctx, msg) => _ErrorWidget(
          message: msg ?? 'Stream haipatikani',
          onRetry: _retry,
        ),
      ),
      betterPlayerDataSource: dataSource,
    );

    _ctrl!.addEventsListener((event) {
      if (event.betterPlayerEventType == BetterPlayerEventType.initialized) {
        if (mounted) setState(() => _loading = false);
      } else if (event.betterPlayerEventType == BetterPlayerEventType.exception) {
        if (mounted) setState(() {
          _loading = false;
          _error = 'Stream imeshindwa. Jaribu tena.';
        });
      }
    });

    if (mounted) setState(() {});
  }

  void _retry() {
    setState(() {
      _loading = true;
      _error = null;
      _ctrl?.dispose();
      _ctrl = null;
    });
    _initPlayer();
  }

  // Base64 url-safe without padding
  String _b64(String hex) {
    final bytes = <int>[];
    for (var i = 0; i < hex.length; i += 2) {
      bytes.add(int.parse(hex.substring(i, i + 2), radix: 16));
    }
    final b64 = Uri.encodeComponent(
      String.fromCharCodes(bytes.map((b) => b)),
    );
    return b64.replaceAll('%', '').replaceAll('=', '');
  }

  @override
  void dispose() {
    SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
    _ctrl?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    final needsSub = !widget.channel.isFree && !auth.isSubscribed;

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_rounded, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text(
          widget.channel.name,
          style: const TextStyle(
            fontFamily: 'Rajdhani',
            fontWeight: FontWeight.w700,
            fontSize: 18,
            color: Colors.white,
          ),
        ),
        actions: [
          Container(
            margin: const EdgeInsets.only(right: 16),
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: AppColors.primary,
              borderRadius: BorderRadius.circular(4),
            ),
            child: const Text(
              'LIVE',
              style: TextStyle(
                fontFamily: 'Rajdhani',
                fontWeight: FontWeight.w700,
                fontSize: 11,
                letterSpacing: 1,
                color: Colors.white,
              ),
            ),
          ),
        ],
      ),
      body: Column(
        children: [
          // Player area
          AspectRatio(
            aspectRatio: 16 / 9,
            child: needsSub
                ? _PaywallWidget(channel: widget.channel)
                : _loading
                    ? _LoadingWidget(channelName: widget.channel.name)
                    : _error != null
                        ? _ErrorWidget(message: _error!, onRetry: _retry)
                        : _ctrl != null
                            ? BetterPlayer(controller: _ctrl!)
                            : _LoadingWidget(channelName: widget.channel.name),
          ),
          // Channel Info
          Expanded(
            child: Container(
              color: AppColors.bg,
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(
                          color: AppColors.surface,
                          borderRadius: BorderRadius.circular(6),
                          border: Border.all(color: AppColors.divider),
                        ),
                        child: Text(
                          widget.channel.category,
                          style: const TextStyle(
                            fontFamily: 'Rajdhani',
                            fontWeight: FontWeight.w600,
                            fontSize: 11,
                            letterSpacing: 2,
                            color: AppColors.muted,
                          ),
                        ),
                      ),
                      const Spacer(),
                      Icon(
                        widget.channel.isFree ? Icons.lock_open_rounded : Icons.lock_rounded,
                        size: 16,
                        color: widget.channel.isFree ? Colors.green : AppColors.accent,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        widget.channel.isFree ? 'Bure' : 'Premium',
                        style: TextStyle(
                          fontFamily: 'Rajdhani',
                          fontSize: 12,
                          color: widget.channel.isFree ? Colors.green : AppColors.accent,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Text(
                    widget.channel.name,
                    style: const TextStyle(
                      fontFamily: 'BebasNeue',
                      fontSize: 28,
                      letterSpacing: 3,
                      color: AppColors.text,
                    ),
                  ),
                  if (widget.channel.description != null) ...[
                    const SizedBox(height: 8),
                    Text(
                      widget.channel.description!,
                      style: const TextStyle(
                        fontFamily: 'Rajdhani',
                        fontSize: 13,
                        color: AppColors.muted,
                      ),
                    ),
                  ],
                  if (widget.channel.currentShow != null) ...[
                    const SizedBox(height: 20),
                    Row(
                      children: [
                        Container(
                          width: 6,
                          height: 6,
                          decoration: const BoxDecoration(
                            shape: BoxShape.circle,
                            color: AppColors.primary,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Text(
                          'Sasa hivi: ${widget.channel.currentShow}',
                          style: const TextStyle(
                            fontFamily: 'Rajdhani',
                            fontSize: 13,
                            color: AppColors.muted,
                          ),
                        ),
                      ],
                    ),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Loading Widget ─────────────────────────────────
class _LoadingWidget extends StatelessWidget {
  final String channelName;
  const _LoadingWidget({required this.channelName});

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.black,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const CircularProgressIndicator(color: AppColors.primary, strokeWidth: 2),
          const SizedBox(height: 16),
          Text(
            'Inapakia $channelName...',
            style: const TextStyle(
              fontFamily: 'Rajdhani',
              color: AppColors.muted,
              fontSize: 13,
            ),
          ),
        ],
      ),
    );
  }
}

// ── Error Widget ───────────────────────────────────
class _ErrorWidget extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;
  const _ErrorWidget({required this.message, required this.onRetry});

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.black,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline_rounded, color: AppColors.primary, size: 40),
          const SizedBox(height: 12),
          Text(message, style: const TextStyle(color: AppColors.muted, fontFamily: 'Rajdhani')),
          const SizedBox(height: 16),
          TextButton(
            onPressed: onRetry,
            child: const Text(
              'JARIBU TENA',
              style: TextStyle(color: AppColors.primary, fontFamily: 'Rajdhani', fontWeight: FontWeight.w700, letterSpacing: 2),
            ),
          ),
        ],
      ),
    );
  }
}

// ── Paywall Widget ─────────────────────────────────
class _PaywallWidget extends StatelessWidget {
  final Channel channel;
  const _PaywallWidget({required this.channel});

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.black,
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Background
          Container(
            decoration: const BoxDecoration(
              gradient: RadialGradient(
                colors: [Color(0x33E8001D), Colors.transparent],
              ),
            ),
          ),
          Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.lock_rounded, color: AppColors.accent, size: 40),
              const SizedBox(height: 12),
              const Text(
                'CHANNEL HII NI PREMIUM',
                style: TextStyle(
                  fontFamily: 'BebasNeue',
                  fontSize: 20,
                  letterSpacing: 3,
                  color: AppColors.text,
                ),
              ),
              const SizedBox(height: 6),
              const Text(
                'Jiunge ili uangalie bila kikwazo',
                style: TextStyle(fontFamily: 'Rajdhani', color: AppColors.muted, fontSize: 13),
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 12),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                ),
                onPressed: () => Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const SubscribeScreen()),
                ),
                child: const Text(
                  'JIUNGE SASA',
                  style: TextStyle(
                    fontFamily: 'Rajdhani',
                    fontWeight: FontWeight.w700,
                    fontSize: 15,
                    letterSpacing: 2,
                    color: Colors.white,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
