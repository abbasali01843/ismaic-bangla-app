# Ismaic Bangla App

An Android application providing Islamic content in Bangla language. Debug APKs are built automatically by GitHub Actions and published on the GitHub Releases page.

## Features

- 📖 Al-Quran with Bangla translation (API-powered, cached offline)
- 🔊 Verse-by-verse recitation audio from API (5 reciters, persistent mini-player)
- 🔖 Bookmarks for ayahs, hadiths and duas + last-read continue card
- 🔍 Global search across Quran, hadith and duas (Bangla + Arabic) with recent searches
- 📝 Personal notes on any ayah, hadith or dua
- 📤 Share ayahs, hadiths and duas as formatted Bangla text
- 📜 Bangla tafsir on every ayah (Ahsanul Bayan, Ibn Kathir, Abu Bakr Zakaria, Fathul Majid)
- 🕌 Step-by-step namaz guide (wudu, rakat chart, prayer steps with duas)
- 🌟 99 Names of Allah with Bangla meanings + 6 Kalimas (fully offline)
- 💰 Zakat calculator (gold/silver nisab in bhori, 2.5%)
- 🌙 Sehri/iftar countdown with live timer + Ramadan greeting
- ✅ Daily prayer tracker with streaks and 7-day history
- 📢 Azan audio at prayer time (downloaded once, cached offline, stoppable)
- 🎧 Background recitation playback with notification controls + surah download for offline listening
- 🔁 Ayah/surah repeat modes for memorization
- 🔠 Adjustable Arabic + Bangla font sizes
- 📚 Hadith in Bangla — full Sihah Sittah: Bukhari, Muslim, Abu Dawud, Tirmidhi, Nasai, Ibn Majah (API-powered, cached offline)
- 🕌 Daily prayer times for Bangladesh cities + Hijri date (API-powered, cached offline)
- 🔔 Prayer-time notifications (exact alarms, per-prayer toggles, survive reboot)
- 📿 Tasbih counter with 10 dhikrs, targets, haptics and lifetime total
- 🧭 Qibla compass (sensors + GPS, Dhaka fallback)
- 🤲 53 daily masnun duas with Arabic, Bangla, English + virtues (bundled, fully offline)
- 📴 Offline-first: fresh API data on every open, cached fallback without internet
- 🌗 Light / dark / system theme, fully Bangla UI

### Data sources (all free, no API key)

| Content | API |
|---|---|
| Quran Arabic + Bangla | [api.alquran.cloud](https://api.alquran.cloud) (Uthmani + Muhiuddin Khan) |
| Quran recitation audio | [cdn.islamic.network](https://cdn.islamic.network) (128kbps MP3, verse by verse) |
| Hadith Bangla + Arabic | [fawazahmed0/hadith-api](https://github.com/fawazahmed0/hadith-api) via jsDelivr CDN |
| Prayer times + Hijri date | [api.aladhan.com](https://aladhan.com/prayer-times-api) (method 1, Karachi) |
| Azan audio | Wikimedia Commons [Azan.ogg](https://commons.wikimedia.org/wiki/File:Azan.ogg) (CC BY-SA 4.0) |

## Project Structure

```
ismaic-bangla-app/
├── app/                        # Android app module
│   ├── src/main/java/...       # Kotlin source (data, di, presentation)
│   ├── src/main/assets/        # Bundled seed data (surahs, duas, dhikrs, names)
│   ├── src/main/res/           # Resources (theme, icons, backup rules)
│   ├── src/test/               # JVM unit tests (incl. Room migration test)
│   └── schemas/                # Exported Room schema (migration review)
├── gradle/wrapper/             # Gradle Wrapper (Gradle 8.13)
├── .github/workflows/          # CI automation (ci.yml)
└── README.md
```

## Quick Start

### Prerequisites

- Android Studio (recent version with AGP 8.13 support)
- Java Development Kit (JDK 17)
- Git

### Local Setup

1. Clone the repository:
```bash
git clone https://github.com/abbasali01843/ismaic-bangla-app.git
cd ismaic-bangla-app
```

2. Open the project in Android Studio (it detects and configures the Gradle project automatically).

3. Build from the command line:
```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build unsigned release APK (validates R8 config)
./gradlew testDebugUnitTest  # Run unit tests
./gradlew lintDebug          # Run Android lint
```

## Releases

Each push to the working branch triggers GitHub Actions, which runs lint, unit tests and both debug + release builds, then publishes the debug APK to the rolling `latest-apk` GitHub Release. Download it from the repository's Releases page and install it directly on a phone (no Play Store account needed).

Release builds are unsigned: signing is attached separately at packaging time, so no keystore or secrets live in this repository.

## CI/CD Pipeline

Automated workflow in `.github/workflows/`:

- **ci.yml** — runs on every push/PR: lint → unit tests → debug/release builds → publishes the debug APK to the `latest-apk` release (pushes only).

## Privacy & Security

- No accounts, no analytics, no advertising SDKs.
- Prayer-time city, settings and bookmarks/notes stay on the device (included in Android Auto Backup so they survive device restore).
- Downloaded recitation audio is treated as disposable cache and excluded from backup.
- Network calls use HTTPS only; no API keys or secrets are stored in the app or repository.

## Support

For issues, questions, or feature requests, please open an [Issue](https://github.com/abbasali01843/ismaic-bangla-app/issues).

## Maintainers

- [@abbasali01843](https://github.com/abbasali01843)

---

**Happy coding! إن شاء الله**
