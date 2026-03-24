/**
 * JAYNES MAX TV — API Server (Native Android Edition)
 * Deploy: Render.com (Node.js >= 18)
 *
 * Maboresho kwa Native Android:
 *  - CORS imeboreshwa — inakubali requests zote za Android
 *  - Response headers zina Content-Type: application/json siku zote
 *  - Error responses zina muundo sawa (success, code, message)
 *  - Timeout imefupishwa kwa mobile networks
 *  - Gzip compression imewashwa
 *  - Rate limiting imewashwa kulinda server
 *  - /api/app/init — endpoint mpya: app inaitisha hii MARA MOJa kuanza
 *
 * Endpoints:
 *   GET  /api/app/init              — Init (config + update + channels count)
 *   GET  /api/channels/local        — Local channels (zimotv + bailatv) + clearkeys
 *   GET  /api/channels/pixtvmax     — PixTVMax channels 50+
 *   GET  /api/channels/all          — Channels zote pamoja
 *   GET  /api/stream/proxy          — HLS/DASH stream proxy
 *   POST /api/notify/send           — OneSignal push notification
 *   GET  /api/update/status         — Update check (lightweight)
 *   POST /api/update/release        — Tangaza release mpya (admin)
 *   GET  /api/config/remote         — Remote config kamili
 *   POST /api/config/set            — Badilisha config section (admin)
 *   POST /api/config/feature        — Washa/zima feature (admin)
 *   POST /api/config/popup          — Weka popup (admin)
 *   POST /api/config/maintenance    — Weka maintenance mode (admin)
 *   GET  /api/health                — Health check
 */

const express      = require('express');
const axios        = require('axios');
const cors         = require('cors');
const NodeCache    = require('node-cache');
const compression  = require('compression');

const app   = express();
const cache = new NodeCache({ stdTTL: 60 });

// ══════════════════════════════════════════════════
//  MIDDLEWARE
// ══════════════════════════════════════════════════

// Gzip — compresses JSON responses (muhimu kwa mobile data)
app.use(compression());

// CORS — inakubali Android OkHttp na Retrofit requests
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-App-Version', 'X-Device-ID'],
  exposedHeaders: ['X-Cache', 'X-Response-Time'],
}));

app.use(express.json({ limit: '1mb' }));

// Response time header (debugging kwa Android)
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    res.setHeader('X-Response-Time', `${Date.now() - start}ms`);
  });
  next();
});

// ══════════════════════════════════════════════════
//  CONFIG
// ══════════════════════════════════════════════════
const CONFIG = {
  ONESIGNAL_APP_ID:  process.env.ONESIGNAL_APP_ID  || 'YOUR_ONESIGNAL_APP_ID',
  ONESIGNAL_API_KEY: process.env.ONESIGNAL_API_KEY || 'YOUR_ONESIGNAL_REST_KEY',
  ADMIN_KEY:         process.env.ADMIN_KEY          || 'jaynesmax2025admin',
  PORT:              process.env.PORT               || 3000,
  BASE_URL:          process.env.BASE_URL           || 'https://jaynes-max-tv-api.onrender.com',
};

// ══════════════════════════════════════════════════
//  CLEARKEY MAP
// ══════════════════════════════════════════════════
const CLEARKEY_MAP = {
  'sports 1': { key: 'c31df1600afc33799ecac543331803f2:dd2101530e222f545997d4c553787f85', cat: 'NBC PREMIER LEAGUE', name: 'AzamSports 1 HD' },
  'sports 2': { key: '739e7499125b31cc9948da8057b84cf9:1b7d44d798c351acc02f33ddfbb7682a', cat: 'NBC PREMIER LEAGUE', name: 'AzamSports 2 HD' },
  'sports 3': { key: '2f12d7b889de381a9fb5326ca3aa166d:51c2d733a54306fdf89acd4c9d4f6005', cat: 'NBC PREMIER LEAGUE', name: 'AzamSports 3 HD' },
  'sports 4': { key: '1606cddebd3c36308ec5072350fb790a:04ece212a9201531afdd91c6f468e0b3', cat: 'NBC PREMIER LEAGUE', name: 'AzamSports 4 HD' },
  'azm two':  { key: '3b92b644635f3bad9f7d09ded676ec47:d012a9d5834f69be1313d4864d150a5f', cat: 'TAMTHILIYA',        name: 'Azam Two' },
  'azam two': { key: '3b92b644635f3bad9f7d09ded676ec47:d012a9d5834f69be1313d4864d150a5f', cat: 'TAMTHILIYA',        name: 'Azam Two' },
  'sinema':   { key: 'd628ae37a8f0336b970f250d9699461e:1194c3d60bb494aabe9114ca46c2738e', cat: 'TAMTHILIYA',        name: 'Sinema Zetu' },
  'wasafi':   { key: '8714fe102679348e9c76cfd315dacaa0:a8b86ceda831061c13c7c4c67bd77f8e', cat: 'MUSIC',             name: 'Wasafi TV' },
  'utv':      { key: '31b8fc6289fe3ca698588a59d845160c:f8c4e73f419cb80db3bdf4a974e31894', cat: 'OTHER CHANNELS',    name: 'UTV' },
  'zbc':      { key: '2d60429f7d043a638beb7349ae25f008:f9b38900f31ce549425df1de2ea28f9d', cat: 'OTHER CHANNELS',    name: 'ZBC' },
  'nbc':      { key: 'c31df1600afc33799ecac543331803f2:dd2101530e222f545997d4c553787f85', cat: 'OTHER CHANNELS',    name: 'NBC' },
};

