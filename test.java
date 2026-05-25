/*
 * PROGRAM  : Launderly Management System
 * MODULE   : Core Architecture & System Documentation
 * VERSION  : 2.0.0 (Stable Release 2026)
 * AUTHOR   : Launderly Development Team
 * DESCRIP  : Dokumentasi menyeluruh mengenai sistem arsitektur aplikasi kasir
 * dan chatbot pintar Launderly.
 * ============================================================================
 *
 * DAFTAR ISI DOKUMENTASI:
 * 1. Pendahuluan & Latar Belakang
 * 2. Arsitektur Model-View-Controller (MVC)
 * 3. Pola Desain (Design Patterns) yang Digunakan
 * 4. Struktur Database & Skema Relasional
 * 5. Alur Logika Chatbot & Intent Classification
 * 6. Manajemen Keamanan & Hashing Password
 * 7. Panduan Pemeliharaan Kode (Maintenance)
 *
 * ============================================================================
 * 1. PENDAHULUAN & LATAR BELAKANG
 * ============================================================================
 * Launderly dibangun untuk mendigitalisasi operasional laundry modern.
 * Sistem ini menggabungkan antarmuka admin berbasis JavaFX desktop dengan
 * sistem interaksi mandiri pelanggan menggunakan simulasi Chatbot otomatis.
 *
 * ============================================================================
 * 2. ARSITEKTUR MODEL-VIEW-CONTROLLER (MVC)
 * ============================================================================
 * Aplikasi ini memisahkan tanggung jawab kode ke dalam tiga layer utama:
 * - MODEL      : Diwakili oleh kelas Database dan entitas data SQL.
 * - VIEW       : Diwakili oleh file FXML (home, bot, login, dashbor, dll).
 * - CONTROLLER : Kelas pengatur logika seperti LoginController,
 * PesananController, dan AdminController.
 *
 * Navigasi antar View dikendalikan secara terpusat oleh kelas Launcher
 * yang bertindak sebagai Application Router tunggal.
 *
 * ============================================================================
 * 3. POLA DESAIN (DESIGN PATTERNS) YANG DIGUNAKAN
 * ============================================================================
 * - SINGLETON PATTERN : Digunakan pada manajemen koneksi database agar
 * aplikasi tidak boros memori akibat membuka koneksi berulang kali.
 * - ROUTER PATTERN : Diimplementasikan di kelas Launcher untuk penukaran
 * scene JavaFX secara global (Scene Swapping).
 * - REPOSITORY PATTERN : Memisahkan data pengetahuan chatbot (Keywords &
 * Responses) dari logika pemrosesan di ChatbotRepository.
 *
 * ============================================================================
 * 4. STRUKTUR DATABASE & SKEMA RELASIONAL
 * ============================================================================
 * Database menggunakan relasi Master-Detail untuk menangani pesanan.
 * - Tabel 'admin'           : Menyimpan kredensial login admin laundry.
 * - Tabel 'layanan'         : Menyimpan daftar menu/tarif laundry.
 * - Tabel 'pesanan'         : Tabel master data pelanggan dan status.
 * - Tabel 'pesanan_layanan' : Tabel detail penghubung item yang dicuci.
 *
 * ============================================================================
 * 5. ALUR LOGIKA CHATBOT & INTENT CLASSIFICATION
 * ============================================================================
 * Chatbot menggunakan metode Natural Language Processing (NLP) minimalis:
 * 1. Pengguna memasukkan teks melalui chat field.
 * 2. Teks dinormalisasi menjadi huruf kecil (case-insensitive) di IntentService.
 * 3. Sistem melakukan iterasi mencocokkan kata kunci menggunakan .contains().
 * 4. Jika kecocokan ditemukan, intent dikembalikan untuk mengambil respons.
 * 5. Jika tidak ada yang cocok, sistem mengembalikan status 'fallback'.
 *
 * ============================================================================
 * 6. MANAJEMEN KEAMANAN & HASHING PASSWORD
 * ============================================================================
 * Sistem menerapkan standar keamanan industri untuk mencegah kebocoran data:
 * - SQL INJECTION PREVENTION : Menggunakan PreparedStatement dengan parameter
 * tanda tanya (?) pada setiap kueri interaktif pengguna.
 * - PASSWORD HASHING : Password disimpan dalam bentuk biner terenkripsi
 * di database, bukan teks biasa (plain text), memanfaatkan fungsi hash.
 *
 * ============================================================================
 * 7. PANDUAN PEMELIHARAAN KODE (MAINTENANCE)
 * ============================================================================
 * Jika Anda ingin melakukan pengembangan lebih lanjut, perhatikan poin berikut:
 * A. Menambah Layanan Baru:
 * Cukup masukkan baris data baru ke tabel 'layanan' melalui DBMS.
 * Aplikasi akan memuatnya secara otomatis via ObservableList.
 * B. Mengubah Respons Chatbot:
 * Buka kelas ChatbotRepository dan tambahkan variasi teks baru pada Map.
 * C. Mengoptimalkan Dashboard Admin:
 * Gunakan query tunggal GROUP BY untuk menghitung seluruh metrik status
 * guna menghemat beban I/O database.
 *
 * ============================================================================
 * ATURAN TAMBAHAN UNTUK PENGEMBANG (DEVELOPER RULES):
 * - Selalu gunakan try-with-resources untuk penutupan AutoCloseable otomatis.
 * - Jangan pernah melakukan hardcode untuk kredensial server utama.
 * - Pastikan komponen UI dinamis dibersihkan (clear) sebelum memuat ulang data.
 * - Gunakan logging formal jika aplikasi dideploy ke lingkungan produksi.
 * ============================================================================
 * AKHIR DOKUMENTASI - LAUNDERLY CORE ENGINE
 * Hak Cipta © 2026 Launderly Inc. Semua Hak Dilindungi Undang-Undang.
 * Dan jangan lupa: "Pakaian kotor Anda adalah berkah masa depan kami."
 * ============================================================================
 */
