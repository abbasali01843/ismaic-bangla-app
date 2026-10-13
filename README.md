# AI Quran — Native Android

A Bengali, Arabic and English Quran reader built natively for Android with an offline-first architecture.

## Current milestone

This repository starts with the Android app shell and cloud APK build workflow. It does **not** yet bundle Quran text or translations. Content integration is intentionally gated on verified licensing and the Quran Foundation Content Sync requirements.

### Planned features
- Native Kotlin + Jetpack Compose UI
- Android 6.0+ baseline (Android 11 supported)
- Room database for local Quran content and bookmarks
- Bengali, Arabic and English interface
- Offline-first reader and search
- Quran Foundation Content Sync integration after developer access is configured
- AI assistant in a later milestone, with source-linked answers and no generated Arabic Quran text

## Build APK from a phone

1. Open this repository in GitHub.
2. Go to **Actions** and enable workflows if prompted.
3. Select **Android Debug APK (Milestone build)** → **Run workflow**.
4. Open the completed run and download the **ai-quran-debug-apk** artifact.
5. Extract the ZIP on your Android phone and install the APK. For sideloading, allow installs from the app you use to open the APK.

The workflow builds a **debug APK** for testing. A release APK needs a separate signing configuration; never commit signing keys or passwords.

## Build locally

Requires JDK 17, Android SDK platform 35, and Gradle 8.13.

```bash
gradle :app:assembleDebug
```

## Content licensing and credentials

- Never put a Quran Foundation `client_secret` in the APK, Git history, or public Actions logs.
- Content API credentials belong on a trusted backend; the Android client must not use the confidential client-credentials flow.
- Use Content Sync for resources that Quran Foundation makes available through that system; keep synced copies updated when connectivity permits.
- Display source and translation attribution in the app.
- Do not redistribute Quran Foundation raw content as a separate dataset, feed, or API.
- Do not add translation/tafsir/audio assets until the exact edition's license permits the intended use.

See [Quran Foundation Developer Terms](https://api-docs.quran.com/legal/developer-terms/) and [Content Sync guide](https://api-docs.quran.com/docs/tutorials/content-sync/getting-started/).

## Project status

- [x] Native Android project skeleton
- [x] Manual GitHub Actions debug APK build (milestone-based; normal commits do not build)
- [x] 114-surah directory and name/number search
- [x] Room schema for Quran verses, translations and bookmarks
- [x] Local surah/ayah bookmarks and last-opened-surah memory
- [x] Reader UI wired to locally stored verses and translation records
- [x] Versioned JSON bundle importer with validation and transactional Room writes
- [x] Startup import status shown in Settings
- [x] Offline content bundle schema and release checklist documented
- [ ] Select and document a verified Quran text and Bengali translation source
- [ ] Add a safe, repeatable content import/sync pipeline
- [ ] Populate and verify the complete offline Quran + selected translation
- [x] Bengali translation-search UI and local Room query (results require translation data to be imported)
- [ ] Android 11 device testing
- [ ] AI assistant (later milestone)

## Security

Report vulnerabilities privately to the repository owner. Never open a public issue containing tokens, API secrets, signing keys, or personal data.
