// lib/screens/home_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../utils/app_theme.dart';
import '../services/channel_provider.dart';
import '../services/api_service.dart';
import 'player_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          // App Bar
          SliverAppBar(
            floating: true,
            snap: true,
            expandedHeight: 80,
            flexibleSpace: FlexibleSpaceBar(
              titlePadding: const EdgeInsets.only(left: 20, bottom: 14),
              title: ShaderMask(
                shaderCallback: (bounds) => const LinearGradient(
                  colors: [AppColors.primary, AppColors.accent],
                ).createShader(bounds),
                child: const Text(
                  'JAYNES MAX TV',
                  style: TextStyle(
                    fontFamily: 'BebasNeue',
                    fontSize: 26,
                    letterSpacing: 5,
                    color: Colors.white,
                  ),
                ),
              ),
              background: Container(color: AppColors.bg),
            ),
            actions: [
              IconButton(
                icon: const Icon(Icons.notifications_outlined, color: AppColors.text),
                onPressed: () {},
              ),
              const SizedBox(width: 8),
            ],
          ),

          // Hero Banner
          SliverToBoxAdapter(child: _HeroBanner()),

          // Live Now label
          const SliverToBoxAdapter(
            child: Padding(
              padding: EdgeInsets.fromLTRB(20, 24, 20, 14),
              child: _SectionLabel(label: 'LIVE SASA HIVI'),
            ),
          ),

          // Live channels horizontal scroll
          SliverToBoxAdapter(child: _LiveChannelsRow()),

          // Categories
          const SliverToBoxAdapter(
            child: Padding(
              padding: EdgeInsets.fromLTRB(20, 24, 20, 14),
              child: _SectionLabel(label: 'CHANNELS ZOTE'),
            ),
          ),

          // Channel Grid
          _ChannelGrid(),

          const SliverToBoxAdapter(child: SizedBox(height: 24)),
        ],
      ),
    );
  }
}