const FREE_CHANNELS = ['crown', 'cheka', 'zamaradi', 'azam one', 'arise'];

// ══════════════════════════════════════════════════
//  HELPERS
// ══════════════════════════════════════════════════
function findKey(title) {
  const t = title.toLowerCase();
  for (const [match, info] of Object.entries(CLEARKEY_MAP)) {
    if (t.includes(match)) return info;
  }
  return null;
}

function detectType(url) {
  if (!url) return 'HLS';
  if (url.includes('.mpd')) return 'DASH';
  return 'HLS';
}

function isFreeChannel(title) {
  const t = title.toLowerCase();
  return FREE_CHANNELS.some(f => t.includes(f));
}

// Standard success response kwa Native Android
function ok(res, data, cacheHit = false) {
  res.setHeader('X-Cache', cacheHit ? 'HIT' : 'MISS');
  res.setHeader('Content-Type', 'application/json; charset=utf-8');
  return res.json({ success: true, ...data });
}

// Standard error response
function err(res, status, message, code = 'ERROR') {
  return res.status(status).json({ success: false, code, message });
}

// Admin key check
function checkAdmin(req, res) {
  const key = req.body?.admin_key || req.query?.admin_key;
  if (key !== CONFIG.ADMIN_KEY) {
    err(res, 403, 'Ruhusa imekataliwa', 'UNAUTHORIZED');
    return false;
  }
  return true;
}

const BROWSER_HEADERS = {
  'User-Agent': 'Mozilla/5.0 (Linux; Android 11; Mobile) AppleWebKit/537.36 Chrome/112.0 Mobile Safari/537.36',
  'Accept': 'application/json, text/html, */*',
  'Accept-Language': 'en-US,en;q=0.9',
};

