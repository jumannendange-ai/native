#!/bin/bash
# Tengeneza keystore mpya kwa JAYNES MAX TV
# Usage: ./generate_keystore.sh

mkdir -p android/keystore

keytool -genkey -v \
  -keystore android/keystore/jaynes_release.jks \
  -alias jaynesmaxtv \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass jaynesmax2025 \
  -keypass jaynesmax2025 \
  -dname "CN=JAYNES MAX TV, OU=Mobile, O=JAYNES, L=Dar es Salaam, S=Tanzania, C=TZ"

echo ""
echo "✅ Keystore imetengenezwa: android/keystore/jaynes_release.jks"
echo ""
echo "📋 Unda file: android/key.properties"
echo "   storePassword=jaynesmax2025"
echo "   keyPassword=jaynesmax2025"
echo "   keyAlias=jaynesmaxtv"
echo "   storeFile=../keystore/jaynes_release.jks"
