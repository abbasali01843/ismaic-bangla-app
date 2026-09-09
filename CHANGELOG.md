# Changelog

সংস্করণ ইতিহাস: প্রতি রিলিজে কী নতুন এলো / কী ঠিক হলো।

## [v1.0.1] - 2026-09-09

### Fixed
- হাদিস: ভালো ইন্টারনেটেও ভুল "ইন্টারনেট সংযোগ নেই" দেখাতো — এখন নির্দিষ্ট ত্রুটির বার্তা
  (সার্ভার ত্রুটি / তথ্য পড়তে সমস্যা / সংগ্রহ পাওয়া যায়নি), আর ব্যর্থ রিফ্রেশেও
  সংরক্ষিত হাদিস দেখা যায়
- হোম: সেকেন্ডারি স্ক্রিন (যেমন সূরার পাতা) থেকে Home bottom-nav চাপলে কিছুই হতো না —
  এখন যেকোনো tab/স্ক্রিন থেকে Home-এ ফেরা যায়, duplicate জমে না, Back ঠিকমতো কাজ করে

### Added
- ২৭টি নতুন regression/unit টেস্ট (Hadith repository / DTO / ViewModel + bottom navigation)

## [v1.0.0] - 2026-09-09

Milestone 1 — আধুনিকায়ন: SDK 36, CI পাইপলাইন (lint → test → build → release),
Room migration 5→6 + টেস্ট, backup allowlist, আধুনিক manifest, README।