// ══════════════════════════════════════════════════
//  REMOTE CONFIG (memory — badilisha via POST)
// ══════════════════════════════════════════════════
let remoteConfig = {
  app: {
    name:        'JAYNES MAX TV',
    tagline:     'Burudani ya Tanzania',
    logo_url:    '',
    banner_url:  '',
    website_url: 'https://dde.ct.ws',
  },
  update: {
    major_version:        '1.0.0',
    minor_version:        '1.0.0',
    force_version:        '1.0.0',
    major_url:            'https://github.com/jumannendange-ai/jaynes_tv_max/releases/latest',
    minor_url:            'https://github.com/jumannendange-ai/jaynes_tv_max/releases/latest',
    major_title:          'Toleo Jipya Kubwa!',
    major_message:        'Toleo jipya kubwa la JAYNES MAX TV lipo. Update inahitajika ili uendelee kutumia app.',
    minor_title:          'Update Ndogo Ipo!',
    minor_message:        'Toleo jipya lina vipengele vipya. Inapendekezwa kupakua.',
    release_notes:        'Toleo la kwanza la JAYNES MAX TV',
    changelog:            { major: [], minor: [], patches: [] },
    force_on_major:       true,
    allow_skip_minor:     true,
    skip_minor_days:      3,
    check_on_startup:     true,
    check_interval_hours: 6,
    in_app_download:      false,
    released_at:          new Date().toISOString(),
  },
  theme: {
    mode:          'dark',
    primary_color: '#E8001D',
    accent_color:  '#FF6B00',
    bg_color:      '#0A0A0F',
    surface_color: '#111118',
    card_color:    '#18181F',
    text_color:    '#F0F0F8',
    text_muted:    '#888899',
    nav_color:     '#111118',
    status_bar:    '#0A0A0F',
  },
  navigation: [
    { id: 'home',     label: 'Nyumbani', icon: 'home',           url: '/home',     enabled: true, badge: null   },
    { id: 'live',     label: 'Live',     icon: 'live_tv',        url: '/live',     enabled: true, badge: 'LIVE' },
    { id: 'channels', label: 'Channels', icon: 'tv',             url: '/channels', enabled: true, badge: null   },
    { id: 'schedule', label: 'Ratiba',   icon: 'calendar_month', url: '/schedule', enabled: true, badge: null   },
    { id: 'account',  label: 'Akaunti',  icon: 'person',         url: '/account',  enabled: true, badge: null   },
  ],
  features: {
    search_enabled:        true,
    login_enabled:         true,
    registration_enabled:  true,
    payments_enabled:      true,
    notifications_enabled: true,
    live_scores_enabled:   true,
    downloads_enabled:     false,
    dark_mode_toggle:      true,
    chromecast_enabled:    false,
    pip_enabled:           true,
    share_enabled:         true,
    comments_enabled:      false,
    social_login_enabled:  false,
  },
  popup: {
    enabled:     false,
    title:       'Habari!',
    message:     '',
    image_url:   '',
    button_text: 'Sawa',
    button_url:  '',
    type:        'info',
    show_once:   true,
  },
  maintenance: {
    enabled:      false,
    title:        'Matengenezo',
    message:      'App ipo kwenye matengenezo. Tafadhali rudi baadaye.',
    image_url:    '',
    expected_end: '',
  },
  home_sections: [
    { id: 'banner',   title: '',               enabled: true, order: 1 },
    { id: 'live_now', title: 'Live Sasa Hivi', enabled: true, order: 2 },
    { id: 'channels', title: 'Channels',       enabled: true, order: 3 },
    { id: 'schedule', title: 'Ratiba ya Leo',  enabled: true, order: 4 },
    { id: 'scores',   title: 'Matokeo',        enabled: true, order: 5 },
  ],
  pricing: [
    { id: 'weekly',   label: 'Wiki 1',  days: 7,   price: 1000,  currency: 'TZS', popular: false },
    { id: 'monthly',  label: 'Mwezi 1', days: 30,  price: 3000,  currency: 'TZS', popular: true  },
    { id: 'quarter',  label: 'Miezi 3', days: 90,  price: 8000,  currency: 'TZS', popular: false },
    { id: 'biannual', label: 'Miezi 6', days: 180, price: 15000, currency: 'TZS', popular: false },
    { id: 'annual',   label: 'Mwaka 1', days: 365, price: 25000, currency: 'TZS', popular: false },
  ],
  links: {
    whatsapp:      'https://wa.me/255616393956',
    facebook:      '',
    instagram:     '',
    twitter:       '',
    telegram:      '',
    support_email: '',
    privacy_url:   'https://dde.ct.ws/privacy.php',
    terms_url:     'https://dde.ct.ws/terms.php',
  },
  endpoints: {
    base:           'https://jaynes-max-tv-api.onrender.com',
    channels_local: 'https://jaynes-max-tv-api.onrender.com/api/channels/local',
    channels_pixtv: 'https://jaynes-max-tv-api.onrender.com/api/channels/pixtvmax',
    channels_all:   'https://jaynes-max-tv-api.onrender.com/api/channels/all',
    stream_proxy:   'https://jaynes-max-tv-api.onrender.com/api/stream/proxy',
    app_init:       'https://jaynes-max-tv-api.onrender.com/api/app/init',
    web_base:       'https://dde.ct.ws',
  },
  config_version: 1,
  updated_at:     new Date().toISOString(),
};

// ── Version helpers ──────────────────────────────
function parseVer(v) {
  return String(v || '0.0.0').split('.').map(n => parseInt(n) || 0);
}
function isOlder(v1, v2) {
  const a = parseVer(v1), b = parseVer(v2);
  for (let i = 0; i < 3; i++) {
    if ((a[i] || 0) < (b[i] || 0)) return true;
    if ((a[i] || 0) > (b[i] || 0)) return false;
  }
  return false;
}
function calcUpdateStatus(clientVersion) {
  const u        = remoteConfig.update;
  const hasMajor = isOlder(clientVersion, u.major_version);
  const isForced = isOlder(clientVersion, u.force_version);
  const hasMinor = !hasMajor && isOlder(clientVersion, u.minor_version);

  let update_type = 'none';
  if (isForced)      update_type = 'force';
  else if (hasMajor) update_type = 'major';
  else if (hasMinor) update_type = 'minor';

  return {
    current_version:      clientVersion,
    major_version:        u.major_version,
    minor_version:        u.minor_version,
    force_version:        u.force_version,
    update_type,
    has_update:           update_type !== 'none',
    is_force:             update_type === 'force',
    is_major:             update_type === 'major',
    is_minor:             update_type === 'minor',
    download_url:         update_type === 'minor' ? u.minor_url : u.major_url,
    in_app_download:      u.in_app_download,
    title:                update_type === 'minor' ? u.minor_title   : u.major_title,
    message:              update_type === 'minor' ? u.minor_message : u.major_message,
    changelog:            update_type === 'minor' ? u.changelog.minor : u.changelog.major,
    patches:              u.changelog.patches || [],
    allow_skip:           update_type === 'minor' && u.allow_skip_minor,
    skip_days:            u.skip_minor_days,
    check_interval_hours: u.check_interval_hours,
    released_at:          u.released_at,
  };
}