// ── Hero Banner ────────────────────────────────────
class _HeroBanner extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.fromLTRB(16, 8, 16, 0),
      height: 180,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF1A0A10), Color(0xFF0A0A1F)],
        ),
        border: Border.all(color: AppColors.divider),
      ),
      child: Stack(
        children: [
          // Background glow
          Positioned(
            left: -20,
            top: -20,
            child: Container(
              width: 200,
              height: 200,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: AppColors.primary.withOpacity(0.1),
              ),
            ),
          ),
          // Content
          Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
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
                      letterSpacing: 2,
                      color: Colors.white,
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                const Text(
                  'Tanzania Live TV',
                  style: TextStyle(
                    fontFamily: 'BebasNeue',
                    fontSize: 30,
                    letterSpacing: 4,
                    color: AppColors.text,
                  ),
                ),
                const SizedBox(height: 4),
                const Text(
                  'Channels 50+ za live, HD quality',
                  style: TextStyle(
                    fontFamily: 'Rajdhani',
                    fontSize: 13,
                    color: AppColors.muted,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

// ── Live Channels Row ──────────────────────────────
class _LiveChannelsRow extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Consumer<ChannelProvider>(
      builder: (_, prov, __) {
        if (prov.loading) {
          return SizedBox(
            height: 100,
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              scrollDirection: Axis.horizontal,
              itemCount: 6,
              separatorBuilder: (_, __) => const SizedBox(width: 12),
              itemBuilder: (_, __) => _ShimmerCard(width: 80, height: 90),
            ),
          );
        }

        final liveChannels = prov.allChannels.where((c) => c.isLive).take(10).toList();

        return SizedBox(
          height: 100,
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            scrollDirection: Axis.horizontal,
            itemCount: liveChannels.length,
            separatorBuilder: (_, __) => const SizedBox(width: 12),
            itemBuilder: (ctx, i) {
              final ch = liveChannels[i];
              return GestureDetector(
                onTap: () => _openPlayer(ctx, ch),
                child: Container(
                  width: 80,
                  decoration: BoxDecoration(
                    color: AppColors.card,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: AppColors.divider),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      if (ch.logo != null)
                        ClipRRect(
                          borderRadius: BorderRadius.circular(8),
                          child: CachedNetworkImage(
                            imageUrl: ch.logo!,
                            width: 44,
                            height: 44,
                            fit: BoxFit.cover,
                            errorWidget: (_, __, ___) => const Icon(
                              Icons.live_tv_rounded,
                              size: 30,
                              color: AppColors.primary,
                            ),
                          ),
                        )
                      else
                        const Icon(Icons.live_tv_rounded, size: 30, color: AppColors.primary),
                      const SizedBox(height: 6),
                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 4),
                        child: Text(
                          ch.name,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          textAlign: TextAlign.center,
                          style: const TextStyle(
                            fontFamily: 'Rajdhani',
                            fontSize: 10,
                            fontWeight: FontWeight.w600,
                            color: AppColors.text,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }
}

// ── Channel Grid ───────────────────────────────────
class _ChannelGrid extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Consumer<ChannelProvider>(
      builder: (_, prov, __) {
        if (prov.loading) {
          return SliverGrid(
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2,
              childAspectRatio: 1.5,
              crossAxisSpacing: 12,
              mainAxisSpacing: 12,
            ),
            delegate: SliverChildBuilderDelegate(
              (_, i) => _ShimmerCard(width: double.infinity, height: 100),
              childCount: 6,
            ),
          );
        }

        final channels = prov.channels.take(6).toList();

        return SliverPadding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          sliver: SliverGrid(
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2,
              childAspectRatio: 1.6,
              crossAxisSpacing: 12,
              mainAxisSpacing: 12,
            ),
            delegate: SliverChildBuilderDelegate(
              (ctx, i) {
                final ch = channels[i];
                return GestureDetector(
                  onTap: () => _openPlayer(ctx, ch),
                  child: _ChannelCard(channel: ch),
                );
              },
              childCount: channels.length,
            ),
          ),
        );
      },
    );
  }
}

class _ChannelCard extends StatelessWidget {
  final Channel channel;
  const _ChannelCard({required this.channel});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.divider),
      ),
      child: Stack(
        children: [
          // Content
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (channel.logo != null)
                  CachedNetworkImage(
                    imageUrl: channel.logo!,
                    height: 36,
                    fit: BoxFit.contain,
                    errorWidget: (_, __, ___) => const Icon(
                      Icons.tv_rounded,
                      color: AppColors.primary,
                      size: 28,
                    ),
                  )
                else
                  const Icon(Icons.tv_rounded, color: AppColors.primary, size: 28),
                const Spacer(),
                Text(
                  channel.name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontFamily: 'Rajdhani',
                    fontWeight: FontWeight.w700,
                    fontSize: 13,
                    color: AppColors.text,
                  ),
                ),
                Text(
                  channel.category,
                  style: const TextStyle(
                    fontFamily: 'Rajdhani',
                    fontSize: 10,
                    color: AppColors.muted,
                    letterSpacing: 1,
                  ),
                ),
              ],
            ),
          ),
          // Live badge
          if (channel.isLive)
            Positioned(
              top: 8,
              right: 8,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                decoration: BoxDecoration(
                  color: AppColors.primary,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: const Text(
                  'LIVE',
                  style: TextStyle(
                    fontFamily: 'Rajdhani',
                    fontSize: 9,
                    fontWeight: FontWeight.w700,
                    letterSpacing: 1,
                    color: Colors.white,
                  ),
                ),
              ),
            ),
          // Lock if premium
          if (!channel.isFree)
            Positioned(
              bottom: 8,
              right: 8,
              child: const Icon(Icons.lock_rounded, size: 14, color: AppColors.accent),
            ),
        ],
      ),
    );
  }
}

// ── Section Label ──────────────────────────────────
class _SectionLabel extends StatelessWidget {
  final String label;
  const _SectionLabel({required this.label});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(width: 3, height: 18, color: AppColors.primary),
        const SizedBox(width: 10),
        Text(
          label,
          style: const TextStyle(
            fontFamily: 'BebasNeue',
            fontSize: 18,
            letterSpacing: 4,
            color: AppColors.text,
          ),
        ),
      ],
    );
  }
}

// ── Shimmer Placeholder ────────────────────────────
class _ShimmerCard extends StatelessWidget {
  final double width, height;
  const _ShimmerCard({required this.width, required this.height});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: width == double.infinity ? null : width,
      height: height,
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(12),
      ),
    );
  }
}

void _openPlayer(BuildContext ctx, Channel ch) {
  Navigator.push(ctx, MaterialPageRoute(
    builder: (_) => PlayerScreen(channel: ch),
  ));
}
