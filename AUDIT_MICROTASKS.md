# QuotaTracker — Audit Micro Tasks

Backlog hasil audit teknis dan UI/UX.

Status: implementasi selesai untuk item yang dapat diverifikasi lewat kode dan CI.
Audit aksesibilitas penuh serta uji langsung Redmi Note 10S masih tertunda.

## P0 — Wajib agar aplikasi stabil

- [x] **Tentukan minimum Android version**
  - Putuskan apakah aplikasi mendukung Android 11 atau wajib Android 12+.
  - Jika mendukung Redmi Note 10S Android 11, ubah `minSdk` dari 31 ke 30.
  - Acceptance: APK dapat di-install pada versi Android yang ditargetkan.

- [x] **Siapkan Android SDK untuk build**
  - Install SDK Platform 35 dan Build Tools.
  - Konfigurasi `ANDROID_HOME` atau `local.properties`.
  - Acceptance: `./gradlew test` dan `./gradlew assembleDebug` berhasil.

- [x] **Evaluasi ulang arsitektur foreground service**
  - Jangan memakai `dataSync` untuk service monitoring permanen.
  - Tentukan kombinasi WorkManager dan service yang sesuai.
  - Acceptance: monitoring tidak bergantung pada `dataSync` FGS tanpa batas.

- [x] **Perbaiki auto-start setelah reboot**
  - Hindari coroutine lepas dari `BroadcastReceiver`.
  - Gunakan `goAsync()` atau jadwalkan WorkManager.
  - Acceptance: monitoring kembali aktif setelah reboot tanpa crash.

- [x] **Tambahkan penanganan Android 15**
  - Implementasikan `onTimeout()` jika FGS tetap dipakai.
  - Jangan memulai `dataSync` FGS langsung dari `BOOT_COMPLETED`.
  - Acceptance: tidak ada `ForegroundServiceStartNotAllowedException` atau ANR.

## P1 — Perbaikan akurasi data

- [x] **Samakan timezone untuk semua tanggal**
  - Selaraskan epoch day, start of day, dan weekly breakdown menggunakan timezone lokal.
  - Acceptance: data tidak berpindah ke hari sebelumnya atau berikutnya.

- [x] **Hubungkan cycle day ke dashboard**
  - Baca `quotaCycleDayFlow` di `DashboardViewModel`.
  - Kirim `cycleDay` ke repository.
  - Acceptance: tanggal reset selain tanggal 1 memengaruhi statistik.

- [x] **Tambahkan kontrol tanggal reset di Settings**
  - Buat dropdown atau slider tanggal 1–28.
  - Panggil `setCycleDay()` dari UI.
  - Acceptance: pengguna dapat mengubah dan menyimpan tanggal reset.

- [x] **Perbaiki warning percentage**
  - Jangan memakai nilai warning lama dari Room setelah user mengubah slider.
  - Jadikan Room atau DataStore sebagai satu sumber kebenaran.
  - Acceptance: threshold notifikasi mengikuti nilai terbaru.

- [x] **Aktifkan fungsi switch warning**
  - `DataMonitorService` harus membaca `warningEnabledFlow`.
  - Acceptance: saat switch dimatikan, tidak ada notifikasi quota warning.

- [x] **Tentukan perilaku quota per aplikasi**
  - Jika harus memberi peringatan, tambahkan monitoring per UID.
  - Jika hanya informasi, perjelas copy UI agar tidak terkesan membatasi penggunaan.
  - Acceptance: perilaku sesuai ekspektasi pengguna.

- [x] **Validasi data device vs data aplikasi**
  - Jelaskan bahwa total device dapat berbeda dari jumlah aplikasi.
  - Acceptance: perbedaan angka tidak membingungkan pengguna.

## P1 — Perbaikan UI/UX

- [x] **Perbaiki makna quota berdasarkan periode**
  - Jangan membandingkan penggunaan harian atau mingguan dengan quota bulanan tanpa penjelasan.
  - Gunakan label “Batas referensi bulanan” atau hitung quota sesuai periode.
  - Acceptance: label dan angka tidak menyesatkan.

- [x] **Perjelas onboarding permission**
  - Bedakan izin wajib: Usage Access.
  - Bedakan izin opsional: overlay dan notifikasi.
  - Tambahkan tombol menuju permission yang belum aktif.
  - Acceptance: pengguna memahami alasan setiap izin.

- [x] **Tambahkan error state**
  - Tampilkan pesan ketika NetworkStats atau Usage Access gagal.
  - Sediakan tombol “Coba lagi”.
  - Acceptance: error tidak terlihat seperti data kosong biasa.

- [x] **Tambahkan input angka untuk quota**
  - Pertahankan slider dan tambahkan input GB manual.
  - Acceptance: pengguna dapat memasukkan nilai seperti `7.5 GB`.

- [x] **Perjelas status floating bubble**
  - Tambahkan tooltip atau label “Bubble aktif/nonaktif”.
  - Acceptance: status tombol mudah dipahami.

- [ ] **Audit aksesibilitas**
  - Tambahkan `contentDescription` untuk ikon interaktif.
  - Pastikan teks, warna, dan ukuran memenuhi aksesibilitas.
  - Acceptance: alur utama dapat digunakan dengan TalkBack.

## P2 — Keamanan dan maintenance

- [x] **Ganti destructive migration**
  - Buat migration Room eksplisit.
  - Acceptance: upgrade aplikasi tidak menghapus history dan setting.

- [x] **Tinjau backup data**
  - Tentukan apakah database usage dan DataStore boleh di-backup.
  - Jika tidak, exclude dari backup.
  - Acceptance: kebijakan backup sesuai kebutuhan privasi.

- [x] **Evaluasi `QUERY_ALL_PACKAGES`**
  - Gunakan `<queries>` yang lebih sempit bila memungkinkan.
  - Acceptance: package visibility cukup tanpa izin terlalu luas.

- [x] **Ignore artefak build**
  - Tambahkan ke `.gitignore`:

    ```gitignore
    *.apk
    *.zip
    /build-output/
    ```

  - Acceptance: APK dan output build tidak muncul sebagai untracked file.

- [x] **Tambah unit test timezone dan quota**
  - Test cycle day, timezone lokal, warning setting, dan periode dashboard.
  - Acceptance: bug utama memiliki regression test.

- [ ] **Uji langsung di Redmi Note 10S**
  - Test Android 12/13, MIUI autostart, battery saver, reboot, overlay, notification, dan foreground tracking.
  - Acceptance: monitoring tetap berjalan setelah layar mati dan aplikasi lain dibuka.

## Urutan pengerjaan

1. P0: task 1–5
2. P1 data: task 6–12
3. P1 UI/UX: task 13–18
4. P2: task 19–24