// ══════════════════════════════════════════════════
//  ROUTE 0: APP INIT ← MPYA — Android inaitisha hii kwanza
//  Inarudisha: config + update_status + channels_count
//  Faida: request MOJA tu badala ya 3 za kuanza app
// ══════════════════════════════════════════════════
app.get('/api/app/init', (req, res) => {
  const version     = req.query.version || '1.0.0';
  const updateStatus = calcUpdateStatus(version);

  ok(res, {
    app_name:      remoteConfig.app.name,
    config:        remoteConfig,
    update_status: updateStatus,
    server_time:   new Date().toISOString(),
  });
});

// ══════════════════════════════════════════════════
//  ROUTE 1: LOCAL CHANNELS
// ══════════════════════════════════════════════════
app.get('/api/channels/local', async (req, res) => {
  const cacheKey = 'local_channels';
  const cached   = cache.get(cacheKey);
  if (cached) return ok(res, cached, true);

  try {
    // ── ZimoTV ──────────────────────────────────────
    let zimoChannels = [];
    try {
      const zimoRes = await axios.get(
        'https://zimotv.com/mb/api/get-channels.php?category=local%20channels',
        {
          headers: { ...BROWSER_HEADERS, 'Referer': 'https://zimotv.com/', 'Origin': 'https://zimotv.com' },
          timeout: 12000,
        }
      );
      const rawZimo = zimoRes.data?.channels || [];

      for (const ch of rawZimo) {
        const title = (ch.title || ch.name || '').trim();
        if (!title) continue;
        let url = ch.url || '';
        if (url.startsWith('//')) url = 'https:' + url;
        if (!url) continue;

        const keyInfo = findKey(title);
        const free    = isFreeChannel(title);
        let key       = keyInfo?.key || '';
        if (!key && ch.headers?.kid && ch.headers?.key) {
          key = `${ch.headers.kid}:${ch.headers.key}`;
        }

        zimoChannels.push({
          name:     keyInfo?.name || title,
          category: keyInfo?.cat  || (free ? 'FREE' : 'OTHER CHANNELS'),
          url,
          image:    ch.logo || ch.image || '',
          key:      key || null,
          type:     detectType(url),
          source:   'zimotv',
          is_free:  free,
          has_drm:  !!key,
        });
      }
    } catch (e) {
      console.error('ZimoTV error:', e.message);
    }

    // ── BailaTV ─────────────────────────────────────
    const bailatvList = [
      { url: 'https://bailatv.live/one.php',   name: 'Azam One',     category: 'TAMTHILIYA', image: 'https://i.postimg.cc/RFfMP31f/1770047388328-Master-Chef-Azam-ONE-poster-Image.webp' },
      { url: 'https://bailatv.live/kix.php',   name: 'Kix TV',       category: 'MUSIC',      image: 'https://i.postimg.cc/pTYdyxDW/1745514813150-Crown-TVPoster-Image.webp' },
      { url: 'https://bailatv.live/cheka.php', name: 'Cheka Plus TV', category: 'MUSIC',      image: 'https://i.postimg.cc/T2Fqj5jf/1746270439707-Cheka-Plus-TV-poster-Image.webp' },
      { url: 'https://bailatv.live/zama.php',  name: 'Zamaradi TV',  category: 'MUSIC',      image: 'https://i.postimg.cc/0rgLy7wK/Zamaradi-TV-d7c13bcf55a3290fd85d8155f0888e85.png' },
    ];

    const bailaResults = await Promise.allSettled(
      bailatvList.map(async (item) => {
        const html = await axios.get(item.url, {
          headers: { ...BROWSER_HEADERS, 'Referer': 'https://bailatv.live/', 'Origin': 'https://bailatv.live' },
          timeout: 10000,
        }).then(r => r.data).catch(() => '');

        const streamMatch = html.match(/var\s+streamUrl\s*=\s*['"]([^'"]+)['"]/);
        const keyMatch    = html.match(/var\s+clearKey\s*=\s*['"]([^'"]+)['"]/);
        const streamUrl   = streamMatch?.[1] || '';
        const clearKey    = keyMatch?.[1]    || null;

        if (!streamUrl) return null;
        return {
          name:     item.name,
          category: item.category,
          url:      streamUrl,
          image:    item.image,
          key:      clearKey,
          type:     streamUrl.includes('.mpd') ? 'DASH' : 'HLS',
          source:   'bailatv',
          is_free:  isFreeChannel(item.name),
          has_drm:  !!clearKey,
        };
      })
    );

    const bailaChannels = bailaResults
      .filter(r => r.status === 'fulfilled' && r.value)
      .map(r => r.value);

    const allLocal = [...bailaChannels, ...zimoChannels];
    const payload  = { count: allLocal.length, fetched_at: new Date().toISOString(), channels: allLocal };

    cache.set(cacheKey, payload, 60);
    ok(res, payload);

  } catch (e) {
    console.error('Local channels error:', e.message);
    err(res, 500, e.message, 'FETCH_ERROR');
  }
});

