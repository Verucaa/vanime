# Kebijakan Privasi Vanim

Terakhir diperbarui: 6 November 2026

## Ringkasan

Vanim tidak memiliki akun, server, iklan, maupun pelacak. Semua data yang kamu buat di
aplikasi ini disimpan di perangkatmu sendiri dan tidak pernah dikirim ke pengembang.

## Data yang disimpan di perangkat

- Riwayat tontonan beserta posisi dan durasi episode (untuk "Lanjutkan Nonton").
- Daftar anime favorit.
- Riwayat pencarian (maksimal 12 entri terakhir).
- Pengaturan aplikasi: tema, kualitas video, kualitas unduhan, opsi pemutar.
- File episode yang diunduh untuk ditonton offline.

Data ini tersimpan di database lokal (Room) dan penyimpanan privat aplikasi (DataStore).
Tidak ada satu pun yang dikirim ke pengembang.

## Data yang tidak dikumpulkan

- Tidak ada akun, email, nomor telepon, atau nama pengguna.
- Tidak ada analitik, pelacakan perilaku, iklan, maupun SDK yang memprofilkan pengguna.
- Tidak ada laporan crash otomatis.
- Tidak ada cookie. Aplikasi ini bukan browser dan tidak memuat halaman web.

## Koneksi ke layanan pihak ketiga

Agar dapat menampilkan konten, aplikasi terhubung langsung dari perangkat ke:

| Layanan | Tujuan | Data yang terlihat oleh layanan |
| --- | --- | --- |
| `xyz-api.animein.net` | Katalog anime, jadwal, poster, tautan aliran video | Alamat IP, User-Agent, permintaan katalog |
| `api.github.com` | Pemeriksaan versi jika repositori rilis diatur saat build | Alamat IP, User-Agent |

Repositori Vanim belum dikonfigurasi; pemeriksaan versi GitHub tidak berjalan sampai penerbit memasok repositori pada saat build. Jika layanan pihak ketiga digunakan, pengembang tidak menerima, menyimpan, atau menjual data tersebut. Penggunaan layanan pihak
ketiga tunduk pada kebijakan privasi masing-masing layanan.

## Izin aplikasi

| Izin | Kegunaan | Wajib? |
| --- | --- | --- |
| `INTERNET` | Mengambil katalog dan memutar video | Ya |
| `ACCESS_NETWORK_STATE` | Mendeteksi Wi-Fi vs data seluler (kualitas video, aturan unduh) | Ya |
| `POST_NOTIFICATIONS` | Notifikasi episode baru dan progres unduhan | Tidak (opsional) |

## Cadangan perangkat

Jika cadangan otomatis Android aktif, data lokal aplikasi dapat ikut dicadangkan oleh layanan
cadangan Google sesuai setelan perangkat. Cadangan diatur oleh sistem operasi, bukan aplikasi.

## Anak-anak

Aplikasi ditujukan untuk pengguna berusia 13 tahun ke atas. Aplikasi tidak mengumpulkan data
pribadi apa pun dari siapa pun, termasuk anak-anak.

## Keamanan

Data tersimpan di penyimpanan privat aplikasi yang dilindungi sandbox Android. Tidak ada
transmisi data pribadi ke server pengembang karena server tersebut tidak ada.

## Hak dan penghapusan data

Data dapat dihapus kapan saja di aplikasi: **Pengaturan → Data → Hapus semua data**.
Tindakan ini menghapus favorit, riwayat tontonan, riwayat pencarian, pengaturan, dan file
unduhan secara permanen. Menghapus aplikasi juga menghapus seluruh datanya.

## Perubahan kebijakan

Repositori publik Vanim belum ditetapkan. Perubahan kebijakan akan diterbitkan bersama versi berikutnya setelah alamat repositori resmi tersedia.

## Kontak

Kontak Vanim belum dikonfigurasi.
