// lib/screens/channels_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../utils/app_theme.dart';
import '../services/channel_provider.dart';
import '../services/api_service.dart';
import 'player_screen.dart';

class ChannelsScreen extends StatefulWidget {
  const ChannelsScreen({super.key});

  @override
  State<ChannelsScreen> createState() => _ChannelsScreenState();
}

class _ChannelsScreenState extends State<ChannelsScreen> {
  final _searchCtrl = TextEditingController();
  bool _searching = false;

  @override
  void dispose() {
    _searchCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: _searching
            ? TextField(
                controller: _searchCtrl,
                autofocus: true,
                style: const TextStyle(color: AppColors.text, fontFamily: 'Rajdhani'),
                decoration: const InputDecoration(
                  hintText: 'Tafuta channel...',
                  hintStyle: TextStyle(color: AppColors.muted),
                  border: InputBorder.none,
                ),
                onChanged: (q) => context.read<ChannelProvider>().search(q),
              )
            : const Text('CHANNELS'),
        actions: [
          IconButton(
            icon: Icon(_searching ? Icons.close : Icons.search_rounded),
            color: AppColors.text,
            onPressed: () {
              setState(() => _searching = !_searching);
              if (!_searching) {
                _searchCtrl.clear();
                context.read<ChannelProvider>().search('');
              }
            },
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: Column(
        children: [
          // Category Filter
          if (!_searching) _CategoryFilter(),
          // Channels List
          Expanded(child: _ChannelList()),
        ],
      ),
    );
  }
}

// ── Category Filter Pills ──────────────────────────
class _CategoryFilter extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Consumer<ChannelProvider>(
      builder: (_, prov, __) {
        final cats = prov.categories;
        return SizedBox(
          height: 48,
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            scrollDirection: Axis.horizontal,
            itemCount: cats.length,
            separatorBuilder: (_, __) => const SizedBox(width: 8),
            itemBuilder: (_, i) {
              final cat = cats[i];
              final selected = prov.selectedCategory == cat;
              return GestureDetector(
                onTap: () => prov.setCategory(cat),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  padding: const EdgeInsets.symmetric(horizontal: 14),
                  decoration: BoxDecoration(
                    gradient: selected ? AppColors.gradient : null,
                    color: selected ? null : AppColors.card,
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(
                      color: selected ? Colors.transparent : AppColors.divider,
                    ),
                  ),
                  alignment: Alignment.center,
                  child: Text(
                    cat,
                    style: TextStyle(
                      fontFamily: 'Rajdhani',
                      fontWeight: FontWeight.w700,
                      fontSize: 12,
                      letterSpacing: 1.5,
                      color: selected ? Colors.white : AppColors.muted,
                    ),
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

// ── Channels List ──────────────────────────────────
class _ChannelList extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Consumer<ChannelProvider>(
      builder: (_, prov, __) {
        if (prov.loading) {
          return const Center(
            child: CircularProgressIndicator(color: AppColors.primary),
          );
        }

        if (prov.error != null) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.wifi_off_rounded, size: 48, color: AppColors.muted),
                const SizedBox(height: 16),
                Text(prov.error!, style: const TextStyle(color: AppColors.muted), textAlign: TextAlign.center),
                const SizedBox(height: 20),
                ElevatedButton(
                  style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary),
                  onPressed: () => prov.loadChannels(),
                  child: const Text('Jaribu Tena', style: TextStyle(fontFamily: 'Rajdhani', fontWeight: FontWeight.w700)),
                ),
              ],
            ),
          );
        }

        if (prov.channels.isEmpty) {
          return const Center(
            child: Text('Hakuna channels zilizopatikana', style: TextStyle(color: AppColors.muted)),
          );
        }

        return ListView.separated(
          padding: const EdgeInsets.symmetric(vertical: 8),
          itemCount: prov.channels.length,
          separatorBuilder: (_, __) => const Divider(height: 1, indent: 72),
          itemBuilder: (ctx, i) {
            final ch = prov.channels[i];
            return _ChannelTile(channel: ch);
          },
        );
      },
    );
  }
}

// ── Channel Tile ───────────────────────────────────
class _ChannelTile extends StatelessWidget {
  final Channel channel;
  const _ChannelTile({required this.channel});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      leading: Container(
        width: 52,
        height: 52,
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: AppColors.divider),
        ),
        child: channel.logo != null
            ? ClipRRect(
                borderRadius: BorderRadius.circular(10),
                child: CachedNetworkImage(
                  imageUrl: channel.logo!,
                  fit: BoxFit.cover,
                  errorWidget: (_, __, ___) => const Icon(Icons.tv_rounded, color: AppColors.primary, size: 24),
                ),
              )
            : const Icon(Icons.tv_rounded, color: AppColors.primary, size: 24),
      ),
      title: Row(
        children: [
          Expanded(
            child: Text(
              channel.name,
              style: const TextStyle(
                fontFamily: 'Rajdhani',
                fontWeight: FontWeight.w700,
                fontSize: 15,
                color: AppColors.text,
              ),
            ),
          ),
          if (channel.isLive)
            Container(
              margin: const EdgeInsets.only(left: 6),
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
          if (!channel.isFree)
            const Padding(
              padding: EdgeInsets.only(left: 6),
              child: Icon(Icons.lock_rounded, size: 14, color: AppColors.accent),
            ),
        ],
      ),
      subtitle: Text(
        channel.category,
        style: const TextStyle(
          fontFamily: 'Rajdhani',
          fontSize: 11,
          color: AppColors.muted,
          letterSpacing: 1,
        ),
      ),
      trailing: const Icon(Icons.play_circle_outline_rounded, color: AppColors.primary, size: 28),
      onTap: () => Navigator.push(
        context,
        MaterialPageRoute(builder: (_) => PlayerScreen(channel: channel)),
      ),
    );
  }
}