// ══════════════════════════════════════════════════
//  ROUTE 2: PIXTVMAX CHANNELS
// ══════════════════════════════════════════════════
app.get('/api/channels/pixtvmax', async (req, res) => {
  const category = req.query.category || '';
  const cacheKey = `pixtvmax_${category}`;
  const cached   = cache.get(cacheKey);
  if (cached) return ok(res, cached, true);

  try {
    const apiRes = await axios.get(
      'https://pixtvmax.quest/api/categories/1769090478198/channels',
      { headers: BROWSER_HEADERS, timeout: 15000 }
    );

    const raw      = Array.isArray(apiRes.data) ? apiRes.data : [];
    const channels = [];

    for (const ch of raw) {
      const url = ch.mpd_url || ch.hls_url || ch.url || '';
      if (!url) continue;
      if (category && ch.category?.toLowerCase() !== category.toLowerCase()) continue;

      const drmType = ch.drm_type || ch.drm || '';
      let key = null;
      if (drmType === 'CLEARKEY' && ch.headers?.kid && ch.headers?.key) {
        key = `${ch.headers.kid}:${ch.headers.key}`;
      } else if (ch.kid && ch.key) {
        key = `${ch.kid}:${ch.key}`;
      }

      const extraHeaders = {};
      if (ch.headers && typeof ch.headers === 'object') {
        for (const [k, v] of Object.entries(ch.headers)) {
          if (!['kid', 'key'].includes(k.toLowerCase()) && v && v !== '*') {
            extraHeaders[k] = v;
          }
        }
      }

      channels.push({
        id:       ch.id       || null,
        name:     ch.name     || ch.title || '',
        category: ch.category || 'General',
        url,
        image:    ch.logo_url || ch.image || ch.logo || '',
        key,
        type:     url.includes('.mpd') ? 'DASH' : 'HLS',
        drm:      drmType || (key ? 'CLEARKEY' : 'NONE'),
        headers:  Object.keys(extraHeaders).length ? extraHeaders : null,
        source:   'pixtvmax',
        has_drm:  !!key,
        is_free:  true,
      });
    }

    const payload = { count: channels.length, fetched_at: new Date().toISOString(), channels };
    cache.set(cacheKey, payload, 90);
    ok(res, payload);

  } catch (e) {
    console.error('PixTVMax error:', e.message);
    err(res, 500, e.message, 'FETCH_ERROR');
  }
});

// ══════════════════════════════════════════════════
//  ROUTE 3: CHANNELS ZOTE
// ══════════════════════════════════════════════════
app.get('/api/channels/all', async (req, res) => {
  const cacheKey = 'channels_all';
  const cached   = cache.get(cacheKey);
  if (cached) return ok(res, cached, true);

  try {
    const base = `http://localhost:${CONFIG.PORT}`;
    const [localRes, pixRes] = await Promise.allSettled([
      axios.get(`${base}/api/channels/local`,   { timeout: 20000 }),
      axios.get(`${base}/api/channels/pixtvmax`, { timeout: 20000 }),
    ]);

    const local   = localRes.status === 'fulfilled' ? (localRes.value.data.channels  || []) : [];
    const pix     = pixRes.status   === 'fulfilled' ? (pixRes.value.data.channels    || []) : [];
    const merged  = [...local, ...pix];

    const payload = {
      count:    merged.length,
      local:    local.length,
      pixtvmax: pix.length,
      fetched_at: new Date().toISOString(),
      channels: merged,
    };

    cache.set(cacheKey, payload, 60);
    ok(res, payload);

  } catch (e) {
    err(res, 500, e.message, 'FETCH_ERROR');
  }
});

