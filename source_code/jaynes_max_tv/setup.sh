#!/bin/bash
# JAYNES MAX TV — Setup Script
# Tumia script hii MARA MOJA kabla ya ku-build local
# Usage: chmod +x setup.sh && ./setup.sh

set -e
echo "======================================"
echo "  JAYNES MAX TV — Setup"
echo "======================================"

FONTS_DIR="assets/fonts"
mkdir -p "$FONTS_DIR"

echo ""
echo "📥 Downloading fonts..."

# BebasNeue
curl -fL "https://github.com/google/fonts/raw/main/ofl/bebasneue/BebasNeue-Regular.ttf" \
     -o "$FONTS_DIR/BebasNeue-Regular.ttf" && echo "✅ BebasNeue-Regular.ttf" || echo "⚠️  BebasNeue failed"

# Rajdhani
for weight in Regular Medium SemiBold Bold; do
  curl -fL "https://github.com/google/fonts/raw/main/ofl/rajdhani/Rajdhani-${weight}.ttf" \
       -o "$FONTS_DIR/Rajdhani-${weight}.ttf" && echo "✅ Rajdhani-${weight}.ttf" || echo "⚠️  Rajdhani-${weight} failed"
done

echo ""
echo "📦 Running flutter pub get..."
flutter pub get

echo ""
echo "🔧 Generating launcher icons..."
dart run flutter_launcher_icons || echo "⚠️  Icons generation skipped (already have icons)"

echo ""
echo "======================================"
echo "✅ Setup complete! Build with:"
echo "   flutter build apk --release"
echo "======================================"
