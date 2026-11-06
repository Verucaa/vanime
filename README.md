<div align="center">
<img src="docs/brand/icon-512.png" width="104" alt="Ikon Vanim">

# Vanim

**Jelajahi, tonton, dan simpan anime favoritmu dalam aplikasi Android yang dirancang ulang dengan nuansa midnight-blue.**

[![Android 7.0+](https://img.shields.io/badge/Android-7.0%2B-087FB4?style=flat-square&logo=android&logoColor=white)](#)
[![GPL-3.0](https://img.shields.io/badge/Lisensi-GPL--3.0-263746?style=flat-square)](./LICENSE)
</div>

> Proyek ini belum memiliki repositori/rilis resmi Vanim. Tautan upstream dan identitas penulis aslinya dicatat terpisah untuk atribusi; jangan anggap URL upstream sebagai rilis Vanim. Lihat [catatan provenance](./docs/upstream.md).

## Yang tersedia

- **Beranda editorial** dengan sorotan anime, lanjutkan menonton, episode terbaru, jadwal, dan pilihan populer.
- **Jelajah dan jadwal** untuk mencari judul, menyaring genre, serta melihat jadwal rilis.
- **Detail dan pemutar Media3** dengan pilihan kualitas/kecepatan, PiP, progres episode, dan kontrol pemutar.
- **Koleksi lokal** berisi favorit, riwayat, dan unduhan offline.
- **Mode offline** yang memakai data terakhir yang tersimpan di perangkat.
- Antarmuka Kotlin/Jetpack Compose dengan tema gelap navy-biru, warna terang, dan dukungan AMOLED.

UI screenshot warisan dihapus karena sudah tidak menggambarkan desain sekarang; tangkapan layar baru perlu dibuat setelah aplikasi dijalankan pada perangkat/emulator Android.

## Build dan tes

Persyaratan: JDK 17 atau lebih baru, Android SDK dengan platform 36, dan Gradle yang sesuai. Dari direktori proyek:

```bash
gradle testDebugUnitTest assembleDebug
```

APK debug, jika build sukses, berada di `app/build/outputs/apk/debug/app-debug.apk`. Namespace Android adalah `com.vanim`, dengan application ID `com.vanim.app`. Karena application ID berubah, Android akan memasang Vanim sebagai aplikasi terpisah; data dari instalasi lama tidak otomatis berpindah.

Pemeriksaan update hanya aktif bila build memasok `GITHUB_REPOSITORY`; tidak ada default repo Vanim yang dibuat-buat.

## Layanan dan privasi

Aplikasi meminta katalog, poster, dan tautan video dari layanan eksternal. Aplikasi tidak meng-host video. Alamat IP dan User-Agent dapat terlihat oleh layanan yang diminta. Baca [Kebijakan Privasi](./docs/legal/privacy.md), [Ketentuan Layanan](./docs/legal/terms.md), [Checklist kepatuhan](./docs/compliance.md), dan [Audit SDK pihak ketiga](./docs/third-party-sdk-audit.md). Ketersediaan dan hak atas konten mengikuti penyedia dan pemegang hak masing-masing.

## Lisensi dan atribusi

Kode sumber memakai GNU General Public License v3.0—lihat berkas [LICENSE](./LICENSE) yang dipertahankan. Pemberitahuan hak cipta dan lisensi komponen pihak ketiga, termasuk Plus Jakarta Sans, tidak diubah. Lisensi kode tidak otomatis memberi hak atas merek atau aset pihak lain.

Identitas penulis upstream tetap dicatat sebagai **YuzakiNetwork (Haruka Fuyune)** di [catatan provenance](./docs/upstream.md); itu bukan klaim kepemilikan Vanim. Metadata listing F-Droid menunggu repositori dan kontak penerbit resmi Vanim.