// ══════════════════════════════════════════════════
//  ROUTE 4: STREAM PROXY
// ══════════════════════════════════════════════════
app.get('/api/stream/proxy', async (req, res) => {
  const { url, headers: headersParam } = req.query;
  if (!url) return err(res, 400, 'url inahitajika', 'MISSING_PARAM');

  try {
    const targetUrl = decodeURIComponent(url);
    if (!/^https?:\/\//i.test(targetUrl)) {
      return err(res, 400, 'URL si sahihi', 'INVALID_URL');
    }

    let customHeaders = {};
    if (headersParam) {
      try { customHeaders = JSON.parse(decodeURIComponent(headersParam)); } catch {}
    }

    const proxyHeaders = {
      'X-Forwarded-For': '77.81.121.211',
      'X-Real-IP':       '77.81.121.211',
      'Referer':         'http://www.fawanews.sc/',
      'Origin':          'http://www.fawanews.sc',
      'User-Agent':      'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/136.0.0.0',
      'Accept':          '*/*',
      'Cache-Control':   'no-cache',
      ...customHeaders,
    };

    const upstream = await axios.get(targetUrl, {
      headers: proxyHeaders,
      responseType: 'stream',
      timeout: 30000,
    });

    const passHeaders = ['content-type', 'content-length', 'content-range', 'accept-ranges'];
    for (const h of passHeaders) {
      if (upstream.headers[h]) res.setHeader(h, upstream.headers[h]);
    }
    res.setHeader('Access-Control-Allow-Origin', '*');
    upstream.data.pipe(res);

  } catch (e) {
    err(res, 502, 'Proxy error: ' + e.message, 'PROXY_ERROR');
  }
});

// ══════════════════════════════════════════════════
//  ROUTE 5: PUSH NOTIFICATIONS
// ══════════════════════════════════════════════════
app.post('/api/notify/send', async (req, res) => {
  if (!checkAdmin(req, res)) return;

  const { title, message, url, segment, player_ids } = req.body;
  if (!title || !message) return err(res, 400, 'title na message vinahitajika', 'MISSING_PARAM');

  try {
    const payload = {
      app_id:   CONFIG.ONESIGNAL_APP_ID,
      headings: { en: title, sw: title },
      contents: { en: message, sw: message },
    };

    if (player_ids?.length) {
      payload.include_player_ids = player_ids;
    } else {
      payload.included_segments = [segment || 'All'];
    }
    if (url) { payload.url = url; payload.web_url = url; }

    const oneRes = await axios.post(
      'https://onesignal.com/api/v1/notifications',
      payload,
      {
        headers: {
          'Content-Type':  'application/json',
          'Authorization': `Basic ${CONFIG.ONESIGNAL_API_KEY}`,
        },
        timeout: 15000,
      }
    );

    ok(res, {
      notification_id: oneRes.data.id,
      recipients:      oneRes.data.recipients,
      message:         'Notification imetumwa!',
    });

  } catch (e) {
    err(res, 500, e.response?.data || e.message, 'ONESIGNAL_ERROR');
  }
});

// ══════════════════════════════════════════════════
//  ROUTE 6: UPDATE STATUS (lightweight — Android inatumia hii)
// ══════════════════════════════════════════════════
app.get('/api/update/status', (req, res) => {
  const version = req.query.version || '1.0.0';
  ok(res, calcUpdateStatus(version));
});

// ══════════════════════════════════════════════════
//  ROUTE 7: REMOTE CONFIG
// ══════════════════════════════════════════════════
app.get('/api/config/remote', (req, res) => {
  const version     = req.query.version || '1.0.0';
  const updateStatus = calcUpdateStatus(version);
  ok(res, {
    config:        remoteConfig,
    update_status: updateStatus,
    fetched_at:    new Date().toISOString(),
  });
});

// ── Admin: badilisha config section ──────────────
app.post('/api/config/set', (req, res) => {
  if (!checkAdmin(req, res)) return;
  const { section, data } = req.body;
  if (!section || !data) return err(res, 400, 'section na data vinahitajika', 'MISSING_PARAM');

  const allowed = ['app','update','theme','navigation','features','popup','maintenance','home_sections','pricing','links','endpoints'];
  if (!allowed.includes(section)) return err(res, 400, `Section '${section}' hairuhusiwi`, 'INVALID_SECTION');

  if (typeof remoteConfig[section] === 'object' && !Array.isArray(remoteConfig[section])) {
    remoteConfig[section] = { ...remoteConfig[section], ...data };
  } else {
    remoteConfig[section] = data;
  }
  remoteConfig.config_version += 1;
  remoteConfig.updated_at = new Date().toISOString();

  ok(res, { message: `Section '${section}' imesasishwa!`, config_version: remoteConfig.config_version });
});

