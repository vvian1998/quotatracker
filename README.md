# 📊 QuotaTracker

Aplikasi Android native (Kotlin + Jetpack Compose) untuk melacak penggunaan kuota internet per-aplikasi secara detail dan menampilkan widget **balon melayang (floating bubble overlay)** saat membuka aplikasi lain.

---

## ✨ Fitur Utama

- 📈 **Dashboard Kuota**: Ring gauge circular interaktif dengan indikator unduhan & unggahan (Mobile & WiFi).
- 💬 **Floating Bubble (Balon Melayang)**: Widget overlay semi-transparan (*frosted glass*) di atas aplikasi yang sedang aktif (misal: YouTube, Instagram) untuk memantau akumulasi data yang dipakai saat itu.
- 📱 **Detail Per-Aplikasi**: Grafik batang pemakaian 7 hari terakhir, pemisahan layar aktif (*foreground*) vs latar belakang (*background*), dan limit kuota per-aplikasi.
- 🗓️ **Riwayat 30 Hari**: Penyimpanan riwayat penggunaan harian dengan Room Database.
- ⚠️ **Peringatan Kuota**: Notifikasi peringatan otomatis saat penggunaan kuota mencapai ambang batas (misal: 80%).
- 🛡️ **Setup Izin**: Wizard terpandu untuk mengaktifkan izin `Usage Access` dan `Display Over Other Apps`.

---

## 🚀 Build APK via GitHub Actions

Workflow GitHub Actions sudah disiapkan di [.github/workflows/build-apk.yml](.github/workflows/build-apk.yml).

### Cara Menjalankan:
1. Push repository ini ke GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial commit QuotaTracker"
   git remote add origin https://github.com/<username>/<repo-name>.git
   git push -u origin main
   ```
2. Buka repository di GitHub, lalu klik tab **Actions**.
3. Pilih workflow **Build QuotaTracker APK**, lalu klik **Run workflow**.
4. Setelah build selesai (~2-3 menit), unduh file APK dari bagian **Artifacts** (`QuotaTracker-debug-apk`).
