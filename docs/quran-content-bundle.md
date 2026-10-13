# Offline Quran content bundle

The Android app looks for `app/src/main/assets/quran-content.json` at startup.
The file is optional: if it is absent, the app remains usable and Settings reports
that no new bundle was imported.

**Only add a dataset after checking its source, edition, license and attribution.**
Do not put API secrets in this file. Do not use generated or unverified Quran text.

## JSON shape

```json
{
  "source": "REPLACE_WITH_VERIFIED_SOURCE",
  "contentVersion": "2026-01",
  "verses": [
    {
      "verseKey": "1:1",
      "surahNumber": 1,
      "ayahNumber": 1,
      "arabicText": "REPLACE_WITH_VERIFIED_ARABIC_TEXT",
      "juzNumber": 1,
      "pageNumber": 1
    }
  ],
  "translations": [
    {
      "verseKey": "1:1",
      "languageCode": "bn",
      "editionId": "REPLACE_WITH_EDITION_ID",
      "editionName": "REPLACE_WITH_EDITION_NAME",
      "translationText": "REPLACE_WITH_LICENSED_TRANSLATION_TEXT",
      "licenseId": "REPLACE_WITH_LICENSE_OR_PERMISSION_REFERENCE",
      "attribution": "REPLACE_WITH_REQUIRED_ATTRIBUTION"
    }
  ]
}
```

The values above are **schema placeholders, not Quran content**. Do not ship this
example as a dataset.

## Import behaviour

- Source and content version are required.
- Verse rows validate the surah range, positive ayah number and non-empty Arabic text.
- Translation rows require a verse key, language, edition, text, license identifier and attribution.
- Rows are written in a single Room transaction.
- The app records the imported source/version locally and will not re-import the same
  version at every launch. Increment `contentVersion` when shipping an updated bundle.
- Replacing a bundle with a newer version upserts supplied rows; it does not currently
  delete rows omitted from the new bundle. Use a complete dataset or implement an explicit
  migration before shipping incremental/delta bundles.

## Release checklist

1. Verify the source and distribution permission for the exact Arabic text and Bengali edition.
2. Validate all 114 surahs and expected ayah counts against an independent trusted reference.
3. Check every translation key maps to an existing verse.
4. Check the required attribution is visible in the app.
5. Test import on a clean install and update from a previous content version.
6. Never label the app "complete offline Quran" until the full dataset passes these checks.