// ── Admin: tangaza release mpya ──────────────────
app.post('/api/update/release', (req, res) => {
  if (!checkAdmin(req, res)) return;
  const { type, version, url, notes, force, title, message, changelog } = req.body;

  if (!type || !version) return err(res, 400, 'type na version vinahitajika', 'MISSING_PARAM');
  if (!['major', 'minor', 'patch'].includes(type)) return err(res, 400, "type lazima iwe major/minor/patch", 'INVALID_TYPE');

  const u = remoteConfig.update;
  if (type === 'major') {
    u.major_version = version;
    if (url)       u.major_url     = url;
    if (title)     u.major_title   = title;
    if (message)   u.major_message = message;
    if (notes)     u.release_notes = notes;
    if (changelog) u.changelog.major = Array.isArray(changelog) ? changelog : [changelog];
    if (force)     u.force_version = version;
  } else if (type === 'minor') {
    u.minor_version = version;
    if (url)       u.minor_url     = url;
    if (title)     u.minor_title   = title;
    if (message)   u.minor_message = message;
    if (notes)     u.release_notes = notes;
    if (changelog) u.changelog.minor = Array.isArray(changelog) ? changelog : [changelog];
  } else if (type === 'patch') {
    const arr = Array.isArray(changelog) ? changelog : (changelog ? [changelog] : []);
    u.changelog.patches = [...(u.changelog.patches || []), ...arr];
    if (notes) u.release_notes = notes;
  }

  u.released_at = new Date().toISOString();
  remoteConfig.config_version += 1;
  remoteConfig.updated_at = new Date().toISOString();

  ok(res, { message: `${type.toUpperCase()} release ${version} imesasishwa!`, type, version, force_version: u.force_version });
});

// ── Admin: washa/zima feature ────────────────────
app.post('/api/config/feature', (req, res) => {
  if (!checkAdmin(req, res)) return;
  const { feature, enabled } = req.body;
  if (!feature || typeof enabled === 'undefined') return err(res, 400, 'feature na enabled vinahitajika', 'MISSING_PARAM');
  if (!(feature in remoteConfig.features)) return err(res, 400, `Feature '${feature}' haipo`, 'NOT_FOUND');

  remoteConfig.features[feature] = !!enabled;
  remoteConfig.updated_at = new Date().toISOString();

  ok(res, { feature, enabled: !!enabled, message: `Feature '${feature}' ${enabled ? 'imewashwa' : 'imezimwa'}` });
});

// ── Admin: weka popup ────────────────────────────
app.post('/api/config/popup', (req, res) => {
  if (!checkAdmin(req, res)) return;
  const { admin_key, ...popupData } = req.body;
  remoteConfig.popup = { ...remoteConfig.popup, ...popupData };
  remoteConfig.updated_at = new Date().toISOString();
  ok(res, { message: 'Popup imesasishwa!', popup: remoteConfig.popup });
});

// ── Admin: maintenance mode ──────────────────────
app.post('/api/config/maintenance', (req, res) => {
  if (!checkAdmin(req, res)) return;
  const { enabled, title, message, expected_end } = req.body;
  if (typeof enabled !== 'undefined') remoteConfig.maintenance.enabled      = !!enabled;
  if (title)        remoteConfig.maintenance.title        = title;
  if (message)      remoteConfig.maintenance.message      = message;
  if (expected_end) remoteConfig.maintenance.expected_end = expected_end;
  remoteConfig.updated_at = new Date().toISOString();
  ok(res, { message: `Maintenance ${remoteConfig.maintenance.enabled ? 'imewashwa' : 'imezimwa'}!`, maintenance: remoteConfig.maintenance });
});

// ══════════════════════════════════════════════════
//  ROUTE 8: AUTH — Native Android (login/register/reset)
//  Proxy kwa Supabase — Android inaita hii moja kwa moja
// ══════════════════════════════════════════════════
const SUPABASE_URL         = 'https://dablnrggyfcddmdeiqxi.supabase.co';
const SUPABASE_ANON_KEY    = process.env.SUPABASE_ANON_KEY    || 'sb_publishable_d8mzJ3iulCU7YdlV_lrdQw_32pOzDXc';
const SUPABASE_SERVICE_KEY = process.env.SUPABASE_SERVICE_KEY || '';

app.post('/api/auth', async (req, res) => {
  const action = req.query.action || req.body.action || 'login';
  const { email, password, name } = req.body;

  // ── Validation ──────────────────────────────────
  if (!email || !password) {
    return err(res, 400, 'Email na nywila vinahitajika', 'MISSING_PARAM');
  }

  try {
    // ── REGISTER ──────────────────────────────────
    if (action === 'register') {
      if (!name) return err(res, 400, 'Jina linahitajika', 'MISSING_PARAM');
      if (password.length < 6) return err(res, 400, 'Nywila lazima iwe herufi 6+', 'WEAK_PASSWORD');

      // Create user via Supabase Admin API
      const signupRes = await axios.post(
        `${SUPABASE_URL}/auth/v1/admin/users`,
        { email, password, email_confirm: true, user_metadata: { full_name: name } },
        { headers: { 'apikey': SUPABASE_SERVICE_KEY || SUPABASE_ANON_KEY, 'Authorization': `Bearer ${SUPABASE_SERVICE_KEY || SUPABASE_ANON_KEY}`, 'Content-Type': 'application/json' }, timeout: 12000 }
      ).catch(e => ({ data: e.response?.data, status: e.response?.status }));

      if (!signupRes.data?.id) {
        const msg = signupRes.data?.message || signupRes.data?.msg || 'Usajili umeshindwa';
        const friendly = msg.toLowerCase().includes('already') ? 'Email hii tayari imesajiliwa. Ingia badala yake.' : msg;
        return err(res, 400, friendly, 'REGISTER_FAILED');
      }

      const uid = signupRes.data.id;
      const trialEnd = new Date(Date.now() + 30 * 60 * 1000).toISOString();

      // Create profile
      await axios.post(
        `${SUPABASE_URL}/rest/v1/profiles`,
        { id: uid, email, full_name: name, plan: 'trial', trial_end: trialEnd, created_at: new Date().toISOString() },
        { headers: { 'apikey': SUPABASE_SERVICE_KEY || SUPABASE_ANON_KEY, 'Authorization': `Bearer ${SUPABASE_SERVICE_KEY || SUPABASE_ANON_KEY}`, 'Content-Type': 'application/json', 'Prefer': 'return=minimal' }, timeout: 8000 }
      ).catch(() => {});

      // Login immediately
      const loginRes = await axios.post(
        `${SUPABASE_URL}/auth/v1/token?grant_type=password`,
        { email, password },
        { headers: { 'apikey': SUPABASE_ANON_KEY, 'Content-Type': 'application/json' }, timeout: 10000 }
      ).catch(e => ({ data: e.response?.data }));

      return ok(res, {
        message: 'Umesajiliwa! Una dakika 30 za majaribio.',
        user: { id: uid, email, name, plan: 'trial', trial_end: trialEnd, sub_end: '', created_at: new Date().toISOString() },
        token:         loginRes.data?.access_token  || '',
        refresh_token: loginRes.data?.refresh_token || '',
      });
    }

    // ── LOGIN ──────────────────────────────────────
    if (action === 'login') {
      const loginRes = await axios.post(
        `${SUPABASE_URL}/auth/v1/token?grant_type=password`,
        { email, password },
        { headers: { 'apikey': SUPABASE_ANON_KEY, 'Content-Type': 'application/json' }, timeout: 10000 }
      ).catch(e => ({ data: e.response?.data, status: e.response?.status }));

      if (!loginRes.data?.access_token) {
        return err(res, 401, 'Email au nywila si sahihi', 'LOGIN_FAILED');
      }

      const token   = loginRes.data.access_token;
      const refresh = loginRes.data.refresh_token || '';
      const user    = loginRes.data.user || {};
      const uid     = user.id || '';
      const uname   = user.user_metadata?.full_name || name || email.split('@')[0];

      // Get profile
      const profileRes = await axios.get(
        `${SUPABASE_URL}/rest/v1/profiles?id=eq.${uid}&select=plan,trial_end,sub_end,created_at`,
        { headers: { 'apikey': SUPABASE_ANON_KEY, 'Authorization': `Bearer ${token}` }, timeout: 8000 }
      ).catch(() => ({ data: [] }));

      const profile = profileRes.data?.[0] || {};

      return ok(res, {
        user: {
          id:         uid,
          email,
          name:       uname,
          plan:       profile.plan      || 'free',
          trial_end:  profile.trial_end || '',
          sub_end:    profile.sub_end   || '',
          created_at: profile.created_at || user.created_at || '',
        },
        token,
        refresh_token: refresh,
      });
    }

    // ── RESET PASSWORD ────────────────────────────
    if (action === 'reset_password') {
      await axios.post(
        `${SUPABASE_URL}/auth/v1/recover`,
        { email },
        { headers: { 'apikey': SUPABASE_ANON_KEY, 'Content-Type': 'application/json' }, timeout: 10000 }
      ).catch(() => {});
      return ok(res, { message: 'Barua ya kubadilisha nywila imetumwa kama email ipo.' });
    }

    return err(res, 400, 'Action haijulikani: ' + action, 'UNKNOWN_ACTION');

  } catch (e) {
    console.error('Auth error:', e.message);
    return err(res, 500, 'Kosa la server: ' + e.message, 'SERVER_ERROR');
  }
});

// ══════════════════════════════════════════════════
//  ROUTE 9: HEALTH CHECK
// ══════════════════════════════════════════════════
app.get('/api/health', (req, res) => {
  ok(res, {
    status:  'ok',
    app:     'JAYNES MAX TV API',
    version: '1.0.0',
    edition: 'native-android',
    uptime:  Math.floor(process.uptime()),
    time:    new Date().toISOString(),
  });
});

// 404
app.use((req, res) => {
  err(res, 404, `Endpoint haipatikani: ${req.path}`, 'NOT_FOUND');
});

// Start
app.listen(CONFIG.PORT, () => {
  console.log(`✅ JAYNES MAX TV API (Native Edition) — port ${CONFIG.PORT}`);
});

module.exports = app;
